package com.onets.core.messages;

import com.onets.core.wallet.AbstractTransaction;

import java.io.Serializable;

/**
 * 交易信息
 * @author Yu K.Q.
 * 接口方法实现是在ClamsTxMessage
 * 接口方法实现是在NxtTxMessage
 * 接口方法实现是在VpncoinTxMessage
 */
public interface TxMessage extends Serializable {
    //use an abstract transaction
    void serializeTo(AbstractTransaction transaction);

    enum Type {
        PUBLIC, PRIVATE
    }

    Type getType();
    String toString();
}
