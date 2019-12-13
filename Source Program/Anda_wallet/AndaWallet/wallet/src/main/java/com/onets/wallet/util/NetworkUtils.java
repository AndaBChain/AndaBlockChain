package com.onets.wallet.util;

import android.content.Context;

import okhttp3.OkHttpClient;

/**
 * @author Yu K.Q.
 * 网络工具类
 */
public class NetworkUtils {
    private static OkHttpClient httpClient;

    /**
     * 获取http客户端
     * @param context
     * @return
     */
    public static OkHttpClient getHttpClient(Context context) {
        if (httpClient == null) {
            httpClient = new OkHttpClient();
        }
        return httpClient;
    }
}
