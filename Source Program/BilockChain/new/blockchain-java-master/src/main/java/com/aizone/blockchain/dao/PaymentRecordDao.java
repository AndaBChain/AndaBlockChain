package com.aizone.blockchain.dao;

import com.aizone.blockchain.model.PaypalPaymentInfo;
import com.aizone.blockchain.utils.Db;
import com.aizone.blockchain.web.controller.PaypalController;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * paypal支付记录相关dao（大部分功能未使用，只有当进行测试不通过时将信息存数据库便于查找原因时使用logSaveTest方法）
 * @author Kelly
 *
 */
@Component
public class PaymentRecordDao
{
  @Autowired
  PaypalController pc;
  private static final String SFWC_FALSE = "N";
  private static final String SFWC_TRUE = "Y";
  /**
   * 增加paypal付款记录
   * @param payInfo
   * @return
   */
  public boolean addPaymentRecord(PaypalPaymentInfo payInfo)
  {
    Connection conn = null;
    conn = Db.getConnection();
    String sql = "insert into paypal_payment_record(payment_date,payment_status,payment_amount,payment_currency,txn_id,receiver_email,payer_email,is_exchanged,anda_address) values (?,?,?,?,?,?,?,?,?)";
    try {
      PreparedStatement psq = conn.prepareStatement(sql);
      psq.setString(1, payInfo.getPaymentDate());
      psq.setString(2, payInfo.getPaymentStatus());
      psq.setString(3, payInfo.getPaymentAmount());
      psq.setString(4, payInfo.getPaymentCurrency());
      psq.setString(5, payInfo.getTxnId());
      psq.setString(6, payInfo.getReceiverEmail());
      psq.setString(7, payInfo.getPayerEmail());
      psq.setString(8, payInfo.getIsExchanged());
      psq.setString(9, payInfo.getAndaAddress());
      Integer i = Integer.valueOf(psq.executeUpdate());
      System.out.println("20181115--------" + i);
      
      if (i.intValue() == 1) {
        logSaveJl(payInfo.getTxnId() + "", SFWC_TRUE, "SUCCESS", "log_paypal");
        return true;
      }
      
      logSaveJl(payInfo.getTxnId() + "", SFWC_FALSE, "FAIL", "log_paypal");
      return false;
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    } finally {
      Db.closeConnection(conn);
    }
    return false;
  }
  

  /**
   * 检查该交易id是否有记录
   * @param txnId
   * @return
   */
  public boolean CheckTxnId(String txnId)
  {
    Connection conn = null;
    conn = Db.getConnection();
    String sql = "select * from paypal_payment_record where txn_id = ?";
    try {
      PreparedStatement psq = conn.prepareStatement(sql);
      psq.setString(1, txnId);
      ResultSet rs = psq.executeQuery();
      System.out.println("20181115--------" + rs);
      if (!rs.next()) {
        return true;
      }
      return false;
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    } finally {
      Db.closeConnection(conn);
    }
    return false;
  }
  

  /**
   * 查询支付信息详情
   * @param txnId
   * @return
   */
  public PaypalPaymentInfo SelectPaymentResult(String txnId)
  {
    PaypalPaymentInfo payInfo = new PaypalPaymentInfo();
    Connection conn = null;
    conn = Db.getConnection();
    String sql = "select payment_date,payment_status,payment_amount,payment_currency,txn_id,receiver_email,payer_email,is_exchanged,anda_address from paypal_payment_record where txn_id = ?";
    try {
      PreparedStatement psq = conn.prepareStatement(sql);
      psq.setString(1, txnId);
      ResultSet rs = psq.executeQuery();
      System.out.println(System.currentTimeMillis() + "" + rs);
      if (rs.next()) {
        payInfo.setPaymentDate(rs.getString("payment_date"));
        payInfo.setPaymentStatus(rs.getString("payment_status"));
        payInfo.setPaymentAmount(rs.getString("payment_amount"));
        payInfo.setPaymentCurrency(rs.getString("payment_currency"));
        payInfo.setTxnId(rs.getString("txn_id"));
        payInfo.setReceiverEmail(rs.getString("receiver_email"));
        payInfo.setPayerEmail(rs.getString("payer_email"));
        payInfo.setIsExchanged(rs.getString("is_exchanged"));
        payInfo.setAndaAddress(rs.getString("anda_address"));
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
    } finally {
      Db.closeConnection(conn);
    }
    return payInfo;
  }
  

  /**
   * 查询支付金额
   * @param txnId
   * @return
   */
  public String SelectPaymentAmount(String txnId)
  {
    String payment_amount = null;
    Connection conn = null;
    conn = Db.getConnection();
    String sql = "select payment_amount from paypal_payment_record where txn_id = ?";
    try {
      PreparedStatement psq = conn.prepareStatement(sql);
      psq.setString(1, txnId);
      ResultSet rs = psq.executeQuery();
      System.out.println(System.currentTimeMillis() + "" + rs);
      if (rs.next()) {
        payment_amount = rs.getString("payment_amount");
      } else {
        payment_amount = "FAIL";
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
    } finally {
      Db.closeConnection(conn);
    }
    return payment_amount;
  }
  

  /**
   * 更新paypal支付记录
   * @param txnId
   * @param andaAddress
   */
  public void UpdatePaypalPaymentRecord(String txnId, String andaAddress)
  {
    Connection conn = null;
    conn = Db.getConnection();
    String sql = "update paypal_payment_record set is_exchanged = ?,anda_address = ? where txn_id = ?";
    try {
      PreparedStatement psq = conn.prepareStatement(sql);
      psq.setString(1, "true");
      psq.setString(2, andaAddress);
      psq.setString(3, txnId);
      int affectRows = psq.executeUpdate();
      System.out.println(System.currentTimeMillis() + ":" + affectRows);
      if (affectRows >= 1) {
        System.out.println("更新完成");
      }
      else {
        System.out.println("更新失败");
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
    } finally {
      Db.closeConnection(conn);
    }
  }
  

  /**
   * 保存paypal兑换时日志
   * @param id
   * @param result
   * @param reason
   * @param table
   */
  public void logSaveJl(String id, String result, String reason, String table)
  {
    Connection conn = null;
    conn = Db.getConnection();
    String sql = "insert into " + table + "(id,result,reason,time) values (?,?,?,?)";
    try
    {
      PreparedStatement psq = conn.prepareStatement(sql);
      psq.setString(1, id);
      psq.setString(2, result);
      psq.setString(3, reason);
      psq.setDate(4, new Date(System.currentTimeMillis()));
      psq.executeUpdate();
    }
    catch (SQLException e) {
      e.printStackTrace();
    } finally {
      Db.closeConnection(conn);
    }
  }
  
  public void logSaveTest(String id, String result, String table)
  {
    Connection conn = null;
    conn = Db.getConnection();
    String sql = "insert into " + table + "(id,result) values (?,?)";
    try
    {
      PreparedStatement psq = conn.prepareStatement(sql);
      psq.setString(1, id);
      psq.setString(2, result);
      psq.executeUpdate();
    }
    catch (SQLException e) {
      e.printStackTrace();
    } finally {
      Db.closeConnection(conn);
    }
  }
  




  /**
   * 测试paypal付款兑换时，将信息保存到数据库中方便查找失败原因
   * @param id
   * @param result
   * @author Kelly
   */
  public void logSaveTest(String id, String result)
  {
    Connection conn = null;
    conn = Db.getConnection();
    String sql = "insert into paypal_pay_log_test(id,result,time) values (?,?,?)";
    try {
      PreparedStatement psq = conn.prepareStatement(sql);
      psq.setString(1, id);
      psq.setString(2, result);
      SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String time = dateformat.format(Long.valueOf(System.currentTimeMillis()));
      psq.setString(3, time);
      psq.executeUpdate();
    }
    catch (SQLException e) {
      e.printStackTrace();
    } finally {
      Db.closeConnection(conn);
    }
  }
  





  public void logSaveTest2(String id, String result, String time)
  {
    Connection conn = null;
    conn = Db.getConnection();
    String sql = "insert into paypal_pay_log_test(id,result,time) values (?,?,?)";
    try {
      PreparedStatement psq = conn.prepareStatement(sql);
      psq.setString(1, id);
      psq.setString(2, result);
      psq.setString(3, time);
      psq.executeUpdate();
    }
    catch (SQLException e) {
      e.printStackTrace();
    } finally {
      Db.closeConnection(conn);
    }
  }
}

