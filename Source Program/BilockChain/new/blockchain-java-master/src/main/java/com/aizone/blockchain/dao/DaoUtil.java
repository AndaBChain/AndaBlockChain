package com.aizone.blockchain.dao;

import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bitcoinj.crypto.MnemonicException;
import org.springframework.context.support.StaticApplicationContext;

import com.google.gson.Gson;

public class DaoUtil {

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

	/**
	 * 模拟安达钱包
	 * 
	 * @return
	 * @throws MnemonicException
	 */
	public static Wallet getWallet() throws MnemonicException {
		String seed = "esrhgzthbsyz";
		ArrayList<String> seenwords = new ArrayList<>();
		for (String word : seed.trim().split(" ")) {
			if (word.isEmpty())
				continue;
			seenwords.add(word);
		}

		Wallet wallet = new Wallet(seenwords, "aa123456789aa");
		return wallet;
	}
	/**
	 * 汇率转换
	 * @return
	 */
	public static int exchangeRate(int amount){
		amount = amount * 1000;
		return amount;
		
	}
}
