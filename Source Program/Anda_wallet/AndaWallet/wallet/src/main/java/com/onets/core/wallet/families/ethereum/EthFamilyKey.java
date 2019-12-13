package com.onets.core.wallet.families.ethereum;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.view.menu.MenuWrapperFactory;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.onets.core.protos.Protos;
import com.onets.core.util.KeyUtils;
import com.onets.core.wallet.families.ethereum.data.WalletDisplay;
import com.onets.core.wallet.families.ethereum.interfaces.StorableWallet;
import com.onets.core.wallet.families.ethereum.utils.AddressNameConverter;
import com.onets.core.wallet.families.ethereum.utils.OwnWalletUtils;
import com.onets.core.wallet.families.ethereum.utils.WalletStorage;
import com.onets.wallet.Constants;
import com.onets.wallet.WalletApplication;
import com.onets.wallet.service.ServerHttp;
import com.onets.wallet.util.WalletUtils;

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
import org.web3j.crypto.CipherException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.methods.response.NewAccountIdentifier;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
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
final public class EthFamilyKey implements EncryptableKeyChain, Serializable {
    private final DeterministicKey entropy;
    private String address;

    public EthFamilyKey(DeterministicKey entropy, @Nullable KeyCrypter keyCrypter,
                        @Nullable KeyParameter key) {
        checkArgument(!entropy.isEncrypted(), "Entropy must not be encrypted");

        String addr = new String(entropy.getPrivKeyBytes());
        Log.d("EthFamilyKey", "EthFamilyKey: addr " + addr);
        /*try {
            Log.d("EthFamilyKey", "EthFamilyKey: newAccount(addr)" + newAccount(addr));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        Constants.saveEthPassInfo(WalletApplication.getInstance(), addr);

        File file1 = new File(WalletApplication.getInstance().getFilesDir(), "pass.txt");
        FileWriter writer = null;
        try {
            writer = new FileWriter(file1);
            writer.write(addr);
            Log.d("E", "EthFamilyKey: --addr" + addr);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //todo:  创建或读取钱包中以太坊的地址,此处可以修改判断条件用来显示多个账户或某一个账户
        final ArrayList<StorableWallet> storedwallets = new ArrayList<StorableWallet>(WalletStorage.getInstance(WalletApplication.getInstance()).get());

        Log.d("EthFamilyKey", "EthFamilyKey: storedwallets.size() " + storedwallets.size());

        if (storedwallets.size() == 0) {
            try {
                byte[] ecKeyPairInput = WalletUtils.getFromAssets("ecKeyPairInput.txt",WalletApplication.getInstance());
                //this.address = "0x" + OwnWalletUtils.restoreWalletFile(addr, new File(WalletApplication.getInstance().getFilesDir(), ""), true,ecKeyPairInput);
                File file = new File(WalletApplication.getInstance().getFilesDir(), "");
                Log.d("EthFamilyKey", "EthFamilyKey: -----------file.getPath()"+file.getPath());

                //useFullScrypt = true ===> createStandard 使用时，产生过OOM
                //useFullScrypt = false ===> createLight
                this.address = "0x" + OwnWalletUtils.generateNewWalletFile(addr,file, false);
                Log.d("EthFamilyKey", "EthFamilyKey: -----address"+address);

            } catch (CipherException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            }
        } else {
            //EthFamilyKey只初始化一次，不走else  多账号时待用
            final List<WalletDisplay> w = new ArrayList<WalletDisplay>();
            for (StorableWallet cur : storedwallets)
                w.add(new WalletDisplay(AddressNameConverter.getInstance(WalletApplication.getInstance()).get(cur.getPubKey()), cur.getPubKey(), new BigInteger("-1"), WalletDisplay.CONTACT));

            List<WalletDisplay> wallets = new ArrayList<>();
            wallets.addAll(w);
            this.address = wallets.get(0).getPublicKey();   //取出集合中默认的账户
        }


        // Encrypt entropy if needed
        if (keyCrypter != null && key != null) {
            this.entropy = entropy.encrypt(keyCrypter, key, null);
        } else {
            this.entropy = entropy;
        }
        Log.i("EthFamilyKey", "EthFamilyKey: create success");

    }


    private EthFamilyKey(DeterministicKey entropy, String address) {
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
    public static EthFamilyKey fromProtobuf(List<Protos.Key> keys) throws UnreadableWalletException {
        return fromProtobuf(keys, null);
    }

    /**
     * Returns the key chain found in the given list of keys.
     */
    public static EthFamilyKey fromProtobuf(List<Protos.Key> keys, @Nullable KeyCrypter crypter)
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

        return new EthFamilyKey(entropyKey, new String(addressBytes));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Encryption support
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public EthFamilyKey toEncrypted(CharSequence password) {
        checkNotNull(password, "Attempt to encrypt with a null password.");
        checkArgument(password.length() > 0, "Attempt to encrypt with an empty password.");
        checkState(!entropy.isEncrypted(), "Attempt to encrypt a key that is already encrypted.");
        KeyCrypter scrypt = new KeyCrypterScrypt();
        KeyParameter derivedKey = scrypt.deriveKey(password);
        return toEncrypted(scrypt, derivedKey);
    }

    @Override
    public EthFamilyKey toEncrypted(KeyCrypter keyCrypter, KeyParameter aesKey) {
        checkState(!entropy.isEncrypted(), "Attempt to encrypt a key that is already encrypted.");
        return new EthFamilyKey(entropy.encrypt(keyCrypter, aesKey, null), address);
    }

    @Override
    public EthFamilyKey toDecrypted(CharSequence password) {
        checkNotNull(password, "Attempt to decrypt with a null password.");
        checkArgument(password.length() > 0, "Attempt to decrypt with an empty password.");
        KeyCrypter crypter = getKeyCrypter();
        checkState(crypter != null, "Chain not encrypted");
        KeyParameter derivedKey = crypter.deriveKey(password);
        return toDecrypted(derivedKey);
    }

    @Override
    public EthFamilyKey toDecrypted(KeyParameter aesKey) {
        checkState(isEncrypted(), "Key is not encrypted");
        checkNotNull(getKeyCrypter(), "Key chain not encrypted");
        DeterministicKey entropyDecrypted = entropy.decrypt(getKeyCrypter(), aesKey);
        return new EthFamilyKey(entropyDecrypted, address);
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
            byte[] ecKeyPairInput = WalletUtils.getFromAssets("ecKeyPairInput.txt",WalletApplication.getInstance());
            return Arrays.equals(address.getBytes(),
                    ("0x" + OwnWalletUtils.restoreWalletFile(addr,
                            new File(WalletApplication.getInstance().getFilesDir(), ""),
                            true,ecKeyPairInput)
                    ).getBytes());
        } catch (KeyCrypterException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return false;
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
