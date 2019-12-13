package com.onets.wallet.ui;

import android.support.v4.app.FragmentActivity;

import com.onets.wallet.WalletApplication;

/**
 * @author Yu K.Q.
 * 抽象钱包FramentActivity
 */
abstract public class AbstractWalletFragmentActivity extends FragmentActivity {

    /*get Wallet Application*/

    /**
     * 获取钱包应用
     * @return
     */
    protected WalletApplication getWalletApplication() {
        return (WalletApplication) getApplication();
    }

    /**
     * Resume
     */
    /*Resume activity*/
    @Override
    protected void onResume() {
        super.onResume();
        getWalletApplication().touchLastResume();
    }

    /**
     * 停止Activity
     */
    /*Stop activity*/
    @Override
    protected void onStop() {
        super.onStop();
        getWalletApplication().touchLastStop();
    }
}
