package com.onets.wallet;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.widget.Toast;

import com.google.common.collect.ImmutableList;
import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.exchange.shapeshift.ShapeShift;
import com.onets.core.util.HardwareSoftwareCompliance;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.Wallet;
import com.onets.core.wallet.WalletAccount;
import com.onets.core.wallet.WalletProtobufSerializer;
import com.onets.core.wallet.families.ripple.client.AndroidClient;
import com.onets.wallet.service.CoinService;
import com.onets.wallet.service.CoinServiceImpl;
import com.onets.wallet.util.Fonts;
import com.onets.wallet.util.LinuxSecureRandom;
import com.onets.wallet.util.NetworkUtils;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.store.UnreadableWalletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Wallet Application
 * @author Yu K.Q.
 */
@ReportsCrashes(
        // Also uncomment ACRA.init(this) in onCreate
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON
)
/**
 * 钱包应用
 */
public class WalletApplication extends Application {
    private static final Logger log = LoggerFactory.getLogger(WalletApplication.class);
    private static final String TAG = "WalletApplication";

    private static HashMap<String, Typeface> typefaces;
    private Configuration config;
    private ActivityManager activityManager;

    private Intent coinServiceIntent;
    private Intent coinServiceConnectIntent;
    private Intent coinServiceCancelCoinsReceivedIntent;

    private File walletFile;//钱包文件
    @Nullable
    private Wallet wallet;
    private PackageInfo packageInfo;//包的信息
    private String versionString;//包版本

    private long lastStop;
    private ConnectivityManager connManager;//连通性管理
    private ShapeShift shapeShift;//货币兑换平台
    private File txCachePath;//TX交易缓存路径

    private static Context mContext;

    /**
     * 比特币
     */
    public SPVBlockStore blockStore;
    public BlockChain blockChain;
    public PeerGroup peerGroup;
    public org.bitcoinj.core.Wallet bit_wallet;
    public NetworkParameters params = TestNet3Params.get();

    private static AndroidClient rippleClient;   // RippleClient   WebSocketApi

    public static AndroidClient getRippleClient() {
        return rippleClient;
    }

    public static Context getInstance() {
        return mContext;
    }

    /*基本上下文*/
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * 创建
     */
    @Override
    public void onCreate() {
        ACRA.init(this);
        mContext = this;

        //瑞波币
        //rippleClient = new AndroidClient();//ripple
        //rippleClient.connect("wss://s-east.ripple.com");//正式网址
        //rippleClient.connect("wss://s.altnet.rippletest.net:51233");//测试网址

        config = new Configuration(PreferenceManager.getDefaultSharedPreferences(this));

        //初始化随机数生成器
        new LinuxSecureRandom(); // init proper random number generator

        performComplianceTests();

        initLogging();

        //严格模式

        super.onCreate();

        packageInfo = packageInfoFromContext(this);
        versionString = packageInfo.versionName.replace(" ", "_") +
                "__" +
                packageInfo.packageName +
                "_android";

        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        coinServiceIntent = new Intent(this, CoinServiceImpl.class);
        coinServiceConnectIntent = new Intent(CoinService.ACTION_CONNECT_COIN,
                null, this, CoinServiceImpl.class);
        coinServiceCancelCoinsReceivedIntent = new Intent(CoinService.ACTION_CANCEL_COINS_RECEIVED,
                null, this, CoinServiceImpl.class);

        createTxCache();

        // Set MnemonicCode.INSTANCE if needed
        if (MnemonicCode.INSTANCE == null) {
            try {

                MnemonicCode.INSTANCE = new MnemonicCode();
            } catch (IOException e) {
                throw new RuntimeException("Could not set MnemonicCode.INSTANCE", e);
            }
        }

        config.updateLastVersionCode(packageInfo.versionCode);

        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        walletFile = getFileStreamPath(Constants.WALLET_FILENAME_PROTOBUF);


        Log.d(TAG, "onCreate: " + walletFile.getPath());
        loadWallet();

        afterLoadWallet();

        Fonts.initFonts(this.getAssets());
    }


    /*创建交易缓存*/
    private void createTxCache() {
        txCachePath = new File(this.getCacheDir(), Constants.TX_CACHE_NAME);
        if (!txCachePath.exists()) {
            if (!txCachePath.mkdirs()) {
                txCachePath = null;
                log.error("Error creating transaction cache folder");
                return;
            }
        }

        // Make cache dirs for all coins
        for (CoinType type : Constants.SUPPORTED_COINS) {
            File coinCachePath = new File(txCachePath, type.getId());
            if (!coinCachePath.exists()) {
                if (!coinCachePath.mkdirs()) {
                    txCachePath = null;
                    log.error("Error creating transaction cache folder");
                    return;
                }
            }
        }
    }

    /*连接状态*/
    public boolean isConnected() {
        NetworkInfo activeInfo = connManager.getActiveNetworkInfo();
        return activeInfo != null && activeInfo.isConnected();
    }

    /*获取ShapeShift*/
    public ShapeShift getShapeShift() {
        if (shapeShift == null) {
            shapeShift = new ShapeShift(NetworkUtils.getHttpClient(getApplicationContext()));
        }
        return shapeShift;
    }

    /*获取TX缓存路径*/
    public File getTxCachePath() {
        return txCachePath;
    }

    /**
     * Some devices have software bugs that causes the EC crypto to malfunction.
     * 一些设备有软件缺陷，导致EC密码出现故障
     */
    private void performComplianceTests() {
        if (!config.isDeviceCompatible()) {
            if (!HardwareSoftwareCompliance.isEllipticCurveCryptographyCompliant()) {
                config.setDeviceCompatible(false);
                ACRA.getErrorReporter().handleSilentException(
                        new Exception("Device failed EllipticCurveCryptographyCompliant test"));
            } else {
                config.setDeviceCompatible(true);
            }
        }
    }

    /*加载后的钱包*/
    private void afterLoadWallet() {
        setupFeeProvider();
//        wallet.autosaveToFile(walletFile, 1, TimeUnit.SECONDS, new WalletAutosaveEventListener());
//
        // clean up spam
//        wallet.cleanup();
//
//        ensureKey();
//
//        migrateBackup();
    }

    /*根据货币的类型，设置费用提供者*/
    private void setupFeeProvider() {
        CoinType.setFeeProvider(new CoinType.FeeProvider() {
            @Override
            public Value getFeeValue(CoinType type) {
                return config.getFeeValue(type);
            }
        });
    }

    private void initLogging() {
//        final File logDir = getDir("log", Constants.TEST ? Context.MODE_WORLD_READABLE : MODE_PRIVATE);
//        final File logFile = new File(logDir, "wallet.log");
//
//        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
//
//        final PatternLayoutEncoder filePattern = new PatternLayoutEncoder();
//        filePattern.setContext(context);
//        filePattern.setPattern("%d{HH:mm:ss.SSS} [%thread] %logger{0} - %msg%n");
//        filePattern.start();
//
//        final RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<ILoggingEvent>();
//        fileAppender.setContext(context);
//        fileAppender.setFile(logFile.getAbsolutePath());
//
//        final TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>();
//        rollingPolicy.setContext(context);
//        rollingPolicy.setParent(fileAppender);
//        rollingPolicy.setFileNamePattern(logDir.getAbsolutePath() + "/wallet.%d.log.gz");
//        rollingPolicy.setMaxHistory(7);
//        rollingPolicy.start();
//
//        fileAppender.setEncoder(filePattern);
//        fileAppender.setRollingPolicy(rollingPolicy);
//        fileAppender.start();
//
//        final PatternLayoutEncoder logcatTagPattern = new PatternLayoutEncoder();
//        logcatTagPattern.setContext(context);
//        logcatTagPattern.setPattern("%logger{0}");
//        logcatTagPattern.start();
//
//        final PatternLayoutEncoder logcatPattern = new PatternLayoutEncoder();
//        logcatPattern.setContext(context);
//        logcatPattern.setPattern("[%thread] %msg%n");
//        logcatPattern.start();
//
//        final LogcatAppender logcatAppender = new LogcatAppender();
//        logcatAppender.setContext(context);
//        logcatAppender.setTagEncoder(logcatTagPattern);
//        logcatAppender.setEncoder(logcatPattern);
//        logcatAppender.start();
//
//        final ch.qos.logback.classic.Logger log = context.getLogger(Logger.ROOT_LOGGER_NAME);
//        log.addAppender(fileAppender);
//        log.addAppender(logcatAppender);
//        log.setLevel(Level.INFO);
    }

    public Configuration getConfiguration() {
        return config;
    }

    /**
     * Get the current wallet.
     * 获取最近钱包
     */
    @Nullable
    public Wallet getWallet() {
        return wallet;
    }

    /**
     * 获取账户
     *
     * @param accountId
     * @return
     */
    @Nullable
    public WalletAccount getAccount(@Nullable String accountId) {
        if (wallet != null) {
            return wallet.getAccount(accountId);
        } else {
            return null;
        }
    }

    /**
     * 获取账户
     *
     * @param type
     * @return
     */
    public List<WalletAccount> getAccounts(CoinType type) {
        if (wallet != null) {
            return wallet.getAccounts(type);
        } else {
            return ImmutableList.of();
        }
    }

    /**
     * 获取账户列表
     *
     * @param types
     * @return
     */
    public List<WalletAccount> getAccounts(List<CoinType> types) {
        if (wallet != null) {
            return wallet.getAccounts(types);
        } else {
            return ImmutableList.of();
        }
    }

    /**
     * 根据地址获取账户
     *
     * @param address
     * @return
     */
    public List<WalletAccount> getAccounts(AbstractAddress address) {
        if (wallet != null) {
            return wallet.getAccounts(address);
        } else {
            return ImmutableList.of();
        }
    }

    /**
     * 获取所有的账户
     *
     * @return
     */
    public List<WalletAccount> getAllAccounts() {
        if (wallet != null) {
            return wallet.getAllAccounts();
        } else {
            return ImmutableList.of();
        }
    }

    /**
     * Check if account exists
     * 检查账户是否存在
     */
    public boolean isAccountExists(String accountId) {
        if (wallet != null) {
            return wallet.isAccountExists(accountId);
        } else {
            return false;
        }
    }

    /**
     * Check if accounts exists for the spesific coin type
     * 检查是否存在特殊货币的账户
     */
    public boolean isAccountExists(CoinType type) {
        return wallet != null && wallet.isAccountExists(type);
    }

    /*设置空钱包*/
    public void setEmptyWallet() {
        setWallet(null);
    }

    /*设置钱包*/
    public void setWallet(@Nullable Wallet wallet) {
        Log.d("WalletApp", "setWallet:--------201808311115 ");
        // Disable auto-save of the previous wallet if exists, so it doesn't override the new one
        if (this.wallet != null) {
            this.wallet.shutdownAutosaveAndWait();
        }

        this.wallet = wallet;
        if (this.wallet != null) {
            this.wallet.autosaveToFile(walletFile, Constants.WALLET_WRITE_DELAY,
                    Constants.WALLET_WRITE_DELAY_UNIT, null);
        }
    }

    /*加载钱包*/
    private void loadWallet() {
        if (walletFile.exists()) {
            final long start = System.currentTimeMillis();

            FileInputStream walletStream = null;

            try {
                walletStream = new FileInputStream(walletFile);

                setWallet(WalletProtobufSerializer.readWallet(walletStream));

                log.info("wallet loaded from: '" + walletFile + "', took " + (System.currentTimeMillis() - start) + "ms");
            } catch (final FileNotFoundException e) {
                ACRA.getErrorReporter().handleException(e);
                Toast.makeText(WalletApplication.this, R.string.error_could_not_read_wallet, Toast.LENGTH_LONG).show();
            } catch (final UnreadableWalletException e) {
                Toast.makeText(WalletApplication.this, R.string.error_could_not_read_wallet, Toast.LENGTH_LONG).show();
                ACRA.getErrorReporter().handleException(e);
                Log.e("----- WalletApplication", "loadWallet ERROR :" + e);
            } finally {
                if (walletStream != null) {
                    try {
                        walletStream.close();
                    } catch (final IOException x) { /* ignore */ }
                }
            }
        }
    }

    /**
     * 保存钱包--现在
     */
    public void saveWalletNow() {
        if (wallet != null) {
            wallet.saveNow();
        }
    }

    /**
     * 保存钱包
     */
    public void saveWalletLater() {
        if (wallet != null) {
            wallet.saveLater();
        }
    }

    /*启动区块链服务*/
    public void startBlockchainService(CoinService.ServiceMode mode) {
        switch (mode) {
            case CANCEL_COINS_RECEIVED:
                startService(coinServiceCancelCoinsReceivedIntent);
                break;
            case NORMAL:
            default:
                startService(coinServiceIntent);
                break;
        }
    }

    /*停止区块链服务*/
    public void stopBlockchainService() {
        stopService(coinServiceIntent);
    }

    /*通过上下文获取包信息*/
    public static PackageInfo packageInfoFromContext(final Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (final PackageManager.NameNotFoundException x) {
            throw new RuntimeException(x);
        }
    }

    public PackageInfo packageInfo() {
        return packageInfo;
    }

    public String getVersionString() {
        return versionString;
    }

    public void touchLastResume() {
        lastStop = -1;
    }

    public void touchLastStop() {
        lastStop = SystemClock.elapsedRealtime();
    }

    public long getLastStop() {
        return lastStop;
    }

    public void maybeConnectAccount(WalletAccount account) {
        if (!account.isConnected()) {
            coinServiceConnectIntent.putExtra(Constants.ARG_ACCOUNT_ID, account.getId());
            startService(coinServiceConnectIntent);
        }
    }

}

