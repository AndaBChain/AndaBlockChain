package com.aizone.blockchain.web.controller;

import com.aizone.blockchain.conf.Settings;
import com.aizone.blockchain.constants.TransactionConstants;
import com.aizone.blockchain.core.Block;
import com.aizone.blockchain.core.BlockBody;
import com.aizone.blockchain.core.BlockChain;
import com.aizone.blockchain.core.BlockHeader;
import com.aizone.blockchain.core.Transaction;
import com.aizone.blockchain.dao.PaymentRecordDao;
import com.aizone.blockchain.dao.TransactionRecordDao;
import com.aizone.blockchain.db.DBAccess;
import com.aizone.blockchain.model.PaypalPaymentInfo;
import com.aizone.blockchain.utils.CreateAFile;
import com.aizone.blockchain.utils.JsonVo;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * paypal收款记录接口（未使用）
 * @author Kelly
 *
 */
@RestController
@RequestMapping({"/payment"})
public class PaypalController
{
  private static final Logger logger = LoggerFactory.getLogger(PaypalController.class);
  

  private static final String ANDA_SERVER_ANDA_ADDRESS = "8f863ab09ab147df18755ea64a679294594a1266";
  
  @Autowired
  private DBAccess dbAccess;
  
  @Autowired
  private BlockChain blockChain;
  
  @Autowired
  private Settings settings;
  
  @Autowired
  private TransactionRecordDao txRecord;
  
  @Autowired
  private PaymentRecordDao payDao;
  

  public void doGet(HttpServletRequest request) {}
  

  /**
   * paypal支付状态详情入库
   * @param request
   * @param response
   */
  @PostMapping({"/paypal/status"})
  public void payPalStatus(HttpServletRequest request, HttpServletResponse response)
  {
    try
    {
      PaymentRecordDao payDao = new PaymentRecordDao();
      payDao.logSaveTest("PaypalController1", "payPalStatus");
      
      PaymentRecordDao daoo = new PaymentRecordDao();
      daoo.logSaveJl("payPalStatus", "IPN 信息", "入口", "log_paypal");
      Enumeration en = request.getParameterNames();
      String str = "cmd=_notify-validate";
      while (en.hasMoreElements()) {
        String paramName = (String)en.nextElement();
        String paramValue = request.getParameter(paramName);
        str = str + "&" + paramName + "=" + URLEncoder.encode(paramValue, "iso-8859-1");
      }
      
      logger.info("IPN 信息:" + str);
      daoo.logSaveJl("payPalStatus", "IPN 信息", str, "log_paypal");
      
      URL u = new URL("https://www.paypal.com/cgi-bin/webscr");
      URLConnection uc = u.openConnection();
      uc.setDoOutput(true);
      uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      PrintWriter pw = new PrintWriter(uc.getOutputStream());
      pw.println(str);
      pw.close();
      
      BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
      String res = in.readLine();
      in.close();
      
      String paymentDate = request.getParameter("payment_date");
      String paymentStatus = request.getParameter("payment_status");
      String paymentAmount = request.getParameter("mc_gross");
      String paymentCurrency = request.getParameter("mc_currency");
      String txnId = request.getParameter("txn_id");
      String receiverEmail = request.getParameter("receiver_email");
      String payerEmail = request.getParameter("payer_email");
      String aaa = paymentDate + "," + paymentStatus + "," + paymentAmount + "," + paymentCurrency + "," + txnId + "," + receiverEmail + "," + payerEmail;
      daoo.logSaveJl("payPalStatus", "IPN 信息", aaa, "log_paypal");
      payDao.logSaveTest("PaypalController2", aaa);
      logger.info("付款 信息:" + aaa);
      payDao.logSaveTest("PaypalController22", res);
      

      if (res.equalsIgnoreCase("VERIFIED"))
      {

        payDao.logSaveTest("PaypalController22", paymentStatus + "---" + txnId + "---" + 
          String.valueOf(payDao.CheckTxnId(txnId)) + "----" + receiverEmail + "----" + paymentCurrency);
        
        if ((paymentStatus.equalsIgnoreCase("Completed")) && (payDao.CheckTxnId(txnId)) && (receiverEmail.equals("1050710445@qq.com")) && (paymentCurrency.equals("USD"))) {
          String isExchanged = "false";
          String andaAddress = null;
          PaypalPaymentInfo payInfo = new PaypalPaymentInfo(paymentDate, paymentStatus, paymentAmount, paymentCurrency, txnId, receiverEmail, payerEmail, isExchanged, andaAddress);
          
          payDao.logSaveTest("PaypalController3", String.valueOf(payInfo));
          if (payDao.CheckTxnId(txnId)) {
            boolean b = payDao.addPaymentRecord(payInfo);
            daoo.logSaveJl("payPalStatus", "入库 信息", payInfo.toString(), "log_paypal");
            payDao.logSaveTest("PaypalController4", String.valueOf(b));
            logger.info("入库 信息" + payInfo.toString());
          }
        }
        else {
          daoo.logSaveJl("payPalStatus", "IPN 信息", "付款未完成或txnId已存在或接收者地址不对", "log_paypal");
          logger.info("付款未完成或txnId已存在或接收者地址不对");
        }
      } else if (res.equals("INVALID"))
      {
        PaypalPaymentInfo payInfo = new PaypalPaymentInfo(paymentDate, paymentStatus, paymentAmount, paymentCurrency, txnId, receiverEmail, payerEmail, null, null);
        daoo.logSaveJl("payPalStatus", "非法 信息：INVALID", payInfo.toString(), "log_paypal");
        logger.info("非法 信息:" + payInfo);
        logger.error("paypal完成支付发送IPN通知返回状态非法，请联系管理员，请求参数：" + str);
        logger.error("Class: " + getClass().getName() + " method: " + Thread.currentThread().getStackTrace()[1].getMethodName());
      } else {
        PaypalPaymentInfo payInfo = new PaypalPaymentInfo(paymentDate, paymentStatus, paymentAmount, paymentCurrency, txnId, receiverEmail, payerEmail, null, null);
        daoo.logSaveJl("payPalStatus", "未知 信息", payInfo.toString(), "log_paypal");
        logger.info("未知信息：" + payInfo);
      }
    } catch (Exception e) {
      PaymentRecordDao daoo = new PaymentRecordDao();
      daoo.logSaveJl("payPalStatus", "IPN 信息", "连接paypal失败", "log_paypal");
      logger.info("连接paypal失败");
      logger.error("确认付款信息发生IO异常" + e.getMessage());
      logger.error("Class: " + getClass().getName() + " method: " + Thread.currentThread().getStackTrace()[1].getMethodName());
      e.printStackTrace();
    }
  }
  



  /**
   * 检查支付结果
   * @param request
   * @return
   */
  @PostMapping({"/checkPaymentResult"})
  public JsonVo checkPaymentResult(HttpServletRequest request)
  {
    String txnId = request.getParameter("txnId");
    String andaAddress = request.getParameter("andaAddress");
    PaymentRecordDao payDao = new PaymentRecordDao();
    String usdAmount = payDao.SelectPaymentAmount(txnId);
    if (usdAmount.equals("FAIL")) {
      JsonVo vo = JsonVo.fail();
      Map<String, Object> map = new HashMap();
      map.put("payStatus", "FAIL");
      vo.setItem(map);
      return vo;
    }
    JsonVo vo = selectPaymentResult(txnId, andaAddress, usdAmount);
    return vo;
  }
  

  /**
   * 查询paypal支付结果
   * @param txnId
   * @param andaAddress
   * @param usdAmount
   * @return
   */
  public JsonVo selectPaymentResult(String txnId, String andaAddress, String usdAmount)
  {
    PaymentRecordDao payDao = new PaymentRecordDao();
    PaypalPaymentInfo payInfo = payDao.SelectPaymentResult(txnId);
    Map<String, Object> map = new HashMap();
    JsonVo resultVo = null;
    if (payInfo != null) {
      if (payInfo.getIsExchanged().equals("false")) {
        TransactionRecordDao dao = new TransactionRecordDao();
        Map<String, String> resultMap = dao.exchangeRate("USD");
        String exchangeRate = (String)resultMap.get("exchangeRate");
        BigDecimal bdUsdAmount = new BigDecimal(usdAmount);
        BigDecimal bdExchangeRateNumerator = new BigDecimal("1");
        BigDecimal bdExchangeRateDenominator = new BigDecimal(exchangeRate);
        BigDecimal bdExchangeRate = bdExchangeRateNumerator.divide(bdExchangeRateDenominator, 4, RoundingMode.HALF_UP);
        BigDecimal andaAmountActual = bdUsdAmount.multiply(bdExchangeRate);
        Map<String, Object> map_ = new HashMap();
        map_.put("AndaAddress", andaAddress);
        map_.put("andaAmountActual", andaAmountActual);
        try {
          String result = tranSactionRun(map_);
          if (result.equals("SUCCESS"))
          {
            map.put("payStatus", "SUCCESS");
            map.put("exchangedResult", "您支付" + bdUsdAmount + "美元，兑换安达通证" + andaAmountActual + "个。");
            resultVo = JsonVo.success();
            System.out.println("兑换成功");
            payDao.UpdatePaypalPaymentRecord(txnId, andaAddress);
          }
          else {
            map.put("payStatus", "SUCCESS");
            map.put("exchangedResult", "FAIL");
            resultVo = JsonVo.fail();
            System.out.println("兑换失败");
          }
          
        }
        catch (MnemonicException e)
        {
          e.printStackTrace();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        
      }
      else
      {
        map.put("payStatus", "FAIL");
        map.put("exchangedResult", "您的此笔交易之前已完成,请勿重复交易");
        resultVo = JsonVo.fail();
        System.out.println("您的此笔交易之前已完成,请勿重复交易");
      }
    }
    else {
      map.put("payStatus", "FAIL");
      map.put("exchangedResult", "未查询到您的付款，请确认是否已支付");
      resultVo = JsonVo.fail();
      System.out.println("未查询到您的付款，请确认是否已支付");
    }
    resultVo.setItem(map);
    return resultVo;
  }
  


  /**
   * 执行兑换
   * @param map_
   * @return
   * @throws Exception
   */
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
    Map<String, Object> map = new HashMap();
    map.put("sender", sendAddress);
    map.put("recipient", map_.get("AndaAddress").toString());
    map.put("amount", new BigDecimal(map_.get("andaAmountActual") + ""));
    map.put("publicKey", publicKey);
    map.put("data", data_);
    map.put("timestamp", tx.getTimestamp().toString());
    map.put("txHash", hash);
    map.put("sign", sign);
    System.out.println("map:" + map);
    Transaction transaction = new Transaction();
    transaction.setSender(String.valueOf(map.get("sender")));
    transaction.setRecipient(String.valueOf(map.get("recipient")));
    transaction.setAmount(new BigDecimal(String.valueOf(map.get("amount"))));
    transaction.setPublicKey(String.valueOf(map.get("publicKey")));
    transaction.setData(String.valueOf(map.get("data")));
    transaction.setTimestamp(Long.valueOf(String.valueOf(map.get("timestamp"))));
    transaction.setTxHash(String.valueOf(map.get("txHash")));
    transaction.setSign(String.valueOf(map.get("sign")));
    boolean r = this.txRecord.addPaypalExchangeRecord(transaction);
    System.out.println(r);
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
      map2.put("USD", new BigDecimal(((Transaction)block.getBody().getTransactions().get(1)).getAmount() + "").multiply(new BigDecimal(TransactionConstants.USD_EXCHANGE_RATE)));
      map2.put("amount", ((Transaction)block.getBody().getTransactions().get(1)).getAmount());
      map2.put("time", format.format(((Transaction)block.getBody().getTransactions().get(1)).getTimestamp()));
      mapNew.put("Body", map2);
      Gson gson = new Gson();
      String ss = gson.toJson(map);
      
      CreateAFile cc = new CreateAFile();
      System.out.println("块" + format.format(((Transaction)block.getBody().getTransactions().get(1)).getTimestamp()) + "");
      cc.CreateFile(ss, format.format(((Transaction)block.getBody().getTransactions().get(1)).getTimestamp()) + "_" + block.getHeader().getIndex());
      System.out.println("*********************打印块*****************************");
    } else {
      this.blockChain.runTransaction(tx);
    }
    
    return "SUCCESS";
  }
}

