package com.onets.core.wallet.families.andachain;

import com.google.common.collect.ImmutableList;
import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.messages.MessageFactory;
import com.onets.core.messages.TxMessage;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.AbstractTransaction;
import com.onets.core.wallet.AbstractWallet;
import com.onets.core.wallet.TransactionWatcherWallet_Anda;
import com.onets.core.wallet.families.bitcoin.OutPointOutput;
import com.onets.core.wallet.families.bitcoin.TrimmedTransaction;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBag;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.onets.core.Preconditions.checkNotNull;
import static com.onets.core.Preconditions.checkState;

/**
 * @author Yu K.Q.
 * @author Yu K.Q.
 * 安达交易实现类
 */
public final class AndaTransaction implements AbstractTransaction {
    private static final Logger log = LoggerFactory.getLogger(AndaTransaction.class);

    final CoinType type;
    final Sha256Hash hash;
    final Transaction tx;
    final boolean isTrimmed;
    final Value valueSent;
    final Value valueReceived;
    final Value value;
    @Nullable
    final Value fee;

    /**
     * 安达交易
     * @param transactionId
     * @param transaction
     * @param isTrimmed
     * @param valueSent
     * @param valueReceived
     * @param fee
     */
    public AndaTransaction(Sha256Hash transactionId, Transaction transaction, boolean isTrimmed,
                           Value valueSent, Value valueReceived, @Nullable Value fee) {
        tx = checkNotNull(transaction);
        type = (CoinType) tx.getParams();
        this.isTrimmed = isTrimmed;
        if (isTrimmed) {
            hash = checkNotNull(transactionId);
            this.valueSent = checkNotNull(valueSent);
            this.valueReceived = checkNotNull(valueReceived);
            this.value = valueReceived.subtract(valueSent);
            this.fee = fee;
        } else {
            hash = null;
            this.valueSent = null;
            this.valueReceived = null;
            this.value = null;
            this.fee = null;
        }
    }

    /**
     * 安达交易
     * @param transaction
     */
    public AndaTransaction(Transaction transaction) {
        this(checkNotNull(transaction).getHash(), transaction, false, null, null, null);
    }

    /**
     * 安达交易
     * @param type
     * @param rawTx
     */
    public AndaTransaction(CoinType type, byte[] rawTx) {
        this(new Transaction(type, rawTx));
    }

    public static AndaTransaction fromTrimmed(Sha256Hash transactionId, Transaction transaction,
                                              Value valueSent, Value valueReceived, Value fee) {
        return new AndaTransaction(transactionId, transaction, true, valueSent, valueReceived, fee);
    }

    /**
     * 获取币类型
     * @return
     */
    @Override
    public CoinType getType() {
        return type;
    }

    /**
     * 获得信心类型
     * @return
     */
    @Override
    public TransactionConfidence.ConfidenceType getConfidenceType() {
        return tx.getConfidence().getConfidenceType();
    }

    @Override
    public void setConfidenceType(TransactionConfidence.ConfidenceType type) {
        tx.getConfidence().setConfidenceType(type);
    }

    @Override
    public int getAppearedAtChainHeight() {
        return tx.getConfidence().getAppearedAtChainHeight();
    }

    @Override
    public void setAppearedAtChainHeight(int appearedAtChainHeight) {
        tx.getConfidence().setAppearedAtChainHeight(appearedAtChainHeight);
    }

    @Override
    public TransactionConfidence.Source getSource() {
        return tx.getConfidence().getSource();
    }

    @Override
    public void setSource(TransactionConfidence.Source source) {
        tx.getConfidence().setSource(source);
    }

    @Override
    public int getDepthInBlocks() {
        return tx.getConfidence().getDepthInBlocks();
    }

    @Override
    public void setDepthInBlocks(int depthInBlocks) {
        tx.getConfidence().setDepthInBlocks(depthInBlocks);
    }

    /**
     * 获取时间戳
     * @return
     */
    @Override
    public long getTimestamp() {
        return tx.getUpdateTime().getTime();
    }

    /**
     * 设置时间戳
     * @param timestamp
     */
    @Override
    public void setTimestamp(long timestamp) {
        tx.setUpdateTime(new Date(timestamp));
    }

    /**
     * 获取Value余额
     * @param abstractWallet
     * @return
     */

    @Override
    public Value getValue(AbstractWallet abstractWallet) {
        if (isTrimmed) {
            return value;
        } else {
            if (abstractWallet instanceof TransactionWatcherWallet_Anda) {
                TransactionWatcherWallet_Anda wallet = (TransactionWatcherWallet_Anda) abstractWallet;
                return getValueReceived(wallet).subtract(getValueSent(wallet));
            } else {
                return type.value(0);
            }
        }
    }

    /**
     * 获取接收到的金额
     * @param wallet
     * @return
     */
    public Value getValueReceived(TransactionWatcherWallet_Anda wallet) {
        if (isTrimmed) {
            return getValueReceived();
        } else {
            return type.value(tx.getValueSentToMe(wallet));
        }
    }

    /**
     * 获取发送的金额
     * @param wallet
     * @return
     */
    public Value getValueSent(TransactionWatcherWallet_Anda wallet) {
        if (isTrimmed) {
            return getValueSent();
        } else {
            tx.ensureParsed();
            // Find the value of the inputs that draw value from the wallet
            Value sent = type.value(0);
            Map<Sha256Hash, AndaTransaction> transactions = wallet.getTransactions();
            for (TransactionInput input : tx.getInputs()) {
                TransactionOutPoint outPoint = input.getOutpoint();
                // This input is taking value from a transaction in our wallet. To discover the value,
                // we must find the connected transaction.
                OutPointOutput connected = wallet.getUnspentTxOutput(outPoint);
                if (connected == null) {
                    AndaTransaction spendingTx = transactions.get(outPoint.getHash());
                    int index = (int) outPoint.getIndex();
                    if (spendingTx != null && spendingTx.isOutputAvailable(index)) {
                        connected = new OutPointOutput(spendingTx, index);
                    }
                }

                if (connected == null)
                    continue;

                // The connected output may be the change to the sender of a previous input sent to this wallet. In this
                // case we ignore it.
                if (!connected.getOutput().isMineOrWatched(wallet))
                    continue;

                sent = sent.add(connected.getValue());
            }

            return sent;
        }
    }

    /**
     * 获取已经接收的金额
     * @return
     */
    public Value getValueReceived() {
        return isTrimmed ? valueReceived : type.value(0);
    }

    /**
     * 获取发送的金额
     * @return
     */
    public Value getValueSent() {
        return isTrimmed ? valueSent : type.value(0);
    }

    /**
     * 获取费用
     * @return
     */
    @Override
    @Nullable
    public Value getFee() {
        return isTrimmed ? fee : type.value(tx.getFee());
    }

    /**
     * 获取新的交易费用
     * @param wallet
     * @return
     */
    @Nullable
    public Value getRawTxFee(TransactionWatcherWallet_Anda wallet) {
        checkState(!isTrimmed, "Cannot get raw tx fee from a trimmed transaction");
        Value fee = type.value(0);
        for (TransactionInput input : tx.getInputs()) {
            TransactionOutPoint outPoint = input.getOutpoint();
            AndaTransaction inTx = wallet.getTransaction(outPoint.getHash());
            if (inTx == null || !inTx.isOutputAvailable((int) outPoint.getIndex())) {
                return null;
            }
            TransactionOutput txo = inTx.getOutput((int) outPoint.getIndex());
            fee = fee.add(txo.getValue());
        }
        for (TransactionOutput output : getOutputs()) {
            fee = fee.subtract(output.getValue());
        }
        return fee;
    }

    /**
     * 获取消息
     * @return
     */
    @Override
    public TxMessage getMessage() {
        MessageFactory messageFactory = type.getMessagesFactory();
        if (messageFactory != null) {
            return messageFactory.extractPublicMessage(this);
        } else {
            return null;
        }
    }

    @Override
    @Nullable
    public List<AbstractAddress> getReceivedFrom() {
        return ImmutableList.of();
    }

    /**
     * 获取要发送到的
     * @return
     */
    @Override
    public List<AbstractOutput> getSentTo() {
        List<AbstractOutput> outputs = new ArrayList<>();
        for (TransactionOutput output : getOutputs(false)) {
            try {
                AbstractAddress address = AndaAddress.from(type, output.getScriptPubKey());
                outputs.add(new AbstractOutput(address, type.value(output.getValue())));
            } catch (Exception e) { /* ignore this output */ }
        }
        return outputs;
    }

    /**
     * 获取SHA256 hash值
     * @return
     */
    @Override
    public Sha256Hash getHash() {
        return isTrimmed ? hash : tx.getHash();
    }

    /**
     * 获取SHA256 hash----字符串输出
     * @return
     */
    @Override
    public String getHashAsString() {
        return getHash().toString();
    }

    /**
     * 获取SHA256 hash----bytes数组输出
     * @return
     */
    @Override
    public byte[] getHashBytes() {
        return getHash().getBytes();
    }

    /**
     *判断是否生成
     * @return
     */
    @Override
    public boolean isGenerated() {
        return tx.isCoinBase() || tx.isCoinStake();
    }

    @Override
    public boolean isTrimmed() {
        return isTrimmed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AndaTransaction other = (AndaTransaction) o;
        return getHash().equals(other.getHash());
    }

    /**
     * 获取RAW的交易
     * @return
     */
    public Transaction getRawTransaction() {
        return tx;
    }

    /**
     * 获取交易输入
     * @return
     */
    public List<TransactionInput> getInputs() {
        return tx.getInputs();
    }

    /**
     * 获取交易输出
     * @param index
     * @return
     */
    public TransactionOutput getOutput(int index) {
        return tx.getOutput(index);
    }

    /**
     * 获取交易输出集
     * @return
     */
    public List<TransactionOutput> getOutputs() {
        return getOutputs(true);
    }

    /**
     * 获取交易输出集
     * @param includeEmptyOutputs
     * @return
     */
    public List<TransactionOutput> getOutputs(boolean includeEmptyOutputs) {
        if (tx instanceof TrimmedTransaction) {
            return ((TrimmedTransaction) tx).getOutputs(includeEmptyOutputs);
        } else {
            return tx.getOutputs();
        }
    }

    /**
     * 判断输出是否可用
     * @param index
     * @return
     */
    private boolean isOutputAvailable(int index) {
        if (tx instanceof TrimmedTransaction) {
            return ((TrimmedTransaction) tx).isOutputAvailable(index);
        } else {
            return index < getNumberOfOutputs();
        }
    }

    /**
     * 获取数字输出
     * @return
     */
    private long getNumberOfOutputs() {
        if (tx instanceof TrimmedTransaction) {
            return ((TrimmedTransaction) tx).getNumberOfOutputs();
        } else {
            return tx.getOutputs().size();
        }
    }

    /**
     * bitcoin序列化
     * @return
     */
    public byte[] bitcoinSerialize() {
        checkState(!isTrimmed, "Cannot serialize a trimmed transaction");
        return tx.bitcoinSerialize();
    }

    /**
     * 获取交易
     * @param wallet
     * @return
     */
    private AndaTransaction getTrimTransaction(TransactionBag wallet) {
        throw new RuntimeException();
//        AndaTransaction transaction = rawTransactions.get(hash);
//
//        if (transaction == null || transaction.isTrimmed()) return false;
//
//        final Value valueSent = transaction.getValueSent(this);
//        final Value valueReceived = transaction.getValueReceived(this);
//        final Value fee = transaction.getFee();
//
//        WalletTransaction.Pool txPool;
//        if (confirmed.containsKey(hash)) {
//            txPool = WalletTransaction.Pool.CONFIRMED;
//        } else if (pending.containsKey(hash)) {
//            txPool = WalletTransaction.Pool.PENDING;
//        } else {
//            throw new RuntimeException("Transaction is not found in any pool");
//        }
//
//        Transaction txFull = transaction.getRawTransaction();
//        Transaction tx = new Transaction(type);
//        tx.getConfidence().setSource(txFull.getConfidence().getSource());
//        tx.getConfidence().setConfidenceType(txFull.getConfidence().getConfidenceType());
//        if (tx.getConfidence().getConfidenceType() == BUILDING) {
//            tx.getConfidence().setAppearedAtChainHeight(txFull.getConfidence().getAppearedAtChainHeight());
//            tx.getConfidence().setDepthInBlocks(txFull.getConfidence().getDepthInBlocks());
//        }
//        tx.setTime(txFull.getTime());
//        tx.setTokenId(txFull.getTokenId());
//        tx.setExtraBytes(txFull.getExtraBytes());
//        tx.setUpdateTime(txFull.getUpdateTime());
//        tx.setLockTime(txFull.getLockTime());
//
//        if (txFull.getAppearsInHashes() != null) {
//            for (Map.Entry<Sha256Hash, Integer> appears : txFull.getAppearsInHashes().entrySet()) {
//                tx.addBlockAppearance(appears.getKey(), appears.getValue());
//            }
//        }
//
//        tx.setPurpose(txFull.getPurpose());
//
//        // Remove unrelated outputs when receiving coins
//        boolean isReceiving = valueReceived.compareTo(valueSent) > 0;
//        if (isReceiving) {
//            for (TransactionOutput output : txFull.getOutputs()) {
//                if (output.isMineOrWatched(this)) {
//                    tx.addOutput(output);
//                }
//            }
//        } else {
//            // When sending keep all outputs
//            for (TransactionOutput output : txFull.getOutputs()) {
//                tx.addOutput(output);
//            }
//        }
//
//        return AndaTransaction.fromTrimmed(hash, tx, valueSent, valueReceived, fee);
    }

}
