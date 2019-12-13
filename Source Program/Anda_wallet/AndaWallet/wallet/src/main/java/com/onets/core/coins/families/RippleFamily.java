package com.onets.core.coins.families;

import android.util.Log;

import com.onets.core.coins.CoinType;
import com.onets.core.exceptions.AddressMalformedException;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.families.ripple.RippleAddress;


/**
 * @author Yu K.Q.
 *
 */
public abstract class RippleFamily extends CoinType {

    {
        family = Families.RIPPLE;
    }
    private static final String TAG = "---------RippleFamily";
    @Override
    public AbstractAddress newAddress(String addressStr) throws AddressMalformedException {
        Log.e(TAG, "newAddress: "+addressStr);
        return RippleAddress.fromString(this, addressStr);
    }
}
