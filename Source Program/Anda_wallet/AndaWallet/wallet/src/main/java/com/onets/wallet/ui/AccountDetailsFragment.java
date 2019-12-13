package com.onets.wallet.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.onets.core.wallet.WalletAccount;
import com.onets.wallet.Constants;
import com.onets.wallet.R;
import com.onets.wallet.WalletApplication;
import com.onets.wallet.util.QrUtils;
import com.onets.wallet.util.UiUtils;

import static com.onets.core.Preconditions.checkNotNull;

/**
 * 账户详细信息，包含公钥及二维码图片
 * @author Yu K.Q.
 */
public class AccountDetailsFragment extends Fragment {
    public String publicKeySerialized;//公钥序列化

    /**
     * 账户细节信息实例
     * @param account
     * @return
     */
    public static AccountDetailsFragment newInstance(WalletAccount account) {
        AccountDetailsFragment fragment = new AccountDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.ARG_ACCOUNT_ID, account.getId());
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 账户细节信息构造
     */
    public AccountDetailsFragment() {}

    /**
     * 创建
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkNotNull(getArguments(), "Must provide arguments with an account id.");
        Log.d("AccountDetails", "getArguments: " + getArguments().getString(Constants.ARG_PRIVATE_KEY));
        //wallet application
        WalletApplication application = (WalletApplication) getActivity().getApplication();
        WalletAccount account = application.getAccount(getArguments().getString(Constants.ARG_ACCOUNT_ID));
        if (account == null) {
            Toast.makeText(getActivity(), R.string.no_such_pocket_error, Toast.LENGTH_LONG).show();
            getActivity().finish();
            return;
        }

        publicKeySerialized = account.getPublicKeySerialized();
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
        View view = inflater.inflate(R.layout.fragment_account_details, container, false);

        TextView publicKey = (TextView) view.findViewById(R.id.public_key);
        publicKey.setOnClickListener(getPubKeyOnClickListener());
        publicKey.setText(publicKeySerialized);

        ImageView qrView = (ImageView) view.findViewById(R.id.qr_code_public_key);
        QrUtils.setQr(qrView, getResources(), publicKeySerialized);

        return view;
    }

    /**
     * 获取公钥
     * @return
     */
    private View.OnClickListener getPubKeyOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                UiUtils.startCopyShareActionMode(publicKeySerialized, activity);
            }
        };
    }

    /**
     * 获取序列化的公钥
     * @return
     */
    public String getPublicKeySerialized() {
        return publicKeySerialized;
    }
}
