package com.aizone.blockchain.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpsUtil implements X509TrustManager{
	//需要修改为服务器比特币钱包地址
	//
	//现为测试地址
	private final static String ANDA_SERVER_BTC_ADDRESS = "mvGuVqUCZt8SKCDxfgscXbmsPwpzkGtSEK";
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		// TODO Auto-generated method stub
 
	}
 
	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		// TODO Auto-generated method stub
 
	}
 
	@Override
	public X509Certificate[] getAcceptedIssuers() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * 处理https GET/POST请求
	 * 请求地址、请求方法、参数
	 * */
	public static String httpsRequest(String requestUrl,String requestMethod,String outputStr){
		StringBuffer buffer=null;
		try{
		//创建SSLContext
		SSLContext sslContext=SSLContext.getInstance("SSL");
		TrustManager[] tm={new HttpsUtil()};
		//初始化
		sslContext.init(null, tm, new java.security.SecureRandom());;
		//获取SSLSocketFactory对象
		SSLSocketFactory ssf=sslContext.getSocketFactory();
		URL url=new URL(requestUrl);
		HttpsURLConnection conn=(HttpsURLConnection)url.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setRequestMethod(requestMethod);
		//设置当前实例使用的SSLSoctetFactory
		conn.setSSLSocketFactory(ssf);
		conn.connect();
		//往服务器端写内容
		if(null!=outputStr){
			OutputStream os=conn.getOutputStream();
			os.write(outputStr.getBytes("utf-8"));
			os.close();
		}
		
		//读取服务器端返回的内容
		InputStream is=conn.getInputStream();
		InputStreamReader isr=new InputStreamReader(is,"utf-8");
		BufferedReader br=new BufferedReader(isr);
		buffer=new StringBuffer();
		String line=null;
		while((line=br.readLine())!=null){
			buffer.append(line);
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		return buffer.toString();
	}
	
	/**
	 * 获取btc交易信息记录
	 */
	public static List getTxDataFromBTC(String btcAddress){
		String txData= HttpsUtil.httpsRequest("https://testnet.blockchain.info/rawaddr/" + btcAddress,"GET",null);
		JSONObject jsonObj;
		@SuppressWarnings("rawtypes")
		List<List> list = new ArrayList<List>();
		try {
			jsonObj = new JSONObject(txData);
			JSONArray array = jsonObj.getJSONArray("txs");//获得比特币交易
			if(array.length() > 0){
				for(int i=0;i<array.length();i++){
					List<Map<String, Object>> list_ = new ArrayList<Map<String, Object>>();
					Map<String,Object> map = new HashMap<String,Object>();
					JSONObject jsonObject = new JSONObject(array.getString(i));
					JSONArray inputs = jsonObject.getJSONArray("inputs");
					String id = jsonObject.getString("hash");
					String time = jsonObject.getString("time");
					JSONObject prev_out =  new JSONObject(inputs.getString(0)).getJSONObject("prev_out");
					int balance = prev_out.getInt("value");
					JSONArray out = jsonObject.getJSONArray("out");
					int value_Anda = 0;
					int value_Send = 0;
					for(int j=0;j<out.length();j++){
						JSONObject out_ = new JSONObject(out.get(j).toString());
						String address = out_.getString("addr");
					
						if(ANDA_SERVER_BTC_ADDRESS.equals(address)){
							value_Anda = out_.getInt("value");
							System.out.println("Anda比特币地址:" + address);
							System.out.println("接收到的金额:" + value_Anda);
							map.put("Anda_Address", address);
							map.put("Anda_value", value_Anda);
						}else{
							value_Send = out_.getInt("value");
							System.out.println("发送方比特币地址:" + address);
							System.out.println("返还金额:" + value_Send);
							map.put("Send_Address", address);
							map.put("Send_value", value_Send);
						}
					}
					System.out.println("余额:" + balance);
					int Actual_BTC = value_Anda-(balance-value_Anda-value_Send);
					System.out.println("转换的安达币:" + Actual_BTC);
					System.out.println("ID:" + id);
					System.out.println("time:" + time);
					map.put("id", id);
					map.put("time", time);
					map.put("balance", balance);
					map.put("Actual_BTC", Actual_BTC);
					list_.add(map);
					list.add(list_);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
		
	}
	
	/**
	 * 根据交易id获取交易信息
	 * @param hash
	 * @return
	 */
	public static Map<String, String> getTxDataFromTxhash(String hash) {
		String path = "";
		String Server_Address = "";
		try {
			ResourceBundle resource = ResourceBundle.getBundle("data");
			path = resource.getString("path");
			Server_Address = resource.getString("Server_Address");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		String txData = HttpsUtil.httpsRequest( path + hash, "GET", null);
		JSONObject jsonObj;
		Map<String, String> map = new HashMap<>();
		try {
			jsonObj = new JSONObject(txData);
			String time = jsonObj.getString("time");
			JSONArray array = jsonObj.getJSONArray("out");//获得输出
			JSONObject prev_out ;
			String addr = "";
			String value = "";
			//遍历获取输出
			for(int i=0;i<array.length();i++){
				prev_out = new JSONObject(array.getString(i));
				addr = prev_out.getString("addr");
				value = prev_out.getString("vakue");
				//如果地址与接收地址相同
				if(addr.equals(Server_Address)){
					map.put("addr", addr);
					map.put("value", value);
					map.put("time", time);
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return map;

	}
	
 
}
