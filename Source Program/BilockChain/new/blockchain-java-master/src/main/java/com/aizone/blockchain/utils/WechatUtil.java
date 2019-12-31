package com.aizone.blockchain.utils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;


/**
 * 微信查询用工具（未使用、未测试）
 * @author Administrator
 *
 */
public class WechatUtil
{
  public static String truncateDataToXML(Class<?> object, Object obj)
  {
    XStream xStream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("_-", "_")));
    xStream.alias("xml", object);
    
    return xStream.toXML(obj);
  }
  


  public static Object truncateDataFromXML(Class<?> object, String str)
  {
    XStream xStream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("_-", "_")));
    xStream.alias("xml", object);
    
    return xStream.fromXML(str);
  }
}


