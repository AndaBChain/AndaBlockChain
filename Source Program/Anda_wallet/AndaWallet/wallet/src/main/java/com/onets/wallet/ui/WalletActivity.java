package com.onets.wallet.ui;

import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.onets.core.coins.CoinType;
import com.onets.core.exceptions.AddressMalformedException;
import com.onets.core.uri.CoinURI;
import com.onets.core.uri.CoinURIParseException;
import com.onets.core.util.GenericUtils;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.SerializedKey;
import com.onets.core.wallet.WalletAccount;
import com.onets.wallet.Constants;
import com.onets.wallet.Fragment_Qianbao.WalletFunctionFragment;
import com.onets.wallet.Fragments.ChongzhiFragment;
import com.onets.wallet.Fragments.QianbaoFragment;
import com.onets.wallet.Fragments.ShengzhiFragment;
import com.onets.wallet.R;
import com.onets.wallet.service.CoinService;
import com.onets.wallet.service.CoinServiceImpl;
import com.onets.wallet.tasks.CheckUpdateTask;
import com.onets.wallet.ui.dialogs.TermsOfUseDialog;
import com.onets.wallet.util.SystemUtils;
import com.onets.wallet.util.WeakHandler;

import org.bitcoinj.core.Transaction;
import org.litepal.LitePalApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.onets.wallet.ui.NavDrawerItemType.ITEM_COIN;
import static com.onets.wallet.ui.NavDrawerItemType.ITEM_HOME;
import static com.onets.wallet.ui.NavDrawerItemType.ITEM_OVERVIEW;
import static com.onets.wallet.ui.NavDrawerItemType.ITEM_SECTION_TITLE;
import static com.onets.wallet.ui.NavDrawerItemType.ITEM_TRADE;

/**
 * @author Yu K.Q.
 * @author Yu K.Q.
 * 钱包中心视图
 */
final public class WalletActivity extends BaseWalletActivity implements
        NavigationDrawerFragment.Listener,
        AccountFragment.Listener, OverviewFragment.Listener, SelectCoinTypeDialog.Listener,
        PayWithDialog.Listener, TermsOfUseDialog.Listener, WalletFunctionFragment.Listener {
    private static final Logger log = LoggerFactory.getLogger(WalletActivity.class);
    private static final String TAG = "WalletActivity";

    private static final int REQUEST_CODE_SCAN = 0;//扫码请求
    private static final int ADD_COIN = 1;//添加币

    private static final int TX_BROADCAST_OK = 0;//TX广播OK
    private static final int TX_BROADCAST_ERROR = 1;//TX广播错误
    private static final int SET_URI = 2;//设置uri
    private static final int OPEN_ACCOUNT = 3;//打开账户
    private static final int OPEN_OVERVIEW = 4;//概述
    private static final int PROCESS_URI = 5;

    private static final int OPEN_ETH_ACCOUNT = 6;

    // Fragment tags
    private static final String ACCOUNT_TAG = "account_tag";
    private static final String OVERVIEW_TAG = "overview_tag";
    private static final String PAY_TO_DIALOG_TAG = "pay_to_dialog_tag";
    private static final String PAY_WITH_DIALOG_TAG = "pay_with_dialog_tag";
    private static final String TERMS_OF_USE_TAG = "terms_of_use_tag";

    // Saved state variables
    private static final String OVERVIEW_VISIBLE = "overview_visible";

    private AnimatorSet enterAnimation;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private DrawerLayout drawerLayout;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;

    /**
     * 上一账户ID
     */
    @Nullable
    private String lastAccountId;
    /**
     * 连接币intenet
     */
    private Intent connectCoinIntent;
    /**
     * 连接所有的币intent
     */
    private Intent connectAllCoinIntent;
    private List<NavDrawerItem> navDrawerItems = new ArrayList<>();
    /**
     * 上次活动模式
     */
    private ActionMode lastActionMode;
    private final Handler handler = new MyHandler(this);
    private boolean isOverviewVisible;
    private OverviewFragment overviewFragment;
    /**
     * 账户Fragment
     */
    @Nullable
    private AccountFragment accountFragment;

    RadioGroup mRgRadios;
    RadioButton RadioButton1, RadioButton2, RadioButton3, RadioButton4;
    Bundle savedInstanceState;

    private WalletAccount account;
    private String eth_account;

    Fragment fragment1 = new ChongzhiFragment();//充值
    Fragment fragment3 = new QianbaoFragment();//钱包
    Fragment fragment4 = new ShengzhiFragment();//升值

    public WalletActivity() {
    }

    /**
     * 创建
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        LitePalApplication.initialize(this);

        if (getWalletApplication().getWallet() == null) {
            startIntro();
            finish();
            return;
        }

        if (savedInstanceState == null && !getConfiguration().getTermsAccepted()) {
            TermsOfUseDialog.newInstance().show(getFM(), TERMS_OF_USE_TAG);
        }

        lastAccountId = getWalletApplication().getConfiguration().getLastAccountId();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Create the overview and account fragments
        FragmentTransaction tr = getFM().beginTransaction();
        if (savedInstanceState == null) {

            checkAlerts();  //检查版本信息

            // Add overview fragment
            overviewFragment = OverviewFragment.getInstance();
            tr.add(R.id.contents, overviewFragment, OVERVIEW_TAG).hide(overviewFragment);

            tr.add(R.id.contents, fragment1).hide(fragment1);//充值
            tr.add(R.id.contents, fragment3).hide(fragment3);//钱包
            tr.add(R.id.contents, fragment4).hide(fragment4);//升值

            // When we have more than one account, show overview as default
            List<WalletAccount> accounts = getAllAccounts();
            if (accounts.size() > 1) {
                handler.sendMessage(handler.obtainMessage(OPEN_OVERVIEW));
            } else if (accounts.size() == 1) {
                handler.sendMessage(handler.obtainMessage(OPEN_ACCOUNT, accounts.get(0)));
            }

            // TODO：Else no accounts, how to handle this case?
        } else {
            isOverviewVisible = savedInstanceState.getBoolean(OVERVIEW_VISIBLE);
            overviewFragment = (OverviewFragment) getFM().findFragmentByTag(OVERVIEW_TAG);
            accountFragment = (AccountFragment) getFM().findFragmentByTag(ACCOUNT_TAG);

            if (isOverviewVisible || accountFragment == null) {
                tr.show(overviewFragment);
                if (accountFragment != null) tr.hide(accountFragment);
                setOverviewTitle();
            } else {
                tr.show(accountFragment).hide(overviewFragment);
                setAccountTitle(accountFragment.getAccount());
            }
        }
        tr.commit();

        // Setup navigation bar 侧面导航栏
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFM().findFragmentById(R.id.navigation_drawer);
        // Set up the drawer.
        createNavDrawerItems();
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                drawerLayout,
                navDrawerItems);


        mRgRadios = (RadioGroup) findViewById(R.id.rg_radios);
        RadioButton1 = (RadioButton) findViewById(R.id.chongzhi);
        RadioButton2 = (RadioButton) findViewById(R.id.rbtn_gwc);
        RadioButton3 = (RadioButton) findViewById(R.id.rbtn_me);
        RadioButton4 = (RadioButton) findViewById(R.id.shengzhi);


        mRgRadios.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                Log.d(TAG, Constants.LOG_LABLE + "onCheckedChanged: i:" + i
                        + "\nRadioButton1.getId():" + RadioButton1.getId()
                        + "\nRadioButton1.getId():" + RadioButton2.getId()
                        + "\nRadioButton1.getId():" + RadioButton3.getId()
                        + "\nRadioButton1.getId():" + RadioButton4.getId()

                );
                if (i == RadioButton1.getId()) {
                    FragmentManager fm = getFM();
                    FragmentTransaction ft = fm.beginTransaction();
                    if (accountFragment != null) ft.hide(accountFragment);
                    if (overviewFragment != null) ft.hide(overviewFragment);
                    ft.hide(fragment3);
                    ft.hide(fragment4);
                    ft.show(fragment1);
                    ft.commit();
                    title = getString(R.string.recharge);
                    isOverviewVisible = false;
                    Toast.makeText(WalletActivity.this, "选择对应的钱包币种进行兑换安达通证", Toast.LENGTH_LONG).show();
                    Log.d(TAG, Constants.LOG_LABLE + "onCheckedChanged: chongzhi " + Constants.ARG_COIN_TYPE);
                }
                if (i == RadioButton2.getId()) {
                    FragmentManager fm = getFM();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.hide(fragment1);
                    ft.hide(fragment3);
                    ft.hide(fragment4);

                    //添加钱币总览界面fragment
                    if (accountFragment != null) {
                        if (overviewFragment.isHidden() && accountFragment.isHidden()) {
                            ft.show(overviewFragment);
                            setOverviewTitle();
                        }
                    } else {
                        if (overviewFragment.isHidden()) {
                            ft.show(overviewFragment);
                            setOverviewTitle();
                        }
                    }

                    ft.commit();

                }
                if (i == RadioButton3.getId()) {
                    FragmentManager fm = getFM();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.hide(fragment1);
                    ft.hide(fragment4);
                    if (accountFragment != null) ft.hide(accountFragment);
                    if (overviewFragment != null) ft.hide(overviewFragment);

                    ft.show(fragment3);
                    ft.commit();
                    title = getString(R.string.wallet);
                    isOverviewVisible = false;

                }
                if (i == RadioButton4.getId()) {
                    FragmentManager fm = getFM();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.hide(fragment1);
                    ft.hide(fragment3);
                    if (accountFragment != null) ft.hide(accountFragment);
                    if (overviewFragment != null) ft.hide(overviewFragment);

                    ft.show(fragment4);
                    //ft.add(R.id.contents, fragment4);
                    ft.commit();
                    title = getString(R.string.upvaluation);
                    isOverviewVisible = false;

                }

                restoreActionBar();
            }
        });


        //===============================================================

        Map<String,WalletAccount> accountId = new HashMap<>();
        List<WalletAccount> accounts = getAllAccounts();    //获取账户
        for (int i = 0; i < accounts.size(); i++) {
            WalletAccount account = accounts.get(i);
            accountId.put(account.getCoinType().getName(),account);
            Log.d(TAG, Constants.LOG_LABLE + "onCreate:  coinType: " +account.getCoinType().getName());
        }

        //Exchange - WalletActivity
        Intent intent = getIntent();
        String coin_type = intent.getStringExtra(Constants.ARG_COIN_TYPE);
        Log.d(TAG, Constants.LOG_LABLE + "Exchange-WalletActivity: coin_type " + coin_type);

        if(coin_type != null){
            //Wallet.loadFromFile()
            if(coin_type.equals("Bitcoin")){
                handler.sendMessage(handler.obtainMessage(OPEN_ACCOUNT, accountId.get("Bitcoin")));
                Log.d(TAG, Constants.LOG_LABLE + "Exchange-WalletActivity: Bitcoin");
            }else if(coin_type.equals("Ethereum")){
                handler.sendMessage(handler.obtainMessage(OPEN_ACCOUNT, accountId.get("Ethereum")));
                Log.d(TAG, Constants.LOG_LABLE + "Exchange-WalletActivity: Ethereum");
            }else if(coin_type.equals("AndaBlockChain")){
                handler.sendMessage(handler.obtainMessage(OPEN_ACCOUNT, accountId.get("AndaBlockChain")));
                Log.d(TAG, Constants.LOG_LABLE + "Exchange-WalletActivity: AndaBlockChain");
            }else if(coin_type.equals("Ripple")){
                handler.sendMessage(handler.obtainMessage(OPEN_ACCOUNT, accountId.get("Ripple")));
                Log.d(TAG, Constants.LOG_LABLE + "Exchange-WalletActivity: Ripple");
            }
        }

        if (intent.hasExtra(Constants.ARG_URI)) {
            handler.sendMessage(handler.obtainMessage(PROCESS_URI,
                    intent.getStringExtra(Constants.ARG_URI)));
            intent.removeExtra(Constants.ARG_URI);
        }

        int id = intent.getIntExtra("id",0);
        Log.d(TAG, Constants.LOG_LABLE + "Exchange-WalletActivity: id " + id);
        String AndaAddress = intent.getStringExtra("AndaAddress");
        if(id == 1 || id == 2 || id == 3){
            Fragment fragmen = new AccountFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.contents,fragmen);
            Intent i = new Intent();
            i.setClass(WalletActivity.this, AccountFragment.class);
            i.putExtra("id", id);
            i.putExtra("AndaAddress", AndaAddress);

        }

        //===============================================================
    }

    private void setOverviewTitle() {
        title = getResources().getString(R.string.title_activity_overview);
    }

    /**
     * 设置账户标题
     * @param account
     */
    private void setAccountTitle(@Nullable WalletAccount account) {
        if (account != null) {
            title = account.getDescriptionOrCoinName();
        } else {
            title = "";
        }
    }

    /**
     * 保存实例状态
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(OVERVIEW_VISIBLE, isOverviewVisible);
    }

    private void navDrawerSelectAccount(@Nullable WalletAccount account, boolean closeDrawer) {
        if (mNavigationDrawerFragment != null && account != null) {
            int position = 0;
            for (NavDrawerItem item : navDrawerItems) {
                if (item.itemType == ITEM_COIN && account.getId().equals(item.itemData)) {
                    mNavigationDrawerFragment.setSelectedItem(position, closeDrawer);
                    break;
                }
                position++;
            }
        }
    }

    private void navDrawerSelectOverview(boolean closeDrawer) {
        if (mNavigationDrawerFragment != null) {
            int position = 0;
            for (NavDrawerItem item : navDrawerItems) {
                if (item.itemType == ITEM_OVERVIEW) {
                    mNavigationDrawerFragment.setSelectedItem(position, closeDrawer);
                    break;
                }
                position++;
            }
        }
    }

    /**
     * overview界面侧边栏
     */
    private void createNavDrawerItems() {
        navDrawerItems.clear();
        /*NavDrawerItem.addItem(navDrawerItems, ITEM_SECTION_TITLE, getString(R.string.navigation_drawer_home));
        NavDrawerItem.addItem(navDrawerItems, ITEM_HOME, getString(R.string.title_activity_home), R.drawable.ic_home, null);*/
        NavDrawerItem.addItem(navDrawerItems, ITEM_SECTION_TITLE, getString(R.string.navigation_drawer_services));
        NavDrawerItem.addItem(navDrawerItems, ITEM_TRADE, getString(R.string.title_activity_trade), R.drawable.trade, null);
        NavDrawerItem.addItem(navDrawerItems, ITEM_SECTION_TITLE, getString(R.string.navigation_drawer_wallet));
        NavDrawerItem.addItem(navDrawerItems, ITEM_OVERVIEW, getString(R.string.title_activity_overview), R.drawable.ic_launcher, null);

        for (WalletAccount account : getAllAccounts()) {
            NavDrawerItem.addItem(navDrawerItems, ITEM_COIN, account.getDescriptionOrCoinName(),
                    Constants.COINS_ICONS.get(account.getCoinType()), account.getId());
            Log.d(TAG, Constants.LOG_LABLE + "WalletAccount  getDescriptionOrCoinName:" + account.getDescriptionOrCoinName()
                    + "    COINS_ICONS:" + Constants.COINS_ICONS.get(account.getCoinType()) + "      getId:" + account.getId());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWalletApplication().startBlockchainService(CoinService.ServiceMode.CANCEL_COINS_RECEIVED);
        connectAllCoinService();

        // Restore the correct action bar shadow
        if (getSupportActionBar() != null) {
            if (isOverviewVisible) {
                getSupportActionBar().setElevation(getResources().getDimensionPixelSize(R.dimen.active_elevation));
            } else {
                getSupportActionBar().setElevation(0);
            }
        }

    }


    /**
     * 点击本地余额
     */
    @Override
    public void onLocalAmountClick() {
        startExchangeRates();
    }

    /**
     * 刷新
     */
    @Override
    public void onRefresh() {
        refreshWallet();
    }

    /**
     * 广播交易成功
     * @param pocket
     * @param transaction
     */
    @Override
    public void onTransactionBroadcastSuccess(WalletAccount pocket, Transaction transaction) {
        handler.sendMessage(handler.obtainMessage(TX_BROADCAST_OK, transaction));
    }

    /**
     * 广播交易失败
     * @param pocket
     * @param transaction
     */
    @Override
    public void onTransactionBroadcastFailure(WalletAccount pocket, Transaction transaction) {
        handler.sendMessage(handler.obtainMessage(TX_BROADCAST_ERROR, transaction));
    }

    /**
     * 账户已经选择
     * @param accountId
     */
    @Override
    public void onAccountSelected(String accountId) {
        log.info(TAG + "  (String accountId)  Coin selected {}", accountId);
        openAccount(accountId);
    }

    /**
     * 账户已经选择
     * @param a
     */
    @Override
    public void onAccountSelected(int a) {
        log.info(TAG + " (int a)  Coin selected {}", a);
        //openAccount(a);

    }

    @Override
    public void onAddCoinsSelected() {
        startActivityForResult(new Intent(WalletActivity.this, AddCoinsActivity.class), ADD_COIN);
    }

    /**
     * 兑换选择
     */
    @Override
    public void onTradeSelected() {
        Log.d(TAG, Constants.LOG_LABLE + "----------------- 兑换");
        startActivity(new Intent(WalletActivity.this, TradeActivity.class));
        // Reselect the last item as the trade is a separate activity
        if (isOverviewVisible) {
            navDrawerSelectOverview(true);
        } else {
            navDrawerSelectAccount(getAccount(lastAccountId), true);
        }

//        FragmentTransaction ft = getFM().beginTransaction();
//        List<Fragment> list = getFM().getFragments();
//        Change_Fragment change_fragment = new Change_Fragment();
////        if (accountFragment != null) ft.hide(accountFragment);
////        if (overviewFragment != null) ft.hide(overviewFragment);
//
//        for (Fragment fragment : list) {
//            if (fragment instanceof Change_Fragment) {
//                ft.detach(fragment);
//            }
//        }
//        ft.add(R.id.contents, change_fragment);
//        //ft.add(android.R.id.content,change_fragment);
//        ft.commit();
    }

    @Override
    public void onOverviewSelected() {
        openOverview(false);
    }

    public void openOverview() {
        openOverview(true);
    }

    public void openOverview(boolean selectInNavDrawer) {
        if (!isOverviewVisible && !isFinishing()) {
            RadioButton2.setChecked(true);
            setOverviewTitle();
            FragmentTransaction ft = getFM().beginTransaction();
            ft.hide(fragment1);
            ft.hide(fragment3);
            ft.hide(fragment4);
            ft.show(overviewFragment);
            if (accountFragment != null) ft.hide(accountFragment);
            ft.commit();
            isOverviewVisible = true;
            connectAllCoinService();
            if (selectInNavDrawer) {
                navDrawerSelectOverview(true);
            }
            // Restore the default action bar shadow
            if (getSupportActionBar() != null) {
                getSupportActionBar().setElevation(
                        getResources().getDimensionPixelSize(R.dimen.active_elevation));
            }
        }
    }

    /**
     * 打开账户
     * @param account
     */
    private void openAccount(WalletAccount account) {
        openAccount(account, true);
    }

    private void openAccount(String accountId) {
        openAccount(getAccount(accountId), true);
    }

    private void openAccount(WalletAccount account, boolean selectInNavDrawer) {
        RadioButton2.setChecked(true);
        if (account != null && !isFinishing()) {
            if (isAccountVisible(account)) return;

            FragmentTransaction ft = getFM().beginTransaction();
            ft.hide(overviewFragment);
            ft.hide(fragment1);
            ft.hide(fragment3);
            ft.hide(fragment4);
            // If this account fragment is hidden, show it
            if (accountFragment != null && account.getId().equals(lastAccountId)) {
                ft.show(accountFragment);
            } else {
                // Else create a new fragment for the new account
                lastAccountId = account.getId();
                if (accountFragment != null) ft.remove(accountFragment);
                accountFragment = AccountFragment.getInstance(lastAccountId);
                ft.add(R.id.contents, accountFragment, ACCOUNT_TAG);
                getWalletApplication().getConfiguration().touchLastAccountId(lastAccountId);
            }
            ft.commit();

            setAccountTitle(account);
            isOverviewVisible = false;

            connectCoinService(lastAccountId);
            if (selectInNavDrawer) {
                navDrawerSelectAccount(account, true);
            }
            // Hide the shadow of the action bar because the PagerTabStrip of the AccountFragment is visible
            if (getSupportActionBar() != null) {
                getSupportActionBar().setElevation(0);
            }
        }
    }

//    private void openAccount(int a) {
//        Log.e(TAG, "openAccount(int a)  "+a);
//        FragmentTransaction ft = getFM().beginTransaction();
//        Fragment fragment1 = new AnDaAccountFragment();
//        ETHAccountFragment ethAccountFragment = new ETHAccountFragment();
//        List<Fragment> list = getFM().getFragments();
//        Log.e(TAG, "Fragment Account List Size  : "+list.size());
//
//        if (a == 4) {
//            for (Fragment fragment : list) {
//                ft.remove(fragment);
//                if (fragment instanceof AnDaAccountFragment) {
//                    ft.add(R.id.contents, fragment1);
//                }
//            }
//
//            title = "安达币";
//        } else if (a == 5) {
//            for (Fragment fragment : list) {
//                ft.remove(fragment);
//                if (fragment instanceof ETHAccountFragment) {
//                    ft.add(R.id.contents, ethAccountFragment);
//                }
//            }
//
//            if (eth_account != null) {
//                Bundle args = new Bundle();
//                args.putString("account", eth_account);
//                ethAccountFragment.setArguments(args);
//            }
//
//            title = "Ethereum";
//        } else if (a == 6) {
//            for (Fragment fragment : list) {
//                if (fragment instanceof Change_Fragment||fragment instanceof AnDaAccountFragment || fragment instanceof ETHAccountFragment || fragment instanceof ChongzhiFragment || fragment instanceof QianbaoFragment || fragment instanceof ShengzhiFragment) {
//                    ft.remove(fragment);
//                }
//            }
//            //============================================
//            //ft.add(R.id.contents, accountFragment);
//            //============================================
//            title = "Bitcoin";
//        }
//        ft.commit();
//        //isOverviewVisible = false;
//
//    }


    /**
     * 判断账户是否可用
     * @param account
     * @return
     */
    private boolean isAccountVisible(WalletAccount account) {
        return account != null && accountFragment != null &&
                accountFragment.isVisible() && account.equals(accountFragment.getAccount());
    }

    /**
     * 连接币服务
     * @param accountId
     */
    private void connectCoinService(String accountId) {
        if (connectCoinIntent == null) {
            connectCoinIntent = new Intent(CoinService.ACTION_CONNECT_COIN, null,
                    getWalletApplication(), CoinServiceImpl.class);
        }
        // Open connection if needed or possible
        connectCoinIntent.putExtra(Constants.ARG_ACCOUNT_ID, accountId);
        getWalletApplication().startService(connectCoinIntent);
    }

    /**
     * 连接所有的币服务
     */
    private void connectAllCoinService() {
        if (connectAllCoinIntent == null) {
            connectAllCoinIntent = new Intent(CoinService.ACTION_CONNECT_ALL_COIN, null,
                    getWalletApplication(), CoinServiceImpl.class);
        }
        getWalletApplication().startService(connectAllCoinIntent);
    }

    /**
     * 恢复操作
     */
    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(title);
        }
    }

    /**
     * 检查版本信息
     */
    private void checkAlerts() {
        // If not store version, show update dialog if needed
        if (!SystemUtils.isStoreVersion(this)) {
            final PackageInfo packageInfo = getWalletApplication().packageInfo();
            new CheckUpdateTask() {
                @Override
                protected void onPostExecute(Integer serverVersionCode) {
                    if (serverVersionCode != null && serverVersionCode > packageInfo.versionCode) {
                        showUpdateDialog();
                    }
                }
            }.execute();
        }
    }

    //显示版本更新dialog
    private void showUpdateDialog() {

        final PackageManager pm = getPackageManager();
//        final Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Constants.MARKET_APP_URL, getPackageName())));
        final Intent binaryIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.BINARY_URL));

        final AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
        builder.setTitle(R.string.wallet_update_title);
        builder.setMessage(R.string.wallet_update_message);

        // Disable market link for now
//        if (pm.resolveActivity(marketIntent, 0) != null)
//        {
//            builder.setPositiveButton("Play Store", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(final DialogInterface dialog, final int id) {
//                    startActivity(marketIntent);
//                    finish();
//                }
//            });
//        }

        if (pm.resolveActivity(binaryIntent, 0) != null) {
            builder.setPositiveButton(R.string.button_download, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int id) {
                    startActivity(binaryIntent);
                    finish();
                }
            });
        }

        builder.setNegativeButton(R.string.button_dismiss, null);
        builder.create().show();
    }

    /**
     * Activity结果
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (requestCode == REQUEST_CODE_SCAN) {
                    if (resultCode == Activity.RESULT_OK) {
                        try {
                            processInput(intent.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT));
                        } catch (final Exception e) {
                            showScanFailedMessage(e);
                        }
                    }
                } else if (requestCode == ADD_COIN) {
                    if (resultCode == Activity.RESULT_OK) {
                        final String accountId = intent.getStringExtra(Constants.ARG_ACCOUNT_ID);
                        createNavDrawerItems();
                        mNavigationDrawerFragment.setItems(navDrawerItems);
                        openAccount(accountId);
                    }
                }
            }
        });
    }

    /**
     * 展示扫描失败的消息
     * @param e
     */
    private void showScanFailedMessage(Exception e) {
        String error = getResources().getString(R.string.scan_error, e.getMessage());
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    /**
     * 进程输入
     * @param input
     * @throws CoinURIParseException
     * @throws AddressMalformedException
     */
    private void processInput(String input) throws CoinURIParseException, AddressMalformedException {
        input = input.trim();
        try {
            processUri(input);
        } catch (final CoinURIParseException x) {
            if (SerializedKey.isSerializedKey(input)) {
                sweepWallet(input);
            } else {
                processAddress(input);
            }
        }
    }

    /**
     * URI进程
     * @param input
     * @throws CoinURIParseException
     */
    private void processUri(String input) throws CoinURIParseException {
        Log.e(TAG, Constants.LOG_LABLE + "processUri: " + input);
        CoinURI coinUri = new CoinURI(input);
        CoinType scannedType = coinUri.getTypeRequired();

        if (!Constants.SUPPORTED_COINS.contains(scannedType)) {
            String error = getResources().getString(R.string.unsupported_coin, scannedType.getName());
            throw new CoinURIParseException(error);
        }

        if (accountFragment != null && accountFragment.isVisible() && accountFragment.getAccount() != null) {
            payWith(accountFragment.getAccount(), coinUri);
            return;
        }

        WalletAccount selectedAccount = null;
        List<WalletAccount> allAccounts = getAllAccounts();
        List<WalletAccount> sendFromAccounts = getAccounts(scannedType);
        if (sendFromAccounts.size() == 1) {
            selectedAccount = sendFromAccounts.get(0);
        } else if (allAccounts.size() == 1) {
            selectedAccount = allAccounts.get(0);
        }

        // TODO rework the address request standard
//        if (coinUri.isAddressRequest() && selectedAccount != null) {
//            UiUtils.replyAddressRequest(this, coinUri, selectedAccount);
//            return;
//        }

        if (selectedAccount != null) {
            payWith(selectedAccount, coinUri);
        } else {
            showPayWithDialog(coinUri);
        }
    }

    /**
     * 地址进程
     * @param addressStr
     * @throws CoinURIParseException
     * @throws AddressMalformedException
     */
    private void processAddress(String addressStr) throws CoinURIParseException, AddressMalformedException {
        List<CoinType> possibleTypes = GenericUtils.getPossibleTypes(addressStr);

        if (possibleTypes.size() == 1) {
            AbstractAddress address = possibleTypes.get(0).newAddress(addressStr);
            processUri(CoinURI.convertToCoinURI(address, null, null, null));
        } else {
            // This address string could be more that one coin type so first check if this address
            // comes from an account to determine the type.
            List<WalletAccount> possibleAccounts = getAccounts(possibleTypes);
            AbstractAddress addressOfAccount = null;
            for (WalletAccount account : possibleAccounts) {
                AbstractAddress testAddress = account.getCoinType().newAddress(addressStr);
                if (account.isAddressMine(testAddress)) {
                    addressOfAccount = testAddress;
                    break;
                }
            }
            if (addressOfAccount != null) {
                // If address is from an account don't show a dialog.
                processUri(CoinURI.convertToCoinURI(addressOfAccount, null, null, null));
            } else {
                // As a last resort let the use choose the correct coin type
                showPayToDialog(addressStr);
            }
        }
    }

    public void showPayToDialog(String addressStr) {
        Dialogs.dismissAllowingStateLoss(getFM(), PAY_TO_DIALOG_TAG);
        SelectCoinTypeDialog.getInstance(addressStr).show(getFM(), PAY_TO_DIALOG_TAG);
    }

    /**
     * 地址类型已经选择
     * @param selectedAddress
     */
    @Override
    public void onAddressTypeSelected(AbstractAddress selectedAddress) {
        try {
            processUri(CoinURI.convertToCoinURI(selectedAddress, null, null, null));
        } catch (CoinURIParseException e) {
            showScanFailedMessage(e);
        }
    }

    private void showPayWithDialog(CoinURI uri) {
        Dialogs.dismissAllowingStateLoss(getFM(), PAY_WITH_DIALOG_TAG);
        PayWithDialog.getInstance(uri).show(getFM(), PAY_WITH_DIALOG_TAG);
    }

    /**
     *
     * @param account
     * @param coinUri
     */
    @Override
    public void payWith(final WalletAccount account, final CoinURI coinUri) {
        openAccount(account);
        // Set url asynchronously as the account may need to open
        handler.sendMessage(handler.obtainMessage(SET_URI, coinUri));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Only show items in the action bar relevant to this screen
        // if the drawer is not showing. Otherwise, let the drawer
        // decide what to show in the action bar.
        if (mNavigationDrawerFragment != null && !mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.global, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_settings:
                startActivity(new Intent(WalletActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_scan_qr_code:
                startActivityForResult(new Intent(this, ScanActivity.class), REQUEST_CODE_SCAN);
                /*final View sendQrButton = findViewById(R.id.action_scan_qr_code);
                sendQrButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        handleScan(v);
                    }
                });
                CheatSheet.setup(sendQrButton);*/
                return true;
            case R.id.action_refresh_wallet:
                refreshWallet();
                return true;
            case R.id.action_sign_verify_message:
                signVerifyMessage();
                return true;
            case R.id.action_account_details:
                accountDetails();
                return true;
            case R.id.action_sweep_wallet:
                sweepWallet(null);
                return true;
            case R.id.action_support:
                sendSupportEmail();
                return true;
            case R.id.action_about:
                startActivity(new Intent(WalletActivity.this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    /**
     * Email发送支持
     */
    private void sendSupportEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.SUPPORT_EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, "");
        try {
            startActivity(Intent.createChooser(intent,
                    getResources().getString(R.string.support_message)));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 开始交换--税率
     */
    void startExchangeRates() {
        WalletAccount account = getAccount(lastAccountId);
        if (account != null) {
            Intent intent = new Intent(this, ExchangeRatesActivity.class);
            intent.putExtra(Constants.ARG_COIN_ID, account.getCoinType().getId());
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_wallet_pocket_selected, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 签名验证消息
     */
    void signVerifyMessage() {
        if (isAccountExists(lastAccountId)) {
            Intent intent = new Intent(this, SignVerifyMessageActivity.class);
            intent.putExtra(Constants.ARG_ACCOUNT_ID, lastAccountId);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_wallet_pocket_selected, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 账户细节
     */
    void accountDetails() {
        if (isAccountExists(lastAccountId)) {
            Intent intent = new Intent(this, AccountDetailsActivity.class);
            intent.putExtra(Constants.ARG_ACCOUNT_ID, lastAccountId);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_wallet_pocket_selected, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 钱包sweep
     * @param key
     */
    void sweepWallet(@Nullable String key) {
        if (isAccountExists(lastAccountId)) {
            Intent intent = new Intent(this, SweepWalletActivity.class);
            intent.putExtra(Constants.ARG_ACCOUNT_ID, lastAccountId);
            if (key != null) intent.putExtra(Constants.ARG_PRIVATE_KEY, key);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_wallet_pocket_selected, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 刷新钱包
     */
    private void refreshWallet() {
        if (getWalletApplication().getWallet() != null) {
            Intent intent;
            if (isOverviewVisible) {
                intent = new Intent(CoinService.ACTION_RESET_WALLET, null,
                        getWalletApplication(), CoinServiceImpl.class);
            } else {
                intent = new Intent(CoinService.ACTION_RESET_ACCOUNT, null,
                        getWalletApplication(), CoinServiceImpl.class);
                intent.putExtra(Constants.ARG_ACCOUNT_ID, lastAccountId);
            }
            getWalletApplication().startService(intent);
        }
    }

    /**
     * 启动信息
     */
    private void startIntro() {
        Intent introIntent = new Intent(this, IntroActivity.class);
        startActivity(introIntent);
    }

    //重写返回键
    @Override
    public void onBackPressed() {
        /*if(SpvStroeDown.peerGroup != null){
            //spv停止程序
            SpvStroeDown.SpvDownStop();
        }*/

        finishActionMode();
        if (mNavigationDrawerFragment != null && mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.closeDrawer();
            return;
        }

        List<WalletAccount> accounts = getAllAccounts();

        if (accounts.size() > 1) {
            if (isOverviewVisible) {
                super.onBackPressed();
            } else {
                // If not in balance screen, back button brings us there
                boolean screenChanged = goToBalance();
                if (!screenChanged) {
                    // When in balance screen, it brings us to the overview
//                     openOverview();
                    super.onBackPressed();
                }
            }
        } else if (accounts.size() == 1) {
            if (accountFragment != null && accountFragment.isVisible()) {
                // If not in balance screen, back button brings us there
                boolean screenChanged = goToBalance();
                if (!screenChanged) {
                    // When in balance screen, exit
                    super.onBackPressed();
                }
            } else {
                openAccount(accounts.get(0));
            }
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 返回
     * @return
     */
    @SuppressWarnings("unused")
    private boolean goToReceive() {
        return accountFragment != null && accountFragment.isVisible() && accountFragment.goToReceive(true);
    }

    /**
     * 检测是否去余额
     * @return
     */
    private boolean goToBalance() {
        return accountFragment != null && accountFragment.isVisible() && accountFragment.goToBalance(true);
    }

    /**
     * 检测是否去发送
     * @return
     */
    private boolean goToSend() {
        return accountFragment != null && accountFragment.isVisible() && accountFragment.goToSend(true);
    }

    /**
     * 检测是否为重复发送
     * @return
     */
    private boolean resetSend() {
        return accountFragment != null && accountFragment.isVisible() && accountFragment.resetSend();
    }

    @Override
    public void registerActionMode(ActionMode actionMode) {
        finishActionMode();
        lastActionMode = actionMode;
    }

    /**
     * 接收选择
     */
    @Override
    public void onReceiveSelected() {
        finishActionMode();
    }

    /**
     * 余额选择
     */
    @Override
    public void onBalanceSelected() {
        finishActionMode();
    }

    /**
     * 发送选择
     */
    @Override
    public void onSendSelected() {
        finishActionMode();
    }


    private void finishActionMode() {
        if (lastActionMode != null) {
            lastActionMode.finish();
            lastActionMode = null;
        }
    }

    /**
     * 账户已经修改
     * @param account
     */
    @Override
    public void onAccountModified(WalletAccount account) {
        // Recreate items
        createNavDrawerItems();
        mNavigationDrawerFragment.setItems(navDrawerItems);
    }

    @Override
    public void onTermsAgree() {
        getConfiguration().setTermAccepted(true);
    }

    @Override
    public void onTermsDisagree() {
        getConfiguration().setTermAccepted(false);
        finish();
    }

    @Override
    public void selectTradeCoin() {
        drawerLayout.openDrawer(Gravity.START);
    }


    private static class MyHandler extends WeakHandler<WalletActivity> {
        public MyHandler(WalletActivity ref) {
            super(ref);
        }

        @Override
        protected void weakHandleMessage(WalletActivity ref, Message msg) {
            switch (msg.what) {
                case TX_BROADCAST_OK:
                    Toast.makeText(ref, ref.getString(R.string.sent_msg),
                            Toast.LENGTH_LONG).show();//已发送！等待确认中。
                    ref.goToBalance();
                    ref.resetSend();
                    break;
                    //不使用该Toast
                /*case TX_BROADCAST_ERROR:
                    Toast.makeText(ref, ref.getString(R.string.get_tx_broadcast_error),
                            Toast.LENGTH_LONG).show();//广播交易时出错
                    ref.goToSend();
                    break;*/
                case SET_URI:
                    if (ref.accountFragment == null) {
                        Toast.makeText(ref, ref.getString(R.string.no_wallet_pocket_selected),
                                Toast.LENGTH_LONG).show();//尚未选择证包帐号
                    }
                    ref.accountFragment.sendToUri((CoinURI) msg.obj);
                    break;
                case OPEN_ETH_ACCOUNT:
                    // ref.openAccount(5);
                    break;
                case OPEN_ACCOUNT:
                    ref.openAccount((WalletAccount) msg.obj);
                    break;
                case OPEN_OVERVIEW:
                    ref.openOverview();
                    break;
                case PROCESS_URI:
                    try {
                        ref.processUri((String) msg.obj);
                    } catch (CoinURIParseException e) {
                        ref.showScanFailedMessage(e);
                    }
                    break;
            }
        }
    }

    public void handleScan(final View clickView) {
        // The animation must be ended because of several graphical glitching that happens when the
        // Camera/SurfaceView is used while the animation is running.
        enterAnimation.end();
        ScanActivity.startForResult(this, clickView, WalletActivity.REQUEST_CODE_SCAN);
    }

    WalletAccount getAccount(){
        return account;
    }
}
