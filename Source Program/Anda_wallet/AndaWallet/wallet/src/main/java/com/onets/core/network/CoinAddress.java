package com.onets.core.network;

import com.google.common.collect.ImmutableList;
import com.onets.core.coins.CoinType;
import com.onets.stratumj.ServerAddress;

import java.util.List;

/**
 * @author Yu K.Q.
 * 币地址
 */
final public class CoinAddress {
    //币类型
    final private CoinType type;
    //地址列表
    final private List<ServerAddress> addresses;

    public CoinAddress(CoinType type, ServerAddress address) {
        this.type = type;
        this.addresses = ImmutableList.of(address);
    }

    public CoinAddress(CoinType type, ServerAddress... addresses) {
        this.type = type;
        this.addresses = ImmutableList.copyOf(addresses);
    }

    public CoinAddress(CoinType type, List<ServerAddress> addresses) {
        this.type = type;
        this.addresses = ImmutableList.copyOf(addresses);
    }

    /**
     * 获取币类型
     * @return
     */
    public CoinType getType() {
        return type;
    }

    /**
     * 获取地址列表
     * @return
     */
    public List<ServerAddress> getAddresses() {
        return addresses;
    }
}
