package com.onets.wallet.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.onets.wallet.R;

/**
 * @author Yu K.Q.
 * 调试Activity
 */
public class DebuggingActivity extends BaseWalletActivity implements UnlockWalletDialog.Listener {

    private static final String DEBUGGING_TAG = "debugging_tag";

    /**
     * 创建
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_wrapper);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DebuggingFragment(), DEBUGGING_TAG)
                    .commit();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    /**
     * 密码
     * @param password
     */
    @Override
    public void onPassword(CharSequence password) {
        Fragment f = getFM().findFragmentByTag(DEBUGGING_TAG);
        if (f != null && f instanceof DebuggingFragment) {
            ((DebuggingFragment) f).setPassword(password);
        }
    }
}
