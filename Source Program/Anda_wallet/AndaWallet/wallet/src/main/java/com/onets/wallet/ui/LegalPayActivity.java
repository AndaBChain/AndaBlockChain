package com.onets.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.google.gson.Gson;
import com.onets.wallet.Constants;
import com.onets.wallet.R;
import com.onets.wallet.WeiXinPayBean;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 法定数字货币
 */
public class LegalPayActivity extends AppCompatActivity implements View.OnClickListener {

    private String url;
    private RadioButton we_pay;
    private RadioButton pal_pay;
    private RadioButton alipay;
    private Button btn_legal_pay;
    private IWXAPI api;  //第三方app和微信通信的openapi接口

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            btn_legal_pay.setEnabled(true);
            return true;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_pay);
        //标题
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setTitle("支付选择");

        //微信支付测试接口，正式开发必须使用公司自己的接口
        url = "http://wxpay.wxutil.com/pub_v2/app/app_pay.php";
        initView();


    }

    private void initView() {
        regToWX(); //注册到微信

        we_pay = (RadioButton) findViewById(R.id.we_pay);
        pal_pay = (RadioButton) findViewById(R.id.pal_pay);
        we_pay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    pal_pay.setChecked(false);
                }


            }
        });

        pal_pay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    we_pay.setChecked(false);
                }
            }
        });

        btn_legal_pay = (Button) findViewById(R.id.btn_legal_pay);
        btn_legal_pay.setOnClickListener(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                // 处理返回逻辑
                startActivity(new Intent(LegalPayActivity.this, SelectCoinActivity.class));
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(LegalPayActivity.this, SelectCoinActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onClick(final View view) {
        final Button pay_button = (Button) findViewById(view.getId());
        pay_button.setEnabled(false);
        if (we_pay.isChecked()) {
            view.setEnabled(false);
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            final Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Connection", "Keep-close")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    //解析服务器返回，获取“支付串码”
                    result = result.replaceAll("package", "packageValue");
                    Gson gson = new Gson();
                    payBean = gson.fromJson(result, WeiXinPayBean.class);
                    Log.e("result", result);
                    //调用微信支付sdk的支付方法，传入支付串码
                    sendPayRequest();
                    handler.sendEmptyMessage(0);
                }
            });

        }
    }

    private WeiXinPayBean payBean;

    private void sendPayRequest() {
        PayReq payReq = new PayReq();
        payReq.appId = payBean.getAppid();
        payReq.partnerId = payBean.getPartnerid();
        payReq.prepayId = payBean.getPrepayid();
        payReq.packageValue = payBean.getPackageValue();
        payReq.nonceStr = payBean.getNoncestr();
        payReq.timeStamp = payBean.getTimestamp() + "";
        payReq.sign = payBean.getSign();
        api.sendReq(payReq);

    }

    private void regToWX() {
        //通过 WXAPIFactory 工厂获取 IWXAPI 实例
//        api = WXAPIFactory.createWXAPI(this, Constants.WEIXIN_APP_ID, true);
        api = WXAPIFactory.createWXAPI(this, "wxb4ba3c02aa476ea1", true);
        //将应用的app id注册到微信中
        api.registerApp(Constants.WEIXIN_APP_ID);
    }
}
