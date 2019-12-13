package com.onets.core.wallet;

import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.coins.ValueType;
import com.onets.core.exceptions.TransactionBroadcastException;
import com.onets.core.network.interfaces.ConnectionEventListener;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.wallet.KeyBag;
import org.spongycastle.crypto.params.KeyParameter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

/**
 * 钱包账户<AbstractTransaction , AbstractAddress>
 * @author Yu K.Q.
 */
public interface WalletAccount<T extends AbstractTransaction, A extends AbstractAddress>
        extends KeyBag, ConnectionEventListener, Serializable {

    class WalletAccountException extends Exception {
        public WalletAccountException(Throwable cause) {
            super(cause);
        }

        public WalletAccountException(String s) {
            super(s);
        }
    }
    /*获取ID 字符串*/
    String getId();
    /*获得描述或加密币币名称 字符串*/
    String getDescriptionOrCoinName();
    /*获得描述 字符串*/
    String getDescription();
    /*将String参数作为描述*/
    void setDescription(String description);
    /*获取公钥 byte数组*/
    byte[] getPublicKey();
    /*获取CoinType*/
    CoinType getCoinType();

    /*判断钱包账户是否微信*/
    boolean isNew();

    /*获取余额*/
    Value getBalance();

    /*刷新*/
    void refresh();

    /*判断是否已连接*/
    boolean isConnected();
    /*判断是否正在加载*/
    boolean isLoading();
    /*断开连接*/
    void disconnect();
    /*获取连接状态 */
    WalletConnectivityStatus getConnectivityStatus();

    /**
     * Returns the address used for change outputs. Note: this will probably go away in future.
     * 返回用于更改输出的地址。注意：这可能会在未来消失
     */
    AbstractAddress getChangeAddress();

    /**
     * Get current receive address, does not mark it as used.
     * 获取当前的接收地址，不将其标记为使用。
     */
    AbstractAddress getReceiveAddress();

    /**
     * Get current refund address, does not mark it as used.
     * 获取当前的退款地址，不将其标记为使用。
     * Notice: This address could be the same as the current receive address
     * 注意：这个地址可以与当前接收地址相同
     */
    AbstractAddress getRefundAddress(boolean isManualAddressManagement);

    /*获取当前的接收地址*/
    AbstractAddress getReceiveAddress(boolean isManualAddressManagement) ;


    /**
     * Returns true if this wallet has previously used addresses
     * 如果这个钱包以前使用过地址，返回true
     */
    boolean hasUsedAddresses();

    /*是否同步广播的交易信息*/
    boolean broadcastTxSync(AbstractTransaction tx) throws TransactionBroadcastException;

    /*广播交易信息*/
    void broadcastTx(AbstractTransaction tx) throws TransactionBroadcastException;

    /**
     * Returns true if this wallet can create new addresses
     * 如果这个钱包可以创建新的地址，返回true
     */
    boolean canCreateNewAddresses();

    /*通过交易ID获取交易*/
    T getTransaction(String transactionId);
    /*获取有待验证的交易信息*/
    Map<Sha256Hash, T> getPendingTransactions();
    /*获取交易*/
    Map<Sha256Hash, T> getTransactions();

    /*获得活跃的地址列表*/
    List<AbstractAddress> getActiveAddresses();
    /*标记地址为使用地址*/
    void markAddressAsUsed(AbstractAddress address);

    /*设置钱包*/
    void setWallet(Wallet wallet);

    /*获取钱包*/
    Wallet getWallet();

    /*钱包保存*/
    void walletSaveLater();
    void walletSaveNow();

    /*加解密部分*/
    boolean isEncryptable();
    boolean isEncrypted();
    KeyCrypter getKeyCrypter();
    void encrypt(KeyCrypter keyCrypter, KeyParameter aesKey);
    void decrypt(KeyParameter aesKey);


    boolean equals(WalletAccount otherAccount);

    /**
     * 事件监听器
     * @param listener
     */
    void addEventListener(WalletAccountEventListener listener);
    void addEventListener(WalletAccountEventListener listener, Executor executor);
    boolean removeEventListener(WalletAccountEventListener listener);

    /**
     * 类型判断
     * @param other
     * @return
     */
    boolean isType(WalletAccount other);
    boolean isType(ValueType type);
    boolean isType(AbstractAddress address);

    /**
     * 检测是否为挖矿地址---矿工地址
     * @param address
     * @return
     */
    boolean isAddressMine(AbstractAddress address);

    /**
     * 可能初始化所有key
     */
    void maybeInitializeAllKeys();

    String getPublicKeyMnemonic();

    /**
     * 获取空钱包请求
     * @param destination
     * @return
     * @throws WalletAccountException
     */
    SendRequest getEmptyWalletRequest(AbstractAddress destination) throws WalletAccountException;

    /**
     * 获取发送请求
     * @param destination
     * @param amount
     * @return
     * @throws WalletAccountException
     */
    SendRequest getSendToRequest(AbstractAddress destination, Value amount) throws WalletAccountException;

    /**
     * 完成安达交易签名
     * @param request
     * @throws WalletAccountException
     */
    void completeAndSignTx(SendRequest request) throws WalletAccountException;

    /**
     * 完成交易
     * @param request
     * @throws WalletAccountException
     */
    void completeTransaction(SendRequest request) throws WalletAccountException;

    /**
     * 交易签名
     * @param request
     * @throws WalletAccountException
     */
    void signTransaction(SendRequest request) throws WalletAccountException;

    /**
     * 签名信息
     * @param unsignedMessage
     * @param aesKey
     */
    void signMessage(SignedMessage unsignedMessage, @Nullable KeyParameter aesKey);

    /**
     * 验证签名
     * @param signedMessage
     */
    void verifyMessage(SignedMessage signedMessage);

    /**
     * 获取序列化的公钥
     * @return
     */
    String getPublicKeySerialized();

    SendRequest sendCoins(AbstractAddress address, CoinType coinType, Value Amount, String s, String password) throws WalletAccountException;

}
