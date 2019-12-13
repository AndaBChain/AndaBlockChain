package com.onets.wallet.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.onets.wallet.R;

import butterknife.ButterKnife;

/**
 * 关于界面
 */
public class AboutActivity extends BaseWalletActivity {

    /**
     * 创建
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        TextView version = (TextView) findViewById(R.id.about_version);
        TextView copyright = findViewById(R.id.about_copyright);
        if (getWalletApplication().packageInfo() != null) {
            version.setText(getWalletApplication().packageInfo().versionName);
        } else {
            version.setVisibility(View.INVISIBLE);
        }

    }


}
