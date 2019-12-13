package com.onets.wallet.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.onets.core.coins.CoinType;
import com.onets.core.coins.FiatType;
import com.onets.core.coins.Value;
import com.onets.core.coins.families.AndaFamily;
import com.onets.core.coins.families.BitFamily;
import com.onets.core.coins.families.EthereumFamily;
import com.onets.core.coins.families.RippleFamily;
import com.onets.core.exceptions.UnsupportedCoinTypeException;
import com.onets.core.uri.CoinURI;
import com.onets.core.util.ExchangeRate;
import com.onets.core.util.GenericUtils;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.WalletAccount;
import com.onets.wallet.AddressBookProvider;
import com.onets.wallet.Configuration;
import com.onets.wallet.Constants;
import com.onets.wallet.ExchangeRatesProvider;
import com.onets.wallet.R;
import com.onets.wallet.WalletApplication;
import com.onets.wallet.ui.dialogs.CreateNewAddressDialog;
import com.onets.wallet.ui.widget.AmountEditView;
import com.onets.wallet.util.QrUtils;
import com.onets.wallet.util.ThrottlingWalletChangeListener;
import com.onets.wallet.util.UiUtils;
import com.onets.wallet.util.WeakHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.onets.core.Preconditions.checkNotNull;
import static com.onets.wallet.ExchangeRatesProvider.getRate;

/**
 * 接受界面fragment
 */
public class AddressRequestFragment extends WalletFragment {
    private static final Logger log = LoggerFactory.getLogger(AddressRequestFragment.class);
    private static final String TAG = "AddressRequestFragment";
    private Context context;

    private static final int UPDATE_VIEW = 0;//视图更新
    private static final int UPDATE_EXCHANGE_RATE = 1;//汇率更新

    // Loader IDs
    private static final int ID_RATE_LOADER = 0;//ID速率加载程序

    // Fragment tags 新地址标记
    private static final String NEW_ADDRESS_TAG = "new_address_tag";

    private CoinType type;//币类型

    @Nullable
    public AbstractAddress showAddress;//显示的接收地址
    public AbstractAddress receiveAddress;//获取的接受地址
    private Value amount;//值
    private String label;//标签
    private String accountId;//账户ID
    private WalletAccount account;//钱包账户
    private String message;//消息字符串

    @Bind(R.id.request_address_label)
    TextView addressLabelView;//地址标签
    @Bind(R.id.request_address)
    TextView addressView;//地址显示
    @Bind(R.id.request_coin_amount)
    AmountEditView sendCoinAmountView;//接收金额
    @Bind(R.id.view_previous_addresses)
    View previousAddressesLink;//以前的地址
    @Bind(R.id.qr_code)
    ImageView qrView;//二维码显示
    String lastQrContent;//最近一次二维码内容
    CurrencyCalculatorLink amountCalculatorLink;//数量计算
    ContentResolver resolver;

    private final MyHandler handler = new MyHandler(this);
    private final ContentObserver addressBookObserver = new AddressBookObserver(handler);
    private Configuration config;

    /**
     * 自定义Handler类
     */
    private static class MyHandler extends WeakHandler<AddressRequestFragment> {
        public MyHandler(AddressRequestFragment ref) {
            super(ref);
        }

        @Override
        protected void weakHandleMessage(AddressRequestFragment ref, Message msg) {
            switch (msg.what) {
                case UPDATE_VIEW://视图更新
                    ref.updateView();
                    break;
                case UPDATE_EXCHANGE_RATE://汇率更新
                    ref.updateExchangeRate((ExchangeRate) msg.obj);
                    break;
            }
        }
    }

    /**
     * 地址簿类
     */
    static class AddressBookObserver extends ContentObserver {
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
     * 接收界面实例
     * @param args
     * @return
     */
    public static AddressRequestFragment newInstance(Bundle args) {
        AddressRequestFragment fragment = new AddressRequestFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static AddressRequestFragment newInstance(String accountId) {
        return newInstance(accountId, null);
    }

    public static AddressRequestFragment newInstance(String accountId,
                                                     @Nullable AbstractAddress showAddress) {
        Bundle args = new Bundle();
        args.putString(Constants.ARG_ACCOUNT_ID, accountId);
        if (showAddress != null) {
            args.putSerializable(Constants.ARG_ADDRESS, showAddress);
        }
        return newInstance(args);
    }

    public AddressRequestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The onCreateOptionsMenu is handled in com.openwallet.wallet.ui.AccountFragment
        // or in com.openwallet.wallet.ui.PreviousAddressesActivity
        setHasOptionsMenu(true);

        WalletApplication walletApplication = (WalletApplication) getActivity().getApplication();
        Bundle args = getArguments();
        if (args != null) {
            accountId = args.getString(Constants.ARG_ACCOUNT_ID);
            Log.d(TAG, "地址查看: accountId " + accountId);
            if (args.containsKey(Constants.ARG_ADDRESS)) {
                showAddress = (AbstractAddress) args.getSerializable(Constants.ARG_ADDRESS);
                Log.d(TAG, "地址查看: showAddress1 " + showAddress);
            }
        }
        // TODO
        account = checkNotNull(walletApplication.getAccount(accountId));
        if (account == null) {
            Toast.makeText(getActivity(), R.string.no_such_pocket_error, Toast.LENGTH_LONG).show();
            return;
        }
        type = account.getCoinType();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        ButterKnife.bind(this, view);

        sendCoinAmountView.resetType(type, true);

        AmountEditView sendLocalAmountView = ButterKnife.findById(view, R.id.request_local_amount);
        sendLocalAmountView.setFormat(FiatType.FRIENDLY_FORMAT);

        amountCalculatorLink = new CurrencyCalculatorLink(sendCoinAmountView, sendLocalAmountView);

        return view;
    }

    @Override
    public void onViewStateRestored(@android.support.annotation.Nullable Bundle savedInstanceState) {
        ExchangeRatesProvider.ExchangeRate rate = getRate(getContext(), type.getSymbol(), config.getExchangeCurrencyCode());
        if (rate != null) {
            Log.i(TAG, "onLoadFinished:     exchangeRate");
            updateExchangeRate(rate.rate);
        }
        updateView();
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        amountCalculatorLink = null;
        lastQrContent = null;
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    /**
     * 点击地址，出现“编辑标签”和“复制地址”选项
     */
    @OnClick(R.id.request_address_view)
    public void onAddressClick() {
        if (showAddress != null) {
            receiveAddress = showAddress;
            Log.d(TAG, "地址查看: showAddress " + showAddress);
            Log.d(TAG, "地址查看: receiveAddress " + receiveAddress);

        }
        Activity activity = getActivity();
        ActionMode actionMode = UiUtils.startAddressActionMode(receiveAddress, activity,
                getFragmentManager());
        // Hack to dismiss this action mode when back is pressed
        if (activity != null && activity instanceof WalletActivity) {
            ((WalletActivity) activity).registerActionMode(actionMode);
        }
    }

    /**
     * 前一地址
     */
    @OnClick(R.id.view_previous_addresses)
    public void onPreviousAddressesClick() {
        Intent intent = new Intent(getActivity(), PreviousAddressesActivity.class);
        intent.putExtra(Constants.ARG_ACCOUNT_ID, accountId);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        account.addEventListener(walletListener);
        amountCalculatorLink.setListener(amountsListener);
        resolver.registerContentObserver(AddressBookProvider.contentUri(
                getActivity().getPackageName(), type), true, addressBookObserver);

        updateView();
    }

    @Override
    public void onPause() {
        resolver.unregisterContentObserver(addressBookObserver);
        amountCalculatorLink.setListener(null);
        account.removeEventListener(walletListener);
        walletListener.removeCallbacks();

        super.onPause();
    }

    /**
     * 选项事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                UiUtils.share(getActivity(), getUri());
                return true;
            case R.id.action_copy:
                UiUtils.copy(getActivity(), getUri());
                return true;
            case R.id.action_new_address:
                showNewAddressDialog();
                return true;
            case R.id.action_edit_label:
                EditAddressBookEntryFragment.edit(getFragmentManager(), type, receiveAddress);
                return true;
            default:
                // Not one of ours. Perform default menu processing
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        this.resolver = context.getContentResolver();
        this.config = ((WalletApplication) context.getApplicationContext()).getConfiguration();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(ID_RATE_LOADER, null, rateLoaderCallbacks);
    }

    @Override
    public void onDetach() {
        getLoaderManager().destroyLoader(ID_RATE_LOADER);
        resolver = null;
        super.onDetach();
    }

    private void showNewAddressDialog() {
        if (!isVisible() || !isResumed()) return;
        Dialogs.dismissAllowingStateLoss(getFragmentManager(), NEW_ADDRESS_TAG);
        DialogFragment dialog = CreateNewAddressDialog.getInstance(account);
        dialog.show(getFragmentManager(), NEW_ADDRESS_TAG);
    }

    private void updateExchangeRate(ExchangeRate exchangeRate) {
        amountCalculatorLink.setExchangeRate(exchangeRate);
    }

    @Override
    public void updateView() {
        if (isRemoving() || isDetached()) return;
        receiveAddress = null;
        if (showAddress != null) {
            receiveAddress = showAddress;
            Log.d(TAG, "地址查看 updateView: receiveSddress1 " + receiveAddress);
        } else {
            receiveAddress = account.getReceiveAddress();
            Log.d(TAG, "地址查看 updateView: receiveAddress " + receiveAddress);
        }

        // Don't show previous addresses link if we are showing a specific address
        if (showAddress == null && account.hasUsedAddresses()) {
            previousAddressesLink.setVisibility(View.VISIBLE);
        } else {
            previousAddressesLink.setVisibility(View.GONE);
        }

        // TODO, add message

        updateLabel();

        updateQrCode(getUri());
    }

    private String getUri() {
        if (type instanceof BitFamily) {
            return CoinURI.convertToCoinURI(receiveAddress, amount, label, message);
        } else if (type instanceof EthereumFamily) {
            return CoinURI.convertToCoinURI(receiveAddress, amount, label, message,
                    account.getPublicKeySerialized());
        } else if (type instanceof AndaFamily) {
            return CoinURI.convertToCoinURI(receiveAddress, amount, label, message,
                    account.getPublicKeySerialized());
        } else if (type instanceof RippleFamily) {
            return CoinURI.convertToCoinURI(receiveAddress, amount, label, message,
                    account.getPublicKeySerialized());
        } else {
            throw new UnsupportedCoinTypeException(type);
        }
    }

    /**
     * Update qr code if the content is different
     * 如果内容不同，更新二维码
     */
    private void updateQrCode(final String qrContent) {
        if (lastQrContent == null || !lastQrContent.equals(qrContent)) {
            QrUtils.setQr(qrView, getResources(), qrContent);
            lastQrContent = qrContent;
        }
    }

    /**
     * 更新标签
     */
    private void updateLabel() {
        label = resolveLabel(receiveAddress);
        if (label != null) {
            addressLabelView.setText(label);
            addressLabelView.setTypeface(Typeface.DEFAULT);
            addressView.setText(
                    GenericUtils.addressSplitToGroups(receiveAddress));
            Log.d(TAG, "地址查看 updateLabel: addressView " + addressView.getText().toString());
            addressView.setVisibility(View.VISIBLE);
        } else {
            //地址分栏分行
            /*addressLabelView.setText(
                    GenericUtils.addressSplitToGroupsMultiline(receiveAddress));*/
            if(account.getCoinType() instanceof AndaFamily){
                addressLabelView.setText("A0" + receiveAddress.toString());
            }else {
                addressLabelView.setText(receiveAddress.toString());
            }

            addressLabelView.setTypeface(Typeface.MONOSPACE);
            addressView.setVisibility(View.GONE);
        }
    }

    private final ThrottlingWalletChangeListener walletListener = new ThrottlingWalletChangeListener() {
        @Override
        public void onThrottledWalletChanged() {
            handler.sendEmptyMessage(UPDATE_VIEW);
        }
    };

    private String resolveLabel(@Nonnull final AbstractAddress address) {
        return AddressBookProvider.resolveLabel(getActivity(), address);
    }

    @Override
    public WalletAccount getAccount() {
        return account;
    }

    private final LoaderManager.LoaderCallbacks<Cursor> rateLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
            String localSymbol = config.getExchangeCurrencyCode();
            String coinSymbol = type.getSymbol();
            return new ExchangeRateLoader(getActivity(), config, localSymbol, coinSymbol);
        }


        @Override
        public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
            if (data != null && data.getCount() > 0) {
                data.moveToFirst();
                final ExchangeRatesProvider.ExchangeRate exchangeRate = ExchangeRatesProvider.getExchangeRate(data);

                Log.i(TAG, "onLoadFinished:     exchangeRate");
                handler.sendMessage(handler.obtainMessage(UPDATE_EXCHANGE_RATE, exchangeRate.rate));
            }
        }

        @Override
        public void onLoaderReset(final Loader<Cursor> loader) {
        }
    };

    /**
     * 金额监听
     */
    private final AmountEditView.Listener amountsListener = new AmountEditView.Listener() {
        boolean isValid(Value amount) {
            return amount != null && amount.isPositive()
                    && amount.compareTo(type.getMinNonDust()) >= 0;
        }

        void checkAndUpdateAmount() {
            Value amountParsed = amountCalculatorLink.getPrimaryAmount();
            if (isValid(amountParsed)) {
                amount = amountParsed;
            } else {
                amount = null;
            }
            updateView();
        }

        @Override
        public void changed() {
            checkAndUpdateAmount();
        }

        @Override
        public void focusChanged(final boolean hasFocus) {
            if (!hasFocus) {
                checkAndUpdateAmount();
            }
        }

    };

    /**
     * 获取TextView显示的地址，输出为字符串格式
     * @return
     */
    public String getAddress(){
        return addressView.getText().toString();
    }
}
