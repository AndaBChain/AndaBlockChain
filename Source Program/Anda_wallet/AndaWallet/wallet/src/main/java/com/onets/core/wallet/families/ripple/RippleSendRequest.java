package com.onets.core.wallet.families.ripple;

import android.util.Log;

import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.coins.families.RippleFamily;
import com.onets.core.util.TypeUtils;
import com.onets.core.wallet.SendRequest;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.onets.core.Preconditions.checkState;

/**
 * @author Yu K.Q.
 */
public class RippleSendRequest extends SendRequest<RippleTransaction> {

    public RippleTransactionImpl.BuilderImpl rippleTxBuilder;

    protected RippleSendRequest(CoinType type) {
        super(type);
    }

    private static String TAG = "--------------RippleSendRequest";

    public static RippleSendRequest to(RippleFamilyWallet from, RippleAddress destination, Value amount) {
        checkNotNull(destination.getType(), "Address is for an unknown network");
        checkState(from.getCoinType().getName().equals(destination.getType().getName()), "Incompatible destination address coin type");
        checkState(TypeUtils.is(destination.getType(), amount.type), "Incompatible sending amount type");
        checkTypeCompatibility(destination.getType());

        RippleSendRequest req = new RippleSendRequest(destination.getType());


        RippleTransactionImpl.BuilderImpl builder = new RippleTransactionImpl.BuilderImpl(
                new String(from.getPublicKey()), amount.value + "", destination.getHexAddress());
        Log.e(TAG, "RippleTransactionImpl:  from  : " + new String(from.getPublicKey()) + "  |   to : " + destination.getHexAddress() + "  |  amount   :" + amount.getBigValue()
        );

        req.rippleTxBuilder = builder;

        return req;
    }

    public static RippleSendRequest emptyWallet(RippleFamilyWallet from, RippleAddress destination) {
        checkNotNull(destination.getType(), "Address is for an unknown network");
        // checkState(destination.getType().getFeePolicy() == FeePolicy.FLAT_FEE, "Only flat fee is supported");

        Value allFundsMinusFee = from.getBalance().subtract(destination.getType().getFeeValue());

        return to(from, destination, allFundsMinusFee);
    }

    private static void checkTypeCompatibility(CoinType type) {
        if (!(type instanceof RippleFamily)) {
            throw new RuntimeException("Unsupported type: " + type);
        }
    }
}
