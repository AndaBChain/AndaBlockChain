package com.onets.core.wallet;

import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.messages.TxMessage;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionConfidence.ConfidenceType;
import org.bitcoinj.core.TransactionConfidence.Source;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

/**
 * @author Yu K.Q.
 * 交易接口--抽象
 */
public interface AbstractTransaction extends Serializable {
    //输出
    class AbstractOutput {
        final AbstractAddress abstractAddress;
        final Value value;

        public AbstractOutput(AbstractAddress abstractAddress, Value value) {
            this.abstractAddress = abstractAddress;
            this.value = value;
        }

        //地址获取
        public AbstractAddress getAddress() {
            return abstractAddress;
        }

        //金额获取
        public Value getValue() {
            return value;
        }
    }

    //获取币类型
    CoinType getType();

    Sha256Hash getHash();
    String getHashAsString();
    byte[] getHashBytes();

    ConfidenceType getConfidenceType();
    void setConfidenceType(ConfidenceType type);

    int getAppearedAtChainHeight();
    void setAppearedAtChainHeight(int appearedAtChainHeight);

    Source getSource();
    void setSource(Source source);

    int getDepthInBlocks();
    void setDepthInBlocks(int depthInBlocks);

    long getTimestamp();
    void setTimestamp(long timestamp);

    Value getValue(AbstractWallet wallet);
    @Nullable Value getFee();
    @Nullable TxMessage getMessage();
    List<AbstractAddress> getReceivedFrom();//接收的源地址
    List<AbstractOutput> getSentTo();//发送的目标地址
    // Coin base or coin stake transaction
    boolean isGenerated();
    // If this transaction has trimmed irrelevant data to save space
    boolean isTrimmed();
    String toString();
    boolean equals(Object o);
}
