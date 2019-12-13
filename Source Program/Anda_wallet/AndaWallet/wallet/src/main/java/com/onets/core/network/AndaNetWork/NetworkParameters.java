package com.onets.core.network.AndaNetWork;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Sha256Hash;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * NetworkParameters包含用于处理比特币链实例化所需的数据
 * 这是一个抽象类，具体实例可以在params包中找到。有四个:
 *  一个用于主网络({@link AndaMainNetParams})，一个用于公共测试网络，另外两个用于单元测试和本地应用程序开发。
 * 虽然这个类包含一些别名，但是鼓励您直接调用每个特定params类的静态get()方法。
 */
public abstract class NetworkParameters implements Serializable {
    /**
     * 该库实现的协议版本
     * （于）注：该常量的定义是参照bitcoin中定义，不确定在安达中是否需要用到
     */
    public static final int PROTOCOL_VERSION = 70001;

    /**
     * 警告签名秘钥
     * （于）注：暂时将原始的写为ANDA_KEY，下面一行注释掉的是参照bitcoin的格式
     */
    public static final byte[] ANDA_KEY = null;
    //public static final byte[] SATOSHI_KEY = Utils.HEX.decode("04fc9702847840aaf195de8442ebecedf5b095cdbb9bc716bda9110971b28a49e0ead8564ff0db22209e0374782c093bb899692d524e9d6a6956e7c5ecbcd68284");

    /** 使用getId()返回的字符串，交易在主网中进行 */
    public static final String ID_MAINNET = "org.andachain.main";//bitcoin：org.bitcoin.production
    /** 使用getId()返回的字符串，测试网络 */
    public static final String ID_TESTNET = "org.andachain.test";
    /** 使用getId()返回的字符串，regtest模块 */
    public static final String ID_REGTEST = "org.andachain.regtest";
    /** Unit test network. 单元测试网络*/
    public static final String ID_UNITTESTNET = "org.andachain.unittest";

    /** The string used by the payment protocol to represent the main net.用于支付协议表示主链 */
    public static final String PAYMENT_PROTOCOL_ID_MAINNET = "main";
    /** The string used by the payment protocol to represent the test net. 用于支付协议表示测试链*/
    public static final String PAYMENT_PROTOCOL_ID_TESTNET = "test";

    //平均每个难度周期为两周
    public static final int TARGET_TIMESPAN = 14 * 24 * 60 * 60;
    //十分钟一个区块的产生速度
    public static final int TARGET_SPACING = 10 * 60;
    //一个难度周期产生的区块数
    public static final int INTERVAL = TARGET_TIMESPAN / TARGET_SPACING;
    protected Block genesisBlock;//创世区块
    protected BigInteger maxTarget;//最大目标
    protected int port;//端口号
    protected long packetMagic;  //指消息源网络，在状态流未知时用于查找下一个消息
    protected int addressHeader;//地址标头
    protected int p2shHeader;//p2sh标头
    protected int dumpedPrivateKeyHeader;//废弃的私钥标头
    protected int interval;//间距区间
    protected int targetTimespan;//目标时间间隔
    protected byte[] alertSigningKey;//警报签名秘钥

    /*See getId(). 对于旧的反序列化钱包来说，这可能是空的.在这种情况下，我们通过观察端口号来推导它.*/
    protected String id;

    /*使用coinbase交易所需的区块深度*/
    protected int spendableCoinbaseDepth;//可使用的Coinbase深度
    protected int subsidyDecreaseBlockCount;//补加上减少的区块数量

    protected int[] acceptableAddressCodes;//可接受的地址编码
    protected String[] dnsSeeds;//dns种子
    protected Map<Integer, Sha256Hash> checkpoints = new HashMap<Integer, Sha256Hash>();//检查点

    protected NetworkParameters(){
        alertSigningKey = ANDA_KEY;
        genesisBlock = createGenesis(this);
    }

    /**
     * 创建创世区块
     * @param networkParameters
     * @return
     */
    private static Block createGenesis(NetworkParameters networkParameters) {
//        Block genesisBlock = new Block(networkParameters);
//        Transaction transaction = new Transaction(networkParameters);
        return null;
    }

}
