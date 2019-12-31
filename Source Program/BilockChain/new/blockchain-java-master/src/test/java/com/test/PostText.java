package com.test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.aizone.blockchain.utils.JsonVo;
import com.aizone.blockchain.web.vo.TransactionVo;
import com.google.gson.Gson;

public class PostText {

	public static void main(String[] args) throws Exception {
		Map<String, Object> map =new HashMap<String, Object>();
		map.put("Sender", 1);
		map.put("Recipient", 2);
		map.put("Amount", 3);
		map.put("PublicKey", 4);
		map.put("Sign", 5);
		map.put("Timestamp", 6);
		map.put("TxHash", 7);
		String url="http://192.168.108.1:8081/transactions/new1";
//		post(url,map);
		BigDecimal amount1 = new BigDecimal(Double.parseDouble("10")*5000+"");
		BigDecimal amount2 =new BigDecimal(10).multiply(new BigDecimal("0.00000009995"));
//		BigDecimal amount2 =new BigDecimal("0.0002").multiply(new BigDecimal("3995"));
		BigDecimal b=new BigDecimal("1.8390800000000003");
		BigDecimal c=new BigDecimal("100000000");
		BigDecimal d = BigDecimal.valueOf(Double.valueOf("0.123456"));
		String Amount =b.divide(c)+"";
		System.out.println(d);
		Gson gson=new Gson();
		String ss=gson.toJson(map);
		System.out.println(map);
		System.out.println(ss);

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
		StringEntity s=new StringEntity(gson.toJson(map),"utf-8");
		String ss=gson.toJson(map);
		System.out.println(ss);
		System.out.println(s);
		//[Content-Type: text/plain; charset=UTF-8,Content-Length: 25,Chunked: false]
		System.out.println(httpPost.getEntity());
		HttpResponse httpResponse = httpClient.execute(httpPost);
		// 拿到实体
		HttpEntity httpEntity = httpResponse.getEntity();
		//ResponseEntityProxy{[Content-Type: text/html,Content-Length: 17931,Chunked: false]}
		System.out.println(httpEntity);
		// 获取结果，这里可以正对相应的数据精细字符集的转码
		String result = "";
		if (httpEntity != null) {
			result = EntityUtils.toString(httpEntity, "utf-8");
			System.out.println(result);
			System.out.println("***/***");
		}
		// 关闭连接
		httpPost.releaseConnection();
		return result;
	}

	
	@PostMapping("/transactions/new1")
	public JsonVo sendTransaction(@RequestBody TransactionVo txVo) throws Exception {
		System.out.println("---------------");
		System.out.println(txVo);
		
		return null;
		
	}


}
