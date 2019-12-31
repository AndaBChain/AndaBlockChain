package com.aizone.blockchain.model;

import java.io.Serializable;
/**
 * 微信查询结果实体类（未测试）
 * @author Kelly
 *
 */
public class OrderQueryResult
  implements Serializable
{
  private static final long serialVersionUID = -1996103742747816922L;
  private String return_code;
  private String appid;
  private String mch_id;
  private String nonce_str;
  private String sign;
  private String result_code;
  private String openid;
  private String trade_type;
  private String trade_state;
  private String bank_type;
  private int total_fee;
  private int cash_fee;
  private String transaction_id;
  private String out_trade_no;
  private String time_end;
  private String trade_state_desc;
  
  public OrderQueryResult() {}
  
  public OrderQueryResult(String return_code, String appid, String mch_id, String nonce_str, String sign, String result_code, String openid, String trade_type, String trade_state, String bank_type, int total_fee, int cash_fee, String transaction_id, String out_trade_no, String time_end, String trade_state_desc)
  {
    this.return_code = return_code;
    this.appid = appid;
    this.mch_id = mch_id;
    this.nonce_str = nonce_str;
    this.sign = sign;
    this.result_code = result_code;
    this.openid = openid;
    this.trade_type = trade_type;
    this.trade_state = trade_state;
    this.bank_type = bank_type;
    this.total_fee = total_fee;
    this.cash_fee = cash_fee;
    this.transaction_id = transaction_id;
    this.out_trade_no = out_trade_no;
    this.time_end = time_end;
    this.trade_state_desc = trade_state_desc;
  }
  
  public String getReturn_code() {
    return this.return_code;
  }
  
  public void setReturn_code(String return_code) {
    this.return_code = return_code;
  }
  
  public String getAppid() {
    return this.appid;
  }
  
  public void setAppid(String appid) {
    this.appid = appid;
  }
  
  public String getMch_id() {
    return this.mch_id;
  }
  
  public void setMch_id(String mch_id) {
    this.mch_id = mch_id;
  }
  
  public String getNonce_str() {
    return this.nonce_str;
  }
  
  public void setNonce_str(String nonce_str) {
    this.nonce_str = nonce_str;
  }
  
  public String getSign() {
    return this.sign;
  }
  
  public void setSign(String sign) {
    this.sign = sign;
  }
  
  public String getResult_code() {
    return this.result_code;
  }
  
  public void setResult_code(String result_code) {
    this.result_code = result_code;
  }
  
  public String getOpenid() {
    return this.openid;
  }
  
  public void setOpenid(String openid) {
    this.openid = openid;
  }
  
  public String getTrade_type() {
    return this.trade_type;
  }
  
  public void setTrade_type(String trade_type) {
    this.trade_type = trade_type;
  }
  
  public String getTrade_state() {
    return this.trade_state;
  }
  
  public void setTrade_state(String trade_state) {
    this.trade_state = trade_state;
  }
  
  public String getBank_type() {
    return this.bank_type;
  }
  
  public void setBank_type(String bank_type) {
    this.bank_type = bank_type;
  }
  
  public int getTotal_fee() {
    return this.total_fee;
  }
  
  public void setTotal_fee(int total_fee) {
    this.total_fee = total_fee;
  }
  
  public int getCash_fee() {
    return this.cash_fee;
  }
  
  public void setCash_fee(int cash_fee) {
    this.cash_fee = cash_fee;
  }
  
  public String getTransaction_id() {
    return this.transaction_id;
  }
  
  public void setTransaction_id(String transaction_id) {
    this.transaction_id = transaction_id;
  }
  
  public String getOut_trade_no() {
    return this.out_trade_no;
  }
  
  public void setOut_trade_no(String out_trade_no) {
    this.out_trade_no = out_trade_no;
  }
  
  public String getTime_end() {
    return this.time_end;
  }
  
  public void setTime_end(String time_end) {
    this.time_end = time_end;
  }
  
  public String getTrade_state_desc() {
    return this.trade_state_desc;
  }
  
  public void setTrade_state_desc(String trade_state_desc) {
    this.trade_state_desc = trade_state_desc;
  }
  
  public String toString()
  {
    return "OrderQueryResult [return_code=" + this.return_code + ", appid=" + this.appid + ", mch_id=" + this.mch_id + ", nonce_str=" + this.nonce_str + ", sign=" + this.sign + ", result_code=" + this.result_code + ", openid=" + this.openid + ", trade_type=" + this.trade_type + ", trade_state=" + this.trade_state + ", bank_type=" + this.bank_type + ", total_fee=" + this.total_fee + ", cash_fee=" + this.cash_fee + ", transaction_id=" + this.transaction_id + ", out_trade_no=" + this.out_trade_no + ", time_end=" + this.time_end + ", trade_state_desc=" + this.trade_state_desc + "]";
  }
}

