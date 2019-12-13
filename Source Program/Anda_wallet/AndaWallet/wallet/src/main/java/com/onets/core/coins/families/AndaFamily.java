package com.onets.core.coins.families;

import android.util.Log;

import com.onets.core.coins.CoinType;
import com.onets.core.exceptions.AddressMalformedException;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.families.andachain.AndaAddress;

/**
 * @author Yu K.Q.
 *
 */
public abstract class AndaFamily extends CoinType {
    private static final String TAG = "AndaFamily";

    {
        family = Families.ANDACHAIN;
    }

    @Override
    public AbstractAddress newAddress(String addressStr) throws AddressMalformedException {
        Log.d(TAG, "newAddress: " + addressStr);
        return AndaAddress.from(this, addressStr);
    }

}
