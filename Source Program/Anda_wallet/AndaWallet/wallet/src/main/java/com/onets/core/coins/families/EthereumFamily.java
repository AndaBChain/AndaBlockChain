package com.onets.core.coins.families;

import android.util.Log;

import com.onets.core.coins.CoinType;
import com.onets.core.exceptions.AddressMalformedException;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.families.ethereum.EthereumAddress;


/**
 * @author Yu K.Q.
 *
 */
public abstract class EthereumFamily extends CoinType {
    private static final String TAG = "EthereumFamily";

    {
        family = Families.ETHEREUM;
    }

    @Override
    public AbstractAddress newAddress(String addressStr) throws AddressMalformedException {
        Log.d(TAG, "newAddress: " + addressStr);
        return EthereumAddress.fromString(this, addressStr);
    }
}
