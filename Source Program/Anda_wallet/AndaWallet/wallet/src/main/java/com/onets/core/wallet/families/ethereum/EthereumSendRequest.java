package com.onets.core.wallet.families.ethereum;

import android.util.Log;

import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.coins.coinMain.EthereumMain;
import com.onets.core.coins.families.EthereumFamily;
import com.onets.core.coins.nxt.Convert;
import com.onets.core.coins.nxt.NxtException;
import com.onets.core.util.TypeUtils;
import com.onets.core.wallet.SendRequest;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.onets.core.Preconditions.checkState;

/**
 * @author Yu K.Q.
 */
public class EthereumSendRequest extends SendRequest<EthereumTransaction> {

    public TransactionImp.BuilderImpl ethTxBuilder;

    protected EthereumSendRequest(CoinType type) {
        super(type);
    }

    private static String TAG = "--------------RippleSendRequest";

    public static EthereumSendRequest to(EthereumFamilyWallet from, EthereumAddress destination, Value amount) {
        checkNotNull(destination.getType(), "Address is for an unknown network");
        checkState(from.getCoinType().getName().equals(destination.getType().getName()), "Incompatible destination address coin type");
        checkState(TypeUtils.is(destination.getType(), amount.type), "Incompatible sending amount type");
        checkTypeCompatibility(destination.getType());

        EthereumSendRequest req = new EthereumSendRequest(destination.getType());

        int timestamp;
        if (req.type instanceof EthereumMain) {
            timestamp = Convert.toNxtEpochTime(System.currentTimeMillis());
        } else {
            throw new RuntimeException("Unexpected Ethereum family type: " + req.type.toString());
        }


        TransactionImp.BuilderImpl builder = new TransactionImp.BuilderImpl(
                new String(from.getPublicKey()), amount.value_big, timestamp, destination.getHexAddress());
        Log.e(TAG, "TransactionImpl:  from  : " + new String(from.getPublicKey()) + "  |   to : "+destination.getHexAddress() +"  |  amount   :" + amount.getBigValue()
                + "   |   fee value : " + req.fee.value
        );

//        builder.recipientId(destination.getAccountId());
        req.ethTxBuilder = builder;
        try {
            req.tx = new EthereumTransaction(req.type,req.ethTxBuilder.build());
        } catch (NxtException.NotValidException e) {
            e.printStackTrace();
        }
        return req;
    }

    public static EthereumSendRequest emptyWallet(EthereumFamilyWallet from, EthereumAddress destination) {
        checkNotNull(destination.getType(), "Address is for an unknown network");
        // checkState(destination.getType().getFeePolicy() == FeePolicy.FLAT_FEE, "Only flat fee is supported");

        Value allFundsMinusFee = from.getBalance().subtract(destination.getType().getFeeValue());

        return to(from, destination, allFundsMinusFee);
    }

    private static void checkTypeCompatibility(CoinType type) {
        // Only EthereumFamily coins are supported
        if (!(type instanceof EthereumFamily)) {
            throw new RuntimeException("Unsupported type: " + type);
        }
    }
}
