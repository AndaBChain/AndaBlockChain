package com.onets.core.wallet.families.ethereum;

import android.util.Log;

import com.onets.core.coins.CoinType;
import com.onets.core.exceptions.AddressMalformedException;
import com.onets.core.wallet.AbstractAddress;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Yu K.Q.
 */
final public class EthereumAddress implements AbstractAddress {
    private final CoinType type;
//    private final long accountId;
    private final String address;

    public EthereumAddress(CoinType type, String address) {
        this.type = type;
//        this.accountId = bytesToLong(address.getBytes(),0,false);
//        this.accountId =  Long.parseLong(address.replace("0x",""));
        this.address = address;

        Log.e(TAG, "EthereumAddress: \ntype:"+type.getName()
                +"\npublic:"+address.getBytes()
//                +"\naccountId:"+accountId
                +"\naddress:"+address
        );
    }
    private static final String TAG = "---------EthereumFamily";

    /**
     * 利用 {@link ByteBuffer}实现byte[]转long
     * @param input
     * @param offset
     * @param littleEndian 输入数组是否小端模式
     * @return
     */
    public long bytesToLong(byte[] input, int offset, boolean littleEndian) {
        // 将byte[] 封装为 ByteBuffer
        ByteBuffer buffer = ByteBuffer.wrap(input,offset,8);
        if(littleEndian){
            // ByteBuffer.order(ByteOrder) 方法指定字节序,即大小端模式(BIG_ENDIAN/LITTLE_ENDIAN)
            // ByteBuffer 默认为大端(BIG_ENDIAN)模式
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        return buffer.getLong();
    }


    public static EthereumAddress fromString(CoinType type,  String address)
            throws AddressMalformedException {
        try {
            return new EthereumAddress(type, address);
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

        EthereumAddress that = (EthereumAddress) o;

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
