package com.aizone.blockchain.bit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HashToString {
	
	/**
	 * 用于获取网站的Json数据
	 * @param urlPath
	 * @return
	 * @throws IOException
	 */
	public synchronized String  searchRequest(String urlPath) throws IOException {
	    
	    StringBuilder response = new StringBuilder();
	    URL url = new URL(urlPath);
	    HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
	    httpconn.setReadTimeout(10000);
	    httpconn.setConnectTimeout(15000);
	    httpconn.setRequestMethod("GET");
	    httpconn.setDoInput(true);
	    httpconn.connect();
	    if (httpconn.getResponseCode()==200){//HttpURLConnection.HTTP_OK
	        BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()),8192);
	        String strLine = null;
	        while ((strLine = input.readLine()) != null){
	            response.append(strLine);
	        }
	        input.close();
	    }
	    return response.toString();
	}

}
