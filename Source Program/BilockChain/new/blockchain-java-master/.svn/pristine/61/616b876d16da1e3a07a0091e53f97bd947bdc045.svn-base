package com.aizone.blockchain.web.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.params.TestNet3Params;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aizone.blockchain.conf.Settings;
import com.aizone.blockchain.constants.TransactionConstants;
import com.aizone.blockchain.core.Block;
import com.aizone.blockchain.core.BlockChain;
import com.aizone.blockchain.core.Transaction;
//import com.aizone.blockchain.dao.PaymentRecordDao;
import com.aizone.blockchain.dao.TransactionRecordDao;
import com.aizone.blockchain.utils.CreateAFile;
import com.aizone.blockchain.utils.JsonVo;
import com.google.gson.Gson;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * paypal查询及兑换接口
 * @author Kelly
 *
 */
@RestController
@RequestMapping({"/paypal"})
public class PaypalCommonController
{
  //获取token地址
  private static final String TOKEN_URL = "https://api.paypal.com/v1/oauth2/token";
  //获取支付详情地址
  private static final String PAYMENT_DETAIL = "https://api.paypal.com/v1/payments/payment/";
  //收款账户的clientID
  private static final String clientId = "AQ5yRaEsLWJuabKBn9c16VaVu0PKI7b7ohjiOfuDz_UVv1BVNosCdnAmBkrOwYjF9hgyyN6b5xmpI5wN";
  //收款账户clientID对应的密码
  private static final String secret = "EMDe_nRWkXhNNqjCXpI5Bpo07_RRl4_1j0_OME7LqMd82nRcze3Aq-5P3urOwOp3zwMGtkHNbAtHTxNe";
  //服务器的安达发币地址
  private static final String ANDA_SERVER_ANDA_ADDRESS = "8f863ab09ab147df18755ea64a679294594a1266";
  @Autowired
  private BlockChain blockChain;
  @Autowired
  private Settings settings;
  @Autowired
  private TransactionRecordDao txRecord;
  
  /**
   * 查询及兑换
   * @param request
   * @return
   */
  @PostMapping({"/payment/getResult"})
  public JsonVo paypalPaymentResult(HttpServletRequest request)
  {
    String txnId = request.getParameter("txnId");
    String andaAddress = request.getParameter("andaAddress");
    PaypalCommonController payment = new PaypalCommonController();
    boolean success = false;
    Double usdAmount = null;
    BigDecimal andaAmount = null;
    try
    {
      do
      {
    	//确认支付情况
        success = payment.verifyPayment(txnId);
      } while (!success);
      //确认通过后
      if (success)
      {
        usdAmount = payment.getPaymentAmount(txnId);
        //执行兑换交易
        andaAmount = excuteExchangeTransaction(andaAddress, usdAmount);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    String result = success ? usdAmount + "美元支付完成,兑换安达通证：" + andaAmount + "个" : "支付校验失败";
    Map<String, String> map = new HashMap<String, String>();
    map.put("paymentResult", result);
    JsonVo vo = JsonVo.success();
    vo.setItem(map);
    return vo;
  }
  


  /**
   * 获取token
   * @return
   */
  private String getAccessToken()
  {
    try
    {
      URL url = new URL(TOKEN_URL);
      String authorization = clientId+":"+secret;
      authorization = Base64.encodeBase64String(authorization.getBytes());
      
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setRequestMethod("POST");// 提交模式
      //设置请求头header
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Accept-Language", "en_US");
      conn.setRequestProperty("Authorization", "Basic " + authorization);
      // conn.setConnectTimeout(10000);//连接超时 单位毫秒
      // conn.setReadTimeout(2000);//读取超时 单位毫秒
      conn.setDoOutput(true);// 是否输入参数
      String params = "grant_type=client_credentials";
      conn.getOutputStream().write(params.getBytes());// 输入参数
      
      InputStreamReader inStream = new InputStreamReader(conn.getInputStream());
      BufferedReader reader = new BufferedReader(inStream);
      StringBuilder result = new StringBuilder();
      String lineTxt = null;
      while ((lineTxt = reader.readLine()) != null) {
        result.append(lineTxt);
      }
      reader.close();
      String accessTokey = JSONObject.fromObject(result.toString()).optString("access_token");
      System.out.println("getAccessToken:" + accessTokey);
      return accessTokey;
    } catch (Exception err) {
      err.printStackTrace();
    }
    return null;
  }
  

  /**
   * 获取支付详情
   * @param paymentId
   * @return
   */
  public String getPaymentDetails(String paymentId)
  {
//	  PaymentRecordDao payDao = new PaymentRecordDao();
    try
    {
      //注释掉的payDao相关为测试不通过时将信息存数据库便于查找原因
//      payDao.logSaveTest("PaypalCommonController--getPaymentDetails1", "getPaymentDetails");
      URL url = new URL(PAYMENT_DETAIL+paymentId);
//      payDao.logSaveTest("PaypalCommonController--getPaymentDetails1", url.toString());
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");// 提交模式
      //设置请求头header
      
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Authorization", "Bearer " + getAccessToken());
      // conn.setConnectTimeout(10000);//连接超时 单位毫秒
      // conn.setReadTimeout(2000);//读取超时 单位毫秒
//      payDao.logSaveTest("PaypalCommonController--getPaymentDetails1", conn.toString());
      InputStreamReader inStream = new InputStreamReader(conn.getInputStream());
      BufferedReader reader = new BufferedReader(inStream);
      StringBuilder result = new StringBuilder();
      String lineTxt = null;
      while ((lineTxt = reader.readLine()) != null) {
//    	  payDao.logSaveTest("PaypalCommonController--getPaymentDetails1", lineTxt);
        result.append(lineTxt);
      }
//      payDao.logSaveTest("PaypalCommonController--getPaymentDetails1", result.toString());
      reader.close();
      return result.toString();
    } catch (Exception err) {
//    	payDao.logSaveTest("PaypalCommonController--getPaymentDetails1", err.toString());
      err.printStackTrace();
    }
     return null;
  }
  

  /**
   * 确认支付情况
   * @param paymentId
   * @return
   * @throws Exception
   * @author Kelly
   */
  public boolean verifyPayment(String paymentId)
    throws Exception
  {
	//注释掉的payDao相关的为测试不通过时将信息存数据库便于查找原因
//    PaymentRecordDao payDao = new PaymentRecordDao();
//    payDao.logSaveTest("PaypalCommonController--verifyPayment1", "verifyPayment");
    String str = getPaymentDetails(paymentId);
//    payDao.logSaveTest("PaypalCommonController--verifyPayment2", str);
    
    System.out.println("paypal回传的json信息;" + str);
    JSONObject detail = JSONObject.fromObject(str);
//    payDao.logSaveTest("PaypalCommonController--verifyPayment3", detail.optString("state"));
  //校验订单是否完成

    if ("approved".equals(detail.optString("state"))) {
      JSONObject transactions = detail.optJSONArray("transactions").optJSONObject(0);
      JSONObject amount = transactions.optJSONObject("amount");
      JSONArray relatedResources = transactions.optJSONArray("related_resources");
      //从数据库查询支付总金额与Paypal校验支付总金额
      @SuppressWarnings("unused")
      double total = 0.0;
      System.out.println("amount.optDouble('total'):" + amount.optDouble("total"));
//      payDao.logSaveTest("PaypalCommonController--verifyPayment4", String.valueOf(amount.optDouble("total")));
      /*if( total != amount.optDouble("total") ){
      		return false;
  		}*/
      //校验交易货币类型
      String currency = "USD";
      if (!currency.equals(amount.optString("currency"))) {
        return false;
      }
      //校验每个子订单是否完成
      int i = 0; for (int j = relatedResources.size(); i < j; i++) {
        JSONObject sale = relatedResources.optJSONObject(i).optJSONObject("sale");
//        payDao.logSaveTest("PaypalCommonController--verifyPayment5", String.valueOf(sale));
        
        if ((sale != null) && 
          (!"completed".equals(sale.optString("state")))) {
//          payDao.logSaveTest("PaypalCommonController--verifyPayment5", sale.optString("state"));
          System.out.println("子订单未完成,订单状态:" + sale.optString("state"));
        }
      }
      
      return true;
    }
    return false;
  }
  


/**
 * 获取支付金额
 * @param paymentId
 * @return
 * @throws Exception
 * @author Kelly
 */
  public Double getPaymentAmount(String paymentId)
    throws Exception
  {
	  //注释掉的为测试时用，通过数据库中数据得知获取失败原因
//    PaymentRecordDao payDao = new PaymentRecordDao();
//    payDao.logSaveTest("PaypalCommonController-getPaymentAmount1", "getPaymentAmount");
    String str = getPaymentDetails(paymentId);
    JSONObject detail = JSONObject.fromObject(str);
    
    JSONObject amount = null;
//    payDao.logSaveTest("PaypalCommonController-getPaymentAmount2", detail.optString("state"));
    if ("approved".equals(detail.optString("state"))) {
      JSONObject transactions = detail.optJSONArray("transactions").optJSONObject(0);
      amount = transactions.optJSONObject("amount");
    }
//    payDao.logSaveTest("PaypalCommonController-getPaymentAmount3", String.valueOf(amount.optDouble("total")));
    return Double.valueOf(amount.optDouble("total"));
  }
  


  /**
   * 兑换交易
   * @param andaAddress
   * @param usdAmount
   * @return
   */
  public BigDecimal excuteExchangeTransaction(String andaAddress, Double usdAmount)
  {
//    PaymentRecordDao payDao = new PaymentRecordDao();
//    payDao.logSaveTest("PaypalCommonController-excuteExchangeTransaction1", "excuteExchangeTransaction");
    TransactionRecordDao dao = new TransactionRecordDao();
     Map<String, String> resultMap = dao.exchangeRate("USD");
    String exchangeRate = (String)resultMap.get("exchangeRate");
    BigDecimal bdUsdAmount = new BigDecimal(usdAmount.doubleValue());
    BigDecimal bdExchangeRateNumerator = new BigDecimal("1");
    BigDecimal bdExchangeRateDenominator = new BigDecimal(exchangeRate);
    BigDecimal bdExchangeRate = bdExchangeRateNumerator.divide(bdExchangeRateDenominator, 8, RoundingMode.HALF_UP);
    BigDecimal andaAmountActuall = bdUsdAmount.multiply(bdExchangeRate);
    BigDecimal andaAmountActual = andaAmountActuall.setScale(8, 5);
    Map<String, Object> map_ = new HashMap<String, Object>();
    map_.put("AndaAddress", andaAddress);
    map_.put("andaAmountActual", andaAmountActual);
    try {
      //执行兑换
      tranSactionRun(map_);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return andaAmountActual;
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
    Transaction txNew = blockChain.sendTransaction(tx);
    txRecord.addPaypalExchangeRecord(txNew);
    if (settings.isAutoMining()) {
      Block block = blockChain.mining();
    
      DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Map<String, Object> mapNew = new HashMap<String, Object>();
      Map<String, Object> map1 = new HashMap<String, Object>();
      Map<String, Object> map2 = new HashMap<String, Object>();
    
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
      String ss = gson.toJson(mapNew);
     
      CreateAFile cc = new CreateAFile();
      System.out.println("块" + format.format(((Transaction)block.getBody().getTransactions().get(1)).getTimestamp()) + "");
      cc.CreateFile(ss, format.format(((Transaction)block.getBody().getTransactions().get(1)).getTimestamp()) + "_" + block.getHeader().getIndex());
      System.out.println("*********************打印块*****************************");
    } else {
      blockChain.runTransaction(tx);
    }
    return "SUCCESS";
  }
}

