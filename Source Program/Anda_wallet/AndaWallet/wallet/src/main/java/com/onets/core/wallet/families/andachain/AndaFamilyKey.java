package com.onets.core.wallet.families.andachain;

import android.util.Log;

import com.google.protobuf.ByteString;
import com.onets.core.protos.Protos;
import com.onets.core.util.KeyUtils;
import com.onets.core.wallet.families.andachain.sm.SM2;
import com.onets.core.wallet.families.andachain.utils.HashUtil;

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
import org.spongycastle.util.encoders.Hex;

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
 * 安达Family Key
 */
final public class AndaFamilyKey implements EncryptableKeyChain, Serializable {
    private final DeterministicKey entropy;
    private String address;

    public AndaFamilyKey(DeterministicKey entropy, @Nullable KeyCrypter keyCrypter,
                         @Nullable KeyParameter key) {
        checkArgument(!entropy.isEncrypted(), "Entropy must not be encrypted");

        String addr = null;
        try {
            addr = new String(entropy.getPrivKeyBytes());
            com.onets.core.wallet.families.andachain.utils.ECKey eckey1 = new com.onets.core.wallet.families.andachain.utils.ECKey(new SM2());
            com.onets.core.wallet.families.andachain.utils.ECKey ecKey = com.onets.core.wallet.families.andachain.utils.ECKey.fromPrivate(HashUtil.sm3_Hash(addr.getBytes()));
            this.address = "A0" + Hex.toHexString(ecKey.getAddress());

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Encrypt entropy if needed
        if (keyCrypter != null && key != null) {
            this.entropy = entropy.encrypt(keyCrypter, key, null);
        } else {
            this.entropy = entropy;
        }
        Log.i("AndaFamilyKey", "AndaFamilyKey: create success");

    }

    private AndaFamilyKey(DeterministicKey entropy, String address) {
        this.entropy = entropy;
        this.address = address;

    }

    /**
     * 判断是否加密
     * @return
     */
    public boolean isEncrypted() {
        return entropy.isEncrypted();
    }

    /**
     * 获取地址
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     * 获取私钥
     * @return
     */
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

    /**
     * 判断是否为key
     * @param key
     * @return
     */
    @Override
    public boolean hasKey(ECKey key) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * 获取key列表
     * @param purpose
     * @param numberOfKeys
     * @return
     */
    @Override
    public List<? extends ECKey> getKeys(KeyPurpose purpose, int numberOfKeys) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * 获取ECKey
     * @param purpose
     * @return
     */
    @Override
    public ECKey getKey(KeyPurpose purpose) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * 添加事件监听器
     * @param listener
     */
    @Override
    public void addEventListener(KeyChainEventListener listener) {
        throw new RuntimeException("Not implemented");
    }

    /**
     *  添加事件监听器
     * @param listener
     * @param executor
     */
    @Override
    public void addEventListener(KeyChainEventListener listener, Executor executor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * 移除事件监听器
     * @param listener
     * @return
     */
    @Override
    public boolean removeEventListener(KeyChainEventListener listener) {
        throw new RuntimeException("Not implemented");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Serialization support
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 序列化到Protobuf
     * @return
     */
    @Override
    public List<org.bitcoinj.wallet.Protos.Key> serializeToProtobuf() {
        throw new RuntimeException("Not implemented. Use toProtobuf() method instead.");
    }

    /**
     * 转换到Protobuf
     * @return
     */
    List<Protos.Key> toProtobuf() {
        LinkedList<Protos.Key> entries = newLinkedList();
        List<Protos.Key.Builder> protos = toEditableProtobuf();
        for (Protos.Key.Builder proto : protos) {
            entries.add(proto.build());
        }
        return entries;
    }

    /**
     * 到可编辑的Protobuf
     * @return
     */
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
     * 返回给定键列表中找到的键链。 用于未加密的链
     */
    public static AndaFamilyKey fromProtobuf(List<Protos.Key> keys) throws UnreadableWalletException {
        return fromProtobuf(keys, null);
    }

    /**
     * 返回给定键列表中找到的键链。
     */
    public static AndaFamilyKey fromProtobuf(List<Protos.Key> keys, @Nullable KeyCrypter crypter)
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

        return new AndaFamilyKey(entropyKey, new String(addressBytes));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Encryption support
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 加密过的
     * @param password
     * @return
     */
    @Override
    public AndaFamilyKey toEncrypted(CharSequence password) {
        checkNotNull(password, "Attempt to encrypt with a null password.");
        checkArgument(password.length() > 0, "Attempt to encrypt with an empty password.");
        checkState(!entropy.isEncrypted(), "Attempt to encrypt a key that is already encrypted.");
        KeyCrypter scrypt = new KeyCrypterScrypt();
        KeyParameter derivedKey = scrypt.deriveKey(password);
        return toEncrypted(scrypt, derivedKey);
    }

    /**
     * 加密过
     * @param keyCrypter
     * @param aesKey
     * @return
     */
    @Override
    public AndaFamilyKey toEncrypted(KeyCrypter keyCrypter, KeyParameter aesKey) {
        checkState(!entropy.isEncrypted(), "Attempt to encrypt a key that is already encrypted.");
        return new AndaFamilyKey(entropy.encrypt(keyCrypter, aesKey, null), address);
    }

    /**
     * 解密
     * @param password
     * @return
     */
    @Override
    public AndaFamilyKey toDecrypted(CharSequence password) {
        checkNotNull(password, "Attempt to decrypt with a null password.");
        checkArgument(password.length() > 0, "Attempt to decrypt with an empty password.");
        KeyCrypter crypter = getKeyCrypter();
        checkState(crypter != null, "Chain not encrypted");
        KeyParameter derivedKey = crypter.deriveKey(password);
        return toDecrypted(derivedKey);
    }

    @Override
    public AndaFamilyKey toDecrypted(KeyParameter aesKey) {
        checkState(isEncrypted(), "Key is not encrypted");
        checkNotNull(getKeyCrypter(), "Key chain not encrypted");
        DeterministicKey entropyDecrypted = entropy.decrypt(getKeyCrypter(), aesKey);
        return new AndaFamilyKey(entropyDecrypted, address);
    }

    /**
     * 检查密码
     * @param password
     * @return
     */
    @Override
    public boolean checkPassword(CharSequence password) {
        checkNotNull(password, "Password is null");
        checkState(getKeyCrypter() != null, "Key chain not encrypted");
        return checkAESKey(getKeyCrypter().deriveKey(password));
    }

    /**
     * 检查AES key
     * @param aesKey
     * @return
     */
    @Override
    public boolean checkAESKey(KeyParameter aesKey) {
        checkNotNull(aesKey, "Cannot check null KeyParameter");
        checkNotNull(getKeyCrypter(), "Key not encrypted");
        try {
            return Arrays.equals(address.getBytes(),
                    ("0x" + Hex.toHexString(com.onets.core.wallet.families.andachain.utils.ECKey.fromPrivate(HashUtil.sm3_Hash(entropy.decrypt(aesKey).getPrivKeyBytes())).getAddress())).getBytes());
        } catch (KeyCrypterException e) {
            return false;
        }
    }

    /**
     * 得到Key Crypter
     * @return
     */
    @Nullable
    @Override
    public KeyCrypter getKeyCrypter() {
        return entropy.getKeyCrypter();
    }

    @Override
    public int numKeys() {
        return 1;
    }

    /**
     * 获得最早的密钥创建时间
     * @return
     */
    @Override
    public long getEarliestKeyCreationTime() {
        return entropy.getCreationTimeSeconds();
    }

    /**
     * num Bloom过滤器条目
     * @return
     */
    @Override
    public int numBloomFilterEntries() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * 得到过滤器
     * @param size
     * @param falsePositiveRate
     * @param tweak
     * @return
     */
    @Override
    public BloomFilter getFilter(int size, double falsePositiveRate, long tweak) {
        throw new RuntimeException("Not implemented");
    }
}
