package com.onets.core.wallet.families.andachain;

import com.onets.core.coins.CoinType;
import com.onets.core.exceptions.AddressMalformedException;
import com.onets.core.wallet.AbstractAddress;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.WrongNetworkException;
import org.bitcoinj.script.Script;

import java.nio.ByteBuffer;

/**
 * @author Yu K.Q.
 * 安达地址
 */
public class AndaAddress extends Address implements AbstractAddress {

    AndaAddress(Address address) throws WrongNetworkException {
        this((CoinType) address.getParameters(), address.getVersion(), address.getHash160());
    }

    AndaAddress(CoinType type, byte[] hash160) {
        super(type,hash160);
    }

    AndaAddress(CoinType type, int version, byte[] hash160) throws WrongNetworkException {
        super(type, version, hash160);
    }

    public AndaAddress(CoinType type, String address) throws AddressFormatException {
        super(type, address);
    }

    public static AndaAddress from(CoinType type, String address) throws AddressMalformedException {
        try {
            return new AndaAddress(type, address);
        } catch (AddressFormatException e) {
            throw new AddressMalformedException(e);
        }
    }

    public static AndaAddress from(CoinType type, int version, byte[] hash160)
            throws AddressMalformedException {
        try {
            return new AndaAddress(type, version, hash160);
        } catch (WrongNetworkException e) {
            throw new AddressMalformedException(e);
        }
    }

    public static AndaAddress from(CoinType type, byte[] publicKeyHash160)
            throws AddressMalformedException {
        try {
            return new AndaAddress(type, type.getAddressHeader(), publicKeyHash160);
        } catch (WrongNetworkException e) {
            throw new AddressMalformedException(e);
        }
    }

    public static AndaAddress from(CoinType type, Script script) throws AddressMalformedException {
        try {
            return new AndaAddress(script.getToAddress(type));
        } catch (WrongNetworkException e) {
            throw new AddressMalformedException(e);
        }
    }

    public static AndaAddress from(CoinType type, ECKey key) {
        return new AndaAddress(type, key.getPubKeyHash());
    }

    public static AndaAddress from(AbstractAddress address) throws AddressMalformedException {
        try {
            if (address instanceof AndaAddress) {
                return (AndaAddress) address;
            } else if (address instanceof Address) {
                return new AndaAddress((Address) address);
            } else {
                return new AndaAddress(address.getType(), address.toString());
            }
        } catch (AddressFormatException e) {
            throw new AddressMalformedException(e);
        }
    }

    public static AndaAddress from(Address address) throws AddressMalformedException {
        try {
            return new AndaAddress(address);
        } catch (WrongNetworkException e) {
            throw new AddressMalformedException(e);
        }
    }

    @Override
    public CoinType getType() {
        return (CoinType) getParameters();
    }

    @Override
    public long getId() {
        return ByteBuffer.wrap(getHash160()).getLong();
    }
}
