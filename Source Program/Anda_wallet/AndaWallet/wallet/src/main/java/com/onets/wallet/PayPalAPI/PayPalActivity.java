package com.onets.wallet.PayPalAPI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.onets.wallet.R;
import com.onets.wallet.ui.WalletActivity;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;

public class PayPalActivity extends AppCompatActivity {
    private static final String TAG = "PayPalActivity";

    // note that these credentials will differ between live & sandbox environments.
    private static final String SANDBOX_CLIENT_ID = "AakcHQX6iHPV9oMJe3aDdiUf3IjZRCBgWtqnB7HVyxg2EodWXefy9Y7VRQbJSsZ9DxMGaIOQ9u-mJy0H";
    private static final String LIVE_CLIENT_ID = "Ae9CVnY_Q5frSFV68VjGXMWQ7asBOphCaWxLDVFOdiR6BJOTKZN1GJKM_V5SqLFz5F0rxEmkM3Xs4tRY";
    private static final String PRODUCT_NAME = "安达通证";

    //创建PayPalConfiguration对象
    private static PayPalConfiguration config = new PayPalConfiguration()
            //Start with mock environment.
            //when ready, switch to sandbox(ENVIRONMENT_SANDBOX) or live(ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_NO_NETWORK)
            .clientId(SANDBOX_CLIENT_ID)
            .merchantName(PRODUCT_NAME);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paypal);

        //启动PayPalService
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        onBuyPressed();
    }

    /**
     * Create the payment and launch the payment intent.
     * 启动支付
     */
    public void onBuyPressed() {
        //
        Intent exchange_Intent = getIntent();
        String AndaAddress = exchange_Intent.getStringExtra("AndaAddress");
        String exchange_Amount = exchange_Intent.getStringExtra("Amount");
        Log.d(TAG, "测试：onBuyPressed: AndaAddress = " + AndaAddress);
        Log.d(TAG, "测试：onBuyPressed: exchange_Amount = " + exchange_Amount);

        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(exchange_Amount), "USD", "Exchange AndaChain", PayPalPayment.PAYMENT_INTENT_SALE);

        Intent payIntent = new Intent(PayPalActivity.this, PaymentActivity.class);

        // send the same configuration for restart resiliency
        payIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        payIntent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
        startActivityForResult(payIntent, 0);
    }

    protected void displayResultText(String result) {
        ((TextView)findViewById(R.id.txtResult)).setText("Result : " + result);
        Toast.makeText(
                getApplicationContext(),
                result, Toast.LENGTH_LONG)
                .show();
    }

    /**
     * 支付完成后回调
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                try {
                    Log.i(TAG, "测试confirm.toJSONObject()：" + confirm.toJSONObject().toString(4));
                    Log.i(TAG, "测试 confirm.getPayment().toJSONObject()：" + confirm.getPayment().toJSONObject().toString(4));

                    Toast.makeText(this, "兑换成功", Toast.LENGTH_SHORT).show();
                    Intent walletIntent = new Intent(PayPalActivity.this, WalletActivity.class);
                    startActivity(walletIntent);
                    //displayResultText("PaymentConfirmation info received from PayPal");
                } catch (JSONException e) {
                    Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.i(TAG, "测试：The user canceled.");
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            Log.i(TAG, "测试：An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
        }
    }

    @Override
    public void onDestroy() {
        // 销毁PayPalService
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }
}
