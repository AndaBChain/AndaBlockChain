package com.onets.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.onets.core.coins.CoinID;
import com.onets.core.coins.CoinType;
import com.onets.core.coins.coinMain.AndaChainMain;
import com.onets.core.coins.coinMain.BitcoinMain;
import com.onets.core.coins.coinMain.BitcoinTest;
import com.onets.core.coins.coinMain.EthereumMain;
import com.onets.core.coins.coinMain.RippleMain;
import com.onets.core.network.CoinAddress;
import com.onets.stratumj.ServerAddress;
import com.onets.wallet.service.ServerHttp;
import com.paypal.android.sdk.payments.PayPalConfiguration;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Constants
 * @author Yu K.Q.
 * @author Yu K.Q.
 */
public class Constants {
    public static final String LOG_LABLE = "打印";
    public static final boolean TEST = false;
    /*钱包的网络在测试网或者在主网上 */
    public static final NetworkParameters NETWORK_PARAMETERS = TEST ? TestNet3Params.get() : MainNetParams.get();

    //TODO: 替换为从微信开发平台网站申请到的合法app id
    public static final String WEIXIN_APP_ID = "wxd930ea5d5a258f4f";

    //PayPal
    public static final String CONFIG_ENVIRONMENT_TEST = PayPalConfiguration.ENVIRONMENT_NO_NETWORK;
    public static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;
    public static final String PAYPAL_CLIENT_ID_TEST = "AUeSD5DcerEYupPzicGuTrwh7H8ENjcA3-0Ha3ts4ZyjRCxiuqdwm-UGZGNMqjQJD-bPt7Zyw7VVDdja";
    public static final String PAYPAL_CLIENT_ID = "AQ5yRaEsLWJuabKBn9c16VaVu0PKI7b7ohjiOfuDz_UVv1BVNosCdnAmBkrOwYjF9hgyyN6b5xmpI5wN";

    public static final int SEED_ENTROPY_DEFAULT = 192;//默认的seed熵
    public static final int SEED_ENTROPY_EXTRA = 256;//额外的seed熵

    public static final BigDecimal ANDA_OUTPUT_LIMIT = BigDecimal.valueOf(10000L);
    public static final BigDecimal ANDA_INPUT_LIMIT = BigDecimal.valueOf(1500000L);

    //自定义的字符串常量
    public static final String ARG_SEED = "seed";
    public static final String ARG_PASSWORD = "password";
    public static final String ARG_SEED_PASSWORD = "seed_password";
    public static final String ARG_EMPTY_WALLET = "empty_wallet";
    public static final String ARG_SEND_TO_ADDRESS = "send_to_address";
    public static final String ARG_SEND_TO_COIN_TYPE = "send_to_coin_type";
    public static final String ARG_SEND_TO_ACCOUNT_ID = "send_to_account_id";
    public static final String ARG_SEND_VALUE = "send_value";
    public static final String ARG_TX_MESSAGE = "tx_message";
    public static final String ARG_COIN_ID = "coin_id";
    public static final String ARG_ACCOUNT_ID = "account_id";
    public static final String ARG_MULTIPLE_COIN_IDS = "multiple_coin_ids";
    public static final String ARG_MULTIPLE_CHOICE = "multiple_choice";
    public static final String ARG_SEND_REQUEST = "send_request";
    public static final String ARG_TRANSACTION_ID = "transaction_id";
    public static final String ARG_ERROR = "error";
    public static final String ARG_MESSAGE = "message";
    public static final String ARG_ADDRESS = "address";
    public static final String ARG_ADDRESS_STRING = "address_string";
    public static final String ARG_EXCHANGE_ENTRY = "exchange_entry";
    public static final String ARG_URI = "test_wallet";
    public static final String ARG_PRIVATE_KEY = "private_key";
    public static final String ARG_COIN_TYPE = "coin_type";
    public static final String ARG_ACCOUNT_LIMIT = "account_limit";

    public static final int PERMISSIONS_REQUEST_CAMERA = 0;//请求相机权限
    public static final String WALLET_FILENAME_PROTOBUF = "wallet";

    /**
     * 服务器地址
     * 该变量用于以太坊和比特币兑换安达通证
     */
    public static final String SERVER_ADDRESS_BITCOIN_NEW_TX = ServerHttp.CHAIN_SERVER_HTTP + "/chain/transactions/addTxRecord";
    public static final String SERVER_ADDRESS_ETHEREUM_NEW_TX = ServerHttp.CHAIN_SERVER_HTTP + "/chain/transactions/addTxRecord1";
    public static final String SERVER_ADDRESS_PAYPAL_NEW_TX = ServerHttp.CHAIN_SERVER_HTTP + "/paypal/payment/getResult";


    public static final long WALLET_WRITE_DELAY = 5;
    public static final TimeUnit WALLET_WRITE_DELAY_UNIT = TimeUnit.SECONDS;

    public static final long STOP_SERVICE_AFTER_IDLE_SECS = 30 * 60; // 30 mins

    public static final String HTTP_CACHE_NAME = "http_cache";
    public static final int HTTP_CACHE_SIZE = 256 * 1024; // 256 KiB
    public static final int NETWORK_TIMEOUT_MS = 15 * (int) DateUtils.SECOND_IN_MILLIS;

    public static final String TX_CACHE_NAME = "tx_cache";
    //TODO: TX_CACHE_SIZE - currently not enforced
    public static final int TX_CACHE_SIZE = 5 * 1024 * 1024; // 5 MiB

    public static final long RATE_UPDATE_FREQ_MS = 30 * DateUtils.SECOND_IN_MILLIS;

    /**
     * Default currency to use if all default mechanisms fail.
     */
    public static final String DEFAULT_EXCHANGE_CURRENCY = "USD";

    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Charset US_ASCII = Charset.forName("US-ASCII");

    public static final char CHAR_HAIR_SPACE = '\u200a';
    public static final char CHAR_THIN_SPACE = '\u2009';
    public static final char CHAR_ALMOST_EQUAL_TO = '\u2248';
    public static final char CHAR_CHECKMARK = '\u2713';
    public static final char CURRENCY_PLUS_SIGN = '+';
    public static final char CURRENCY_MINUS_SIGN = '-';
    public static final int ADDRESS_FORMAT_GROUP_SIZE = 4;
    public static final int ADDRESS_FORMAT_LINE_SIZE = 12;

    public static final String MARKET_APP_URL = "market://details?id=%s";
    public static final String BINARY_URL = "https://gitlab.com/openwallet/openwallet-releases";

    public static final String VERSION_URL = "https://openwallet.xyz/version";
    public static final String SUPPORT_EMAIL = "https://reddit.com/r/openwallet";

    //TODO: move to resource files
    //服务器默认货币，不可变列表
    public static final List<CoinAddress> DEFAULT_COINS_SERVERS = ImmutableList.of(
            //BitcoinMain
            new CoinAddress(BitcoinMain.get(), new ServerAddress("btc-cce-1.coinomi.net", 5001),
                    new ServerAddress("btc-cce-2.coinomi.net", 5001)),
            //BitcoinTest
            new CoinAddress(BitcoinTest.get(), new ServerAddress("btc-testnet-cce-1.coinomi.net", 15001),
                    new ServerAddress("btc-testnet-cce-2.coinomi.net", 15001)),
            //EthereumMain
            new CoinAddress(EthereumMain.get(), new ServerAddress("eth-cce-1.coinomi.net", 5051),
                    new ServerAddress("eth-cce-2.coinomi.net", 5051)),
            //RippleMain
            new CoinAddress(RippleMain.get(), new ServerAddress("192.168.0.116", 5051)),
            //AndaChainMain
            new CoinAddress(AndaChainMain.get(), new ServerAddress("192.168.0.116", 8080))
    );

    public static final HashMap<CoinType, Integer> COINS_ICONS;
    public static final HashMap<CoinType, String> COINS_BLOCK_EXPLORERS;

    static {
        COINS_ICONS = new HashMap<>();
        COINS_ICONS.put(CoinID.BITCOIN_MAIN.getCoinType(), R.drawable.bitcoin);
        COINS_ICONS.put(CoinID.BITCOIN_TEST.getCoinType(), R.drawable.bitcoin_test);
        COINS_ICONS.put(CoinID.ETHEREUM_MAIN.getCoinType(), R.drawable.ethereum);
        COINS_ICONS.put(CoinID.ANDACHAIN_MAIN.getCoinType(), R.drawable.anda);
        COINS_ICONS.put(CoinID.RIPPLE_MAIN.getCoinType(), R.drawable.ripple_clover_launcher);

        COINS_BLOCK_EXPLORERS = new HashMap<CoinType, String>();
        COINS_BLOCK_EXPLORERS.put(CoinID.BITCOIN_MAIN.getCoinType(), "https://blockchain.info/tx/%s");
        COINS_BLOCK_EXPLORERS.put(CoinID.BITCOIN_TEST.getCoinType(), "https://testnet.blockchain.info/tx/%s");
    }

    public static final CoinType DEFAULT_COIN = BitcoinMain.get();
    public static final CoinType DEFAULT_COIN_TEST = BitcoinTest.get();
    public static final List<CoinType> DEFAULT_COINS = ImmutableList.of((CoinType) BitcoinMain.get());
    public static final List<CoinType> DEFAULT_COINS_TEST = ImmutableList.of((CoinType) BitcoinTest.get());
    public static final ArrayList<String> DEFAULT_TEST_COIN_IDS = Lists.newArrayList(
            BitcoinTest.get().getId()
    );

    /*支持的加密币列表*/
    public static final List<CoinType> SUPPORTED_COINS = ImmutableList.of(
            AndaChainMain.get(),
            BitcoinMain.get(),
            EthereumMain.get(),
            RippleMain.get()
    );

    /**
     * 使用SharedPreferences保存用户密码信息——安达币
     * @param context
     * @param password
     */
    public static void saveAndaPassword(Context context, String password){
        //获取SharedPreferences对象
        SharedPreferences sharedPre = context.getSharedPreferences("Anda_password", Context.MODE_PRIVATE);
        //SharedPreferences sharedPre = context.getSharedPreferences("password", Context.MODE_PRIVATE);
        //获取Editor对象
        SharedPreferences.Editor editor = sharedPre.edit();
        //设置参数
        editor.putString("Anda_password", password);
        //提交
        editor.apply();
    }

    /**
     * 使用SharedPreferences保存用户密码信息——比特币
     * @param context
     * @param password
     */
    public static void saveBitcoinPassword(Context context, String password){
        //获取SharedPreferences对象
        SharedPreferences sharedPre = context.getSharedPreferences("Bitcoin_password", Context.MODE_PRIVATE);
        //获取Editor对象
        SharedPreferences.Editor editor = sharedPre.edit();
        //设置参数
        editor.putString("Bitcoin_password", password);
        //提交
        editor.apply();
    }

    /**
     * 使用SharedPreferences保存用户密码信息——以太坊
     * @param context
     * @param password
     */
    public static void saveEthereumPassword(Context context, String password){
        //获取SharedPreferences对象
        SharedPreferences sharedPre = context.getSharedPreferences("Ethereum_password", Context.MODE_PRIVATE);
        //获取Editor对象
        SharedPreferences.Editor editor = sharedPre.edit();
        //设置参数
        editor.putString("Ethereum_password", password);
        //提交
        editor.apply();
    }

    /**
     * 使用SharedPreferences保存用户密码信息——瑞波币
     * @param context
     * @param password
     */
    public static void saveRipplePassword(Context context, String password){
        //获取SharedPreferences对象
        SharedPreferences sharedPre = context.getSharedPreferences("Ripple_password", Context.MODE_PRIVATE);
        //获取Editor对象
        SharedPreferences.Editor editor = sharedPre.edit();
        //设置参数
        editor.putString("Ripple_password", password);
        //提交
        editor.apply();
    }



    /**
     * 使用SharedPreferences保存用户以太坊密码信息
     * @param context
     * @param password
     */
    public static void saveEthPassInfo(Context context, String password){
        //获取SharedPreferences对象
        SharedPreferences sharedPre = context.getSharedPreferences("ETHPassword", Context.MODE_PRIVATE);
        //获取Editor对象
        SharedPreferences.Editor editor = sharedPre.edit();
        //设置参数
        editor.putString("password", password);
        //提交
        editor.apply();
    }
}
