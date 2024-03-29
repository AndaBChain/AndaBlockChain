package com.onets.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.onets.wallet.R;

/**
 * @author Yu K.Q.
 */
public class PasswordConfirmationActivity extends AbstractWalletFragmentActivity
        implements PasswordConfirmationFragment.Listener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_wrapper);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PasswordConfirmationFragment())
                    .commit();
        }
    }

    @Override
    public void onPasswordConfirmed(Bundle args) {
        final Intent result = new Intent();
        result.putExtras(args);
        setResult(RESULT_OK, result);

        // delayed finish
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }
}
