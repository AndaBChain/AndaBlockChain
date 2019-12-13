package org.rockyang.blockchain.event;

import org.springframework.context.ApplicationEvent;

/**
 * 增加区块确认数事件
 * @author Wang HaiTian
 */
public class BlockConfirmNumEvent extends ApplicationEvent {

    /**
     * @param blockIndex 区块高度
     */
    public BlockConfirmNumEvent(Integer blockIndex)
    {
        super(blockIndex);
    }
}
