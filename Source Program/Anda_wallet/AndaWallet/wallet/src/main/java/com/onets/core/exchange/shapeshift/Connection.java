package com.onets.core.exchange.shapeshift;



import okhttp3.OkHttpClient;

/**
 * @author Yu K.Q.
 */
abstract public class Connection {
    private static final String DEFAULT_BASE_URL = "https://shapeshift.io/";

    OkHttpClient client;
    String baseUrl = DEFAULT_BASE_URL;

    protected Connection(OkHttpClient client) {
        this.client = client;
    }

    protected Connection() {
        client = new OkHttpClient();
    }

    protected String getApiUrl(String path) {
        return baseUrl + path;
    }
}
