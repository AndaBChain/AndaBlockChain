package com.onets.core.network;

import com.onets.core.coins.CoinType;

/**
 * @author Yu K.Q.
 * 区块头
 */
public class BlockHeader {
    //币类型
    final CoinType type;
    //时间戳
    final long timestamp;
    //区块深度
    final int blockHeight;

    /**
     * timestamp in seconds (unix epoch)
     */
    public BlockHeader(CoinType type, long timestamp, int blockHeight) {
        this.type = type;
        this.timestamp = timestamp;
        this.blockHeight = blockHeight;
    }

    /**
     * 获取币类型
     * @return
     */
    public CoinType getType() {
        return type;
    }

    /**
     * 获取时间戳
     * @return
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 获取区块深度
     * @return
     */
    public int getBlockHeight() {
        return blockHeight;
    }

    /**
     * 判断区块深度是否与区块头深度一致
     * @param blockHeader
     * @return
     */
    public boolean equals(BlockHeader blockHeader) {
        return (this.getBlockHeight() == blockHeader.getBlockHeight() &&
                 this.getTimestamp() == this.getTimestamp());
    }
}
