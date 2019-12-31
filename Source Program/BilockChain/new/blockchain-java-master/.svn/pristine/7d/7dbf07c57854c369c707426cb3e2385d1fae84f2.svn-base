package com.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.spongycastle.util.encoders.Hex;

import com.aizone.blockchain.core.Transaction;
import com.google.gson.Gson;

public final class walletTxTest {

	private walletTxTest() {
	}

	/**
	 * post请求
	 * 
	 * @param url
	 * @param headers
	 * @param params
	 *            参数
	 * @return
	 * @throws Exception
	 * @throws ParseException
	 */
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

	public static void main(String[] args) {

		String seed = "esrhgzthbsyz";
		try {
			ArrayList<String> seenwords = new ArrayList<>();
			for (String word : seed.trim().split(" ")) {
				if (word.isEmpty())
					continue;
				seenwords.add(word);
			}

			Wallet wallet = new Wallet(seenwords, "aa123456789aa");
			Transaction tx = new Transaction();
			
			Transaction tx1 = new Transaction();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ip", "123.232.102.22");
			map.put("port", 5678);
			//8f863ab09ab147df18755ea64a679294594a1266 服务器地址
//			String sender="r2TpfR7CGtD97rTsTjrwNUNT6wYEUsT34p";//服务器 发送账户地址
			String sender = "b7578430db8b62e1a71c5350ce6d8dc74d72d9aa";//本机
			String recipient = "qgub3qKXSsYRSoPY1vPjdpURsCVJgVhf2W";//接受地址
			String amount = "20";//发送金额
			
			map.put("sender", sender);
			tx.setSender(sender);
			tx1.setSender(sender);
			map.put("recipient", recipient);
			tx.setRecipient(recipient);
			tx1.setRecipient(recipient);
//			map.put("amount",Double.valueOf(amount));
//			tx.setAmount(BigDecimal.valueOf(Double.valueOf(amount)));
//			tx1.setAmount(BigDecimal.valueOf(Double.valueOf(amount)));
			map.put("amount",new BigDecimal(amount));
			tx.setAmount(new BigDecimal(amount));
			tx1.setAmount(new BigDecimal(amount));

			String publicKey = Hex.toHexString(wallet.getMasterKey().getPubKey());

			System.out.println("公钥:" + publicKey);
			map.put("publicKey", publicKey);
			tx.setTimestamp(System.currentTimeMillis());
			tx1.setTimestamp(System.currentTimeMillis());
			//map.put("data", "android");
			//tx.setData("android");
			//tx1.setData("android");
			map.put("timestamp", tx.getTimestamp().toString());
			map.put("txHash", tx.hash());

			System.out.println("原始未签名交易数据" + tx.toStringOragin());
			System.out.println("----------------------------");

			org.bitcoinj.core.ECKey eckey1 = org.bitcoinj.core.ECKey
					.fromPrivate(wallet.getMasterKey().getPrivKeyBytes());

			org.bitcoinj.core.ECKey eckey2 = org.bitcoinj.core.ECKey.fromPublicOnly(wallet.getMasterKey().getPubKey());
			// String sign1 = eckey1.signMessage(tx.toStringOragin());
			String data = tx.toStringOragin();
			String sign = eckey1.signMessage(data);

			map.put("sign", sign);

			System.out.println("-----------------------");
			System.out.println("签名:" + sign);
			System.out.println("------------------------------");

			boolean verb = false;
			try {
				String data1 = tx1.toStringOragin();
				eckey2.verifyMessage(data1, sign);
				verb = true;
			} catch (Exception e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
				verb = false;
			}
			System.out.println("签名验证:" + verb);
			// map = null;
			// System.out.println(map.get("amount"));
			long starttime = System.currentTimeMillis();
			System.out.println("付款地址："+map.get("sender"));
			System.out.println("接收地址："+map.get("recipient"));
				//String string = args[i];
			//123.232.102.22  192.168.0.12 亚马逊13.251.77.125
				System.out.println(post("http://192.168.108.1:8081/chain/transactions/new",map));
				
			
			long endtime = System.currentTimeMillis();
			
			long spendtime = endtime - starttime;
			System.out.println("花费时间为:"+spendtime /1000 +"秒");
			 //System.out.println(post("http://123.232.102.22:8081/chain/transactions/new",map));
			// System.out.println(post("http://127.0.0.1:8080/account/getAccountBalance",
			// map));
			// System.out.println(post("http://127.0.0.1:8080/account/getAccountBalance",
			// map));

			// System.out.println(post("http://127.0.0.1:8080/account/commitnew",
			// map));
			// System.out.println(post("http://127.0.0.1:8080/chain/node/add",
			// map));
			//System.out.println(post("http://127.0.0.1:8080/account/new", map));
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}

	}

}
