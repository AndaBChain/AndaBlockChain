package com.onets.wallet.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.onets.core.coins.CoinType;
import com.onets.core.wallet.Wallet;
import com.onets.core.wallet.WalletAccount;
import com.onets.wallet.Configuration;
import com.onets.wallet.WalletApplication;

import java.util.List;

import javax.annotation.Nullable;

/**
 * @author Yu K.Q.
 * 钱包基础界面
 */
abstract public class BaseWalletActivity extends AppCompatActivity {

    public WalletApplication getWalletApplication() {
        return (WalletApplication) getApplication();
    }

    /**
     * 根据accountid获取账户
     * @param accountId
     * @return
     */
    @Nullable
    public WalletAccount getAccount(String accountId) {
        return getWalletApplication().getAccount(accountId);
    }

    /**
     * 获取所有账户信息
     * @return
     */
    public List<WalletAccount> getAllAccounts() {
        return getWalletApplication().getAllAccounts();
    }

    /**
     * 根据币类型获取账户
     * @param type
     * @return
     */
    public List<WalletAccount> getAccounts(CoinType type) {
        return getWalletApplication().getAccounts(type);
    }

    /**
     * 获取账户列表
     * @param types
     * @return
     */
    public List<WalletAccount> getAccounts(List<CoinType> types) {
        return getWalletApplication().getAccounts(types);
    }

    /**
     * 账户是否存在
     * @param accountId
     * @return
     */
    public boolean isAccountExists(String accountId) {
        return getWalletApplication().isAccountExists(accountId);
    }

    /**
     * 获取配置
     * @return
     */
    public Configuration getConfiguration() {
        return getWalletApplication().getConfiguration();
    }

    public FragmentManager getFM() {
        return getSupportFragmentManager();
    }

    public void replaceFragment(Fragment fragment, int container) {
        replaceFragment(fragment, container, null);
    }

    public void replaceFragment(Fragment fragment, int container, @Nullable String tag) {
        FragmentTransaction transaction = getFM().beginTransaction();

        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(container, fragment, tag);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    /**
     * 获取Wallet
     * @return
     */
    @Nullable
    public Wallet getWallet() {
        return getWalletApplication().getWallet();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWalletApplication().touchLastResume();
    }

    /**
     * 停止
     */
    @Override
    protected void onStop() {
        super.onStop();
        getWalletApplication().touchLastStop();
    }
}
