package com.onets.core.network;

import android.util.Log;

import com.onets.core.coins.CoinType;
import com.onets.core.coins.families.AndaFamily;
import com.onets.core.coins.families.BitFamily;
import com.onets.core.coins.families.EthereumFamily;
import com.onets.core.coins.families.RippleFamily;
import com.onets.core.exceptions.UnsupportedCoinTypeException;
import com.onets.core.network.interfaces.BlockchainConnection;
import com.onets.core.wallet.WalletAccount;
import com.onets.core.wallet.families.andachain.AndaServerClient;
import com.onets.core.wallet.families.ethereum.EthereumServerClient;
import com.onets.core.wallet.families.ripple.RippleServerClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

/**
 * @author Yu K.Q.
 * 服务客户端
 */
public class ServerClients {
    private static final Logger log = LoggerFactory.getLogger(ServerClient.class);
    private final ConnectivityHelper connectivityHelper;
    private HashMap<CoinType, BlockchainConnection> connections = new HashMap<>();
    private HashMap<CoinType, CoinAddress> addresses = new HashMap<>();


    /**
     * 连接助手
     */
    private static ConnectivityHelper DEFAULT_CONNECTIVITY_HELPER = new ConnectivityHelper() {
        @Override
        public boolean isConnected() { return true; }
    };
    private File cacheDir;
    private int cacheSize;

    /**
     * 服务客户端列表
     * @param coins
     */
    public ServerClients(List<CoinAddress> coins) {
        // Supply a dumb ConnectivityHelper that reports that connection is always available
        this(coins, DEFAULT_CONNECTIVITY_HELPER);
    }

    public ServerClients(List<CoinAddress> coinAddresses, ConnectivityHelper connectivityHelper) {
        this.connectivityHelper = connectivityHelper;
        setupAddresses(coinAddresses);
    }

    /**
     * 设置地址
     * @param coins
     */
    private void setupAddresses(List<CoinAddress> coins) {
        for (CoinAddress coinAddress : coins) {
            addresses.put(coinAddress.getType(), coinAddress);
        }
    }

    /**
     * 恢复账户
     * @param account
     */
    public void resetAccount(WalletAccount account) {
        BlockchainConnection connection = connections.get(account.getCoinType());
        if (connection == null) return;
        connection.addEventListener(account);
        connection.resetConnection();
    }

    /**
     * 开始同步
     * @param account
     */
    public void startAsync(WalletAccount account) {
        if (account == null) {
            log.warn("Provided wallet account is null, not doing anything");
            return;
        }
        CoinType type = account.getCoinType();
        BlockchainConnection connection = getConnection(type);
        connection.addEventListener(account);
        connection.startAsync();
    }

    /**
     * 根据币类型获取连接
     */
    String TAG = "----------ServerClients";
    private BlockchainConnection getConnection(CoinType type) {
        if (connections.containsKey(type)) return connections.get(type);
        // Try to create a connection
        if (addresses.containsKey(type)) {
            if (type instanceof BitFamily) {
                Log.i(TAG, "getConnection: BitFamily");

                ServerClient client = new ServerClient(addresses.get(type), connectivityHelper);
                client.setCacheDir(cacheDir, cacheSize);
                connections.put(type, client);
                return client;
            } else if (type instanceof EthereumFamily) {
                Log.i(TAG, "getConnection: EthereumFamily");

                EthereumServerClient client = new EthereumServerClient(addresses.get(type), connectivityHelper);
                connections.put(type, client);
                return client;
            } else if (type instanceof AndaFamily) {
                Log.i(TAG, "getConnection: AndaBlockChainFamily");
                AndaServerClient client = new AndaServerClient(addresses.get(type), connectivityHelper);
                connections.put(type, client);
                return client;
            } else if (type instanceof RippleFamily) {
                Log.i(TAG, "getConnection: RippleFamily");
                RippleServerClient client = new RippleServerClient(addresses.get(type), connectivityHelper);
                connections.put(type, client);
                return client;
            }else {
                throw new UnsupportedCoinTypeException(type);
            }
        } else {
            // Should not happen
            throw new RuntimeException("Tried to create connection for an unknown server.");
        }
    }

    /**
     * 停止所有
     */
    public void stopAllAsync() {
        for (BlockchainConnection client : connections.values()) {
            client.stopAsync();
        }
        connections.clear();
    }

    public void ping() {
        ping(null);
    }

    /**
     * ping检测
     * @param versionString
     */
    public void ping(@Nullable String versionString) {
        for (final CoinType type : connections.keySet()) {
            BlockchainConnection connection = connections.get(type);
            if (connection.isActivelyConnected()) connection.ping(versionString);
        }
    }

    /**
     * 重置连接
     */
    public void resetConnections() {
        for (final CoinType type : connections.keySet()) {
            BlockchainConnection connection = connections.get(type);
            if (connection.isActivelyConnected()) connection.resetConnection();
        }
    }

    /**
     * 设置缓存目录
     * @param cacheDir
     * @param cacheSize
     */
    public void setCacheDir(File cacheDir, int cacheSize) {
        this.cacheDir = cacheDir;
        this.cacheSize = cacheSize;
    }

    /**
     * 开始或重置账户同步
     * @param account
     */
    public void startOrResetAccountAsync(WalletAccount account) {
        //排除为AndaChain创建serverclients
        if(!account.getCoinType().getName().equals("AnDaChain")) {
            Log.e("--------------", "startOrResetAccountAsync: "+account.getCoinType().getName());
            if (connections.containsKey(account.getCoinType())) {
                resetAccount(account);
            } else {
                startAsync(account);
            }
        }
    }
}
