package com.onets.wallet.util;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;

import com.google.gson.Gson;
import com.onets.core.wallet.families.andachain.AndaAddress;
import com.onets.wallet.Constants;
import com.onets.wallet.service.ServerHttp;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.ripple.bouncycastle.crypto.engines.ISAACEngine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Http客户端POST请求类
 */
public class HttpClientPost {
    private static final String TAG = "HttpClientPost";
    //手机号及安达证包地址URL
    public static final String HTTPCustomer = ServerHttp.ROOT_SERVER_HTTP + "/InfoWrite";
    //发送交易URL
    public static final String ADD_TRANSACTION = ServerHttp.CHAIN_SERVER_HTTP + "/chain/transactions/new";
    //Account信息发送
    public static final String COMMIT_ACCOUNT_INFO = ServerHttp.CHAIN_SERVER_HTTP + "/account/commitnew";

    //余额获取
    public static final String GET_ACCOUNT_BALANCE = ServerHttp.CHAIN_SERVER_HTTP + "/account/getAccountBalance";
    //发送地址和金额
    public static final String  PUT_ADDRESS ="http://192.168.0.13:8081/chain/transactions/addTxRecord" ;
    //public static final String  PUT_ADDRESS ="http://www.baidu.com" ;

    Gson gson = new Gson();

    /**
     * 严格模式
     */
    public static void closeStrictMode() {
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectNetwork()
                        .penaltyLog()
                        .build()
        );
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy.Builder()
                        .detectLeakedSqlLiteObjects()
                        .penaltyLog()
                        .build());
    }


    /**
     * 使用HttpClient Post方式上传手机号和安达证包地址
     *
     * @param phone         SIM卡手机号
     * @param walletAddress 安达证包地址
     */
    public void getInfoServer(String phone, String walletAddress) {
        //closeStrictMode();
        HttpPost httpPost = new HttpPost(HTTPCustomer);
        HttpClient client = new DefaultHttpClient();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", phone));
        params.add(new BasicNameValuePair("walletAddress", walletAddress));

        try {
            HttpEntity httpEntity = new UrlEncodedFormEntity(params, "utf-8");
            httpPost.setEntity(httpEntity);
            Log.d(TAG, "getInfoServer:  " + httpPost);
            HttpResponse httpResponse = client.execute(httpPost);
            Log.d(TAG, "getStatusCode: getInfoServer- " + httpResponse.getStatusLine().getStatusCode());

            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity, HTTP.UTF_8);
                Log.d(TAG, "getInfoServer: 手机号上传成功 " + result);
            }else if (httpResponse.getStatusLine().getStatusCode() == 500){
                Log.d(TAG, "getInfoServer: 服务器连接失败，手机号上传失败" );
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送交易 使用POST传送数据
     * @param sender 发送者地址
     * @param recipient 接收者地址
     * @param amount 交易金额
     * @param publicKey
     * @param sign
     * @param timestamp
     */
    public boolean getNewTransactionPost(String sender, String recipient, double amount, String publicKey, String sign, String timestamp, String TxHash, String data){
        //closeStrictMode();
        boolean b = false;
        //创建客户端对象
        HttpClient client = new DefaultHttpClient();
        //创建post请求对象
        HttpPost httpPost = new HttpPost(ADD_TRANSACTION);
        String result = " ";
        if(sender == "" || sender == null || recipient == "" || recipient == null){
            b = false;
        }else {
            //封装form表单提交的数据,并放入集合中
            Map<String,Object> map = new HashMap<>();
            map.put("sender",sender);
            map.put("recipient",recipient);
            map.put("publicKey",publicKey);
            map.put("sign",sign);
            map.put("txHash",TxHash);
            map.put("timestamp",timestamp);
            map.put("amount",amount);
            map.put("data",data);

            InputStream is = null;
            try {
                //要提交的数据都已经在集合中了，把集合传给实体对象
                //设置post请求对象的实体，其实就是把要提交的数据封装至post请求的输出流中
                //设置消息头
                httpPost.setHeader("Content-Type","application/json;charset=utf-8");
                httpPost.setHeader("Accept","application/json");
                //设置发送数据(数据尽量为json),可以设置数据的发送时的字符集
                httpPost.setEntity(new StringEntity(gson.toJson(map),"utf-8"));
                //使用客户端发送post请求
                HttpResponse response = client.execute(httpPost);
                Log.d(TAG, "getStatusCode: getNewTransactionPost- " + response.getStatusLine().getStatusCode());
                if (response.getStatusLine().getStatusCode() == 200) {
                    is = response.getEntity().getContent();
                    String text = Utils.getTextFromStream(is);
                    JSONObject jsonObj = new JSONObject(text);
                    String  message = jsonObj.getString("message");

                    if(message == null){
                        b = false;
                    }else{
                        //String message_str = message.toString();
                        if("SUCCESS".equals(message)){
                            b = true;
                        }else{
                            b = false;
                        }
                    }
                }
            } catch (UnsupportedEncodingException e) {
                b = false;
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                b = false;
                e.printStackTrace();
            } catch (IOException e) {
                b = false;
                e.printStackTrace();
            } catch (JSONException e) {
                b = false;
                e.printStackTrace();
            }finally {
                if (is != null){
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return b;
    }

    /**
     * 向模拟公链系统发送账户信息
     * @param address
     * @param publicKey
     * @param privateKey
     */
    public boolean commitAccountInfoPost(String address, String publicKey, String privateKey){

        //closeStrictMode();
        boolean b = false;
        //创建客户端对象
        HttpClient client = new DefaultHttpClient();
        //创建post请求对象
        HttpPost httpPost = new HttpPost(COMMIT_ACCOUNT_INFO);
        String result = " ";
        Log.d(TAG, Constants.LOG_LABLE + "commitAccountInfoPost: " + httpPost);
        //封装form表单提交的数据,并放入集合中
        Map<String,String> map = new HashMap<>();
        //List<NameValuePair> parameter = new ArrayList<NameValuePair>();
        map.put("address",address);
        map.put("publicKey",publicKey);
        map.put("privateKey",privateKey);

        InputStream is = null;
        try {
            //要提交的数据都已经在集合中了，把集合传给实体对象
            //设置post请求对象的实体，其实就是把要提交的数据封装至post请求的输出流中
            //设置消息头
            httpPost.setHeader("Content-Type","application/json;charset=utf-8");
            httpPost.setHeader("Accept","application/json");
            //设置发送数据(数据尽量为json),可以设置数据的发送时的字符集
            httpPost.setEntity(new StringEntity(gson.toJson(map),"utf-8"));
            /* 使用客户端发送post请求 */
            HttpResponse response = client.execute(httpPost);
            Log.d(TAG, "getStatusCode: commitAccountInfoPost- " + response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() == 200){
                is = response.getEntity().getContent();
                String text = Utils.getTextFromStream(is);
                JSONObject jsonObj = new JSONObject(text);
                String  message = jsonObj.getString("message");
                Log.d(TAG, "commitAccountInfoPost: " + message);

                if(message == null){
                    b = false;
                }else{
                    // String message_str = message.toString();
                    if(message.equals("Success")){
                        b = true;
                    }else{
                        b = false;
                    }
                }
            }else{
                b = false;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            b=false;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            b=false;
        } catch (IOException e) {
            e.printStackTrace();
            b=false;
        } catch (JSONException e) {
            e.printStackTrace();
            b=false;
        }finally {
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return b;
    }

    public boolean commitAccountInfoPost(String address, String publicKey, String privateKey, String macAddress){

        closeStrictMode();
        boolean b = false;
        //创建客户端对象
        HttpClient client = new DefaultHttpClient();
        //创建post请求对象
        HttpPost httpPost = new HttpPost(COMMIT_ACCOUNT_INFO);
        String result = " ";

        //封装form表单提交的数据,并放入集合中
        Map<String,String> map = new HashMap<>();
        //List<NameValuePair> parameter = new ArrayList<NameValuePair>();
        map.put("address",address);
        map.put("publicKey",publicKey);
        map.put("privateKey",privateKey);
        map.put("macAddress", macAddress);

        InputStream is = null;
        try {
            //要提交的数据都已经在集合中了，把集合传给实体对象
            //设置post请求对象的实体，其实就是把要提交的数据封装至post请求的输出流中
            //设置消息头
            httpPost.setHeader("Content-Type","application/json;charset=utf-8");
            httpPost.setHeader("Accept","application/json");
            //设置发送数据(数据尽量为json),可以设置数据的发送时的字符集
            httpPost.setEntity(new StringEntity(gson.toJson(map),"utf-8"));
            /* 使用客户端发送post请求 */
            HttpResponse response = client.execute(httpPost);
            Log.d(TAG, "getStatusCode: commitAccountInfoPost- " + response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() == 200){
                is = response.getEntity().getContent();
                String text = Utils.getTextFromStream(is);
                JSONObject jsonObj = new JSONObject(text);
                String  message = jsonObj.getString("message");
                Log.d(TAG, "commitAccountInfoPost: " + message);

                if(message == null){
                    b = false;
                }else{
                    // String message_str = message.toString();
                    if(message.equals("Success")){
                        b = true;
                    }else{
                        b = false;
                    }
                }
            }else{
                b = false;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            b=false;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            b=false;
        } catch (IOException e) {
            e.printStackTrace();
            b=false;
        } catch (JSONException e) {
            e.printStackTrace();
            b=false;
        }finally {
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return b;
    }

    /**
     * 从模拟公链系统获取余额
     */
    public double getAccountBalance(String address) {
        Double d = 0.00;
        closeStrictMode();
        //创建客户端对象
        HttpClient client = new DefaultHttpClient();
        //创建post请求对象
        HttpPost httpPost = new HttpPost(GET_ACCOUNT_BALANCE);
        String result = " ";

        //封装form表单提交的数据,并放入集合中
        Map<String,String> map = new HashMap<>();
       // List<NameValuePair> parameter = new ArrayList<NameValuePair>();
      //  parameter.add(new BasicNameValuePair("address",address));
        map.put("address",address);
        InputStream is = null;
        try {
            //要提交的数据都已经在集合中了，把集合传给实体对象
            //设置post请求对象的实体，其实就是把要提交的数据封装至post请求的输出流中
            //设置消息头
            httpPost.setHeader("Content-Type","application/json;charset=utf-8");
            httpPost.setHeader("Accept","application/json");
            //设置发送数据(数据尽量为json),可以设置数据的发送时的字符集
            httpPost.setEntity(new StringEntity(gson.toJson(map),"utf-8"));

            //使用客户端发送post请求
            HttpResponse response = client.execute(httpPost);
            Log.d(TAG, "getStatusCode: getAccountBalance- " + response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == 200){
                is = response.getEntity().getContent();
                String text = Utils.getTextFromStream(is);
                JSONObject jsonObj = new JSONObject(text);
                String  message = jsonObj.getString("item");
                Log.d(TAG, "getAccountBalance: message " + message);
               /* Map<String,String> map_Json = getMap(text);
                String message = map_Json.get("item");*/
                if(message == null || message.equals("null")){
                    d = 0.00;
                }else{
                   // String message_str = message.toString();
                    if( !"".equals(message)){
                        d = Double.valueOf(message);
                    }else{
                        d = 0.0;
                    }
                }
            }else{
                d = 0.00;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            d = 0.00;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            d = 0.00;
        } catch (IOException e) {
            e.printStackTrace();
            d = 0.00;
        } catch (JSONException e) {
            e.printStackTrace();
            d = 0.00;
        }finally {
            try {
                if (is != null){
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return d;
    }

    /**
     * 比特币/以太坊兑换安达
     * @param url
     * @param map
     * @return
     * @throws Exception
     */
    public String post(String url, Map<String, Object> map) throws Exception {
        closeStrictMode();
        //StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        HttpClient client = new DefaultHttpClient();
        //创建post请求对象
        HttpPost httpPost = new HttpPost(url);
        String result = " ";
        //封装form表单提交的数据,并放入集合中
        InputStream is = null;
        try {
            //要提交的数据都已经在集合中了，把集合传给实体对象
            //设置post请求对象的实体，其实就是把要提交的数据封装至post请求的输出流中
            //设置消息头
            httpPost.setHeader("Content-Type","application/json;charset=utf-8");
            httpPost.setHeader("Accept","application/json");
            //设置发送数据(数据尽量为json),可以设置数据的发送时的字符集
            httpPost.setEntity(new StringEntity(gson.toJson(map),"utf-8"));
            Log.d(TAG, "handleSendConfirmB: map.json " + gson.toJson(map));

            //使用客户端发送post请求
            HttpResponse response = client.execute(httpPost);
            //{"code":200,"message":"SUCCESS","item":{"sender":"cf87c2e720f9019399a80f9661e0d450e4997e68","sign":"IPFOdYMZhq184D6FKh46X1RU7UwQ5RgrsG8kAPvmC4ocTzjr07fpJt5l8mYiotUdb8LdIsGhEHzEch4CY3IeacE=","recipient":"b7578430db8b62e1a71c5350ce6d8dc74d72d9aa","publicKey":"020143ffa7d8146388b339f114f5e626c846183d8b7e9e8992de300339cbb22436","amount":100000,"timestamp":1537323831745,"txHash":"c81917f78624bf75f4357461e1b1be029f3d82585047d58528388df87b127e39","status":"SUCCESS","errorMessage":null,"data":"6e8bb340-6f92-4069-920f-32e3ac89b200"}}
            Log.d(TAG, "getStatusCode: post- " + response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == 200){
                is = response.getEntity().getContent();
                String message = Utils.getTextFromStream(is);
               // JSONObject jsonObj = new JSONObject(text);
              //  String  message = jsonObj.getString("message");
               /* Map<String,String> map_Json = getMap(text);
                String message = map_Json.get("item");*/
                if(message == null){
                    result = "FAIL";
                }else{
                    // String message_str = message.toString();
                    if( !"".equals(message)){
                        result=message;
                    }else{
                        result = "FAIL";
                    }
                }

            }else{
                result = "FAIL";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();

        } catch (ClientProtocolException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
            result = "client.execute(httpPost) FAILED";

        }finally {
            if (is != null){
                is.close();
            }
        }
        Log.d(TAG, "handleSendConfirmB: result " + result);
        return result;
    }

    public String PayPalExchangeAnda(String url, Map<String, Object> map){
        closeStrictMode();

        HttpClient client = new DefaultHttpClient();
        //创建post请求对象
        HttpPost httpPost = new HttpPost(url);
        String result = "";
        InputStream inputStream = null;

        try {
            httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
            httpPost.setHeader("Accept", "application/json");

            httpPost.setEntity(new StringEntity(gson.toJson(map), "utf-8"));
            Log.d(TAG, "测试：PayPalExchangeAnda: map.json" + gson.toJson(map));

            HttpResponse response = client.execute(httpPost);
            Log.d(TAG, "PayPalExchangeAnda: " + response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == 200){
                inputStream = response.getEntity().getContent();
                String message = Utils.getTextFromStream(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
