package com.aizone.blockchain.web.controller;

import com.aizone.blockchain.conf.Settings;
import com.aizone.blockchain.constants.TransactionConstants;
import com.aizone.blockchain.core.Block;
import com.aizone.blockchain.core.BlockBody;
import com.aizone.blockchain.core.BlockChain;
import com.aizone.blockchain.core.BlockHeader;
import com.aizone.blockchain.core.Transaction;
import com.aizone.blockchain.dao.TransactionRecordDao;
import com.aizone.blockchain.model.OrderQueryParams;
import com.aizone.blockchain.model.OrderQueryResult;
import com.aizone.blockchain.net.WechatConnection;
import com.aizone.blockchain.utils.CreateAFile;
import com.aizone.blockchain.utils.Db;
import com.aizone.blockchain.utils.JsonVo;
import com.aizone.blockchain.utils.WechatUtil;
import com.aizone.blockchain.utils.XMLUtils;
import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.TestNet3Params;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * 微信支付查询及兑换（未使用，未测试）
 * @author Kelly
 *
 */
@RestController
@RequestMapping({"/wx/payment"})
public class WXPaymentController
{
  private static Logger logger = LoggerFactory.getLogger(WXPaymentController.class);
  
  private static final String ANDA_SERVER_ANDA_ADDRESS = "8f863ab09ab147df18755ea64a679294594a1266";
  
  @Autowired
  private BlockChain blockChain;
  
  @Autowired
  private Settings settings;
  
  @Autowired
  private TransactionRecordDao txRecord;
  

  @PostMapping({"/getResult"})
  public JsonVo selectWXPaymentResult(@RequestBody Map<String, String> map)
  {
    logSaveTest("selectWXPaymentResult0", map.toString());
    String appid = (String)map.get("appid");
    String mch_id = (String)map.get("mch_id");
    String nonce_str = (String)map.get("nonce_str");
    String out_trade_no = (String)map.get("out_trade_no");
    String sign = (String)map.get("sign");
    String andaAddress = (String)map.get("andaAddress");
    JsonVo vo = new JsonVo();
    Map<String, String> map_ = new HashMap();
    map_.put("appid", appid);
    map_.put("mch_id", mch_id);
    map_.put("nonce_str", nonce_str);
    map_.put("out_trade_no", out_trade_no);
    map_.put("sign", sign);
    logSaveTest("selectWXPaymentResult1", map_.toString());
    Map<String, Object> result = getPayResult(map_);
    logSaveTest("selectWXPaymentResult2", result.toString());
    double amount = 0.0D;
    boolean re = ((Boolean)result.get("select_status")).booleanValue();
    logSaveTest("selectWXPaymentResult3", String.valueOf(re));
    Map<String, String> mapNew = new HashMap();
    if (re) {
      int cnyAmount = ((Integer)result.get("cash_fee")).intValue();
      logSaveTest("selectWXPaymentResult4", String.valueOf(cnyAmount));
      amount = cnyAmount / 100.0D;
      logSaveTest("selectWXPaymentResult5", String.valueOf(amount));
      BigDecimal andaAmountActual = excuteExchangeTransaction(andaAddress, Double.valueOf(amount));
      logSaveTest("selectWXPaymentResult6", String.valueOf(andaAmountActual));
      vo.setCode(200);
      vo.setMessage("支付成功");
      String payResult = amount + "元支付完成,兑换安达通证：" + andaAmountActual + "个";
      mapNew.put("paymentResult", payResult);
    } else if ("查询订单失败".equals((String)result.get("select_order"))) {
      logSaveTest("selectWXPaymentResult7", "查询订单失败");
      mapNew.put("paymentResult", "查询订单失败");
      vo.setCode(400);
      vo.setMessage("查询订单失败");
    } else {
      logSaveTest("selectWXPaymentResult8", "查询订单失败或支付失败");
      mapNew.put("paymentResult", "查询订单失败或支付失败");
      vo.setCode(400);
      vo.setMessage("查询订单失败或支付失败");
    }
    vo.setItem(mapNew);
    return vo;
  }
  
  public Map<String, Object> getPayResult(Map<String, String> map_) {
    logSaveTest("getPayResult0", map_.toString());
    String orderInfo = generateOrderInfo(map_);
    logSaveTest("getPayResult1", orderInfo);
    OrderQueryResult ext = null;
    Map<String, Object> map = new HashMap();
    try {
      ext = (OrderQueryResult)WechatConnection.connect("https://api.mch.weixin.qq.com/pay/orderquery", orderInfo, OrderQueryResult.class);
      logSaveTest("getPayResult2", ext.toString());
    } catch (IOException e) {
      logSaveTest("getPayResult3", "微信查询订单" + orderInfo + "失败！");
      logger.error("微信查询订单" + orderInfo + "失败！", e);
      map.put("select_status", Boolean.valueOf(false));
      map.put("select_order", "查询订单失败");
    }
    
    if (ext == null) {
      logSaveTest("getPayResult4", "ext == null");
      map.put("select_status", Boolean.valueOf(false));
    }
    
    if ("SUCCESS".equals(ext.getResult_code())) {
      logSaveTest("getPayResult5", "Result_code=success");
      if (("SUCCESS".equals(ext.getResult_code())) && ("SUCCESS".equals(ext.getTrade_state()))) {
        logSaveTest("getPayResult6", "Result_code=success&....");
        

        int cash_fee = ext.getCash_fee();
        map.put("select_status", Boolean.valueOf(true));
        map.put("cash_fee", Integer.valueOf(cash_fee));
      } else {
        logSaveTest("getPayResul7", "订单" + orderInfo + "交易失败，交易状态：" + ext.getTrade_state());
        logger.error("订单" + orderInfo + "交易失败，交易状态：" + ext.getTrade_state());
        map.put("select_status", Boolean.valueOf(false));
      }
    } else {
      logSaveTest("getPayResult8", "订单" + orderInfo + "查询失败！");
      logger.error("订单" + orderInfo + "查询失败！");
      map.put("select_status", Boolean.valueOf(false));
    }
    
    return map;
  }
  
  private String generateOrderInfo(Map<String, String> map_) {
    logSaveTest("generateOrderInfo", map_.toString());
    OrderQueryParams ext = new OrderQueryParams();
    ext.setAppid((String)map_.get("appid"));
    ext.setMch_id((String)map_.get("mch_id"));
    ext.setOut_trade_no((String)map_.get("out_trade_no"));
    ext.setNonce_str((String)map_.get("nonce_str"));
    ext.setSign((String)map_.get("sign"));
    logSaveTest("generateOrderInfo", ext.toString());
    return WechatUtil.truncateDataToXML(OrderQueryParams.class, ext);
  }
  












  @PostMapping({"/getWeChatPayReturn"})
  public String getWeChatPayReturn(HttpServletRequest request)
  {
    logSaveTest("getWeChatPayReturn", request.toString());
    
    String appid = "";
    
    String mch_id = "";
    
    String device_info = "";
    
    String nonce_str = "";
    
    String sign = "";
    
    String result_code = "";
    
    String err_code = "";
    
    String err_code_des = "";
    
    String openid = "";
    
    String is_subscribe = "";
    
    String trade_type = "";
    
    String bank_type = "";
    
    int total_fee = 0;
    
    String fee_type = "";
    
    int cash_fee = 0;
    
    String cash_fee_type = "";
    
    int coupon_fee = 0;
    
    int coupon_count = 0;
    
    String coupon_id_$n = "";
    
    int coupon_fee_$n = 0;
    
    String transaction_id = "";
    
    String out_trade_no = "";
    
    String attach = "";
    
    String time_end = "";
    try {
      InputStream inStream = request.getInputStream();
      logSaveTest("getWeChatPayReturn", inStream.toString());
      int _buffer_size = 1024;
      if (inStream != null) {
        logSaveTest("getWeChatPayReturn", inStream.toString());
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] tempBytes = new byte[_buffer_size];
        int count = -1;
        while ((count = inStream.read(tempBytes, 0, _buffer_size)) != -1) {
          outStream.write(tempBytes, 0, count);
        }
        tempBytes = null;
        outStream.flush();
        
        String result = new String(outStream.toByteArray(), "UTF-8");
        logSaveTest("getWeChatPayReturn", result);
        
        Document doc = DocumentHelper.parseText(result);
        logSaveTest("getWeChatPayReturn", doc.toString());
        
        Map<String, Object> resultMap = XMLUtils.Dom2Map(doc);
        logSaveTest("getWeChatPayReturn", resultMap.toString());
        
        appid = resultMap.get("appid") + "";
        
        mch_id = resultMap.get("mch_id") + "";
        
        device_info = resultMap.get("appid") + "";
        
        nonce_str = resultMap.get("nonce_str") + "";
        
        sign = resultMap.get("sign") + "";
        
        result_code = resultMap.get("result_code") + "";
        
        err_code = resultMap.get("err_code") + "";
        
        err_code_des = resultMap.get("err_code_des") + "";
        
        openid = resultMap.get("openid") + "";
        
        is_subscribe = resultMap.get("is_subscribe") + "";
        
        trade_type = resultMap.get("trade_type") + "";
        
        bank_type = resultMap.get("bank_type") + "";
        
        total_fee = ((Integer)resultMap.get("total_fee")).intValue();
        
        fee_type = resultMap.get("fee_type") + "";
        
        cash_fee = ((Integer)resultMap.get("cash_fee")).intValue();
        
        cash_fee_type = resultMap.get("cash_fee_type") + "";
        
        coupon_fee = ((Integer)resultMap.get("coupon_fee")).intValue();
        
        coupon_count = ((Integer)resultMap.get("coupon_count")).intValue();
        
        coupon_id_$n = resultMap.get("coupon_id_$n") + "";
        
        coupon_fee_$n = ((Integer)resultMap.get("coupon_fee_$n")).intValue();
        
        transaction_id = resultMap.get("transaction_id") + "";
        
        out_trade_no = resultMap.get("out_trade_no") + "";
        
        attach = resultMap.get("attach") + "";
        
        time_end = resultMap.get("time_end") + "";
      }
      

      String resultNew = "<xml><appid><![CDATA[" + appid + "]]></appid> <mch_id><![CDATA[" + mch_id + "]]></mch_id> <device_info><![CDATA[" + device_info + "]]></device_info> <nonce_str><![CDATA[" + nonce_str + "]]></nonce_str> <sign><![CDATA[" + sign + "]]></sign> <result_code><![CDATA[" + result_code + "]]></result_code> <err_code><![CDATA[" + err_code + "]]></err_code> <err_code_des><![CDATA[" + err_code_des + "]]></err_code_des> <openid><![CDATA[" + openid + "]]></openid> <is_subscribe><![CDATA[" + is_subscribe + "]]></is_subscribe> <trade_type><![CDATA[" + trade_type + "]]></trade_type> <bank_type><![CDATA[" + bank_type + "]]></bank_type> <total_fee><![CDATA[" + total_fee + "]]></total_fee> <fee_type><![CDATA[" + fee_type + "]]></fee_type> <cash_fee><![CDATA[" + cash_fee + "]]></cash_fee> <cash_fee_type><![CDATA[" + cash_fee_type + "]]></cash_fee_type> <coupon_fee><![CDATA[" + coupon_fee + "]]></coupon_fee> <coupon_count><![CDATA[" + coupon_count + "]]></coupon_count> <coupon_id_$n><![CDATA[" + coupon_id_$n + "]]></coupon_id_$n> <coupon_fee_$n><![CDATA[" + coupon_fee_$n + "]]></coupon_fee_$n> <transaction_id><![CDATA[" + transaction_id + "]]></transaction_id> <out_trade_no><![CDATA[" + out_trade_no + "]]></out_trade_no> <attach><![CDATA[" + attach + "]]></attach> <time_end><![CDATA[" + time_end + "]]></time_end></xml>";
      























      logSaveTest("getWeChatPayReturn", resultNew);
      return resultNew;
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    
    return "fail";
  }
  













  public BigDecimal excuteExchangeTransaction(String andaAddress, Double cnyAmount)
  {
    TransactionRecordDao dao = new TransactionRecordDao();
    Map<String, String> resultMap = dao.exchangeRate("CNY");
    String exchangeRate = (String)resultMap.get("exchangeRate");
    BigDecimal bdUsdAmount = new BigDecimal(cnyAmount.doubleValue());
    BigDecimal bdExchangeRateNumerator = new BigDecimal("1");
    BigDecimal bdExchangeRateDenominator = new BigDecimal(exchangeRate);
    BigDecimal bdExchangeRate = bdExchangeRateNumerator.divide(bdExchangeRateDenominator, 8, RoundingMode.HALF_UP);
    BigDecimal andaAmountActuall = bdUsdAmount.multiply(bdExchangeRate);
    BigDecimal andaAmountActual = andaAmountActuall.setScale(8, 5);
    Map<String, Object> map_ = new HashMap();
    map_.put("AndaAddress", andaAddress);
    map_.put("andaAmountActual", andaAmountActual);
    try {
      tranSactionRun(map_);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return andaAmountActual;
  }
  





  public String tranSactionRun(@RequestBody Map<String, Object> map_)
    throws Exception
  {
    Wallet wallet = new Wallet(TestNet3Params.get());
    String data_ = UUID.randomUUID().toString();
    
    String sendAddress = ANDA_SERVER_ANDA_ADDRESS;
    Transaction tx = new Transaction();
    tx.setSender(sendAddress);
    tx.setRecipient(map_.get("AndaAddress").toString());
    tx.setAmount(new BigDecimal(map_.get("andaAmountActual") + ""));
    System.out.println(tx.getAmount());
    tx.setTimestamp(Long.valueOf(System.currentTimeMillis()));
    tx.setData(data_);
    String publicKey = Hex.toHexString(wallet.getWatchingKey().getPubKey());
    tx.setPublicKey(publicKey);
    String hash = tx.hash();
    tx.setTxHash(hash);
    
    ECKey eckey1 = ECKey.fromPrivate(new BigInteger(wallet.getWatchingKey().getPrivKeyBytes33()));
    String data = tx.toStringOragin();
    String sign = eckey1.signMessage(data);
    tx.setSign(sign);
    
    Transaction txNew = this.blockChain.sendTransaction(tx);
    this.txRecord.addWXExchangeRecord(txNew);
    
    if (this.settings.isAutoMining()) {
      Block block = this.blockChain.mining();
      
      DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Map<String, Object> mapNew = new HashMap();
      Map<String, Object> map1 = new HashMap();
      Map<String, Object> map2 = new HashMap();
      
      map1.put("index", block.getHeader().getIndex());
      map1.put("hash", block.getHeader().getHash());
      map1.put("previousHash", block.getHeader().getPreviousHash());
      map1.put("difficulty", block.getHeader().getDifficulty());
      map1.put("nonce", block.getHeader().getNonce());
      mapNew.put("BlockHeader", map1);
      map2.put("sender", ANDA_SERVER_ANDA_ADDRESS);
      map2.put("recipient", ((Transaction)block.getBody().getTransactions().get(1)).getRecipient());
      map2.put("publicKey", ((Transaction)block.getBody().getTransactions().get(1)).getPublicKey());
      map2.put("txHash", ((Transaction)block.getBody().getTransactions().get(1)).getTxHash());
      map2.put("USD", new BigDecimal(((Transaction)block.getBody().getTransactions().get(1)).getAmount() + "").divide(new BigDecimal(TransactionConstants.CNY_EXCHANGE_RATE), 8, RoundingMode.HALF_UP));
      map2.put("amount", ((Transaction)block.getBody().getTransactions().get(1)).getAmount());
      map2.put("time", format.format(((Transaction)block.getBody().getTransactions().get(1)).getTimestamp()));
      mapNew.put("Body", map2);
      Gson gson = new Gson();
      String ss = gson.toJson(mapNew);
      
      CreateAFile cc = new CreateAFile();
      System.out.println("块" + format.format(((Transaction)block.getBody().getTransactions().get(1)).getTimestamp()) + "");
      cc.CreateFile(ss, format.format(((Transaction)block.getBody().getTransactions().get(1)).getTimestamp()) + "_" + block.getHeader().getIndex());
      System.out.println("*********************打印块*****************************");
    } else {
      this.blockChain.runTransaction(tx);
    }
    return "SUCCESS";
  }
  





  public void logSaveTest(String id, String result)
  {
    Connection conn = null;
    conn = Db.getConnection();
    String sql = "insert into wx_pay_log_test(id,result) values (?,?)";
    try {
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
}

