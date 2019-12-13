package com.onets.wallet.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.onets.wallet.R;


/**
 * 账户细节界面
 */
public class AccountDetailsActivity extends BaseWalletActivity implements TradeStatusFragment.Listener {

    /**
     * 创建
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_wrapper);

        if (savedInstanceState == null) {
            Fragment fragment = new AccountDetailsFragment();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    /**
     * 完成
     */
    @Override
    public void onFinish() {
        finish();
    }
}
