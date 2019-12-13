package com.onets.core.wallet.families.andachain.utils;

import android.util.Log;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 安达链API
 */
public class AndaChainAPI {

    String TAG = "Anda BlockChain API";
    private static AndaChainAPI instance;

    public static AndaChainAPI getInstance() {
        if (instance == null)
            instance = new AndaChainAPI();
        return instance;
    }

    /*获取交易*/
    /*public void getTransactions(Object[] address) throws IOException {
        System.out.println("not implement");
    }*/
    public void getTransactions(String address) throws IOException {
        System.out.println("not implement");
    }

    /*签名发送交易*/
    public String getSignAndSendTransaction(Object[] txParams) {
        return (String) get("personal_signAndSendTransaction", txParams);
    }

    /*获取gas值*/
    public String getGasPrice() throws IOException {
        return (String) get("Anda_gasPrice", new Object[]{});
    }


    public String getBalance(Object[] address) throws IOException {
        return (String) get("Anda_getBalance", address);
    }

    public String getBlockNumber() throws IOException {
        return (String) get("Anda_blockNumber", new Object[]{});
    }


    public String getNonceForAddress(Object[] nonceParams) throws IOException {
        return (String) get("Anda_getTransactionCount", nonceParams);
    }

    public String getPersonal_newAccount(Object[] seed) throws IOException {
        return (String) get("personal_newAccount", seed);
    }


    public String getTransactionByHash(Object[] transactionHash) throws IOException {
        return (String) get("Anda_getTransactionByHashStr", transactionHash);
    }

    /*public List<String> getNormalTransactions(Object[] address,boolean force) {

        if (!force && RequestCache.getInstance().contains(RequestCache.TYPE_TXS_NORMAL, address[0] + "")) {
            List txhashList = ListUtil.StringToList(RequestCache.getInstance().get(RequestCache.TYPE_TXS_NORMAL, address[0] + ""));
            return txhashList;
        }
        try {
            JsonRpcHttpClient client = new JsonRpcHttpClient(new URL("http://192.168.0.108:8255/rpc"));
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Connection", "Keep-Alive");
            client.setHeaders(headers);
            // 设置连接超时
            client.setConnectionTimeoutMillis(10000);
            client.setReadTimeoutMillis(30000);
            Object msg = client.invoke("anda_listNormalTxHashfromAddress", address, Object.class);
            Log.i(TAG, "Anda BlockChain  ------ callback :" + msg);
            return (List<String>) msg;
        } catch (Throwable throwable) {
            Log.e(TAG, "" + throwable);
            List<String> msg = new ArrayList<>();
            msg.add("error" + throwable);
            return msg;
        }
    }*/

    private Object get(String methodName, Object[] params) {
        try {
            Log.i(TAG, "Anda BlockChain  ------   get: " + methodName);
            JsonRpcHttpClient client = new JsonRpcHttpClient(new URL("http://221.214.108.2:8255/rpc"));
//            JsonRpcHttpClient client = new JsonRpcHttpClient(new URL("http://192.168.0.108:8255/rpc/"));
//            JsonRpcHttpClient client = new JsonRpcHttpClient(new URL("http://192.168.0.119:8255/rpc"));

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Connection", "Keep-Alive");
            client.setHeaders(headers);
            // 设置连接超时
            client.setConnectionTimeoutMillis(10000);
            client.setReadTimeoutMillis(30000);
            Object msg = client.invoke(methodName, params, Object.class);
            Log.i(TAG, "Anda BlockChain  ------ callback :" + msg);
            return msg;
        } catch (Throwable throwable) {
            Log.e(TAG, "" + throwable);
            return "error" + throwable;
        }
    }

    private AndaChainAPI() {

    }

}
