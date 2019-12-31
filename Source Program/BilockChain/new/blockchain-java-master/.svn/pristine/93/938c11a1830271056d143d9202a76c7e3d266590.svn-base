package com.aizone.blockchain.model;

import java.io.Serializable;

/**
 * 微信订单查询参数实体类（未测试）
 * @author Kelly
 *
 */
public class OrderQueryParams
  implements Serializable
{
  private static final long serialVersionUID = -168458096490563992L;
  private String appid;
  private String mch_id;
  private String out_trade_no;
  private String nonce_str;
  private String sign;
  
  public OrderQueryParams() {}
  
  public OrderQueryParams(String appid, String mch_id, String out_trade_no, String nonce_str, String sign)
  {
    this.appid = appid;
    this.mch_id = mch_id;
    this.out_trade_no = out_trade_no;
    this.nonce_str = nonce_str;
    this.sign = sign;
  }
  
  public String getAppid() { return this.appid; }
  
  public void setAppid(String appid) {
    this.appid = appid;
  }
  
  public String getMch_id() { return this.mch_id; }
  
  public void setMch_id(String mch_id) {
    this.mch_id = mch_id;
  }
  
  public String getOut_trade_no() { return this.out_trade_no; }
  
  public void setOut_trade_no(String out_trade_no) {
    this.out_trade_no = out_trade_no;
  }
  
  public String getNonce_str() { return this.nonce_str; }
  
  public void setNonce_str(String nonce_str) {
    this.nonce_str = nonce_str;
  }
  
  public String getSign() { return this.sign; }
  
  public void setSign(String sign) {
    this.sign = sign;
  }
  
  public String toString() {
    return "OrderQueryParams [appid=" + this.appid + ", mch_id=" + this.mch_id + ", out_trade_no=" + this.out_trade_no + ", nonce_str=" + this.nonce_str + ", sign=" + this.sign + "]";
  }
}

