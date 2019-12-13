package com.onets.core.wallet.families.ripple;

import android.util.Log;

import com.onets.core.coins.CoinType;
import com.onets.core.exceptions.AddressMalformedException;
import com.onets.core.wallet.AbstractAddress;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;

import java.nio.ByteBuffer;

/**
 * @author Yu K.Q.
 * 瑞波币地址
 */
final public class RippleAddress implements AbstractAddress {
    private final CoinType type;
//    private final long accountId;
    private final String address;

    public RippleAddress(CoinType type, String address) {
        this.type = type;
//        this.accountId = bytesToLong(address.getBytes(),0,false);
//        this.accountId =  Long.parseLong(address.replace("0x",""));
        this.address = address;

        Log.e(TAG, "RippleAddress: \ntype:"+type.getName()
                +"\npublic:"+address.getBytes()
//                +"\naccountId:"+accountId
                +"\naddress:"+address
        );
    }
    private static final String TAG = "---------RippleAddress";


    public static RippleAddress fromString(CoinType type, String address)
            throws AddressMalformedException {
        try {
            return new RippleAddress(type, address);
        } catch (Exception e) {
            throw new AddressMalformedException(e);
        }
    }


//    public long getAccountId() {
//        return accountId;
//    }

    public String getHexAddress() {
        return address;
    }

    @Override
    public CoinType getType() {
        return type;
    }

    @Override
    public String toString() {
        return getHexAddress();
    }

    @Override
    public long getId() {
        byte[] versionAndDataBytes = new byte[0];
        try {
            versionAndDataBytes = Base58.decodeChecked(address);
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }
        return ByteBuffer.wrap(new byte[versionAndDataBytes.length - 1]).getLong();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RippleAddress that = (RippleAddress) o;

//        if (accountId != that.accountId) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result;
        return result;
    }
}
