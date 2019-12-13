package com.onets.core.wallet.families.ethereum;

import com.google.common.collect.ImmutableList;
import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.messages.TxMessage;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.AbstractTransaction;
import com.onets.core.wallet.AbstractWallet;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionConfidence;

import java.util.List;

import static com.onets.core.Preconditions.checkNotNull;

/**
 * @author Yu K.Q.
 * @author Yu K.Q.
 */
public final class EthereumTransaction implements AbstractTransaction {
    final CoinType type;
    Sha256Hash hash;
    final TransactionImp tx;

    TransactionConfidence.ConfidenceType confidence = TransactionConfidence.ConfidenceType.BUILDING;

    public EthereumTransaction(CoinType type, TransactionImp transaction) {
        this.type = type;
        tx = checkNotNull(transaction);
    }


    public void setConfidenceType(TransactionConfidence.ConfidenceType conf) {
        confidence = conf;
    }

    @Override
    public CoinType getType() {
        return type;
    }

    @Override
    public TransactionConfidence.ConfidenceType getConfidenceType() {
        return (tx.getConfirmations() > 0 ) ? confidence : TransactionConfidence.ConfidenceType.PENDING;
    }

    @Override
    public int getAppearedAtChainHeight() {
        return (int) tx.getHeight();
    }

    @Override
    public void setAppearedAtChainHeight(int appearedAtChainHeight) {
        tx.setHeight(appearedAtChainHeight);
    }

    @Override
    public TransactionConfidence.Source getSource() {
        return TransactionConfidence.Source.NETWORK;
    }

    @Override
    public void setSource(TransactionConfidence.Source source) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int getDepthInBlocks() {
        return tx.getConfirmations();
    }

    @Override
    public Value getValue(AbstractWallet wallet) {
        return Value.valueOfEth(wallet.getCoinType(), tx.getAmountETH());
    }

    @Override
    public TxMessage getMessage() {
//        if (tx.getMessage() != null) {
//            return new NxtTxMessage(tx);
//        }
        return null;
    }

    @Override
    public Value getFee() {
        //todo : 修改 gasLmit   gasPrice
        if(tx.getGasLimit() == null || tx.getGasPrice() == null){
            return type.getDefaultFeeValue();
        }else {
            return Value.valueOfEth(type, tx.getGasLimit().multiply(tx.getGasPrice()));
        }
    }

    @Override
    public List<AbstractOutput> getSentTo() {
        return ImmutableList.of(new AbstractOutput(new EthereumAddress(type, tx.getTo()),
                Value.valueOfEth(type, tx.getAmountETH()+"")));
    }

    @Override
    public List<AbstractAddress> getReceivedFrom() {
          return ImmutableList.of((AbstractAddress) new EthereumAddress(type, tx.getSenderAddress()));
    }

    @Override
    public Sha256Hash getHash() {
        //todo:  txhash
        if (hash == null) {
            hash = new Sha256Hash(tx.getFullHash().substring(2));
        }
        return hash;
    }

    @Override
    public byte[] getHashBytes() {
        return getHash().getBytes();
    }

    @Override
    public void setDepthInBlocks(int depthInBlocks) {

    }

    @Override
    public long getTimestamp() {
        return tx.getTimestamp(); // TODO use block timestamp instead
//        return tx.getBlockTimestamp();
    }

    @Override
    public void setTimestamp(long timestamp) {
        throw new RuntimeException("NxtTransaction::setTimestamp not implemented");
    }

    @Override
    public String getHashAsString() {
        return getHash().toString();
    }

    @Override
    public boolean isGenerated() {
        return false;
    }

    @Override
    public boolean isTrimmed() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EthereumTransaction other = (EthereumTransaction) o;
        return getHash().equals(other.getHash());
    }

    public TransactionImp getRawTransaction() {
        return tx;
    }


}
