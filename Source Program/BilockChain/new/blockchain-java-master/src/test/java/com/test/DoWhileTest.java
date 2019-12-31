package com.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
@Component
public class DoWhileTest {
	
	private static boolean ONLY = true;

	public static void main(String[] args) throws Exception {
		while(true){
			//sendGet("http://123.232.102.22:8081/chain/mine","1");
			sendGet("http://192.168.0.12:8081/chain/mine","1");
		}
		
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
	
	 /**
     * 向指定URL发送GET方法的请求
     * 
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

}
