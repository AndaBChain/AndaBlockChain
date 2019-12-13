package com.onets.core.wallet;

import javax.annotation.Nullable;

import static com.onets.core.Preconditions.checkNotNull;

/**
 * @author Yu K.Q.
 * 签名消息
 */
final public class SignedMessage {
    public enum Status {
        SignedOK, VerifiedOK, Unknown, AddressMalformed, KeyIsEncrypted, MissingPrivateKey,
        InvalidSigningAddress, InvalidMessageSignature
    }

    final String message;
    final String address;
    String signature;
    Status status = Status.Unknown;

    public SignedMessage(String address, String message, String signature) {
        this.address = checkNotNull(address);
        this.message = checkNotNull(message);
        this.signature = signature;
    }

    public SignedMessage(String address, String message) {
        this(address, message, null);
    }

    public SignedMessage(SignedMessage otherMessage, Status newStatus) {
        message = otherMessage.message;
        address = otherMessage.address;
        signature = otherMessage.signature;
        status = newStatus;
    }

    /**
     * 获取地址
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     * 获取消息
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取签名
     * @return
     */
    @Nullable
    public String getSignature() {
        return signature;
    }

    /**
     * 获取状态
     * @return
     */
    public Status getStatus() {
        return status;
    }
}
