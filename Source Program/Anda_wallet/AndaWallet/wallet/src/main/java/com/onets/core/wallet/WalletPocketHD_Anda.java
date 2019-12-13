/**
 * Copyright 2013 Google Inc.
 * Copyright 2014 Yu K.Q.
 * Copyright 2014 Yu K.Q.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onets.core.wallet;

import android.util.Log;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.exceptions.AddressMalformedException;
import com.onets.core.exceptions.Bip44KeyLookAheadExceededException;
import com.onets.core.protos.Protos;
import com.onets.core.util.KeyUtils;
import com.onets.core.wallet.families.andachain.AndaAddress;
import com.onets.core.wallet.families.andachain.AndaSendRequest;
import com.onets.core.wallet.families.bitcoin.BitAddress;
import com.onets.core.wallet.families.bitcoin.BitSendRequest;
import com.onets.test1.NioClient1;
import com.onets.wallet.Constants;
import com.onets.wallet.util.HttpClientPost;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.RedeemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.params.KeyParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;

import static com.onets.core.Preconditions.checkArgument;
import static com.onets.core.Preconditions.checkNotNull;
import static com.onets.core.util.AndaAddressUtils.getHash160;
import static org.bitcoinj.wallet.KeyChain.KeyPurpose.CHANGE;
import static org.bitcoinj.wallet.KeyChain.KeyPurpose.RECEIVE_FUNDS;
import static org.bitcoinj.wallet.KeyChain.KeyPurpose.REFUND;

/**
 * @author Yu K.Q.
 *
 *
 */
public class WalletPocketHD_Anda extends AndaWalletBase {
    private static final Logger log = LoggerFactory.getLogger(WalletPocketHD_Anda.class);

    @VisibleForTesting
    protected SimpleHDKeyChain keys;

    public WalletPocketHD_Anda(DeterministicKey rootKey, CoinType coinType,
                               @Nullable KeyCrypter keyCrypter, @Nullable KeyParameter key) {
        this(new SimpleHDKeyChain(rootKey, keyCrypter, key), coinType);
    }

    WalletPocketHD_Anda(SimpleHDKeyChain keys, CoinType coinType) {
        this(KeyUtils.getPublicKeyId(coinType, keys.getRootKey().getPubKey()), keys, coinType);
        Log.d(TAG, "测试-WalletPocketHD_Anda: KeyUtils.getPublicKeyId( ) " + KeyUtils.getPublicKeyId(coinType, keys.getRootKey().getPubKey()));
    }

    WalletPocketHD_Anda(String id, SimpleHDKeyChain keys, CoinType coinType) {
        super(checkNotNull(coinType), id);
        Log.d(TAG, "测试-WalletPocketHD_Anda: id " + id);
        this.keys = checkNotNull(keys);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Region vending transactions and other internal state
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get the BIP44 index of this account
     */
    public int getAccountIndex() {
        lock.lock();
        try {
            return keys.getAccountIndex();
        } finally {
            lock.unlock();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Serialization support
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    List<Protos.Key> serializeKeychainToProtobuf() {
        lock.lock();
        try {
            return keys.toProtobuf();
        } finally {
            lock.unlock();
        }
    }

    @VisibleForTesting Protos.WalletPocket toProtobuf() {
        lock.lock();
        try {
            return WalletPocketProtobufSerializer_Anda.toProtobuf(this);
        } finally {
            lock.unlock();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Encryption support
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public boolean isEncryptable() {
        return true;
    }

    @Override
    public boolean isEncrypted() {
        lock.lock();
        try {
            return keys.isEncrypted();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the wallet pocket's KeyCrypter, or null if the wallet pocket is not encrypted.
     * (Used in encrypting/ decrypting an ECKey).
     */
    @Nullable
    @Override
    public KeyCrypter getKeyCrypter() {
        lock.lock();
        try {
            return keys.getKeyCrypter();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Encrypt the keys in the group using the KeyCrypter and the AES key. A good default KeyCrypter to use is
     * {@link org.bitcoinj.crypto.KeyCrypterScrypt}.
     *
     * @throws org.bitcoinj.crypto.KeyCrypterException Thrown if the wallet encryption fails for some reason,
     *         leaving the group unchanged.
     */
    @Override
    public void encrypt(KeyCrypter keyCrypter, KeyParameter aesKey) {
        checkNotNull(keyCrypter);
        checkNotNull(aesKey);

        lock.lock();
        try {
            this.keys = this.keys.toEncrypted(keyCrypter, aesKey);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Decrypt the keys in the group using the previously given key crypter and the AES key. A good default
     * KeyCrypter to use is {@link org.bitcoinj.crypto.KeyCrypterScrypt}.
     *
     * @throws org.bitcoinj.crypto.KeyCrypterException Thrown if the wallet decryption fails for some reason, leaving the group unchanged.
     */
    @Override
    public void decrypt(KeyParameter aesKey) {
        checkNotNull(aesKey);

        lock.lock();
        try {
            this.keys = this.keys.toDecrypted(aesKey);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String getPublicKeySerialized() {
        // Change the path of the key to match the BIP32 paths i.e. 0H/<account index>H
        DeterministicKey key = keys.getWatchingKey();
        ImmutableList<ChildNumber> path = ImmutableList.of(key.getChildNumber());
        key = new DeterministicKey(path, key.getChainCode(), key.getPubKeyPoint(), null, null);
        return key.serializePubB58();
    }

    @Override
    public SendRequest sendCoins(AbstractAddress address, CoinType coinType, Value Amount, String s, String password) throws WalletAccountException {
        AndaSendRequest request = null;
        CharSequence pss_word ="";
        AndaAddress andaAddress = null;

        try {
            andaAddress = AndaAddress.from(coinType, address.toString());
            request = sendCoinsOffline(andaAddress, Amount);

            completeTransaction(request);
            broadcastADC(request, andaAddress, null, Amount);

        } catch (AddressMalformedException e) {
            e.printStackTrace();
            Log.d(TAG, "sendCoins: 地址转化为AndaAddress类型失败");
        }

        return request;
    }

    public synchronized void broadcastADC(AndaSendRequest request, AndaAddress andaAddress, String AndaAddress, Value Amount){
        if(SpvStroeDown.peerGroup == null){
            SpvStroeDown.SPVstoreDown();
        }

        org.bitcoinj.core.Wallet.SendResult  result = new Wallet.SendResult();
        org.bitcoinj.core.Transaction tx = request.tx.getRawTransaction();
        result.tx = tx;

        try {
            result.broadcastComplete = SpvStroeDown.peerGroup.broadcastTransaction(tx);
            Log.d(TAG, Constants.LOG_LABLE + "broadcastBtc: " + tx);

            SpvStroeDown.SpvDownStop();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    @Override
    public boolean isWatchedScript(Script script) {
        // Not supported
        return false;
    }

    @Override
    public boolean isPayToScriptHashMine(byte[] payToScriptHash) {
        // Not supported
        return false;
    }

    /**
     * Locates a keypair from the basicKeyChain given the hash of the public key. This is needed
     * when finding out which key we need to use to redeem a transaction output.
     *
     * @return ECKey object or null if no such key was found.
     */
    @Nullable
    @Override
    public ECKey findKeyFromPubHash(byte[] pubkeyHash) {
        lock.lock();
        try {
            return keys.findKeyFromPubHash(pubkeyHash);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Locates a keypair from the basicKeyChain given the raw public key bytes.
     * @return ECKey or null if no such key was found.
     */
    @Nullable
    @Override
    public ECKey findKeyFromPubKey(byte[] pubkey) {
        lock.lock();
        try {
            return keys.findKeyFromPubKey(pubkey);
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    @Override
    public RedeemData findRedeemDataFromScriptHash(byte[] bytes) {
        return null;
    }

    @Override
    public byte[] getPublicKey() {
        lock.lock();
        try {
            return keys.getRootKey().getPubKey();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public AndaAddress getChangeAddress() {
        return currentAddress(CHANGE);
    }

    @Override
    public AndaAddress getReceiveAddress() {
        return currentAddress(RECEIVE_FUNDS);
    }

    public AndaAddress getRefundAddress() { return currentAddress(REFUND); }

    @Override
    public boolean hasUsedAddresses() {
        return getNumberIssuedReceiveAddresses() != 0;
    }

    @Override
    public boolean canCreateNewAddresses() {
        return true;
    }

    @Override
    public AndaAddress getReceiveAddress(boolean isManualAddressManagement) {
        return getAddress(RECEIVE_FUNDS, isManualAddressManagement);
    }

    @Override
    public AndaAddress getRefundAddress(boolean isManualAddressManagement) {
        return getAddress(REFUND, isManualAddressManagement);
    }

    /**
     * Get the last used receiving address
     */
    @Nullable
    public AndaAddress getLastUsedAddress(SimpleHDKeyChain.KeyPurpose purpose) {
        lock.lock();
        try {
            DeterministicKey lastUsedKey = keys.getLastIssuedKey(purpose);
            if (lastUsedKey != null) {
                return AndaAddress.from(type, lastUsedKey);
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns true is it is possible to create new fresh receive addresses, false otherwise
     */
    public boolean canCreateFreshReceiveAddress() {
        lock.lock();
        try {
            DeterministicKey currentUnusedKey = keys.getCurrentUnusedKey(RECEIVE_FUNDS);
            int maximumKeyIndex = SimpleHDKeyChain.LOOKAHEAD - 1;

            // If there are used keys
            if (!addressesStatus.isEmpty()) {
                int lastUsedKeyIndex = 0;
                // Find the last used key index
                for (Map.Entry<AbstractAddress, String> entry : addressesStatus.entrySet()) {
                    if (entry.getValue() == null) continue;
                    DeterministicKey usedKey = keys.findKeyFromPubHash(getHash160(entry.getKey()));
                    if (usedKey != null && keys.isExternal(usedKey) && usedKey.getChildNumber().num() > lastUsedKeyIndex) {
                        lastUsedKeyIndex = usedKey.getChildNumber().num();
                    }
                }
                maximumKeyIndex = lastUsedKeyIndex + SimpleHDKeyChain.LOOKAHEAD;
            }

            log.info("Maximum key index for new key is {}", maximumKeyIndex);

            // If we exceeded the BIP44 look ahead threshold
            return currentUnusedKey.getChildNumber().num() < maximumKeyIndex;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get a fresh address by marking the current receive address as used. It will throw
     * {@link Bip44KeyLookAheadExceededException} if we requested too many addresses that
     * exceed the BIP44 look ahead threshold.
     */
    public AndaAddress getFreshReceiveAddress() throws Bip44KeyLookAheadExceededException {
        lock.lock();
        try {
            if (!canCreateFreshReceiveAddress()) {
                throw new Bip44KeyLookAheadExceededException();
            }
            keys.getKey(RECEIVE_FUNDS);
            return currentAddress(RECEIVE_FUNDS);
        } finally {
            lock.unlock();
            walletSaveNow();
        }
    }

    public AndaAddress getFreshReceiveAddress(boolean isManualAddressManagement)
            throws Bip44KeyLookAheadExceededException {
        lock.lock();
        try {
            AndaAddress newAddress = null;
            AndaAddress freshAddress = getFreshReceiveAddress();
            if (isManualAddressManagement) {
                newAddress = getLastUsedAddress(RECEIVE_FUNDS);
            }
            if (newAddress == null) {
                newAddress = freshAddress;
            }
            return newAddress;
        } finally {
            lock.unlock();
            walletSaveNow();
        }
    }

    private static final Comparator<DeterministicKey> HD_KEY_COMPARATOR =
            new Comparator<DeterministicKey>() {
                @Override
                public int compare(final DeterministicKey k1, final DeterministicKey k2) {
                    int key1Num = k1.getChildNumber().num();
                    int key2Num = k2.getChildNumber().num();
                    // In reality Integer.compare(key2Num, key1Num) but is not available on older devices
                    return (key2Num < key1Num) ? -1 : ((key2Num == key1Num) ? 0 : 1);
                }
            };

    /**
     * Returns the number of issued receiving keys
     */
    public int getNumberIssuedReceiveAddresses() {
        lock.lock();
        try {
            return keys.getNumIssuedExternalKeys();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns a list of addresses that have been issued.
     * The list is sorted in descending chronological order: older in the end
     */
    public List<AbstractAddress> getIssuedReceiveAddresses() {
        lock.lock();
        try {
            ArrayList<DeterministicKey> issuedKeys = keys.getIssuedExternalKeys();
            ArrayList<AbstractAddress> receiveAddresses = new ArrayList<>();

            Collections.sort(issuedKeys, HD_KEY_COMPARATOR);

            for (ECKey key : issuedKeys) {
                receiveAddresses.add(AndaAddress.from(type, key));
            }
            return receiveAddresses;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the currently used receiving and change addresses
     */
    public Set<AbstractAddress> getUsedAddresses() {
        lock.lock();
        try {
            HashSet<AbstractAddress> usedAddresses = new HashSet<>();

            for (Map.Entry<AbstractAddress, String> entry : addressesStatus.entrySet()) {
                if (entry.getValue() != null) {
                    usedAddresses.add(entry.getKey());
                }
            }

            return usedAddresses;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取地址
     * @param purpose
     * @param isManualAddressManagement
     * @return
     */
    public AndaAddress getAddress(SimpleHDKeyChain.KeyPurpose purpose,
                              boolean isManualAddressManagement) {
        AndaAddress receiveAddress = null;
        if (isManualAddressManagement) {
            receiveAddress = getLastUsedAddress(purpose);
        }

        if (receiveAddress == null) {
            receiveAddress = currentAddress(purpose);
        }
        return receiveAddress;
    }

    /**
     * Get the currently latest unused address by purpose.
     */
    @VisibleForTesting AndaAddress currentAddress(SimpleHDKeyChain.KeyPurpose purpose) {
        lock.lock();
        try {
            return AndaAddress.from(type, keys.getCurrentUnusedKey(purpose));
        } finally {
            lock.unlock();
            subscribeToAddressesIfNeeded();
        }
    }

    /**
     * Used to force keys creation, could take long time to complete so use it in a background
     * thread.
     */
    @VisibleForTesting
    public void maybeInitializeAllKeys() {
        lock.lock();
        try {
            keys.maybeLookAhead();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取公钥助记符
     * @return
     */
    @Override
    public String getPublicKeyMnemonic() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * 获取活跃的地址（已使用）
     * @return
     */
    @Override
    public List<AbstractAddress> getActiveAddresses() {
        lock.lock();
        try {
            ImmutableList.Builder<AbstractAddress> activeAddresses = ImmutableList.builder();
            for (DeterministicKey key : keys.getActiveKeys()) {
                activeAddresses.add(AndaAddress.from(type, key));
            }
            return activeAddresses.build();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 标记地址已使用
     * @param address
     */
    @Override
    public void markAddressAsUsed(AbstractAddress address) {
        checkArgument(address.getType().equals(type), "Wrong address type");
        if (address instanceof AndaAddress) {
            markAddressAsUsed((AndaAddress)address);
        } else {
            throw new IllegalArgumentException("Wrong address class");
        }

    }

    /**
     * 标记地址已使用
     * @param address
     */
    public void markAddressAsUsed(AndaAddress address) {
        keys.markPubHashAsUsed(address.getHash160());
    }

    @Override
    public String toString() {
        return WalletPocketHD_Anda.class.getSimpleName() + " " + id.substring(0, 4)+ " " + type;
    }
}
