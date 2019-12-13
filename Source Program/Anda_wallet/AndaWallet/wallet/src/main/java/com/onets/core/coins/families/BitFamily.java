package com.onets.core.coins.families;

import android.util.Log;

import com.onets.core.coins.CoinType;
import com.onets.core.exceptions.AddressMalformedException;
import com.onets.core.wallet.families.bitcoin.BitAddress;

/**
 * @author Yu K.Q.
 *
 * This is the classical Bitcoin family that includes Litecoin, Dogecoin, Dash, etc
 */
public abstract class BitFamily extends CoinType {
    private static final String TAG = "BitFamily";
    {
        family = Families.BITCOIN;
    }

    @Override
    public BitAddress newAddress(String addressStr) throws AddressMalformedException {
        Log.d(TAG, "newAddress: " + addressStr);
        return BitAddress.from(this, addressStr);
    }
}
