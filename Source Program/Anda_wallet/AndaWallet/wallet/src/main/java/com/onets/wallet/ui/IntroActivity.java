package com.onets.wallet.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.onets.wallet.R;

/**
 * Introduce Activity
 * 首次创建调用该类
 */
public class IntroActivity extends AbstractWalletFragmentActivity
        implements WelcomeFragment.Listener, PasswordConfirmationFragment.Listener,
        SetPasswordFragment.Listener, SelectCoinsFragment.Listener {
    private static final String TAG = "IntroActivity";

    Intent intent;
    String coin_choose;//choose coin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_wrapper);

        // If we detected that this device is incompatible
        //如果是设备不匹配，dialog显示并退出程序
        //否则，跳转到创建钱包界面
        if (!getWalletApplication().getConfiguration().isDeviceCompatible()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.incompatible_device_warning_title)//“设备不匹配”
                    .setMessage(R.string.incompatible_device_warning_message)//说明
                    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {//点击OK，退出程序
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .create().show();
        } else {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new WelcomeFragment())
                        .commit();
                intent = getIntent();
               coin_choose = intent.getStringExtra("SelectCoin");
            }
        }
    }

    /**
     * replace fragment
     * @param fragment
     */
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    /*创建新钱包*/
    @Override
    public void onCreateNewWallet() {
        Log.d(TAG, "onCreateNewWallet: ");
        if (getWalletApplication().getWallet() == null) {
            replaceFragment(new SeedFragment());
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.override_wallet_warning_title)
                    .setMessage(R.string.override_new_wallet_warning_message)
                    .setNegativeButton(R.string.button_cancel, null)
                    .setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            replaceFragment(new SeedFragment());
                        }
                    })
                    .create().show();
        }
    }

    /*覆盖现有钱包*/
    @Override
    public void onRestoreWallet() {
        if (getWalletApplication().getWallet() == null) {
            replaceFragment(RestoreFragment.newInstance());
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.override_wallet_warning_title)
                    .setMessage(R.string.override_restore_wallet_warning_message)
                    .setNegativeButton(R.string.button_cancel, null)
                    .setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            replaceFragment(RestoreFragment.newInstance());
                        }
                    })
                    .create().show();
        }
    }

    @Override
    public void onSeedCreated(String seed) {
        replaceFragment(RestoreFragment.newInstance(seed));
    }

    @Override
    public void onSeedVerified(Bundle args) {
        replaceFragment(SetPasswordFragment.newInstance(args));
    }

    @Override
    public void onPasswordConfirmed(Bundle args) {
        selectCoins(args);
    }

    @Override
    public void onPasswordSet(Bundle args) {
        args.putString("coin_choose",coin_choose);
        //todo:密码设置完成，添加coin
        selectCoins(args);
    }

    private void selectCoins(Bundle args) {
        String message = getResources().getString(R.string.select_coins);
        replaceFragment(SelectCoinsFragment.newInstance(message, true, args));
    }

    @Override
    public void onCoinSelection(Bundle args) {
        replaceFragment(FinalizeWalletRestorationFragment.newInstance(args));
    }
}
