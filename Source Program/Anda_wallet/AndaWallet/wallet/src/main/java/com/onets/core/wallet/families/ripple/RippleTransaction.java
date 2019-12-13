package com.onets.core.wallet.families.ripple;

import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.messages.TxMessage;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.AbstractTransaction;
import com.onets.core.wallet.AbstractWallet;
import com.onets.wallet.util.WalletUtils;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionConfidence;

import java.util.List;

import static com.onets.core.Preconditions.checkNotNull;

/**
 * @author Yu K.Q.
 * @author Yu K.Q.
 */
public final class RippleTransaction implements AbstractTransaction {
    final CoinType type;
    Sha256Hash hash;
    RippleTransactionImpl tx;

    TransactionConfidence.ConfidenceType confidence = TransactionConfidence.ConfidenceType.BUILDING;

    private static final String TAG = "RippleTransaction";

    public RippleTransaction(CoinType type, RippleTransactionImpl transaction) {
        this.type = type;
        tx = checkNotNull(transaction);

        if (tx.getResult() == null) {
            Log.i(TAG, "RippleTransaction:  getResult  null");
        } else {
            if (tx.getResult().getTx_json() == null) {
                Log.i(TAG, "RippleTransaction:  Tx_json  null");
            } else {
                Log.i(TAG, "RippleTransactionImpl: "
                        + "\ngetAccount:" + tx.getResult().getTx_json().getAccount()
                        + "\ngetAmount:" + tx.getResult().getTx_json().getAmount()
                        + "\ngetDestination:" + tx.getResult().getTx_json().getDestination()
                        + "\ngetHash:" + tx.getResult().getTx_json().getHash()
                );
            }
        }
    }


    @Override
    public void setConfidenceType(TransactionConfidence.ConfidenceType conf) {
        confidence = conf;
    }

    @Override
    public CoinType getType() {
        return type;
    }

    @Override
    public TransactionConfidence.ConfidenceType getConfidenceType() {
        if (tx.getResult().getEngine_result() == null) {
            if (confidence == TransactionConfidence.ConfidenceType.BUILDING) {
                return TransactionConfidence.ConfidenceType.BUILDING;
            } else {
                return TransactionConfidence.ConfidenceType.DEAD;
            }
        } else {

            Log.i(TAG, "getConfidenceType:    -------------------------------------------------------------   ");

            return (tx.getResult().getEngine_result().equals("tesSUCCESS")) ? confidence : TransactionConfidence.ConfidenceType.DEAD;
        }
    }

    @Override
    public int getAppearedAtChainHeight() {
        return tx.getResult().getTx_json().getLastLedgerSequence();
    }

    @Override
    public void setAppearedAtChainHeight(int appearedAtChainHeight) {
        tx.getResult().getTx_json().setLastLedgerSequence(appearedAtChainHeight);
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
        return 5;
    }

    @Override
    public Value getValue(AbstractWallet wallet) {
        return Value.valueOf(wallet.getCoinType(), tx.getResult().getTx_json().getAmount());
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
        if (tx.getResult().getTx_json().getFee() == null) {
            return Value.valueOf(type, "11");
        } else {
            return Value.valueOf(type, tx.getResult().getTx_json().getFee());
        }
    }

    @Override
    public List<AbstractOutput> getSentTo() {
        return ImmutableList.of(new AbstractOutput(new RippleAddress(type, tx.getResult().getTx_json().getDestination()),
                Value.valueOf(type, tx.getResult().getTx_json().getAmount())));
    }

    @Override
    public List<AbstractAddress> getReceivedFrom() {
        return ImmutableList.of((AbstractAddress) new RippleAddress(type, tx.getResult().getTx_json().getAccount()));
    }

    @Override
    public Sha256Hash getHash() {
        //todo:  txhash
        if (hash == null) {
            if (tx.getResult().getTx_json().getHash() == null) {
                //创建交易时先添加一个默认的交易Hash（未能提前计算出hash值）

//                byte[] txhash = Base64.encode("2075B4B943F429FCA50CCA88EAC07E2923C21450A02670A99DBF7B4D20C9F5AE".getBytes(),0,64,Base64.DEFAULT);
                hash = new Sha256Hash("2075b4b943f429fca50cca88eac07e2923c21450a02670a99dbf7b4d20c9f5ae");
            } else {
                String txhash = WalletUtils.exChange(tx.getResult().getTx_json().getHash());
                Log.i(TAG, "-------------------------------------  getHash:" +
                        "\nsource:  " + tx.getResult().getTx_json().getHash() + "  | length:" + tx.getResult().getTx_json().getHash().length()
                        + "\nresult:  " + txhash + "  | length:" + txhash.length()
                );
                hash = new Sha256Hash(txhash);
            }
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
        return 0; // TODO use block timestamp instead
//        return tx.getBlockTimestamp();
    }

    @Override
    public void setTimestamp(long timestamp) {
        throw new RuntimeException("RippleTransaction::setTimestamp not implemented");
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
        RippleTransaction other = (RippleTransaction) o;
        return getHash().equals(other.getHash());
    }

    public RippleTransactionImpl getRawTransaction() {
        return tx;
    }

    public void setRawTransaction(RippleTransactionImpl tx) {
        this.tx = tx;
    }

}
