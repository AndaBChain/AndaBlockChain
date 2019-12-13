package com.onets.core.wallet.families.ripple.utils;

import com.onets.core.wallet.families.ethereum.utils.RequestCache;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * ripple 接口
 */
public class RippleAPI {

    private static RippleAPI instance;

    public static RippleAPI getInstance() {
        if (instance == null)
            instance = new RippleAPI();
        return instance;
    }


    /**
     * 获取正常交易
     * @param address 地址字符串
     * @param b
     * @param force
     * @throws IOException
     */
    public void getNormalTransactions(String address, Callback b, boolean force) throws IOException {
        if (!force && RequestCache.getInstance().contains(RequestCache.TYPE_TXS_NORMAL, address)) {
            b.onResponse(null, new Response.Builder().code(200).message("").request(new Request.Builder()
                    .url("https://data.ripple.com/v2/accounts/" + address + "/payments?currency=XRP&limit=20")
                    .build()).protocol(Protocol.HTTP_1_0).body(ResponseBody.create(MediaType.parse("JSON"), RequestCache.getInstance().get(RequestCache.TYPE_TXS_NORMAL, address))).build());
            return;
        }
        get("https://data.ripple.com/v2/accounts/" + address + "/payments?currency=XRP&limit=20", b);
    }


    /**
     * 获取余额
     * @param address
     * @param b
     * @throws IOException
     */
    public void getBalance(String address, Callback b) throws IOException {
        get("https://data.ripple.com/v2/accounts/" + address + "/balances?currency=XRP", b);
    }



    public void get(String url, Callback b) throws IOException {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(url)
//                .addHeader("Connection","close")
                .addHeader("Connection", "Keep-Alive")
                .build();

        client.newCall(request).enqueue(b);
    }

    private RippleAPI() {
    }

}
