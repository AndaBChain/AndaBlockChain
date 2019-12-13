package com.onets.wallet.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.onets.core.coins.Value;
import com.onets.wallet.Constants;
import com.onets.wallet.PayPalAPI.PayPalActivity;
import com.onets.wallet.R;
import com.onets.wallet.util.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import static com.onets.wallet.util.HttpClientPost.closeStrictMode;

/**
 * 比特币兑换安达
 */
public class Exchange extends AppCompatActivity {
    private static final String TAG = "Exchange";

    @Bind(R.id.receive_address_label)//比特币/以太坊兑换的接收地址label
    TextView receiveAddressLabel;

    @Bind(R.id.receive_address)//比特币/以太坊兑换的接收地址
    TextView receiveAddress;

    @Bind(R.id.anda_address_label)//接收的安达地址label
    TextView andaAddressLabel;

    @Bind(R.id.anda_address)//接受的安达地址
    EditText andaAddress;

    @Bind(R.id.exchange_amounts_label)//兑换的比特/以太数量label
    TextView exchangeAmountsLabel;

    @Bind(R.id.send_amount)//兑换的比特/以太数量
    EditText sendAmount;

    @Bind(R.id.coin_type)//兑换的比特/以太类型
    TextView coinType;

    @Bind(R.id.anda_exchange_amounts)//兑换的安达数量
    TextView andaExchangeAmounts;

    @Bind(R.id.text_rate)
    TextView coinRate;

    @Bind(R.id.exchange_button)
    Button exchangeButton;
    Gson gson = new Gson();
    ProgressBar pbar;

    String rateStr = null;

    String data = null;
    String coin_type = null;
    int coin_value = 0;
    double rate_fee = 0.0005;//兑换手续费

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        data = intent.getStringExtra("Type");
        Log.d(TAG, "Exchange: data " + data);

        //根据传来的信息进行赋值
        //判断兑换的是BTC还是ETC
        if (data.equals("BTC")) {//比特币
            coin_type = "Bitcoin";
            coinType.setText(data);
            exchangeAmountsLabel.setText("兑换的比特币数量");
            receiveAddressLabel.setText("比特币兑换的接收地址");
            receiveAddress.setText(R.string.server_bitcoin_address);
            rateStr = "1 BTC = 4000 ABT";
            coin_value = 1;
            coinRate.setText(rateStr);
        } else if (data.equals("ETH")) {//以太币
            coin_type = "Ethereum";
            coinType.setText(data);
            exchangeAmountsLabel.setText("兑换的以太币数量");
            receiveAddressLabel.setText("以太币兑换的接收地址");
            receiveAddress.setText(R.string.server_ethereum_address);
            rateStr = "1 BTC = 100 ABT";
            coin_value = 2;
            coinRate.setText(rateStr);
        } else if (data.equals("XRP")) {//瑞波币
            coin_type = "Ripple";
            coinType.setText(data);
            exchangeAmountsLabel.setText("兑换的瑞波币数量");
            receiveAddressLabel.setText("瑞波币兑换的接收地址");
            receiveAddress.setText("瑞波币地址");
            rateStr = "";
            coin_value = 3;
            coinRate.setText(rateStr);
        }else if (data.equals("PayPal")){//Paypal支付
            coin_type = "USD";
            coinType.setText(coin_type);
            exchangeAmountsLabel.setText("PayPal兑换金额");
            receiveAddressLabel.setText("PayPal的接收账户地址");
            receiveAddress.setText("2551279354@qq.com");
            rateStr = "0.89 USD = 1 ABT";
            coin_value = 4;
            coinRate.setText(rateStr);
        }

        pbar = findViewById(R.id.proBar);

    }

    @OnClick(R.id.exchange_button)
    void exchangeButionClick(){
        String AndaAddress = andaAddress.getText().toString();
        String amount = sendAmount.getText().toString();
        if ("".equals(AndaAddress)){
            Toast.makeText(this, "请填入有效安达证包地址",Toast.LENGTH_LONG).show();
        }else if (AndaAddress.contains(":") || AndaAddress.contains("?")){
            Toast.makeText(this, "请填入有效的安达证包地址，保留部分为':'之后、'?'之前的部分",Toast.LENGTH_LONG).show();
        } else if (data.equals("PayPal")){
            Intent intent = new Intent(Exchange.this, PayPalActivity.class);
            Log.d(TAG, "测试exchangeButionClick: coin_type " + coin_type);
            intent.putExtra(Constants.ARG_COIN_TYPE, coin_type);
            intent.putExtra("id", coin_value);
            intent.putExtra("Type", data);
            Log.d(TAG, "测试exchangeButionClick: Type " + data);
            intent.putExtra("AndaAddress", AndaAddress);
            intent.putExtra("Amount", amount);
            intent.putExtra("exchange", true);

            startActivity(intent);
        } else {
            Intent intent = new Intent(Exchange.this, WalletActivity.class);
            Log.d(TAG, "exchangeButionClick: coin_type " + coin_type);
            intent.putExtra(Constants.ARG_COIN_TYPE, coin_type);
            intent.putExtra("id", coin_value);
            intent.putExtra("Type", data);
            Log.d(TAG, "exchangeButionClick: Type " + data);
            intent.putExtra("AndaAddress", AndaAddress);
            intent.putExtra("Amount", amount);
            intent.putExtra("exchange", true);
            //intent.putExtra("andaAmount", andaExchangeAmounts.getText().toString());

            startActivity(intent);
        }

    }

    @OnTextChanged(value = R.id.send_amount, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterTextChanged(Editable edit){
        //比特/以太数量
        double amount1 = Double.valueOf(edit.toString());
        double amount2 = 0.00;
        //判断兑换的是BTC还是ETC
        if (data.equals("BTC")) {//比特币
            //rateStr = "1:5000";
            amount2 = amount1 * (1 - rate_fee) * 4000;
            andaExchangeAmounts.setText(String.valueOf(amount2));
        } else if (data.equals("ETH")) {//以太币
            //rateStr = "1:150";
            amount2 = amount1 * (1 - rate_fee) * 100;
            andaExchangeAmounts.setText(String.valueOf(amount2));
        } else if (data.equals("XRP")) {//瑞波币
            //rateStr = "1:4500";
            amount2 = amount1 * (1 - rate_fee) * 4500;
            andaExchangeAmounts.setText(String.valueOf(amount2));
        }else if (data.equals("PayPal")){
            amount2 = amount1 * (1 - rate_fee) / 0.89 * 1;
            andaExchangeAmounts.setText(String.valueOf(amount2));
        }
    }

    public static String post(String url, Map<String, Object> map) throws Exception {
        // 需要传输的数据
        // 谷歌的Gson
        Gson gson = new Gson();
        // 相对于commons-httpclient 3.1这里采用接口的方式来获取httpclient了
        HttpClient httpClient = HttpClients.createDefault();
        // 声明请求方式
        HttpPost httpPost = new HttpPost(url);
        // 设置消息头
        httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
        httpPost.setHeader("Accept", "application/json");
        // 设置发送数据(数据尽量为json),可以设置数据的发送时的字符集
        httpPost.setEntity(new StringEntity(gson.toJson(map), "utf-8"));
        // 获取相应数据，这里可以获取相应的数据
        HttpResponse httpResponse = httpClient.execute(httpPost);
        // 拿到实体
        HttpEntity httpEntity = httpResponse.getEntity();
        // 获取结果，这里可以正对相应的数据精细字符集的转码
        String result = "";
        if (httpEntity != null) {
            result = EntityUtils.toString(httpEntity, "utf-8");
        }
        // 关闭连接
        httpPost.releaseConnection();
        return result;
    }
    public void getAddressServer(org.bitcoinj.core.Transaction resultTx, String andaAddress, Value amount) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                closeStrictMode();
                boolean b = false;
                //创建客户端对象
                HttpClient client = new DefaultHttpClient();
                //创建post请求对象
                HttpPost httpPost = new HttpPost("http://192.168.0.12:8081/chain/transactions/addTxRecord");
                String result = " ";
                //封装form表单提交的数据,并放入集合中
                Map<String, Object> map = new HashMap<>();

                map.put("ResultTx",resultTx);
                map.put("Amount", String.valueOf(amount));
                map.put("AndaAddress", andaAddress);
                try {
                    //要提交的数据都已经在集合中了，把集合传给实体对象
                    //设置post请求对象的实体，其实就是把要提交的数据封装至post请求的输出流中
                    //设置消息头
                    httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
                    httpPost.setHeader("Accept", "application/json");
                    //设置发送数据(数据尽量为json),可以设置数据的发送时的字符集
                    httpPost.setEntity(new StringEntity(gson.toJson(map), "utf-8"));
                    //使用客户端发送post请求
                    HttpResponse response = client.execute(httpPost);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        Log.i("haha", "---------" + response.getStatusLine().getStatusCode());
                        InputStream is = response.getEntity().getContent();
                        String text = Utils.getTextFromStream(is);
                    /*JSONObject jsonObj = new JSONObject(text);
                    String message = jsonObj.getString("message");*/
                           /* Map<String,String> map_Json = getMap(text);
                            String message = map_Json.get("message");*/
                        Log.i(TAG, "run: --------"+text);
                        showResponse(text);
                        if (text == null) {
                            Log.i(TAG, "run: --------"+text+"  为空");
                        } else {
                            //String message_str = message.toString();
                            if ("SUCCESS".equals(text)) {

                            } else {

                            }
                        }
                    }else{
                        showResponse("false");
                        Log.i(TAG,"haha------------------"+response.getStatusLine().getStatusCode());
                    }
                } catch (UnsupportedEncodingException e) {

                    e.printStackTrace();
                } catch (ClientProtocolException e) {

                    e.printStackTrace();
                } catch (IOException e) {
                    showResponse("false");
                    Log.i(TAG, "haha: ---------IO");
                    e.printStackTrace();
                } /*catch (JSONException e) {
                b = false;
                Log.i(TAG, "haha: ---------JSON");
                e.printStackTrace();
            }*/
            }
        }).start();
    }

    private void showResponse(final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pbar.setVisibility(View.GONE);
                if (response.equals("true")) {
                    Toast.makeText(Exchange.this,"数据上传成功，请尽快手动转发比特币", Toast.LENGTH_LONG).show();
                    AlertDialog.Builder dialog=new AlertDialog.Builder(Exchange.this);
                    dialog.setTitle("提示：");
                    dialog.setMessage("请尽快向比特币地址：  转发比特币！");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog.show();
                }else{
                    Toast.makeText(Exchange.this,"数据上传失败，请重新上传或检查网络", Toast.LENGTH_LONG).show();
                }
            }

        });
    }

}