package com.onets.core.messages;

import com.onets.core.wallet.AbstractTransaction;

import javax.annotation.Nullable;

/**
 * 消息模式
 * @author Yu K.Q.
 * 接口方法实现是在ClamsTxMessage
 * 接口方法实现是在NxtTxMessage
 * 接口方法实现是在VpncoinTxMessage
 */
public interface MessageFactory {
    int maxMessageSizeBytes();

    boolean canHandlePublicMessages();

    boolean canHandlePrivateMessages();

    TxMessage createPublicMessage(String message);

    @Nullable
    TxMessage extractPublicMessage(AbstractTransaction transaction);
}
