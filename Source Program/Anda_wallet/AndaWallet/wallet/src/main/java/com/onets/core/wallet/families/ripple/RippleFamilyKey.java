package com.onets.core.wallet.families.ripple;

import android.util.Log;

import com.google.protobuf.ByteString;
import com.onets.core.protos.Protos;
import com.onets.core.util.KeyUtils;
import com.ripple.config.Config;
import com.ripple.core.coretypes.AccountID;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;

import org.bitcoinj.core.BloomFilter;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.crypto.KeyCrypterException;
import org.bitcoinj.crypto.KeyCrypterScrypt;
import org.bitcoinj.store.UnreadableWalletException;
import org.bitcoinj.wallet.EncryptableKeyChain;
import org.bitcoinj.wallet.KeyChainEventListener;
import org.spongycastle.crypto.params.KeyParameter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newLinkedList;

/**
 * @author Yu K.Q.
 */
final public class RippleFamilyKey implements EncryptableKeyChain, Serializable {
    private final DeterministicKey entropy;
    private String address;

    public RippleFamilyKey(DeterministicKey entropy, @Nullable KeyCrypter keyCrypter,
                           @Nullable KeyParameter key) {
        checkArgument(!entropy.isEncrypted(), "Entropy must not be encrypted");

        String addr = new String(entropy.getPrivKeyBytes());
        Config.initBouncy();
//        SecureRandom random = new SecureRandom();
//        byte[] seedBytes = new byte[16];
//        random.nextBytes(seedBytes);
        byte[] seedBytes = Arrays.copyOfRange(addr.getBytes(),0,16);
        Seed seed = new Seed(seedBytes);
        IKeyPair iKeyPair = seed.keyPair();
        byte[] pub160Hash = iKeyPair.pub160Hash();
        AccountID accountID = AccountID.fromBytes(pub160Hash);
        Log.i("RippleFamilyKey","secret= " + seed + ", address=" + accountID);

        this.address = accountID.address;

        // Encrypt entropy if needed
        if (keyCrypter != null && key != null) {
            this.entropy = entropy.encrypt(keyCrypter, key, null);
        } else {
            this.entropy = entropy;
        }
        Log.i("RippleFamilyKey", "RippleFamilyKey: create success");

    }

    private RippleFamilyKey(DeterministicKey entropy, String address) {
        this.entropy = entropy;
        this.address = address;

    }

    public boolean isEncrypted() {
        return entropy.isEncrypted();
    }

    public String getAddress() {
        return address;
    }

    public byte[] getPrivateKey() {
        return entropy.getPrivKeyBytes();
    }

    //    @Nullable
//    @Override
//    public ECKey findKeyFromPubHash(byte[] pubkeyHash) {
//        throw new RuntimeException("Not implemented");
//    }
//
//    @Nullable
//    @Override
//    public ECKey findKeyFromPubKey(byte[] pubkey) {
//        throw new RuntimeException("Not implemented");
//    }
//
//    @Nullable
//    @Override
//    public RedeemData findRedeemDataFromScriptHash(byte[] scriptHash) {
//        throw new RuntimeException("Not implemented");
//    }
//
    @Override
    public boolean hasKey(ECKey key) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<? extends ECKey> getKeys(KeyPurpose purpose, int numberOfKeys) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ECKey getKey(KeyPurpose purpose) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void addEventListener(KeyChainEventListener listener) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void addEventListener(KeyChainEventListener listener, Executor executor) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean removeEventListener(KeyChainEventListener listener) {
        throw new RuntimeException("Not implemented");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Serialization support
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public List<org.bitcoinj.wallet.Protos.Key> serializeToProtobuf() {
        throw new RuntimeException("Not implemented. Use toProtobuf() method instead.");
    }

    List<Protos.Key> toProtobuf() {
        LinkedList<Protos.Key> entries = newLinkedList();
        List<Protos.Key.Builder> protos = toEditableProtobuf();
        for (Protos.Key.Builder proto : protos) {
            entries.add(proto.build());
        }
        return entries;
    }

    List<Protos.Key.Builder> toEditableProtobuf() {
        LinkedList<Protos.Key.Builder> entries = newLinkedList();

        // Entropy
        Protos.Key.Builder entropyProto = KeyUtils.serializeKey(entropy);
        entropyProto.setType(Protos.Key.Type.DETERMINISTIC_KEY);
        final Protos.DeterministicKey.Builder detKey = entropyProto.getDeterministicKeyBuilder();
        detKey.setChainCode(ByteString.copyFrom(entropy.getChainCode()));
        for (ChildNumber num : entropy.getPath()) {
            detKey.addPath(num.i());
        }
        entries.add(entropyProto);

        // Anda key
        Protos.Key.Builder publicKeyProto = Protos.Key.newBuilder();
        publicKeyProto.setType(Protos.Key.Type.ORIGINAL);
        publicKeyProto.setPublicKey(ByteString.copyFrom(address.getBytes()));
        entries.add(publicKeyProto);

        return entries;
    }

    /**
     * Returns the key chain found in the given list of keys. Used for unencrypted chains
     */
    public static RippleFamilyKey fromProtobuf(List<Protos.Key> keys) throws UnreadableWalletException {
        return fromProtobuf(keys, null);
    }

    /**
     * Returns the key chain found in the given list of keys.
     */
    public static RippleFamilyKey fromProtobuf(List<Protos.Key> keys, @Nullable KeyCrypter crypter)
            throws UnreadableWalletException {
        if (keys.size() != 2) {
            throw new UnreadableWalletException("Expected 2 keys, NXT secret and Curve25519 " +
                    "pub/priv pair, instead got: " + keys.size());
        }

        Protos.Key entropyProto = keys.get(0);
        DeterministicKey entropyKey = KeyUtils.getDeterministicKey(entropyProto, null, crypter);

        Protos.Key publicKeyProto = keys.get(1);
        if (publicKeyProto.getType() != Protos.Key.Type.ORIGINAL) {
            throw new UnreadableWalletException("Unexpected type for Anda address: " +
                    publicKeyProto.getType());
        }
        byte[] addressBytes = publicKeyProto.getPublicKey().toByteArray();

        return new RippleFamilyKey(entropyKey, new String(addressBytes));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Encryption support
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public RippleFamilyKey toEncrypted(CharSequence password) {
        checkNotNull(password, "Attempt to encrypt with a null password.");
        checkArgument(password.length() > 0, "Attempt to encrypt with an empty password.");
        checkState(!entropy.isEncrypted(), "Attempt to encrypt a key that is already encrypted.");
        KeyCrypter scrypt = new KeyCrypterScrypt();
        KeyParameter derivedKey = scrypt.deriveKey(password);
        return toEncrypted(scrypt, derivedKey);
    }

    @Override
    public RippleFamilyKey toEncrypted(KeyCrypter keyCrypter, KeyParameter aesKey) {
        checkState(!entropy.isEncrypted(), "Attempt to encrypt a key that is already encrypted.");
        return new RippleFamilyKey(entropy.encrypt(keyCrypter, aesKey, null), address);
    }

    @Override
    public RippleFamilyKey toDecrypted(CharSequence password) {
        checkNotNull(password, "Attempt to decrypt with a null password.");
        checkArgument(password.length() > 0, "Attempt to decrypt with an empty password.");
        KeyCrypter crypter = getKeyCrypter();
        checkState(crypter != null, "Chain not encrypted");
        KeyParameter derivedKey = crypter.deriveKey(password);
        return toDecrypted(derivedKey);
    }

    @Override
    public RippleFamilyKey toDecrypted(KeyParameter aesKey) {
        checkState(isEncrypted(), "Key is not encrypted");
        checkNotNull(getKeyCrypter(), "Key chain not encrypted");
        DeterministicKey entropyDecrypted = entropy.decrypt(getKeyCrypter(), aesKey);
        return new RippleFamilyKey(entropyDecrypted, address);
    }

    @Override
    public boolean checkPassword(CharSequence password) {
        checkNotNull(password, "Password is null");
        checkState(getKeyCrypter() != null, "Key chain not encrypted");
        return checkAESKey(getKeyCrypter().deriveKey(password));
    }

    @Override
    public boolean checkAESKey(KeyParameter aesKey) {
        checkNotNull(aesKey, "Cannot check null KeyParameter");
        checkNotNull(getKeyCrypter(), "Key not encrypted");
        try {

            String addr = new String(entropy.decrypt(aesKey).getPrivKeyBytes());
            byte[] seedBytes = Arrays.copyOfRange(addr.getBytes(),0,16);
            Seed seed = new Seed(seedBytes);
            IKeyPair iKeyPair = seed.keyPair();
            byte[] pub160Hash = iKeyPair.pub160Hash();
            AccountID accountID = AccountID.fromBytes(pub160Hash);
            return Arrays.equals(address.getBytes(), accountID.address.getBytes());
        } catch (KeyCrypterException e) {
            return false;
        }
    }

    @Nullable
    @Override
    public KeyCrypter getKeyCrypter() {
        return entropy.getKeyCrypter();
    }

    @Override
    public int numKeys() {
        return 1;
    }

    @Override
    public long getEarliestKeyCreationTime() {
        return entropy.getCreationTimeSeconds();
    }

    @Override
    public int numBloomFilterEntries() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public BloomFilter getFilter(int size, double falsePositiveRate, long tweak) {
        throw new RuntimeException("Not implemented");
    }
}
