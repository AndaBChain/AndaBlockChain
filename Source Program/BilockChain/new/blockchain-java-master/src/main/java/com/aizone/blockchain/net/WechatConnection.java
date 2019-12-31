package com.aizone.blockchain.net;

import com.aizone.blockchain.utils.WechatUtil;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 微信连接（未测试）
 * @author Kelly
 *
 */
public class WechatConnection
{
  public static final String SUCCESS_CODE = "SUCCESS";
  
  public static Object connect(String url, String info, Class<?> object)
    throws IOException
  {
    HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
    conn.setConnectTimeout(8000);
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    

    BufferedOutputStream bos = new BufferedOutputStream(conn.getOutputStream());
    bos.write(info.getBytes());
    bos.flush();
    bos.close();
    

    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    


    StringBuffer str = new StringBuffer();
    String line; while ((line = reader.readLine()) != null) {
      str.append(line);
    }
    
    return WechatUtil.truncateDataFromXML(object, str.toString());
  }
}

