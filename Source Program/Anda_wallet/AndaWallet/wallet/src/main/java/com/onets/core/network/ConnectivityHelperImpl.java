package com.onets.core.network;

/**
 * 连接助手实现类
 */
public class ConnectivityHelperImpl implements ConnectivityHelper {
    /**
     * 检测连接状态
     * @return
     */
    @Override
    public boolean isConnected() {
        //ping检测是否连接外网
        return Utils.ping();
    }
}
