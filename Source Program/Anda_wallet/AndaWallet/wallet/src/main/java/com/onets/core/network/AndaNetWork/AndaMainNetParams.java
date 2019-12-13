package com.onets.core.network.AndaNetWork;

/**
 * Parameters for the main production network on which people trade goods and services.
 * 交易商品和服务的主要网络的参数
 * 注：该文件根据 bitcoinj-0.12.3 版本 bitcoinj/params/MainNetParams.java文件编写
 * 注：bitcoinj版本——当前是用的版本是0.12.3，最新版本与此版本中类及方法有较大变动
 */
public class AndaMainNetParams extends NetworkParameters{

    public AndaMainNetParams(){
        super();
        //一个难度周期内产生的区块数
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;//TARGET_TIMESPAN = 1209600
        maxTarget = org.bitcoinj.core.Utils.decodeCompactBits(0x1d00ffffL);
        dumpedPrivateKeyHeader = 128;//废弃的私钥标头
        addressHeader = 0;//地址标头
        p2shHeader = 5;//p2sh标头
        acceptableAddressCodes = new int[] {addressHeader, p2shHeader};//可接受的地址编码
        port = 8444;//端口,自定义是否修改
        packetMagic = 0xf9beb4d9L;
        genesisBlock.setDifficultyTarget(0x1d00ffffL);//设置创世区块难度目标
        //TODO： 时间戳改变
        genesisBlock.setTime(1231006505L);//创世区块时间戳
        genesisBlock.setNonce(2083236893);//创世区块随机数
        id = NetworkParameters.ID_MAINNET;
        //subsidyDecreaseBlockCount = 210000;//区块数减半
        spendableCoinbaseDepth = 100;
    }
}
