package com.onets.wallet.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.view.ActionMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Charsets;
import com.onets.core.coins.CoinID;
import com.onets.core.coins.CoinType;
import com.onets.core.coins.FiatType;
import com.onets.core.coins.Value;
import com.onets.core.coins.ValueType;
import com.onets.core.coins.families.AndaFamily;
import com.onets.core.coins.families.BitFamily;
import com.onets.core.coins.families.EthereumFamily;
import com.onets.core.coins.families.RippleFamily;
import com.onets.core.exceptions.AddressMalformedException;
import com.onets.core.exchange.shapeshift.ShapeShift;
import com.onets.core.exchange.shapeshift.data.ShapeShiftMarketInfo;
import com.onets.core.messages.MessageFactory;
import com.onets.core.messages.TxMessage;
import com.onets.core.uri.CoinURI;
import com.onets.core.uri.CoinURIParseException;
import com.onets.core.util.ExchangeRate;
import com.onets.core.util.GenericUtils;
import com.onets.core.util.MonetaryFormat;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.SendRequest;
import com.onets.core.wallet.WalletAccount;
import com.onets.core.wallet.families.andachain.AndaAddress;
import com.onets.core.wallet.families.andachain.AndaSendRequest;
import com.onets.core.wallet.families.bitcoin.BitAddress;
import com.onets.wallet.AddressBookProvider;
import com.onets.wallet.Configuration;
import com.onets.wallet.Constants;
import com.onets.wallet.ExchangeRatesProvider;
import com.onets.wallet.R;
import com.onets.wallet.WalletApplication;
import com.onets.wallet.data.AddressBookEntry;
import com.onets.wallet.tasks.MarketInfoPollTask;
import com.onets.wallet.ui.widget.AddressView;
import com.onets.wallet.ui.widget.AmountEditView;
import com.onets.wallet.util.CheatSheet;
import com.onets.wallet.util.HttpClientPost;
import com.onets.wallet.util.ThrottlingWalletChangeListener;
import com.onets.wallet.util.UiUtils;
import com.onets.wallet.util.WalletUtils;
import com.onets.wallet.util.WeakHandler;

import org.acra.ACRA;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.crypto.KeyCrypterException;
import org.bitcoinj.utils.Threading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.RawTransaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ManagedTransaction;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static com.onets.core.Preconditions.checkNotNull;
import static com.onets.core.coins.Value.canCompare;
import static com.onets.wallet.ExchangeRatesProvider.getRates;
import static com.onets.wallet.util.UiUtils.setGone;
import static com.onets.wallet.util.UiUtils.setVisible;

/**
 * Fragment that prepares a transaction
 *
 * @author Yu K.Q.
 * @author Yu K.Q.
 * 发送
 */
public class SendFragment extends WalletFragment {
    private static final Logger log = LoggerFactory.getLogger(SendFragment.class);
    private static final String TAG = "SendFragment";

    Wallet wallet;
    private WalletActivity activity;
    public String result;
    public PeerGroup peerGroup;
    public ValueType type;

    int id;
    //状态：输入 准备 发送中 已发送 发送失败
    private enum State {
        INPUT, PREPARATION, SENDING, SENT, FAILED
    }

    // the fragment initialization parameters 片段初始化参数
    private static final int REQUEST_CODE_SCAN = 0;//请求扫描
    private static final int SIGN_TRANSACTION = 1;//交易签名

    private static final int UPDATE_VIEW = 0;//更新视图
    private static final int UPDATE_LOCAL_EXCHANGE_RATES = 1;//更新汇率
    private static final int UPDATE_WALLET_CHANGE = 2;//更新钱包变动
    private static final int UPDATE_MARKET = 3;//更新市场
    private static final int SET_ADDRESS = 4;//地址设置

    // Loader IDs
    private static final int ID_RATE_LOADER = 0;//ID速率加载
    private static final int ID_RECEIVING_ADDRESS_LOADER = 1;//ID接收地址加载

    // Saved state
    private static final String STATE_ADDRESS = "address";
    private static final String STATE_ADDRESS_CAN_CHANGE_TYPE = "address_can_change_type";
    private static final String STATE_AMOUNT = "amount";
    private static final String STATE_AMOUNT_TYPE = "amount_type";

    @Nullable
    private Value lastBalance; //最近的余额 TODO setup wallet watcher for the latest balance
    private State state = State.INPUT;//输入状态
    private AbstractAddress address;//证包地址
    private boolean addressTypeCanChange;//地址类型是否可以改变
    private Value sendAmount;//发送金额

    private CoinType sendAmountType;//发送金额类型
    private MessageFactory messageFactory;//消息
    private boolean isTxMessageAdded;//Tx信息是否添加
    private boolean isTxMessageValid;//Tx信息是否有效
    private WalletAccount account;//钱包账户

    private MyHandler handler = new MyHandler(this);
    private ContentObserver addressBookObserver = new AddressBookObserver(handler);//地址簿
    private WalletApplication application;//应用
    private Configuration config;//配置
    private Map<String, ExchangeRate> localRates = new HashMap<>();//当地汇率
    private ShapeShiftMarketInfo marketInfo;//市场信息变化折线图

    private com.onets.core.wallet.transaction.Transaction tran;//安达币的交易，信息用于上传到服务器

    @Bind(R.id.send_to_address)
    AutoCompleteTextView sendToAddressView;//发送到的地址
    @Bind(R.id.send_to_address_static)
    AddressView sendToStaticAddressView;
    @Bind(R.id.send_coin_amount)
    AmountEditView sendCoinAmountView;//发送的币数量
    @Bind(R.id.send_local_amount)
    AmountEditView sendLocalAmountView;//发送的法定货币数量
    @Bind(R.id.address_error_message)
    TextView addressError;//地址错误提醒区
    @Bind(R.id.amount_error_message)
    TextView amountError;//“输入的金额无效”显示
    @Bind(R.id.amount_warning_message)
    TextView amountWarning;//“手续费导致数量问题”显示
    @Bind(R.id.scan_qr_code)
//    ImageButton scanQrCodeButton;
    View scanQrCodeButton;//二维码扫描图标
    @Bind(R.id.erase_address)
    ImageButton eraseAddressButton;//清除地址button图标
    @Bind(R.id.tx_message_add_remove)
    Button txMessageButton;//Tx信息按钮，用于添加或清除
    @Bind(R.id.tx_message_label)
    TextView txMessageLabel;//Tx消息标签
    @Bind(R.id.tx_message)
    EditText txMessageView;//Tx消息
    @Bind(R.id.tx_message_counter)
    TextView txMessageCounter;//Tx消息计数
    @Bind(R.id.send_confirm)
    Button sendConfirmButton;//发送按钮
    @Bind(R.id.Edit)
    EditText Edit;

    //接收过来的安达地址
    String andaAddress;

    @Nullable
    ReceivingAddressViewAdapter sendToAdapter;//适配器
    CurrencyCalculatorLink amountCalculatorLink;//数量计算连接
    Timer timer;
    MyMarketInfoPollTask pollTask;
    ActionMode actionMode;
    EditViewListener txMessageViewTextChangeListener;//编辑框更改监听器
    Listener listener;
    ContentResolver resolver;//内容解析器

    ECKey ecKey;//在安达交易信息上传时使用

    private Wallet.SendResult sendResult;//比特币发送结果

    /**
     * Use this factory method to create a new instance of
     * this fragment using the an account id.
     *
     * @param accountId the id of an account
     * @return A new instance of fragment WalletSendCoins.
     * 发送Fragment
     */
    public static SendFragment newInstance(String accountId) {
        SendFragment fragment = new SendFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.ARG_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using a URI.
     *
     * @param uri the payment uri
     * @return A new instance of fragment WalletSendCoins.
     */
    public static SendFragment newInstance(String accountId, CoinURI uri) {
        SendFragment fragment = new SendFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ARG_URI, uri.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public SendFragment() {
        // Required empty public constructor
    }

    /**
     * 创建
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // The onCreateOptionsMenu is handled in com.onets.wallet.ui.AccountFragment
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        WalletAccount a = null;
        //检测参数是否为空
        if (args != null) {
            if (args.containsKey(Constants.ARG_ACCOUNT_ID)) {
                String accountId = args.getString(Constants.ARG_ACCOUNT_ID);
                a = checkNotNull(application.getAccount(accountId));
            }

            if (args.containsKey(Constants.ARG_URI)) {
                try {
                    processUri(args.getString(Constants.ARG_URI));
                } catch (CoinURIParseException e) {
                    // TODO handle more elegantly
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    ACRA.getErrorReporter().handleException(e);
                }
            }

            // TODO review the following code. This is used when a user clicks on a URI.
            //检测钱包账户为空
            if (a == null) {
                List<WalletAccount> accounts = application.getAllAccounts();
                if (accounts.size() > 0) a = accounts.get(0);
                //钱包账户为空
                if (a == null) {
                    ACRA.getErrorReporter().putCustomData("wallet-exists",
                            application.getWallet() == null ? "no" : "yes");
                    Toast.makeText(getActivity(), R.string.no_such_pocket_error,
                            Toast.LENGTH_LONG).show();
                    getActivity().finish();
                    return;
                }
            }
            checkNotNull(a, "No account selected");
        } else {
            throw new RuntimeException("Must provide account ID or a payment URI");
        }

        //发送余额类型
        sendAmountType = a.getCoinType();
        //消息工厂
        messageFactory = a.getCoinType().getMessagesFactory();
        //钱包账户
        account = a;

        if (savedInstanceState != null) {
            address = (AbstractAddress) savedInstanceState.getSerializable(STATE_ADDRESS);
            addressTypeCanChange = savedInstanceState.getBoolean(STATE_ADDRESS_CAN_CHANGE_TYPE);
            sendAmount = (Value) savedInstanceState.getSerializable(STATE_AMOUNT);
            sendAmountType = (CoinType) savedInstanceState.getSerializable(STATE_AMOUNT_TYPE);
        }

        //更新余额
        updateBalance();

        String localSymbol = config.getExchangeCurrencyCode();
        for (ExchangeRatesProvider.ExchangeRate rate : getRates(getActivity(), localSymbol).values()) {
            localRates.put(rate.currencyCodeId, rate.rate);
        }
    }

    /*进程URI*/
    private void processUri(String uri) throws CoinURIParseException {
        CoinURI coinUri = new CoinURI(uri);
        CoinType scannedType = coinUri.getTypeRequired();

        if (!Constants.SUPPORTED_COINS.contains(scannedType)) {
            String error = getResources().getString(R.string.unsupported_coin, scannedType.getName());
            throw new CoinURIParseException(error);
        }

        //账户为空
        if (account == null) {
            List<WalletAccount> allAccounts = application.getAllAccounts();
            List<WalletAccount> sendFromAccounts = application.getAccounts(scannedType);
            if (sendFromAccounts.size() == 1) {
                account = sendFromAccounts.get(0);
            } else if (allAccounts.size() == 1) {
                account = allAccounts.get(0);
            } else {
                throw new CoinURIParseException("No default account found");
            }
        }

        if (coinUri.isAddressRequest()) {
            Log.i(TAG, "--------- processUri:   isAddressRequest ");
            UiUtils.replyAddressRequest(getActivity(), coinUri, account);
        } else {
            Log.i(TAG, "--------- processUri:   setUri ");
            setUri(coinUri);
        }
    }

    /*更新余额*/
    private void updateBalance() {
        if (account != null) {
            lastBalance = account.getBalance();
        }
    }

    /**
     * 创建视图
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_send, container, false);
        ButterKnife.bind(this, view);


        sendToAdapter = new ReceivingAddressViewAdapter(inflater.getContext());
        sendToAddressView.setAdapter(sendToAdapter);
        sendToAddressView.setOnFocusChangeListener(receivingAddressListener);
        sendToAddressView.addTextChangedListener(receivingAddressListener);

        sendCoinAmountView.resetType(sendAmountType, true);
        if (sendAmount != null) sendCoinAmountView.setAmount(sendAmount, false);
        sendLocalAmountView.setFormat(FiatType.FRIENDLY_FORMAT);
        amountCalculatorLink = new CurrencyCalculatorLink(sendCoinAmountView, sendLocalAmountView);
        amountCalculatorLink.setExchangeDirection(config.getLastExchangeDirection());
        amountCalculatorLink.setExchangeRate(getCurrentRate());

        addressError.setVisibility(View.GONE);
        amountError.setVisibility(View.GONE);
        amountWarning.setVisibility(View.GONE);

        setupTxMessage();

        return view;
    }

    /**
     * 销毁视图
     */
    @Override
    public void onDestroyView() {
        config.setLastExchangeDirection(amountCalculatorLink.getExchangeDirection());
        amountCalculatorLink = null;
        sendToAdapter = null;
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    /**
     * 重新开始
     */
    @Override
    public void onResume() {
        super.onResume();
        //接收跳转过来的数据
        Intent intent = getActivity().getIntent();
        id = intent.getIntExtra("id", 0);
        andaAddress = intent.getStringExtra("AndaAddress");
        String sendAmount = intent.getStringExtra("Amount");

        if (id == 1) {//比特币

            sendToAddressView.setText(R.string.server_bitcoin_address);  //固定的发送比特币地址
            MonetaryFormat format = new MonetaryFormat().noCode();
            type = account.getBalance().type;
            Log.d(TAG, "onResume: ----Type:"+type);
            Value amount = format.parse(type,sendAmount);

            sendCoinAmountView.setAmount(amount,false);
            Log.i(TAG, "------------------------发送界面onResume: "+address);

        }else if (id == 2){//以太坊

            sendToAddressView.setText(R.string.server_ethereum_address);//固定的发送比特币地址
            MonetaryFormat format = new MonetaryFormat().noCode();
            type = account.getBalance().type;
            Value amount = format.parse(type, sendAmount);

            sendCoinAmountView.setAmount(amount, false);
        }else if (id == 3){//瑞波币

        }

        amountCalculatorLink.setListener(amountsListener);

        resolver.registerContentObserver(AddressBookProvider.contentUri(
                getActivity().getPackageName()), true, addressBookObserver);

        addAccountEventListener(account);

        updateBalance();
        updateView();

    }

    /**
     * 暂停
     */
    @Override
    public void onPause() {
        removeAccountEventListener(account);

        resolver.unregisterContentObserver(addressBookObserver);

        amountCalculatorLink.setListener(null);

        finishActionMode();

        stopPolling();

        super.onPause();
    }

    /**
     * 添加账户事件监听器
     *
     * @param a
     */
    private void addAccountEventListener(WalletAccount a) {
        if (a != null) {
            a.addEventListener(transactionChangeListener, Threading.SAME_THREAD);
        }
    }

    /**
     * 移除账户事件监听器
     *
     * @param a
     */
    private void removeAccountEventListener(WalletAccount a) {
        if (a != null) a.removeEventListener(transactionChangeListener);
        transactionChangeListener.removeCallbacks();
    }

    /**
     * 设置交易信息
     */
    private void setupTxMessage() {
        if (account == null || messageFactory == null) {
            txMessageButton.setVisibility(GONE);
            // Remove old listener if needed
            if (txMessageViewTextChangeListener != null) {
                txMessageView.removeTextChangedListener(txMessageViewTextChangeListener);
            }
            return;
        }

        txMessageButton.setVisibility(View.VISIBLE);
        txMessageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTxMessageAdded) { // if tx message added, remove it
                    hideTxMessage();
                } else { // else show tx message fields
                    showTxMessage();
                }
            }
        });

        final int maxMessageBytes = messageFactory.maxMessageSizeBytes();
        final int messageLengthThreshold = (int) (maxMessageBytes * .8); // 80% full
        final int txMessageCounterPaddingOriginal = txMessageView.getPaddingBottom();
        final int txMessageCounterPadding =
                getResources().getDimensionPixelSize(R.dimen.tx_message_counter_padding);
        final int colorWarn = getResources().getColor(R.color.fg_warning);
        final int colorError = getResources().getColor(R.color.fg_error);

        // Remove old listener if needed
        if (txMessageViewTextChangeListener != null) {
            txMessageView.removeTextChangedListener(txMessageViewTextChangeListener);
        }
        // This listener checks the length of the message and displays a counter if it passes a
        // threshold or the max size. It also changes the bottom padding of the message field
        // to accommodate the counter.
        // 这个监听器检查消息的长度，并显示一个计数器，如果它通过一个阈值或最大大小。它还改变了消息字段的底部填充以适应计数器。
        txMessageViewTextChangeListener = new EditViewListener() {
            @Override
            public void afterTextChanged(final Editable s) {
                // Not very efficient because it creates a new String object on each key press
                int length = s.toString().getBytes(Charsets.UTF_8).length;
                boolean isTxMessageValidNow = true;
                if (length < messageLengthThreshold) {
                    if (txMessageCounter.getVisibility() != GONE) {
                        txMessageCounter.setVisibility(GONE);
                        txMessageView.setPadding(0, 0, 0, txMessageCounterPaddingOriginal);
                    }
                } else {
                    int remaining = maxMessageBytes - length;
                    if (txMessageCounter.getVisibility() != VISIBLE) {
                        txMessageCounter.setVisibility(VISIBLE);
                        txMessageView.setPadding(0, 0, 0, txMessageCounterPadding);
                    }
                    txMessageCounter.setText(Integer.toString(remaining));
                    if (length <= maxMessageBytes) {
                        txMessageCounter.setTextColor(colorWarn);
                    } else {
                        isTxMessageValidNow = false;
                        txMessageCounter.setTextColor(colorError);
                    }
                }
                // Update view only if the message validity changed
                if (isTxMessageValid != isTxMessageValidNow) {
                    isTxMessageValid = isTxMessageValidNow;
                    updateView();
                }
            }

            /**
             * 聚焦改变
             * @param v
             * @param hasFocus
             */
            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {
                if (!hasFocus) {
                    validateTxMessage();
                }
            }
        };

        txMessageView.addTextChangedListener(txMessageViewTextChangeListener);
    }


    /**
     * 显示交易信息
     */
    private void showTxMessage() {
        if (messageFactory != null) {
            txMessageButton.setText(R.string.tx_message_public_remove);
            txMessageLabel.setVisibility(View.VISIBLE);
            txMessageView.setVisibility(View.VISIBLE);
            isTxMessageAdded = true;
            isTxMessageValid = true; // Initially the empty message is valid, even if it is ignored
        }
    }

    /**
     * 隐藏交易信息
     */
    private void hideTxMessage() {
        if (messageFactory != null) {
            txMessageButton.setText(R.string.tx_message_public_add);
            txMessageLabel.setVisibility(View.GONE);
            txMessageView.setText(null);
            txMessageView.setVisibility(View.GONE);
            isTxMessageAdded = false;
            isTxMessageValid = false;
        }
    }

    /**
     * 地址清空点击
     */
    @OnClick(R.id.erase_address)
    public void onAddressClearClick() {
        clearAddress(true);
        updateView();
    }

    /**
     * 清空地址
     *
     * @param clearTextField
     */
    private void clearAddress(boolean clearTextField) {
        address = null;
        if (clearTextField) setSendToAddressText(null);
        sendAmountType = account.getCoinType();
        addressTypeCanChange = false;
    }

    /**
     * 设置地址
     *
     * @param address
     * @param typeCanChange
     */
    private void setAddress(AbstractAddress address, boolean typeCanChange) {
        this.address = address;
        this.addressTypeCanChange = typeCanChange;
    }

    /**
     * 保存实例状态
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(STATE_ADDRESS, address);
        outState.putBoolean(STATE_ADDRESS_CAN_CHANGE_TYPE, addressTypeCanChange);
        outState.putSerializable(STATE_AMOUNT, sendAmount);
        outState.putSerializable(STATE_AMOUNT_TYPE, sendAmountType);
    }

    /**
     * 开始或停止市场价格轮询
     */
    private void startOrStopMarketRatePolling() {
        if (address != null && !account.isType(address)) {
            String pair = ShapeShift.getPair(account.getCoinType(), address.getType());
            if (timer == null) {
                startPolling(pair);
            } else {
                pollTask.updatePair(pair);
            }
        } else if (timer != null) {
            stopPolling();
        }
    }

    /**
     * Start polling for the market information of the current pair, if it is already stated this
     * call does nothing
     * 如果已经说明，则开始轮询当前对的市场信息
     * 调用什么也不做
     */
    private void startPolling(String pair) {
        if (timer == null) {
            ShapeShift shapeShift = application.getShapeShift();
            pollTask = new MyMarketInfoPollTask(handler, shapeShift, pair);
            timer = new Timer();
            timer.schedule(pollTask, 0, Constants.RATE_UPDATE_FREQ_MS);
        }
    }

    /**
     * Stop the polling for the market info, if it is already stop this call does nothing
     */
    private void stopPolling() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            pollTask.cancel();
            timer = null;
            pollTask = null;
        }
    }

    @OnClick(R.id.scan_qr_code)
    void handleScan() {
        startActivityForResult(new Intent(getActivity(), ScanActivity.class), REQUEST_CODE_SCAN);
    }

    @OnClick(R.id.send_confirm)
    public void onSendClick() {
        //validateAddress();
        //validateAmount();

        if (account.getCoinType() instanceof AndaFamily) {
            handleSendConfirmA();
        } else if (account.getCoinType() instanceof BitFamily) {
            handleSendConfirmB();
        } else if (account.getCoinType() instanceof EthereumFamily) {
            handleSendConfirmE();
        } else if (account.getCoinType() instanceof RippleFamily) {

        }

    }

    @OnClick(R.id.send_Test)
    public void testClick(){

    }

    /**
     * 安达币发送交易
     */
    private void handleSendConfirmA() {

        state = State.PREPARATION;
        updateView();
        boolean toresult = false;

        Log.d(TAG, "handleSendConfirmA: hasPrivKey() " + account.getWallet().getMasterKey().hasPrivKey());
        Log.d(TAG, "handleSendConfirmA: privKey() " + account.getWallet().getMasterKey().getPrivKey());
        ecKey = ECKey.fromPrivate(account.getWallet().getMasterKey().getPrivKey());

        if (application.getWallet() != null) {
            //获取发送金额及目标地址金额
            try {
                String recipientStr = sendToAddressView.getText().toString();
                Log.d(TAG, "handleSendConfirmA: recipientStr " + recipientStr);
                AndaAddress recipientAddress = AndaAddress.from(account.getCoinType(), recipientStr);
                String recipientId = String.valueOf(recipientAddress.getId());
                Log.d(TAG, "handleSendConfirmA: recipientId " + recipientId);
            } catch (AddressMalformedException e) {
                e.printStackTrace();
                Log.d(TAG, "handleSendConfirmA: 输入地址转换为安达地址失败");
            }

            BigDecimal sendamount = BigDecimal.valueOf(Double.valueOf(sendCoinAmountView.getAmountText()));

            //交易金额限制判断 返回值 -1 小于 0 等于 1 大于
            if (sendamount.compareTo(Constants.ANDA_OUTPUT_LIMIT) == 1 ) {
                Toast.makeText(getActivity(), "交易金额超过限制或交易后对方余额超过钱包限制", Toast.LENGTH_SHORT).show();
                updateView();
            } else {

                try {
                    SendRequest sendRequest = account.sendCoins(address, account.getCoinType(), sendAmount, null, null);
                } catch (WalletAccount.WalletAccountException e) {
                    e.printStackTrace();
                }

                /*onMakeTransaction(address, sendAmount, getTxMessage());
                ///安达币
                tran = new com.onets.core.wallet.transaction.Transaction(
                        account.getReceiveAddress().toString(),
                        sendToAddressView.getText().toString(),
                        sendamount
                );
                Log.d(TAG, "handleSendConfirmA:tran: " + tran.toStringOragin());

                String sign = ecKey.signMessage(tran.toStringOragin());
                Log.d(TAG, "handleSendConfirmA: sign " + sign);

                //安达币发送-使用的http
                toresult = AndaSendRequest.to(
                        tran.getSender(),//1发送者地址
                        tran.getRecipient(),//2接收者地址
                        Hex.toHexString(account.getWallet().getMasterKey().getPubKey()),//3公钥
                        sign,//4签名
                        tran.hash(),      //5TxHash
                        tran.getTimestamp().toString(),  //6时间戳
                        tran.getAmount().doubleValue(),//7发送金额
                        txMessageView.getText().toString()//8public message
                );

                Log.d(TAG, "handleSendConfirmA: toresult " + toresult);
                if (toresult) {
                    Toast.makeText(getContext(), "已发送", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getContext(), WalletActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "发送失败,请确认接收者地址或交易金额", Toast.LENGTH_LONG).show();
                }*/
            }

        }
    }

    /**
     * 比特币发送交易
     */
    private void handleSendConfirmB() {
        try {
            //目标地址
            String rebitAddress = sendToAddressView.getText().toString();
            BitAddress bitAddress = BitAddress.from(account.getCoinType(),rebitAddress);
            Log.d(TAG, Constants.LOG_LABLE + "handleSendConfirmB: bitAddress " + bitAddress.toString());
            //交易金额
            Value coinAmount = sendCoinAmountView.getAmount();
            Log.d(TAG, Constants.LOG_LABLE + "handleSendConfirmB: coinAmount " + coinAmount);

            //判断是否加密
            boolean flag = account.isEncrypted();
            //加密
            Log.d(TAG, Constants.LOG_LABLE + "handleSendConfirmB: flag " + flag);
            if(flag){
                //输入密码界面
                final EditText edit = new EditText(getActivity());
                AlertDialog.Builder dialog=new AlertDialog.Builder(getActivity());
                dialog.setTitle("请输入密码：");
                dialog.setIcon(android.R.drawable.btn_star);
                dialog.setCancelable(false);
                dialog.setView(edit);
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = "";
                        password = edit.getText().toString();
                        try {
                            Log.d(TAG, Constants.LOG_LABLE + "handleSendConfirmB: password " + password);
                            SendRequest request = account.sendCoins(bitAddress,account.getCoinType(),coinAmount,password,andaAddress);
                            /*Toast.makeText(getActivity(), "交易已执行", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getContext(), WalletActivity.class);
                            startActivity(intent);*/

                        } catch (WalletAccount.WalletAccountException e) {
                            e.printStackTrace();
                        }
                    }
                });
                dialog.show();
            }else{
                String password = "";

                SendRequest request = account.sendCoins(bitAddress,account.getCoinType(),coinAmount,password,andaAddress);
                Intent intent = new Intent(getContext(), WalletActivity.class);
                startActivity(intent);
            }
        } catch (AddressMalformedException e) {
            e.printStackTrace();
        } catch (WalletAccount.WalletAccountException e) {
            e.printStackTrace();
        }

    }

    /**
     * 以太坊发送交易
     */
    private void handleSendConfirmE(){
        //本地地址
        String mineAddress = account.getReceiveAddress().toString();

        //密码部分
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("ETHPassword", Context.MODE_PRIVATE);
        String ethPass = sharedPreferences.getString("password", "");
        Log.d(TAG, "handleSendConfirmE: ethPass " + ethPass);

        //目标地址
        String toAddress = sendToAddressView.getText().toString();

        //以太坊钱包文件
        String file = WalletApplication.getInstance().getFilesDir().toString();
        String str = mineAddress.substring(2);
        file = file + "/" + str;
        File file1 = new File(file);

        //ethereum环境
        String url = "https://mainnet.infura.io/v3/716847d8519544208dde57f55ef80541";
        Web3j web3j = Web3jFactory.build(new HttpService(url));
        Log.d(TAG, "handleSendConfirmE: url " + url);

        //设置需要的gas值
        BigInteger GAS_PRICE = ManagedTransaction.GAS_PRICE;//默认值
        //BigInteger GAS_LIMIT = Contract.GAS_LIMIT;//默认值
        BigInteger GAS_LIMIT = BigInteger.valueOf(25000);

        //value值转换
        BigInteger value = Convert.toWei(sendCoinAmountView.getAmountText(), Convert.Unit.ETHER).toBigInteger();

        //预计发送总值
        BigInteger amount = GAS_LIMIT.multiply(GAS_PRICE);
        Log.d(TAG, "handleSendConfirmE: GAS_LIMIT * GAS_PRICE " + amount);
        amount = amount.add(value);
        Log.d(TAG, "handleSendConfirmE: GAS_LIMIT * GAS_PRICE + VALUE " + amount);

        Toast.makeText(getActivity(),
                "以太坊正在发送中，请不要在交易成功前关闭钱包",
                Toast.LENGTH_LONG).show();

        try {
            //获得nonce
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    mineAddress, DefaultBlockParameterName.LATEST
            ).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            Log.d(TAG, "handleSendConfirmE: nonce " + nonce);

            Toast.makeText(getActivity(),
                    "以太坊正在发送中，请不要在交易成功前关闭钱包",
                    Toast.LENGTH_LONG).show();

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {

                        //加载本地KeyStore 文件生成Credentials对象
                        Credentials credentials = org.web3j.crypto.WalletUtils.loadCredentials(
                                ethPass, file1
                        );
                        Log.d(TAG, "handleSendConfirmE: credentials");

                        //生成RawTransaction交易对象
                        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                                nonce, GAS_PRICE, GAS_LIMIT, toAddress, value
                        );
                        Log.d(TAG, "handleSendConfirmE: rawTransaction " + rawTransaction.getData());

                        //使用Credentials对象对RawTransaction对象进行签名
                        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                        String hexValue = Numeric.toHexString(signedMessage);
                        Log.d(TAG, "handleSendConfirmE: hexValue " + hexValue);

                        //发送交易
                        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
                        Log.d(TAG, "handleSendConfirmE: ethSenderTransaction");

                        if (ethSendTransaction.hasError()){
                            String message = ethSendTransaction.getError().getMessage();
                            Log.d(TAG, "handleSendConfirmE: transaction failed, info " + message);

                            Toast.makeText(getActivity(),
                                    "transaction failed, info" + message,
                                    Toast.LENGTH_LONG
                            ).show();

                        }else {
                            String transactionHash = ethSendTransaction.getTransactionHash();
                            Log.d(TAG, "handleSendConfirmE: transactionHash " + transactionHash);

                            Log.d(TAG, "handleSendConfirmE: toAddress " + toAddress);

                            //0x7BF470fe5d7553e444989229721b47375CB9B695
                            if (toAddress.equals("0x7BF470fe5d7553e444989229721b47375CB9B695")){
                                Map<String, Object> map = new HashMap<String, Object>();
                                map.put("coinSendAddress", mineAddress);
                                map.put("AndaAddress",andaAddress);
                                map.put("Amount",value.divide(BigInteger.valueOf(1000000000)));
                                map.put("id", transactionHash);

                                HttpClientPost httpclient = new HttpClientPost();
                                String result_ =  httpclient.post(Constants.SERVER_ADDRESS_ETHEREUM_NEW_TX,map);

                                Log.d(TAG, "handleSendConfirmE： "+result_);
                                for (Map.Entry<String, Object> entry : map.entrySet()) {
                                    Log.d(TAG, "handleSendConfirmE: Key = " + entry.getKey() + ",Value= " + entry.getValue());
                                }
                            }

                            //验证交易
                            EthTransaction ethTransaction = web3j.ethGetTransactionByHash(transactionHash).send();
                            Log.d(TAG, "handleSendConfirmE: transaction from " + mineAddress + " to " + toAddress + " amount " + value);

                            org.web3j.protocol.core.methods.response.Transaction transactionResult = ethTransaction.getResult();
                            Log.d(TAG, "handleSendConfirmE: transactionResult " + transactionResult.getValue());
                            Log.d(TAG, "handleSendConfirmE: transactionResult.getHash() " + transactionResult.getHash());

                            Intent intent = new Intent(getContext(), WalletActivity.class);
                            startActivity(intent);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (CipherException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取交易信息
     *
     * @return
     */
    @Nullable
    private TxMessage getTxMessage() {
        if (isTxMessageAdded && messageFactory != null && txMessageView.getText().length() != 0) {
            String message = txMessageView.getText().toString();
            try {
                return messageFactory.createPublicMessage(message);
            } catch (Exception e) { // Should not happen
                ACRA.getErrorReporter().handleSilentException(e);
            }
        }
        return null;
    }

    /**
     * 交易生成
     *
     * @param toAddress 接收者地址
     * @param amount    金额
     * @param txMessage 交易消息
     */
    public void onMakeTransaction(AbstractAddress toAddress, Value amount, @Nullable TxMessage txMessage) {
        Intent intent = new Intent(getActivity(), SignTransactionActivity.class);


        /**
         * 检测是否为空钱包
         */
        if (canCompare(lastBalance, amount) && amount.compareTo(lastBalance) == 0) {
            intent.putExtra(Constants.ARG_EMPTY_WALLET, true);
            Log.i(TAG, "onMakeTransaction:   empty_wallet     ");
        } else {
            intent.putExtra(Constants.ARG_SEND_VALUE, amount);
            Log.i(TAG, "onMakeTransaction:   send_value     ");
        }
        intent.putExtra(Constants.ARG_ACCOUNT_ID, account.getId());
        intent.putExtra(Constants.ARG_SEND_TO_ADDRESS, toAddress);
        /**
         * 检测交易信息是否为空
         */
        if (txMessage != null) intent.putExtra(Constants.ARG_TX_MESSAGE, txMessage);

        startActivityForResult(intent, SIGN_TRANSACTION);
        state = State.INPUT;
    }

    /**
     * 重置恢复
     */
    public void reset() {
        // No-op if the view is not created
        if (getView() == null) return;

        clearAddress(true);
        hideTxMessage();
        sendToAddressView.setVisibility(View.VISIBLE);
        sendToStaticAddressView.setVisibility(View.VISIBLE);
        amountCalculatorLink.setPrimaryAmount(null);
        sendAmount = null;
        state = State.INPUT;
        addressError.setVisibility(View.VISIBLE);
        amountError.setVisibility(View.VISIBLE);
        amountWarning.setVisibility(View.VISIBLE);
        updateView();
    }

    /**
     * 二维码结果
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {

        if (requestCode == REQUEST_CODE_SCAN) {
            if (resultCode == Activity.RESULT_OK) {
                //从扫描的结果中截取地址
                String input = intent.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);//从ScanActivity返回结果
                Bundle bundle = intent.getExtras();
                result = bundle.getString("result");
                int hand = result.indexOf(":");
                result = result.substring(hand + 1);
                int foot = result.indexOf("?");
                if (foot != -1) {
                    result = result.substring(0, foot);    //截取地址
                }
                //将截取的地址显示在界面TextView组件中
                sendToAddressView.setText(result);

                //扫描后，弹出dialog，显示地址
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("要发送到的地址");
                dialog.setMessage(result);
                dialog.setCancelable(true);
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog.show();

                if (!processInput(input)) {
                    String error = getResources().getString(R.string.scan_error, input);
                    Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == SIGN_TRANSACTION) {
            if (resultCode == Activity.RESULT_OK) {
                Exception error = (Exception) intent.getSerializableExtra(Constants.ARG_ERROR);

                if (error == null) {
                    Toast.makeText(getActivity(), R.string.sending_msg, Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onTransactionBroadcastSuccess(account, null);
                } else {
                    if (error instanceof KeyCrypterException) {
                        //输入的密码无效
                        Toast.makeText(getActivity(), R.string.password_failed, Toast.LENGTH_LONG).show();
                    } else if (error instanceof IOException) {
                        //无法连接到网络
                        Toast.makeText(getActivity(), R.string.send_coins_error_network, Toast.LENGTH_LONG).show();
                    }
                    /*if (error instanceof InsufficientMoneyException) {
                        //余额不足
                        Toast.makeText(getActivity(), R.string.amount_error_not_enough_money_plain, Toast.LENGTH_LONG).show();
                    } else if (error instanceof NoSuchPocketException) {
                        //与证包通讯时出错。如此问题重复出现，请重新恢复证包
                        Toast.makeText(getActivity(), R.string.no_such_pocket_error, Toast.LENGTH_LONG).show();
                    } else if (error instanceof KeyCrypterException) {
                        //输入的密码无效
                        Toast.makeText(getActivity(), R.string.password_failed, Toast.LENGTH_LONG).show();
                    } else if (error instanceof IOException) {
                        //无法连接到网络
                        Toast.makeText(getActivity(), R.string.send_coins_error_network, Toast.LENGTH_LONG).show();
                    } else if (error instanceof Wallet.DustySendRequested) {
                        //发送金额太小，会被网络拒绝。
                        Toast.makeText(getActivity(), R.string.send_coins_error_dust, Toast.LENGTH_LONG).show();
                    } else {
                        log.error("An unknown error occurred while sending coins", error);
                        //无法发送：
                        String errorMessage = getString(R.string.send_coins_error, error.getMessage());
                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                    }*/

                    if (listener != null) listener.onTransactionBroadcastFailure(account, null);
                }
            }
        }
    }

    /**
     * 输入进程
     *
     * @param input
     * @return
     */
    private boolean processInput(String input) {
        input = input.trim();
        try {
            updateStateFrom(new CoinURI(input));
            return true;
        } catch (final CoinURIParseException x) {
            try {
                Log.i(TAG, "processInput: -----  CoinURIParseException :" + x);
                parseAddress(input);
                updateView();
                return true;
            } catch (AddressMalformedException e) {
                return false;
            }
        }
    }

    /**
     * 更新状态
     *
     * @param coinUri
     * @throws CoinURIParseException
     */
    public void updateStateFrom(CoinURI coinUri) throws CoinURIParseException {
        // No-op if the view is not created
        if (getView() == null) return;

        // TODO rework the address request standard
//        if (coinUri.isAddressRequest() && coinUri.getTypeRequired().equals(account.getCoinType())) {
//            UiUtils.replyAddressRequest(getActivity(), coinUri, account);
//            return;
//        }
        Log.i(TAG, " ---------    updateStateFrom:  setUri ");
        setUri(coinUri);

        // delay these actions until fragment is resumed
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (sendAmount != null) {
                    amountCalculatorLink.setPrimaryAmount(sendAmount);
                }
                updateView();
                validateEverything();
                requestFocusFirst();
            }
        });
    }

    /**
     * 设置URI
     *
     * @param coinUri
     * @throws CoinURIParseException
     */
    private void setUri(CoinURI coinUri) throws CoinURIParseException {
        setAddress(coinUri.getAddress(), false);
        if (address == null) {
            // TODO when going to support the payment protocol, address could be null
            throw new CoinURIParseException("missing address");
        }
        Log.i(TAG, "setUri: ----- address :" + coinUri.getAddress()
                + "\n-----type :" + address.getType()
                + "\n-----amount :" + coinUri.getAmount()
        );
        sendAmountType = address.getType();
        sendAmount = coinUri.getAmount();
        final String label = coinUri.getLabel();
    }

    /*更新视图*/
    @Override
    public void updateView() {
        if (isRemoving() || isDetached()) return;

        //sendConfirmButton.setEnabled(everythingValid());

        if (address == null) {
            setVisible(sendToAddressView);
            setGone(sendToStaticAddressView);
            setVisible(scanQrCodeButton);
            Log.d(TAG, "updateView: scan Qr Code Visible");
            setGone(eraseAddressButton);

            finishActionMode();
        } else {
            //setGone(sendToAddressView);
            Log.i(TAG, "地址到了这里");
            setVisible(sendToStaticAddressView);
            sendToStaticAddressView.setAddressAndLabel(address);
            setGone(scanQrCodeButton);
            setVisible(eraseAddressButton);
        }

        if (sendCoinAmountView.resetType(sendAmountType)) {
            amountCalculatorLink.setExchangeRate(getCurrentRate());
        }

        startOrStopMarketRatePolling();

        // enable actions
        scanQrCodeButton.setEnabled(state == State.INPUT);
        eraseAddressButton.setEnabled(state == State.INPUT);

        /*scanQrCodeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.handleScan(v);
            }
        });*/
        CheatSheet.setup(scanQrCodeButton);
    }

    /**
     * 判断交易信息是否有效
     *
     * @return
     */
    private boolean isTxMessageValid() {
        return isTxMessageAdded && isTxMessageValid;
    }

    /**
     * 判断输出是否有效
     *
     * @return
     */
    private boolean isOutputsValid() {
        return address != null;
    }

    /**
     * 判断金额是否有效
     *
     * @return
     */
    private boolean isAmountValid() {
        return isAmountValid(sendAmount);
    }


    private boolean isAmountValid(Value amount) {
        return amount != null && isAmountWithinLimits(amount);
    }

    /**
     * Check if amount is within the minimum and maximum deposit limits and if is dust or if is more
     * money than currently in the wallet
     * 判断金额是否限制
     */
    private boolean isAmountWithinLimits(Value amount) {
        boolean isWithinLimits = amount != null && amount.isPositive() && !amount.isDust();
        Log.d(TAG, Constants.LOG_LABLE + "isAmountWithinLimits: isWithinLimits0 " + isWithinLimits);

        // Check if within min & max deposit limits
        if (isWithinLimits && marketInfo != null && canCompare(marketInfo.limit, amount)) {
            isWithinLimits = amount.within(marketInfo.minimum, marketInfo.limit);
            Log.d(TAG, Constants.LOG_LABLE + "isAmountWithinLimits: isWithinLimits1 " + isWithinLimits);
        }

        // Check if we have the amount
        if (isWithinLimits && canCompare(lastBalance, amount)) {
            isWithinLimits = amount.compareTo(lastBalance) <= 0;
            Log.d(TAG, Constants.LOG_LABLE + "isAmountWithinLimits: lastBalance " + lastBalance.type);
            Log.d(TAG, Constants.LOG_LABLE + "isAmountWithinLimits: amount " + amount.type);
            Log.d(TAG, Constants.LOG_LABLE + "isAmountWithinLimits: isWithinLimits2 " + isWithinLimits);
        }
        return isWithinLimits;
    }

    /**
     * Check if amount is smaller than the dust limit or if applicable, the minimum deposit.
     * 判断金额是否不足
     */
    private boolean isAmountTooSmall(Value amount) {
        return amount.compareTo(getLowestAmount(amount.type)) < 0;
    }

    /**
     * Get the lowest deposit or withdraw for the provided amount type
     * 获取最低金额
     */
    private Value getLowestAmount(ValueType type) {
        Value min = type.getMinNonDust();
        if (marketInfo != null) {
            if (marketInfo.minimum.isOfType(min)) {
                min = Value.max(marketInfo.minimum, min);
            } else if (marketInfo.rate.canConvert(type, marketInfo.minimum.type)) {
                min = Value.max(marketInfo.rate.convert(marketInfo.minimum), min);
            }
        }
        return min;
    }


    /**
     * 校验
     *
     * @return
     */
    private boolean everythingValid() {
        return state == State.INPUT && isOutputsValid() && isAmountValid() &&
                (!isTxMessageAdded || isTxMessageValid());
    }

    /**
     * 请求第一焦点
     */
    private void requestFocusFirst() {
        if (!isOutputsValid()) {
            sendToAddressView.requestFocus();
        } else if (!isAmountValid()) {
            amountCalculatorLink.requestFocus();
            // FIXME causes problems in older Androids
//            Keyboard.focusAndShowKeyboard(sendAmountView, getActivity());
        } else if (isTxMessageAdded && !isTxMessageValid()) {
            txMessageView.requestFocus();
        } else if (everythingValid()) {
            sendConfirmButton.requestFocus();
        } else {
            log.warn("unclear focus");
        }
    }

    /**
     * 校验地址、金额、交易信息
     */
    private void validateEverything() {
        Log.d(TAG, Constants.LOG_LABLE + "validateEverything: ");
        validateAddress();
        validateAmount();
        validateTxMessage();
    }

    /**
     * 校验交易信息
     */
    private void validateTxMessage() {
        if (isTxMessageAdded && messageFactory != null && txMessageView != null) {
            int messageBytes = txMessageView.getText().toString().getBytes(Charsets.UTF_8).length;
            isTxMessageValid = messageBytes <= messageFactory.maxMessageSizeBytes();
            updateView();
        }
    }

    /**
     * 校验交易金额
     */
    private void validateAmount() {
        Log.i(TAG, Constants.LOG_LABLE + "到这里了  校验金额");
        validateAmount(false);
    }

    private void validateAmount(boolean isTyping) {
        Value amountParsed = amountCalculatorLink.getPrimaryAmount();

        Log.d(TAG, Constants.LOG_LABLE + "validateAmount: lastBalance " + lastBalance);
        Log.d(TAG, Constants.LOG_LABLE + "validateAmount: amountParsed " + amountParsed);
        Log.d(TAG, Constants.LOG_LABLE + "validateAmount: isAmountValid " + isAmountValid(amountParsed));

        if (isAmountValid(amountParsed)) {

            Log.i(TAG, Constants.LOG_LABLE + "validateAmount  有效 ");
            sendAmount = amountParsed;
            amountError.setVisibility(View.GONE);
            Log.i(TAG, Constants.LOG_LABLE + "sendAmount  type: " + sendAmount.type.getName());

            if (sendAmount.type.getName().equals("Ethereum")) {
                Log.i(TAG, Constants.LOG_LABLE + "validateAmount:ETH  " + sendAmount.getBigValue());
            }
            // Show warning that fees apply when entered the full amount inside the account
            if (canCompare(sendAmount, lastBalance) && sendAmount.compareTo(lastBalance) == 0) {
                amountWarning.setText(R.string.amount_warn_fees_apply);
                amountWarning.setVisibility(View.VISIBLE);
            } else {
                amountWarning.setVisibility(View.GONE);
            }


        } else {
            amountWarning.setVisibility(View.GONE);
            // ignore printing errors for null and zero amounts
            Log.i(TAG, Constants.LOG_LABLE + "校验金额无效");
            Log.d(TAG, Constants.LOG_LABLE + "validateAmount: shouldShowErrors " + shouldShowErrors(isTyping, amountParsed));
            if (shouldShowErrors(isTyping, amountParsed)) {
                sendAmount = null;
                if (amountParsed == null) {
                    amountError.setText(R.string.amount_error);
                } else if (amountParsed.isNegative()) {
                    amountError.setText(R.string.amount_error_negative);
                } else if (!isAmountWithinLimits(amountParsed)) {
                    String message = getString(R.string.error_generic);
                    // If the amount is dust or lower than the deposit limit
                    if (isAmountTooSmall(amountParsed)) {
                        Log.d(TAG, Constants.LOG_LABLE + "validateAmount: isAmountTooSmall " + isAmountTooSmall(amountParsed));
                        Value minAmount = getLowestAmount(amountParsed.type);
                        message = getString(R.string.amount_error_too_small,
                                minAmount.toFriendlyString());
                    } else {
                        // If we have the amount
                        if (canCompare(lastBalance, amountParsed) &&
                                amountParsed.compareTo(lastBalance) > 0) {
                            Log.d(TAG, Constants.LOG_LABLE + "validateAmount: cancompare " + canCompare(lastBalance, amountParsed));
                            Log.d(TAG, Constants.LOG_LABLE + "validateAmount: compareTo " + amountParsed.compareTo(lastBalance));
                            message = getString(R.string.amount_error_not_enough_money,
                                    lastBalance.toFriendlyString());
                        }

                        if (marketInfo != null && canCompare(marketInfo.limit, amountParsed) &&
                                amountParsed.compareTo(marketInfo.limit) > 0) {
                            message = getString(R.string.trade_error_max_limit,
                                    marketInfo.limit.toFriendlyString());
                        }
                    }
                    amountError.setText(message);
                } else { // Should not happen, but show a generic error
                    amountError.setText(R.string.amount_error);
                }
                amountError.setVisibility(View.VISIBLE);
            } else {
                amountError.setVisibility(View.GONE);
            }
        }
        updateView();
    }

    /**
     * Decide if should show errors in the UI.
     * 是否显示错误信息
     */
    private boolean shouldShowErrors(boolean isTyping, Value amount) {
        Log.d(TAG, Constants.LOG_LABLE + "shouldShowErrors: isTyping " + isTyping);
        Log.d(TAG, Constants.LOG_LABLE + "shouldShowErrors: amount " + amount);


        if (amount != null && !amount.isZero() && !isAmountWithinLimits(amount)) {
            return true;
        }

        if (isTyping) return false;
        if (amountCalculatorLink.isEmpty()) return false;
        if (amount != null && amount.isZero()) return false;

        return true;
    }

    /**
     * 校验地址
     */
    private void validateAddress() {
        Log.i(TAG, "验证地址");
        validateAddress(true);

    }

    private void validateAddress(boolean isTyping) {
        if (address == null) {
            Log.i(TAG, "地址为空");
            String input = sendToAddressView.getText().toString().trim();
            try {
                if (!input.isEmpty()) {
                    if (account.getCoinType() instanceof EthereumFamily) {
                        //TODO validate Ethereum address
                        if (processInput(input)) return;
                        parseAddress(GenericUtils.fixAddress(input));
                        updateView();
                        addressError.setVisibility(View.GONE);
                        return;
                    } else if (account.getCoinType() instanceof AndaFamily) {
                        if (processInput(input)) return;
                        parseAddress(GenericUtils.fixAddress(input));
                        updateView();
                        addressError.setVisibility(View.GONE);
                        return;
                    }

                    Log.d(TAG, "validateAddress: -----  input :" + input);

                    // Process fast the input string
                    if (processInput(input)) return;

                    Log.d(TAG, "validateAddress: ----- input  complete");

                    // Try to fix address if needed
                    parseAddress(GenericUtils.fixAddress(input));
                } else {
                    // empty field should not raise error message
                    clearAddress(false);
                }
                addressError.setVisibility(View.GONE);
            } catch (final AddressMalformedException x) {
                // could not decode address at all
                if (!isTyping) {
                    clearAddress(false);
                    addressError.setText(R.string.address_error);
                    addressError.setVisibility(View.VISIBLE);
                }
            }
            updateView();
        }
    }

    /**
     * 设置发送地址
     *
     * @param addressStr
     */
    private void setSendToAddressText(String addressStr) {
        // Remove listener before changing input, to avoid infinite recursion
        sendToAddressView.removeTextChangedListener(receivingAddressListener);
        sendToAddressView.setOnFocusChangeListener(null);
        sendToAddressView.setText(addressStr);
        Log.i(TAG, "地址----------" + addressStr);
        sendToAddressView.addTextChangedListener(receivingAddressListener);
        sendToAddressView.setOnFocusChangeListener(receivingAddressListener);
    }

    /**
     * 地址转换
     *
     * @param addressStr
     * @throws AddressMalformedException
     */
    private void parseAddress(String addressStr) throws AddressMalformedException {
        //检测币类型
        if (account.getCoinType() instanceof EthereumFamily) {
            Log.i(TAG, "parseAddress:   ---------  EthereumFamily   ");
            //地址格式检测
            if (addressStr != null && addressStr.length() == 42 && addressStr.substring(0, 2).equals("0x")) {
                setAddress(account.getCoinType().newAddress(addressStr), true);
                sendAmountType = account.getCoinType();
                return;
            } else {
                throw new AddressMalformedException("Unsupported address: " + addressStr);
            }
        } else if (account.getCoinType() instanceof AndaFamily) {
            Log.i(TAG, "parseAddress:   ---------  AndaFamily   ");
            if (addressStr != null && addressStr.length() == 42 && addressStr.substring(0, 2).equals("0x")) {
                setAddress(account.getCoinType().newAddress(addressStr), true);
                sendAmountType = account.getCoinType();
                return;
            } else {
                throw new AddressMalformedException("Unsupported address: " + addressStr);
            }
        } else if (account.getCoinType() instanceof RippleFamily) {
            Log.i(TAG, "parseAddress:   ---------  RippleFamily   ");
            if (addressStr != null
                    && addressStr.length() >= 25 && addressStr.length() <= 35
                    && addressStr.startsWith("r")
                    && !addressStr.contains("0") && !addressStr.contains("O")
                    && !addressStr.contains("I") && !addressStr.contains("l")
                    ) {
                setAddress(account.getCoinType().newAddress(addressStr), true);
                sendAmountType = account.getCoinType();
                return;
            } else {
                throw new AddressMalformedException("Unsupported address: " + addressStr);
            }
        }
        List<CoinType> possibleTypes = GenericUtils.getPossibleTypes(addressStr);

        if (possibleTypes.size() == 1) {
            setAddress(possibleTypes.get(0).newAddress(addressStr), true);
            sendAmountType = possibleTypes.get(0);
        } else {
            // This address string could be more that one coin type so first check if this address
            // comes from an account to determine the type.
            List<WalletAccount> possibleAccounts = application.getAccounts(possibleTypes);
            AbstractAddress addressOfAccount = null;
            for (WalletAccount account : possibleAccounts) {
                AbstractAddress testAddress = account.getCoinType().newAddress(addressStr);
                if (account.isAddressMine(testAddress)) {
                    addressOfAccount = testAddress;
                    break;
                }
            }

            if (addressOfAccount != null) {
                // If address is from another account don't show a dialog. The type should not
                // change as we know 100% that is correct one.
                setAddress(addressOfAccount, false);
                sendAmountType = addressOfAccount.getType();
            } else {
                // As a last resort let the use choose the correct coin type
                if (listener != null) listener.showPayToDialog(addressStr);
            }
        }
    }

    /**
     * 地址选择
     *
     * @param selectedAddress
     */
    public void onAddressSelected(AbstractAddress selectedAddress) {
        setAddress(selectedAddress, true);
        sendAmountType = selectedAddress.getType();
        updateView();
    }

    /**
     * 从空钱包设置金额
     */
    private void setAmountForEmptyWallet() {
        updateBalance();
        if (state != State.INPUT || account == null || lastBalance == null) return;

        if (lastBalance.isZero()) {
            String message = getResources().getString(R.string.amount_error_not_enough_money_plain);
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        } else {
            amountCalculatorLink.setPrimaryAmount(lastBalance);
            Log.d(TAG, Constants.LOG_LABLE + "setAmountForEmptyWallet: ");
            validateAmount();
        }
    }

    /**
     * 项目选择
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_empty_wallet:
                setAmountForEmptyWallet();
                return true;
            default:
                // Not one of ours. Perform default menu processing
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        try {
            this.listener = (Listener) context;
            this.application = (WalletApplication) context.getApplicationContext();
            this.config = application.getConfiguration();
            this.resolver = context.getContentResolver();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + Listener.class);
        }
    }

    /**
     * 创建Activity
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(ID_RATE_LOADER, null, rateLoaderCallbacks);
        getLoaderManager().initLoader(ID_RECEIVING_ADDRESS_LOADER, null, receivingAddressLoaderCallbacks);
    }

    @Override
    public void onDetach() {
        getLoaderManager().destroyLoader(ID_RECEIVING_ADDRESS_LOADER);
        getLoaderManager().destroyLoader(ID_RATE_LOADER);

        listener = null;
        resolver = null;
        super.onDetach();
    }

    /**
     * 获取账户
     *
     * @return
     */
    @Override
    public WalletAccount getAccount() {
        return account;
    }

    /**
     * 监听接口
     */
    public interface Listener {
        void onTransactionBroadcastSuccess(WalletAccount pocket, Transaction transaction);

        void onTransactionBroadcastFailure(WalletAccount pocket, Transaction transaction);

        void showPayToDialog(String addressStr);
    }

    /**
     * 编辑视图监听
     */
    private abstract class EditViewListener implements View.OnFocusChangeListener, TextWatcher {
        @Override
        public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
        }

        @Override
        public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        }
    }

    /**
     * 地址点击
     */
    @OnClick(R.id.send_to_address_static)
//界面中隐藏
    void onStaticAddressClick() {
        if (address != null) {
            final boolean showChangeType = addressTypeCanChange &&
                    GenericUtils.hasMultipleTypes(address);
            ActionMode.Callback callback = new UiUtils.AddressActionModeCallback(
                    address, application.getApplicationContext(), getFragmentManager()) {

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.getMenuInflater().inflate(R.menu.address_options_extra, menu);
                    menu.findItem(R.id.action_change_address_type).setVisible(showChangeType);
                    return super.onCreateActionMode(mode, menu);
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.action_change_address_type:
                            if (listener != null) listener.showPayToDialog(getAddress().toString());
                            mode.finish();
                            return true;
                    }
                    return super.onActionItemClicked(mode, menuItem);
                }
            };
            actionMode = UiUtils.startActionMode(getActivity(), callback);
            // Hack to dismiss this action mode when back is pressed
            if (listener != null && listener instanceof WalletActivity) {
                ((WalletActivity) listener).registerActionMode(actionMode);
            }
        }
    }

    /**
     * 完成Action模式
     */
    private void finishActionMode() {
        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 若2秒之内接收地址没有发生变化，验证地址有效性
            validateAddress(true);
        }
    };


    private static final int MSG_SEARCH = 1;
    EditViewListener receivingAddressListener = new EditViewListener() {
        @Override
        public void onFocusChange(final View v, final boolean hasFocus) {
            if (!hasFocus) {
                validateAddress();
            }
        }

        /**
         * 文字变动以后
         * @param s
         */
        @Override
        public void afterTextChanged(final Editable s) {
            final String constraint = s.toString().trim();
            //文字变动 ， 有未发出的搜索请求，应取消
            if (mHandler.hasMessages(MSG_SEARCH)) {
                mHandler.removeMessages(MSG_SEARCH);
            }

            if (!constraint.isEmpty()) {
                //延迟2000ms开始验证
                mHandler.sendEmptyMessageDelayed(MSG_SEARCH, 2000);
                validateAddress();
            } else {
                //validateAddress(true);
            }

        }

    };

    /**
     * 余额编辑视图，监听
     */
    private final AmountEditView.Listener amountsListener = new AmountEditView.Listener() {
        @Override
        public void changed() {
            Log.d(TAG, Constants.LOG_LABLE + "changed: AmountEditView.Listener");
            validateAmount(true);
        }

        @Override
        public void focusChanged(final boolean hasFocus) {
            if (!hasFocus) {
                Log.d(TAG, Constants.LOG_LABLE + "focusChanged: AmountEditView.Listener");
                validateAmount();
            }
        }
    };

    private final LoaderCallbacks<Cursor> rateLoaderCallbacks = new LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
            String localSymbol = config.getExchangeCurrencyCode();
            return new ExchangeRateLoader(getActivity(), config, localSymbol);
        }

        @Override
        public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
            if (data != null && data.getCount() > 0) {
                HashMap<String, ExchangeRate> rates = new HashMap<>(data.getCount());
                data.moveToFirst();
                do {
                    ExchangeRatesProvider.ExchangeRate rate = ExchangeRatesProvider.getExchangeRate(data);
                    rates.put(rate.currencyCodeId, rate.rate);
                } while (data.moveToNext());
                handler.sendMessage(handler.obtainMessage(UPDATE_LOCAL_EXCHANGE_RATES, rates));
            }
        }

        @Override
        public void onLoaderReset(final Loader<Cursor> loader) {
        }
    };

    /**
     * 本地交换兑率更新
     *
     * @param rates
     */
    private void onLocalExchangeRatesUpdate(HashMap<String, ExchangeRate> rates) {
        localRates = rates;
        if (state == State.INPUT) {
            amountCalculatorLink.setExchangeRate(getCurrentRate());
        }
    }

    /**
     * Updates the exchange rate and limits for the specific market.
     * Note: if the current pair is different that the marketInfo pair, do nothing
     */
    private void onMarketUpdate(ShapeShiftMarketInfo marketInfo) {
        if (address != null && marketInfo.isPair(account.getCoinType(), address.getType())) {
            this.marketInfo = marketInfo;
        }
    }

    @Nullable
    private ExchangeRate getCurrentRate() {
        return localRates.get(sendAmountType.getSymbol());
    }

    /**
     * 钱包更新
     */
    private void onWalletUpdate() {
        updateBalance();
        Log.d(TAG, Constants.LOG_LABLE + "onWalletUpdate: ");
        validateAmount();
    }

    /**
     * 自定义handler，弱处理
     */
    private static class MyHandler extends WeakHandler<SendFragment> {
        public MyHandler(SendFragment referencingObject) {
            super(referencingObject);
        }

        @Override
        protected void weakHandleMessage(SendFragment ref, Message msg) {
            switch (msg.what) {
                case UPDATE_VIEW://视图更新
                    ref.updateView();
                    break;
                case UPDATE_LOCAL_EXCHANGE_RATES://更新汇率
                    ref.onLocalExchangeRatesUpdate((HashMap<String, ExchangeRate>) msg.obj);
                    break;
                case UPDATE_WALLET_CHANGE://更新钱包变化
                    ref.onWalletUpdate();
                    break;
                case UPDATE_MARKET://市场更新
                    ref.onMarketUpdate((ShapeShiftMarketInfo) msg.obj);
                    break;
                case SET_ADDRESS://设置地址
                    ref.onAddressSelected((AbstractAddress) msg.obj);
                    break;
            }
        }
    }

    private final ThrottlingWalletChangeListener transactionChangeListener = new ThrottlingWalletChangeListener() {
        @Override
        public void onThrottledWalletChanged() {
            handler.sendMessage(handler.obtainMessage(UPDATE_WALLET_CHANGE));
        }
    };

    private final LoaderCallbacks<Cursor> receivingAddressLoaderCallbacks = new LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
            final String constraint = args != null ? args.getString("constraint") : null;
            // TODO support addresses from other accounts
            Uri uri = AddressBookProvider.contentUri(application.getPackageName(), account.getCoinType());
            return new CursorLoader(application, uri, null, AddressBookProvider.SELECTION_QUERY,
                    new String[]{constraint != null ? constraint : ""}, null);
        }

        @Override
        public void onLoadFinished(final Loader<Cursor> cursor, final Cursor data) {
            if (sendToAdapter != null) sendToAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(final Loader<Cursor> cursor) {
            if (sendToAdapter != null) sendToAdapter.swapCursor(null);
        }
    };

    /**
     * 接收地址视图适配器
     */
    private final class ReceivingAddressViewAdapter extends CursorAdapter implements FilterQueryProvider {
        /**
         * 接收地址视图适配器
         *
         * @param context
         */
        public ReceivingAddressViewAdapter(final Context context) {
            super(context, null, false);
            setFilterQueryProvider(this);
        }

        /**
         * 新的视图
         *
         * @param context
         * @param cursor
         * @param parent
         * @return
         */
        @Override
        public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            return inflater.inflate(com.onets.wallet.R.layout.address_book_row, parent, false);
        }

        /**
         * 绑定视图
         *
         * @param view
         * @param context
         * @param cursor
         */
        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            final String label = cursor.getString(cursor.getColumnIndexOrThrow(AddressBookProvider.KEY_LABEL));
            final String coinId = cursor.getString(cursor.getColumnIndexOrThrow(AddressBookProvider.KEY_COIN_ID));
            final String addressStr = cursor.getString(cursor.getColumnIndexOrThrow(AddressBookProvider.KEY_ADDRESS));

            CoinType type = CoinID.typeFromId(coinId);

            final ViewGroup viewGroup = (ViewGroup) view;
            final TextView labelView = (TextView) viewGroup.findViewById(R.id.address_book_row_label);
            labelView.setText(label);
            final TextView addressView = (TextView) viewGroup.findViewById(R.id.address_book_row_address);
            try {
                //addressView.setText(GenericUtils.addressSplitToGroupsMultiline(type.newAddress(addressStr)));
                addressView.setText(type.newAddress(addressStr).toString());
            } catch (AddressMalformedException e) {
                ACRA.getErrorReporter().handleSilentException(e);
                addressView.setText(addressStr);
            }
        }

        /**
         * 转换为字符串
         *
         * @param cursor
         * @return
         */
        @Override
        public CharSequence convertToString(final Cursor cursor) {
            return cursor.getString(cursor.getColumnIndexOrThrow(AddressBookProvider.KEY_ADDRESS));
        }

        /**
         * 执行查询
         *
         * @param constraint
         * @return
         */
        @Override
        public Cursor runQuery(final CharSequence constraint) {
            final Bundle args = new Bundle();
            if (constraint != null)
                args.putString("constraint", constraint.toString());
            getLoaderManager().restartLoader(ID_RECEIVING_ADDRESS_LOADER, args, receivingAddressLoaderCallbacks);
            return getCursor();
        }
    }

    private static class MyMarketInfoPollTask extends MarketInfoPollTask {
        private final Handler handler;

        MyMarketInfoPollTask(Handler handler, ShapeShift shapeShift, String pair) {
            super(shapeShift, pair);
            this.handler = handler;
        }

        @Override
        public void onHandleMarketInfo(ShapeShiftMarketInfo marketInfo) {
            handler.sendMessage(handler.obtainMessage(UPDATE_MARKET, marketInfo));
        }
    }

    /**
     *
     */
    private static class AddressBookObserver extends ContentObserver {
        private final MyHandler handler;

        public AddressBookObserver(MyHandler handler) {
            super(handler);
            this.handler = handler;
        }

        @Override
        public void onChange(final boolean selfChange) {
            handler.sendEmptyMessage(UPDATE_VIEW);
        }

    }

    /**
     * 接受地址监听类 对发送时地址编辑状态添加监听事件
     * 在EditViewListener receivingAddressListener有使用
     */
    private final class ReceivingAddressListener implements View.OnFocusChangeListener, TextWatcher, AdapterView.OnItemClickListener {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            final String constraint = s.toString().trim();
            if (!constraint.isEmpty()) {
                validateAddress();
            } else {
                updateView();
            }
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                validateAddress();
                updateView();
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            final AddressBookEntry entry = receivingAddressViewAdapterBook.getItem(position);

        }
    }

    private final class ReceivingAddressViewAdapterBook extends ArrayAdapter<AddressBookEntry> {
        private final LayoutInflater inflater;

        public ReceivingAddressViewAdapterBook(final Context context) {
            super(context, 0);
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = inflater.inflate(R.layout.address_book_row, parent, false);
            }
            final AddressBookEntry entry = getItem(position);
            ((TextView) view.findViewById(R.id.address_book_row_label)).setText(entry.getLabel());
            ((TextView) view.findViewById(R.id.address_book_row_address)).setText(WalletUtils.formatHash(
                    entry.getAddress(), Constants.ADDRESS_FORMAT_GROUP_SIZE, Constants.ADDRESS_FORMAT_LINE_SIZE));
            return view;
        }
    }
}
