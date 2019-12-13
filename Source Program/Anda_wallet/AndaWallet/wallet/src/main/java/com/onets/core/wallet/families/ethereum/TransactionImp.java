package com.onets.core.wallet.families.ethereum;

import android.support.annotation.NonNull;

import com.onets.core.coins.nxt.Convert;
import com.onets.core.coins.nxt.NxtException;
import com.onets.core.wallet.families.ethereum.utils.Transaction;

import org.web3j.utils.Numeric;

import java.math.BigInteger;

/**
 * 以太坊交易实现类
 * Created by Hasee on 2018/1/15.
 */

public final class TransactionImp implements Transaction {

    @Override
    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    /*Builder接口实现*/
    public static final class BuilderImpl implements Builder {

        private final String senderAddress;//发送方地址
        private final int timestamp;//交易时间戳
        private long recipientId;//接收方id
        private byte[] signature;//交易签名
        private long blockId;//交易所在块id
        private int height = Integer.MAX_VALUE;//
        private long id;//

        private String txHash;//交易哈希
        private int ecBlockHeight;
        private long ecBlockId;

        private BigInteger nonce;//随机数
        private BigInteger gasPrice;//gas值
        private BigInteger gasLimit;//gas限制
        private String to;
        private BigInteger value;
        private String data;

        public BuilderImpl(String senderAddress, BigInteger value_big, int timestamp, String toAddress) {
            this.timestamp = timestamp;
            this.senderAddress = senderAddress;
            this.value = value_big;
            this.to = toAddress;
        }

        @Override
        public TransactionImp build() throws NxtException.NotValidException {
            return new TransactionImp(this);
        }

        @Override
        public BuilderImpl recipientId(long recipientId) {
            this.recipientId = recipientId;
            return this;
        }

        BuilderImpl id(long id) {
            this.id = id;
            return this;
        }

        public BuilderImpl signature(byte[] signature) {
            this.signature = signature;
            return this;
        }


        BuilderImpl blockId(long blockId) {
            this.blockId = blockId;
            return this;
        }

        BuilderImpl toAddress(String toAddress) {
            this.to = toAddress;
            return this;
        }

        BuilderImpl height(int height) {
            this.height = height;
            return this;
        }

//        BuilderImpl senderId(long senderId) {
//            this.senderId = senderId;
//            return this;
//        }

        BuilderImpl txHash(String fullHash) {
            this.txHash = fullHash;
            return this;
        }

        BuilderImpl txHash(byte[] fullHash) {
            if (fullHash != null) {
                this.txHash = Convert.toHexString(fullHash);
            }
            return this;
        }

        public BuilderImpl ecBlockHeight(int height) {
            this.ecBlockHeight = height;
            return this;
        }

        public BuilderImpl ecBlockId(long blockId) {
            this.ecBlockId = blockId;
            return this;
        }
        public BuilderImpl gasUsed(long gas) {
            this.gasLimit = new BigInteger(gas+"");
            return this;
        }
        public BuilderImpl gasPrice(long gasPrice) {
            this.gasPrice = new BigInteger(gasPrice+"");
            return this;
        }


    }

    private final long recipientId;

    private final String senderAddress;
    private final int ecBlockHeight;
    private final long ecBlockId;
    private final int timestamp;

    private volatile int height = Integer.MAX_VALUE;
    private volatile byte[] signature;
    private volatile int blockTimestamp = -1;
    private volatile long id;
    //    private volatile long senderId;
    private volatile String fullHash;
    private int confirmations = 0;


    private BigInteger nonce;
    private BigInteger gasPrice;
    private BigInteger gasLimit;
    private String to;
    private BigInteger value;
    private String data;


    private TransactionImp(BuilderImpl builder) throws NxtException.NotValidException {
        this.timestamp = builder.timestamp;
        this.senderAddress = builder.senderAddress;
        this.recipientId = builder.recipientId;
        this.value = builder.value;
        this.signature = builder.signature;
        this.height = builder.height;
        this.id = builder.id;
        this.to = builder.to;
//        this.senderId = builder.senderId;
        this.ecBlockId = builder.ecBlockId;
        this.fullHash = builder.txHash;
        this.ecBlockHeight = builder.ecBlockHeight;
        this.gasLimit = builder.gasLimit;
        this.gasPrice = builder.gasPrice;

    }

    private TransactionImp(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value, String data) {

        recipientId = 0;
        senderAddress = "";
        ecBlockHeight = 0;
        ecBlockId = 0;
        timestamp = 0;

        this.nonce = nonce;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.to = to;
        this.value = value;
        if (data != null) {
            this.data = Numeric.cleanHexPrefix(data);
        }

    }


    public static TransactionImp createContractTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, BigInteger value, String init) {
        return new TransactionImp(nonce, gasPrice, gasLimit, "", value, init);
    }

    public static TransactionImp createEtherTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value) {
        return new TransactionImp(nonce, gasPrice, gasLimit, to, value, "");
    }

    public static TransactionImp createTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, String data) {
        return createTransaction(nonce, gasPrice, gasLimit, to, BigInteger.ZERO, data);
    }

    public static TransactionImp createTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value, String data) {
        return new TransactionImp(nonce, gasPrice, gasLimit, to, value, data);
    }


    public static TransactionImp parseTransaction(String senderAddress, String to, BigInteger amount,long gas, long gasPrice, String txHash,
                                                  String time, int height, long blockHeight, int confirmat)
            throws NxtException.NotValidException {


        BuilderImpl builder = new BuilderImpl(senderAddress, amount, Integer.valueOf(time), to)
                .height(height).txHash(txHash).gasUsed(gas).gasPrice(gasPrice);

        builder.ecBlockHeight(Integer.valueOf(blockHeight + ""));

        int confirmations = confirmat;
        TransactionImp tx = builder.build();
        tx.setConfirmations(confirmations);
        return tx;

    }


    public BigInteger getNonce() {
        return this.nonce;
    }

    public BigInteger getGasPrice() {
        return this.gasPrice;
    }

    public BigInteger getGasLimit() {
        return this.gasLimit;
    }

    public String getTo() {
        return this.to;
    }

    public BigInteger getValue() {
        return this.value;
    }

    public String getData() {
        return this.data;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }


    @Override
    public long getId() {
        //todo:  getId

        return id;
    }


    @Override
    public long getRecipientId() {
        return recipientId;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public int getConfirmations() {
        return this.confirmations;
    }

    @Override
    public BigInteger getAmountETH() {
        return value;
    }

    @Override
    public String getFullHash() {
        if (fullHash == null) {
//            getId();
            return "0x7578e0c332c16575723ffb2d67ccef5809878182bb8095a19b628902c6f76624";
        }
        return fullHash;
    }

    @Override
    public void sign(byte[] privateKey) {
        //todo:  sign
        //checkForSignature();
        signature = privateKey;
    }

    private void checkForSignature() {
        if (signature != null) {
            throw new IllegalStateException("Transaction already signed");
        }
    }

    @Override
    public byte[] getBytes() {
//        try {
//            ByteBuffer buffer = ByteBuffer.allocate(getSize());
//            buffer.order(ByteOrder.LITTLE_ENDIAN);
//            buffer.put(type.getType());
//            buffer.put((byte) ((version << 4) | type.getSubtype()));
//            buffer.putInt(timestamp);
//            buffer.putShort(deadline);
//            buffer.put(senderPublicKey);
//            buffer.putLong(type.hasRecipient() ? recipientId : getGenesisId());
//            if (useNQT()) {
//                buffer.putLong(amountNQT);
//                buffer.putLong(feeNQT);
//                if (referencedTransactionFullHash != null) {
//                    buffer.put(Convert.parseHexString(referencedTransactionFullHash));
//                } else {
//                    buffer.put(new byte[32]);
//                }
//            } else {
//                buffer.putInt((int) (amountNQT / Constants.ONE_NXT));
//                buffer.putInt((int) (feeNQT / Constants.ONE_NXT));
//                if (referencedTransactionFullHash != null) {
//                    buffer.putLong(Convert.fullHashToId(Convert.parseHexString(referencedTransactionFullHash)));
//                } else {
//                    buffer.putLong(0L);
//                }
//            }
//            buffer.put(signature != null ? signature : new byte[64]);
//            if (version > 0) {
//                buffer.putInt(getFlags());
//                buffer.putInt(ecBlockHeight);
//                buffer.putLong(ecBlockId);
//            }
//            for (Appendix appendage : appendages) {
//                appendage.putBytes(buffer);
//            }
//            return buffer.array();
//        } catch (RuntimeException e) {
//            //TODO
//            //Logger.logDebugMessage("Failed to get transaction bytes for transaction: " + getJSONObject().toJSONString());
//            throw e;
//        }
        return new byte[0];
    }

    @Override
    public int getBlockHeight() {
        return ecBlockHeight;
    }

    @Override
    public int compareTo(@NonNull Transaction transaction) {
        return Long.compare(this.getId(), transaction.getId());
    }

//    public static byte[] sha3(byte[] input) {
//        MessageDigest digest;
//        try {
//            digest = MessageDigest.getInstance(HASH_256_ALGORITHM_NAME, CRYPTO_PROVIDER);
//            digest.update(input);
//            return digest.digest();
//        } catch (NoSuchAlgorithmException e) {
//            Log.e("-----------", "Can't find such algorithm :" + e);
//            throw new RuntimeException(e);
//        }
//
//    }

    public byte[] getSignature() {
        return signature;
    }
}

