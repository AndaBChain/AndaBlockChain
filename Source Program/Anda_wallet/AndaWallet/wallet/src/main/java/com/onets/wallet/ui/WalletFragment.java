package com.onets.wallet.ui;

import android.support.v4.app.Fragment;

import com.onets.core.wallet.WalletAccount;

/**
 * @author Yu K.Q.
 * 账户Fragment
 */
public abstract class WalletFragment extends Fragment implements ViewUpdateble {
    abstract public WalletAccount getAccount();
}
