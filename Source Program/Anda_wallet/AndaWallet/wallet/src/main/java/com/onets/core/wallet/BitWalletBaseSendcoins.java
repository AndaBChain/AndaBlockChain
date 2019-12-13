package com.onets.core.wallet;

import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.RedeemData;
import org.spongycastle.crypto.params.KeyParameter;

import java.util.List;

import javax.annotation.Nullable;

public class BitWalletBaseSendcoins extends BitWalletBase {
   public  BitWalletBaseSendcoins(CoinType coinType, String id) {
        super(coinType, id);
    }


    @Override
    public byte[] getPublicKey() {
        return new byte[0];
    }

    @Override
    public AbstractAddress getChangeAddress() {
        return null;
    }

    @Override
    public AbstractAddress getReceiveAddress() {
        return null;
    }

    @Override
    public AbstractAddress getRefundAddress(boolean isManualAddressManagement) {
        return null;
    }

    @Override
    public AbstractAddress getReceiveAddress(boolean isManualAddressManagement) {
        return null;
    }

    @Override
    public boolean hasUsedAddresses() {
        return false;
    }

    @Override
    public boolean canCreateNewAddresses() {
        return false;
    }

    @Override
    public List<AbstractAddress> getActiveAddresses() {
        return null;
    }

    @Override
    public void markAddressAsUsed(AbstractAddress address) {

    }

    @Override
    public boolean isEncryptable() {
        return false;
    }

    @Override
    public boolean isEncrypted() {
        return false;
    }

    @Override
    public KeyCrypter getKeyCrypter() {
        return null;
    }

    @Override
    public void encrypt(KeyCrypter keyCrypter, KeyParameter aesKey) {

    }

    @Override
    public void decrypt(KeyParameter aesKey) {

    }

    @Override
    public void maybeInitializeAllKeys() {

    }

    @Override
    public String getPublicKeyMnemonic() {
        return null;
    }

    @Override
    public String getPublicKeySerialized() {
        return null;
    }

    @Override
    public SendRequest sendCoins(AbstractAddress address, CoinType coinType, Value Amount, String s, String password) throws WalletAccountException {
        return null;
    }

    @Override
    public boolean isWatchedScript(Script script) {
        return false;
    }

    @Override
    public boolean isPayToScriptHashMine(byte[] bytes) {
        return false;
    }

    @Nullable
    @Override
    public ECKey findKeyFromPubHash(byte[] bytes) {
        return null;
    }

    @Nullable
    @Override
    public ECKey findKeyFromPubKey(byte[] bytes) {
        return null;
    }

    @Nullable
    @Override
    public RedeemData findRedeemDataFromScriptHash(byte[] bytes) {
        return null;
    }
}
