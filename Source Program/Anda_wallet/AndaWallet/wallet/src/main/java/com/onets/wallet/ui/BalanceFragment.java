package com.onets.wallet.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.util.GenericUtils;
import com.onets.core.wallet.AbstractTransaction;
import com.onets.core.wallet.AbstractWallet;
import com.onets.core.wallet.WalletAccount;
import com.onets.core.wallet.WalletConnectivityStatus;
import com.onets.core.wallet.families.andachain.AndaSendRequest;
import com.onets.test1.NioClient1;
import com.onets.wallet.AddressBookProvider;
import com.onets.wallet.Configuration;
import com.onets.wallet.Constants;
import com.onets.wallet.ExchangeRatesProvider;
import com.onets.wallet.ExchangeRatesProvider.ExchangeRate;
import com.onets.wallet.R;
import com.onets.wallet.WalletApplication;
import com.onets.wallet.ui.widget.Amount;
import com.onets.wallet.ui.widget.SwipeRefreshLayout;
import com.onets.wallet.util.HttpClientPost;
import com.onets.wallet.util.PhoneInfoUtils;
import com.onets.wallet.util.ThrottlingWalletChangeListener;
import com.onets.wallet.util.WeakHandler;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.utils.Threading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RejectedExecutionException;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 余额部分碎片
 * Use the {@link BalanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BalanceFragment extends WalletFragment implements LoaderCallbacks<List<AbstractTransaction>> {
    private static final Logger log = LoggerFactory.getLogger(BalanceFragment.class);

    private static final int WALLET_CHANGED = 0;
    private static final int UPDATE_VIEW = 1;//视图更新
    private static final int CLEAR_LABEL_CACHE = 2;//清除标签缓存

    private static final int AMOUNT_FULL_PRECISION = 8;//全精度
    private static final int AMOUNT_MEDIUM_PRECISION = 6;//中精度
    private static final int AMOUNT_SHORT_PRECISION = 4;//短精度
    private static final int AMOUNT_SHIFT = 0;//数量变化

    private static final int ID_TRANSACTION_LOADER = 0;
    private static final int ID_RATE_LOADER = 1;

    private String accountId;//账户ID
    private WalletAccount pocket;//钱包账户
    private CoinType type;//币类型
    private Coin currentBalance;//当前余额
    private Value eth_currentBalance;//以太坊当前余额
    private ExchangeRate exchangeRate;//汇率

    private boolean isFullAmount = true;
    private WalletApplication application;//钱包应用
    private Configuration config;//配置
    private final MyHandler handler = new MyHandler(this);
    private final ContentObserver addressBookObserver = new AddressBookObserver(handler);//地址簿

    @Bind(R.id.transaction_rows)
    ListView transactionRows;
    @Bind(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;//滑动容器
    @Bind(R.id.history_empty)
    TextView emptyPocketMessage;
    @Bind(R.id.account_balance)
    Amount accountBalance;
    @Bind(R.id.account_exchanged_balance)
    Amount accountExchangedBalance;
    @Bind(R.id.connection_label)
    TextView connectionLabel;//显示无法联网
    private TransactionsListAdapter adapter;
    private Listener listener;
    private ContentResolver resolver;

    private boolean isFirstUse;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param accountId of the account
     * @return A new instance of fragment InfoFragment.
     * 余额实例
     */
    public static BalanceFragment newInstance(String accountId) {
        BalanceFragment fragment = new BalanceFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.ARG_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }

    public BalanceFragment() {
        // Required empty public constructor
    }

    /**
     * 创建
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The onCreateOptionsMenu is handled in com.openwallet.wallet.ui.AccountFragment
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            accountId = getArguments().getString(Constants.ARG_ACCOUNT_ID);
            Log.d(TAG, Constants.LOG_LABLE + "accountId: --" + accountId);
        }

        pocket = application.getAccount(accountId);
        if (pocket == null) {//钱包为空
            Log.d(TAG, Constants.LOG_LABLE + "onCreate: pocket == null");
            Toast.makeText(getActivity(), R.string.no_such_pocket_error, Toast.LENGTH_LONG).show();
            return;
        }
        //钱包不为空，获取当前币的类型
        type = pocket.getCoinType();
        if (type.getName().equals("AndaBlockChain"))
        {
            //第一次运行，上传手机号和地址
            CommitAndaInfo();

            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void run() {
                    NioClient1 nioClient1 = new NioClient1();
                    String AndaAddress = pocket.getReceiveAddress().toString();
                    nioClient1.NioClientSocket(AndaAddress);
                }
            }).start();


        }
        Log.d(TAG, Constants.LOG_LABLE + "address: " + pocket.getReceiveAddress().toString());
    }

    /**
     * 第一次运行，上传手机号和地址
     */
    public void CommitAndaInfo(){
        //读取SharedPreferences中需要的数据
        SharedPreferences preferences = this.getActivity().getSharedPreferences("isFirstUse",Context.MODE_PRIVATE);
        Log.d(TAG, Constants.LOG_LABLE + "preferences:" + preferences);
        isFirstUse = preferences.getBoolean("isFirstUse",true);
        Log.d(TAG, Constants.LOG_LABLE + "isFirstUse:" + isFirstUse);

        //首次登陆上传手机号和安达钱包地址isFirstUse
        if (isFirstUse){
            String phoneNumber = new PhoneInfoUtils(getActivity()).getNativePhoneNumber();
            Log.d(TAG, Constants.LOG_LABLE + "phoneNumber: " + phoneNumber);
            String AndaAddress = pocket.getReceiveAddress().toString();
            String publicKey = pocket.getPublicKeySerialized();
            HttpClientPost httpClientPost = new HttpClientPost();
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    //发送账户信息,添加Mac的目的是以防同一台设备出现多次安装发币的情况
                    String macAddress = new PhoneInfoUtils(getActivity()).getAddressMAC(getActivity());
                    Log.d(TAG, Constants.LOG_LABLE + "macAddress: " + macAddress);
                    boolean b = httpClientPost.commitAccountInfoPost(AndaAddress, publicKey, "", macAddress);
                    //boolean b = httpClientPost.commitAccountInfoPost(AndaAddress, publicKey, "");
                    Log.d(TAG,Constants.LOG_LABLE + "判断地址信息是否上传成功:"+b);
                }
            }.start();

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    //此处上传手机号和安达地址到root项目
                    httpClientPost.getInfoServer(phoneNumber, AndaAddress);
                }
            };
            Timer timer = new Timer();
            timer.schedule(timerTask, 2000);//2秒后执行TimerTask的run方法
        }
        SharedPreferences.Editor editor = preferences.edit();
        Log.d(TAG, Constants.LOG_LABLE + "editor: " + editor);
        editor.putBoolean("isFirstUse",false);
        Log.d(TAG, Constants.LOG_LABLE + "editor: " + editor);
        editor.apply();
    }

    /**
     * 创建视图
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_balance, container, false);
        addHeaderAndFooterToList(inflater, container, view);
        ButterKnife.bind(this, view);

        setupSwipeContainer();

        // TODO show empty message
        // Hide empty message if have some transaction history 如果有一些交易历史，隐藏空消息
        if (pocket.getTransactions().size() > 0) {
            emptyPocketMessage.setVisibility(View.GONE);
        }

        setupAdapter(inflater);
        accountBalance.setSymbol(type.getSymbol());
        exchangeRate = ExchangeRatesProvider.getRate(
                application.getApplicationContext(), type.getSymbol(), config.getExchangeCurrencyCode());
        // Update the amount
        updateBalance(pocket.getBalance());

        return view;
    }

    /**
     * 销毁视图
     */
    @Override
    public void onDestroyView() {
        adapter = null;
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    private void setupAdapter(LayoutInflater inflater) {
        // Init list adapter
        adapter = new TransactionsListAdapter(inflater.getContext(), (AbstractWallet) pocket);
        adapter.setPrecision(AMOUNT_MEDIUM_PRECISION, 0);
        transactionRows.setAdapter(adapter);
    }

    /**
     * 设置滑动容器
     */
    private void setupSwipeContainer() {
        // Setup refresh listener which triggers new data loading 安装刷新侦听器，触发新数据加载
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (listener != null) {
                    listener.onRefresh();
                }
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                R.color.progress_bar_color_1,
                R.color.progress_bar_color_2,
                R.color.progress_bar_color_3,
                R.color.progress_bar_color_4);
    }

    private void addHeaderAndFooterToList(LayoutInflater inflater, ViewGroup container, View view) {
        ListView list = ButterKnife.findById(view, R.id.transaction_rows);

        // Initialize header
        View header = inflater.inflate(R.layout.fragment_balance_header, null);
        list.addHeaderView(header, null, true);

        // Set a space in the end of the list
        View listFooter = new View(inflater.getContext());
        listFooter.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin));
        list.addFooterView(listFooter);
    }

    /**
     * 设置连接状态---正式
     */
    private void setupConnectivityStatus() {
        // Set connected for now...
       if (new com.onets.core.network.ConnectivityHelperImpl().isConnected()){
           setConnectivityStatus(WalletConnectivityStatus.CONNECTED);
       }else{
           setConnectivityStatus(WalletConnectivityStatus.DISCONNECTED);
       }
        // ... but check the status in some seconds
       // handler.sendMessageDelayed(handler.obtainMessage(WALLET_CHANGED), 2000);
    }

    /**
     * 点击余额时
     */
    @OnClick(R.id.account_balance)
    public void onMainAmountClick() {
        isFullAmount = !isFullAmount;
        updateView();
    }

    /**
     * 余额本地点击
     */
    @OnClick(R.id.account_exchanged_balance)
    public void onLocalAmountClick() {
        if (listener != null) listener.onLocalAmountClick();
    }

    /**
     * 开始运行
     */
    @Override
    public void onStart() {
        super.onStart();
        setupConnectivityStatus();
    }

    /**
     * 停止
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    // TODO use the ListView feature that shows a view on empty list. Check exchange rates fragment

    /**
     * 检查空的Pocket消息
     */
    @Deprecated
    private void checkEmptyPocketMessage() {
        if (emptyPocketMessage.isShown()) {
            if (!pocket.isNew()) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        emptyPocketMessage.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    /**
     * 更新余额
     */
    private void updateBalance() {
        updateBalance(pocket.getBalance());
    }

    /**
     * 更新余额
     * @param newBalance
     */
    private void updateBalance(final Value newBalance) {

        //TODO 币类型判断

        currentBalance = newBalance.toBitCoin();
        eth_currentBalance = newBalance;
        Log.d(TAG, "updateBalance: -----2018926929eth_currentBalance"+eth_currentBalance);
        updateView();
    }

    /**
     * 更新连接状态
     */
    private void updateConnectivityStatus() {
        setConnectivityStatus(pocket.getConnectivityStatus());
    }

    /**
     * 设置连接状态
     * @param connectivity
     */
    private void setConnectivityStatus(final WalletConnectivityStatus connectivity) {
        switch (connectivity) {
            case CONNECTED:
                connectionLabel.setVisibility(View.GONE);
                break;
            case LOADING:
                connectionLabel.setVisibility(View.GONE);
                break;
            case DISCONNECTED:
                connectionLabel.setVisibility(View.VISIBLE);
                break;
            default:
                throw new RuntimeException("Unknown connectivity status: " + connectivity);
        }
    }

    private final ThrottlingWalletChangeListener walletChangeListener = new ThrottlingWalletChangeListener() {

        @Override
        public void onThrottledWalletChanged() {
            if (adapter != null) adapter.notifyDataSetChanged();
            handler.sendMessage(handler.obtainMessage(WALLET_CHANGED));
        }
    };

    /**
     * 触摸时候
     * @param context
     */
    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        try {
            listener = (Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass() + " must implement " + Listener.class);
        }
        resolver = context.getContentResolver();
        application = (WalletApplication) context.getApplicationContext();
        config = application.getConfiguration();
    }

    /**
     * Activity创建的时候
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(ID_TRANSACTION_LOADER, null, this);
        getLoaderManager().initLoader(ID_RATE_LOADER, null, rateLoaderCallbacks);
    }

    @Override
    public void onDetach() {
        getLoaderManager().destroyLoader(ID_TRANSACTION_LOADER);
        getLoaderManager().destroyLoader(ID_RATE_LOADER);
        listener = null;
        resolver = null;
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();

        resolver.registerContentObserver(AddressBookProvider.contentUri(
                getActivity().getPackageName(), type), true, addressBookObserver);

        pocket.addEventListener(walletChangeListener, Threading.SAME_THREAD);

        checkEmptyPocketMessage();

        updateView();
    }

    /**
     * 终止
     */
    @Override
    public void onPause() {
        pocket.removeEventListener(walletChangeListener);
        walletChangeListener.removeCallbacks();

        resolver.unregisterContentObserver(addressBookObserver);

        super.onPause();
    }

    /**
     * 创建加载器
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<List<AbstractTransaction>> onCreateLoader(int id, Bundle args) {
        return new AbstractTransactionsLoader(getActivity(), pocket);
    }

    /**
     * 加载完成
     * @param loader
     * @param transactions
     */
    @Override
    public void onLoadFinished(Loader<List<AbstractTransaction>> loader, final List<AbstractTransaction> transactions) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (adapter != null) {
//                    Log.i(TAG, "run:   transactions :  " + transactions.size());
                    adapter.replace(transactions);
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<List<AbstractTransaction>> loader) { /* ignore */ }

    /**
     * 获取Account
     * @return
     */
    @Override
    public WalletAccount getAccount() {
        return pocket;
    }

    /**
     * 交易加载--抽象类
     */
    private static class AbstractTransactionsLoader extends AsyncTaskLoader<List<AbstractTransaction>> {
        private final WalletAccount account;
        private final ThrottlingWalletChangeListener transactionAddRemoveListener;


        /**
         * 交易加载
         * @param context
         * @param account
         */
        private AbstractTransactionsLoader(final Context context, @Nonnull final WalletAccount account) {
            super(context);

            this.account = account;
            this.transactionAddRemoveListener = new ThrottlingWalletChangeListener() {
                @Override
                public void onThrottledWalletChanged() {
                    try {
                        forceLoad();
                    } catch (final RejectedExecutionException x) {
                        log.info("rejected execution: " + AbstractTransactionsLoader.this.toString());
                    }
                }
            };
        }

        /**
         * 开始加载
         */
        @Override
        protected void onStartLoading() {
            super.onStartLoading();

            account.addEventListener(transactionAddRemoveListener, Threading.SAME_THREAD);
            transactionAddRemoveListener.onWalletChanged(account); // trigger at least one reload

            forceLoad();
        }

        /**
         * 终止加载
         */
        @Override
        protected void onStopLoading() {
            account.removeEventListener(transactionAddRemoveListener);
            transactionAddRemoveListener.removeCallbacks();

            super.onStopLoading();
        }

        /**
         * 后台加载
         * @return
         */
        @Override
        public List<AbstractTransaction> loadInBackground() {
            final List<AbstractTransaction> filteredAbstractTransactions = Lists.newArrayList(account.getTransactions().values());
            Log.e("---------Transaction   ", "loadInBackground:  transaction  size : " + filteredAbstractTransactions.size());
            Collections.sort(filteredAbstractTransactions, TRANSACTION_COMPARATOR);

            return filteredAbstractTransactions;
        }

        private static final Comparator<AbstractTransaction> TRANSACTION_COMPARATOR = new Comparator<AbstractTransaction>() {
            @Override
            public int compare(final AbstractTransaction tx1, final AbstractTransaction tx2) {
                final boolean pending1 = tx1.getConfidenceType() == TransactionConfidence.ConfidenceType.PENDING;
                final boolean pending2 = tx2.getConfidenceType() == TransactionConfidence.ConfidenceType.PENDING;

                if (pending1 != pending2)
                    return pending1 ? -1 : 1;

                // TODO use dates once implemented
//                final Date updateTime1 = tx1.getUpdateTime();
//                final long time1 = updateTime1 != null ? updateTime1.getTime() : 0;
//                final Date updateTime2 = tx2.getUpdateTime();
//                final long time2 = updateTime2 != null ? updateTime2.getTime() : 0;

                // If both not pending
                if (!pending1 && !pending2) {
                    final int time1 = tx1.getAppearedAtChainHeight();
                    final int time2 = tx2.getAppearedAtChainHeight();
                    if (time1 != time2)
                        return time1 > time2 ? -1 : 1;
                }

                return Arrays.equals(tx1.getHashBytes(), tx2.getHashBytes()) ? 1 : -1;
            }
        };
    }

    private final LoaderCallbacks<Cursor> rateLoaderCallbacks = new LoaderCallbacks<Cursor>() {
        /**
         * 创建加载
         * @param id
         * @param args
         * @return
         */
        @Override
        public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
            String localSymbol = config.getExchangeCurrencyCode();
            String coinSymbol = type.getSymbol();
            return new ExchangeRateLoader(getActivity(), config, localSymbol, coinSymbol);
        }

        /**
         * 加载完成
         * @param loader
         * @param data
         */
        @Override
        public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
            if (data != null && data.getCount() > 0) {
                data.moveToFirst();
                exchangeRate = ExchangeRatesProvider.getExchangeRate(data);
                handler.sendEmptyMessage(UPDATE_VIEW);
                if (log.isInfoEnabled()) {
                    try {
                        log.info("Got exchange rate: {}",
                                exchangeRate.rate.convert(type.oneCoin()).toFriendlyString());
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(final Loader<Cursor> loader) {
        }
    };


    private String TAG = "----------BalanceFragment";

    /**
     * 更新视图
     */
    @Override
    public void updateView() {
        //检测状态
        if (isRemoving() || isDetached()) return;

        //定义默认余额
        String amount = "0";
        //检查账户余额是不是包含.
        if (accountBalance.getAmount().contains(".")) {
            int index = accountBalance.getAmount().indexOf(".");
            amount = accountBalance.getAmount().substring(0, index);
            Log.d(TAG, "测试updateView: ----2019926944amount: （dai .）"+amount);
        }else {
            amount = accountBalance.getAmount();
            Log.d(TAG, "测试updateView: ----2019926909amount: "+amount);
        }
        //当类型为Ripple时做的操作

        if (type.getName().equals("Ripple")) {

            //当余额为空检测
            if (accountBalance.getAmount() == null) {
                emptyPocketMessage.setText(getResources().getString(R.string.ripple_history_empty));
            } else if (Long.valueOf(amount) < 20) {
                emptyPocketMessage.setText(getResources().getString(R.string.ripple_history_empty));
            } else {
                emptyPocketMessage.setText(getResources().getString(R.string.history_empty));
            }
        }

        //检测币类型为安达链的时候
        if(type.getName().equals("AndaBlockChain")){
            /**
             * 币类型为安达的时候----
             */
            //检测余额不为空
            /*if(pocket.getBalance() != null){
                String newBalanceStr = GenericUtils.formatCoinValue(type,pocket.getBalance(),
                        isFullAmount? AMOUNT_FULL_PRECISION:AMOUNT_SHORT_PRECISION,AMOUNT_SHIFT);
                accountBalance.setAmount(newBalanceStr);
            }*/
            Log.d(TAG, "updateView: "+pocket.getReceiveAddress().toString());
            //余额显示
            String newBalanceStr = Double.toString(AndaSendRequest.to(pocket.getReceiveAddress().toString()));
            Log.d(TAG, "判断: -------pocket.getReceiveAddress() "+pocket.getReceiveAddress().toString());
            Log.d(TAG, "判断: -------newBalanceStr"+newBalanceStr);
            accountBalance.setAmount(newBalanceStr);

        }else {
            //检测币类型为以太坊时候
            if (type.getName().equals("Ethereum")) {
                /**
                 * 以太坊余额不为0
                 */

                Log.d(TAG, "eth_currentBalance compare: " + eth_currentBalance.getBigValue().compareTo(BigInteger.ZERO));
                Log.d(TAG, "updateView: eth_currentBalance"+eth_currentBalance);

                     //不为零的时候
                if (eth_currentBalance.getBigValue().compareTo(BigInteger.ZERO) != 0) {
                    String newBalanceStr = GenericUtils.formatCoinValueEth(type, eth_currentBalance,
                            isFullAmount ? AMOUNT_FULL_PRECISION : AMOUNT_SHORT_PRECISION, AMOUNT_SHIFT);
                    Log.d(TAG, "newBalanceStr: " + newBalanceStr);
                    accountBalance.setAmount(newBalanceStr);
                }else{
                    //为零的时候
                    accountBalance.setAmount(eth_currentBalance.toString());
                }
                /**
                 * 以太坊余额不为0 且交换税率不为空且getView不为空
                 */
                if (eth_currentBalance.getBigValue().compareTo(BigInteger.ZERO) != 0 && exchangeRate != null && getView() != null) {
                    try {
                        Value fiatAmount = exchangeRate.rate.convert(eth_currentBalance);
                        Log.i(TAG, "accountExchangedBalance:" + fiatAmount.getBigValue());
                        Log.i(TAG, "GenericUtils.formatFiatValue(fiatAmount):" + GenericUtils.formatFiatValue(fiatAmount));
                        accountExchangedBalance.setAmount(GenericUtils.formatFiatValue(fiatAmount));
                        accountExchangedBalance.setSymbol(fiatAmount.type.getSymbol());
                    } catch (Exception e) {
                        // Should not happen
                        accountExchangedBalance.setAmount("");
                        accountExchangedBalance.setSymbol("ERROR");
                    }
                }
            } else if (type.getName().equals("Bitcoin")){//比特币
                //当前余额不为空
                Log.d(TAG, "currentBalance: " + currentBalance);
                if (currentBalance != null) {
                    String newBalanceStr = GenericUtils.formatCoinValue(type, currentBalance,
                            isFullAmount ? AMOUNT_FULL_PRECISION : AMOUNT_SHORT_PRECISION, AMOUNT_SHIFT);
                    accountBalance.setAmount(newBalanceStr);
                }

                //当前余额不为空且交换税率不为空且getView不为空
                if (currentBalance != null && exchangeRate != null && getView() != null) {
                    try {
                        Value fiatAmount = exchangeRate.rate.convert(type, currentBalance);
                        accountExchangedBalance.setAmount(GenericUtils.formatFiatValue(fiatAmount));
                        accountExchangedBalance.setSymbol(fiatAmount.type.getSymbol());
                    } catch (Exception e) {
                        // Should not happen
                        accountExchangedBalance.setAmount("");
                        accountExchangedBalance.setSymbol("ERROR");
                    }
                }
            }
        }
        swipeContainer.setRefreshing(pocket.isLoading());
        //适配器不为空
        if (adapter != null) adapter.clearLabelCache();
    }

    /**
     * 清空标签缓存
     */
    private void clearLabelCache() {
        if (adapter != null) adapter.clearLabelCache();
    }

    private static class MyHandler extends WeakHandler<BalanceFragment> {
        public MyHandler(BalanceFragment ref) {
            super(ref);
        }

        @Override
        protected void weakHandleMessage(BalanceFragment ref, Message msg) {
            switch (msg.what) {
                case WALLET_CHANGED:
                    ref.updateBalance();
                    ref.checkEmptyPocketMessage();
                    ref.updateConnectivityStatus();
                    break;
                case UPDATE_VIEW:
                    ref.updateView();
                    break;
                case CLEAR_LABEL_CACHE:
                    ref.clearLabelCache();
                    break;
            }
        }
    }

    //地址薄
    static class AddressBookObserver extends ContentObserver {
        private final MyHandler handler;

        public AddressBookObserver(MyHandler handler) {
            super(handler);
            this.handler = handler;
        }

        @Override
        public void onChange(final boolean selfChange) {
            handler.sendEmptyMessage(CLEAR_LABEL_CACHE);
        }
    }

    /**
     * 监听接口
     */
    public interface Listener {
        void onLocalAmountClick();
        void onRefresh();
    }

}
