package com.aizone.blockchain.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aizone.blockchain.constants.TransactionConstants;
import com.aizone.blockchain.core.Transaction;
import com.aizone.blockchain.model.TxRecord;
import com.aizone.blockchain.utils.Db;
import com.aizone.blockchain.utils.HttpsUtil;
import com.aizone.blockchain.web.controller.BlockController;

@Component
public class TransactionRecordDao {
	
	@Autowired
	BlockController btx;
	
	private final static String SFWC_FALSE = "N";
	private final static String SFWC_TRUE = "Y";
	
	//测试地址
	private final static String ANDA_SERVER_BTC_ADDRESS = "mvGuVqUCZt8SKCDxfgscXbmsPwpzkGtSEK";
	//测试地址
	private final static String ANDA_SERVER_ANDA_ADDRESS = "cf87c2e720f9019399a80f9661e0d450e4997e68";
	
	
	public static String selectAddress(){
		Connection conn = null;
		conn = Db.getConnection();
		String address = "";		
		String sql = "select * from walletaddress order by time desc limit 1;";
		try {
			PreparedStatement psq = conn.prepareStatement(sql);
			ResultSet rs = psq.executeQuery();
			if(rs.next() == true){
				address = rs.getString("address");
			}
			return address;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Db.closeConnection(conn);
		}
		return address;
		
	}
	/**
	 * 添加交易记录
	 * 客户端上传记录
	 * @param user
	 * @return
	 */
	public boolean addTxRecoreClient(Map<String,Object> map) {
		Connection conn = null;
		conn = Db.getConnection();
		
		String sql = "insert into clientupload(id,andaAddress,amount,time) values (?,?,?,?)";
		try {
			PreparedStatement psq = conn.prepareStatement(sql);
			psq.setString(1, map.get("id")+"");
			psq.setString(2, map.get("AndaAddress")+"");
			psq.setString(3, map.get("amount")+"");
			psq.setDate(4, new Date(System.currentTimeMillis()));
			Integer i = psq.executeUpdate();
			System.out.println("20181115--------" + i);
			if (i == 1) {
				logSaveJl(map.get("uuId")+"", SFWC_TRUE, "SUCCESS", "log_clientuploadjl");
				return true;
			} else {
				//插入日志
				logSaveJl(map.get("uuId")+"", SFWC_FALSE, "FAIL", "log_clientuploadjl");
				return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Db.closeConnection(conn);
		}
		return false;
	}
	
	/**
	 * 添加交易记录
	 * 客户端上传记录
	 * @param user
	 * @return
	 */
	public boolean add1(Transaction tr) {
		Connection conn = null;
		conn = Db.getConnection();
		
		String sql = "insert into client(sender,recipient,amount,publicKey,sign,txHash,time) values (?,?,?,?,?,?,?)";
		try {
			PreparedStatement psq = conn.prepareStatement(sql);
			psq.setString(1, tr.getSender());
			psq.setString(2, tr.getRecipient());
			psq.setString(3, tr.getAmount()+"");
			psq.setString(4, tr.getPublicKey());
			psq.setString(5, tr.getSign());
			psq.setString(6, tr.getTxHash());
			psq.setDate(7, new Date(System.currentTimeMillis()));
			Integer i = psq.executeUpdate();
			System.out.println("20181115--------" + i);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Db.closeConnection(conn);
		}
		return false;
	}
	
	/**
	 * 添加以太币的交易记录
	 * 客户端上传记录
	 * @param user
	 * @return
	 */
	public boolean addTxRecoreClient1(Map<String,Object> map) {
		Connection conn = null;
		conn = Db.getConnection();
		
		String sql = "insert into ethupload(id,andaAddress,amount,time) values (?,?,?,?)";
		try {
			PreparedStatement psq = conn.prepareStatement(sql);
			psq.setString(1, map.get("id")+"");
			psq.setString(2, map.get("AndaAddress")+"");
			psq.setString(3, map.get("amount")+"");
			psq.setDate(4, new Date(System.currentTimeMillis()));
			Integer i = psq.executeUpdate();
			System.out.println("数据--------" + i);
			if (i == 1) {
				logSaveJl(map.get("uuId")+"", SFWC_TRUE, "SUCCESS", "log_clientuploadjl");
				return true;
			} else {
				//插入日志
				logSaveJl(map.get("uuId")+"", SFWC_FALSE, "FAIL", "log_clientuploadjl");
				return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Db.closeConnection(conn);
		}
		return false;
	}
	
	
	/**
	 * 添加交易记录
	 * 客户端上传记录
	 * @param user
	 * @return
	 */
	public boolean add2(Transaction tr) {
		Connection conn = null;
		conn = Db.getConnection();
		
		String sql = "insert into eth(sender,recipient,amount,publicKey,sign,txHash,time) values (?,?,?,?,?,?,?)";
		try {
			PreparedStatement psq = conn.prepareStatement(sql);
			psq.setString(1, tr.getSender());
			psq.setString(2, tr.getRecipient());
			psq.setString(3, tr.getAmount()+"");
			psq.setString(4, tr.getPublicKey());
			psq.setString(5, tr.getSign());
			psq.setString(6, tr.getTxHash());
			psq.setDate(7, new Date(System.currentTimeMillis()));
			Integer i = psq.executeUpdate();
			System.out.println("20181115--------" + i);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Db.closeConnection(conn);
		}
		return false;
	}
	/**
	 * 添加交易记录
	 *将比特币服务器的交易信息添加到服务器
	 * @param map
	 * @return
	 */
	public void addBtcServerRecord(Map<String, Object> map) {
		Connection conn = null;
		conn = Db.getConnection();
		String id = map.get("id").toString();//id
		String time = map.get("time").toString();//时间
		int balance = Integer.parseInt(map.get("balance").toString());//账户余额
		int Actual_BTC = Integer.parseInt(map.get("Actual_BTC").toString());//实际接收金额(去除矿工费)
		String Anda_Address = map.get("Anda_Address").toString();//接收地址
		String Send_Address = map.get("Send_Address").toString();//发送地址
		int Anda_value = Integer.parseInt(map.get("Anda_value").toString());//接收到的金额 
		int Send_value = Integer.parseInt(map.get("Send_value").toString());//返还金额
		
		String sql = "insert into btcservertxrecord(id,sender,recipient,amount,sfwc,time) values (?,?,?,?,?,?)";
		try {
			PreparedStatement psq = conn.prepareStatement(sql);
			psq.setString(1, id);
			psq.setString(2, Send_Address);
			psq.setString(3, Anda_Address);
			psq.setInt(4, Actual_BTC);
			psq.setString(5, SFWC_FALSE);
			psq.setDate(6, new Date(System.currentTimeMillis()));;
			Integer i = psq.executeUpdate();
			if (i == 1) {
				//添加日志
				logSaveJl(id, SFWC_TRUE, "SUCCESS", "log_serverjyjl");
			}else{
				logSaveJl(id, SFWC_FALSE, "FAIL", "log_serverjyjl");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Db.closeConnection(conn);
		}
	}
	/**
	 * 查询交易记录(从比特币服务器获取的),并以List形式返回
	 * @return 
	 * @return
	 */
	public List<TxRecord> getTxRecoreServer(){
		Connection conn = null;
		conn = Db.getConnection();
		String sql = "SELECT id,sender,recipient,amount,sfwc FROM btcservertxrecord WHERE sfwc = ?;";
		List<TxRecord> l = new ArrayList<TxRecord>();
		try {
			PreparedStatement psq = conn.prepareStatement(sql);
			psq.setString(1, "N");
			ResultSet rs = psq.executeQuery();
			while (rs.next()) {
				TxRecord txRecord = new TxRecord();
				txRecord.setId(rs.getString("id"));
				txRecord.setAndaAddress("");
				txRecord.setSender(rs.getString("sender"));
				txRecord.setAmount(rs.getInt("amount"));
				txRecord.setRecipient(rs.getString("recipient"));
				txRecord.setSfwc(rs.getString("sfwc"));
				l.add(txRecord);
			}
			return l;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Db.closeConnection(conn);
		}
		return l;
		
	}
	/**
	 * 查询交易记录(服务器上传的),并以List形式返回
	 * @return
	 */
	public List<TxRecord> getTxRecoreClient(){
		Connection conn = null;
		conn = Db.getConnection();
		String sql = "SELECT id,sender,recipient,amount,andaAddress,sfwc FROM clientupload WHERE sfwc = ?;";
		List<TxRecord> l = new ArrayList<TxRecord>();
		try {
			PreparedStatement psq = conn.prepareStatement(sql);
			psq.setString(1, "N");
			ResultSet rs = psq.executeQuery();
			while (rs.next()) {
				TxRecord txRecord = new TxRecord();
				txRecord.setId(rs.getString("id"));
				txRecord.setAndaAddress(rs.getString("andaAddress"));
				txRecord.setSender(rs.getString("sender"));
				txRecord.setAmount(rs.getInt("amount"));
				txRecord.setRecipient(rs.getString("recipient"));
				txRecord.setSfwc(rs.getString("sfwc"));
				l.add(txRecord);
			}
			return l;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Db.closeConnection(conn);
		}
		return l;
		
	}
	
	/**
	 * 查询交易记录时间(从客户上传记录获取的,最新一笔交易完成时间)
	 * @return
	 */
	public long getTimeRecoreServer(){
		Connection conn = null;
		conn = Db.getConnection();
		long time = 1L;
		String sql = "SELECT MAX(time) time FROM btcservertxrecord;";
		try {
			PreparedStatement psq = conn.prepareStatement(sql);
			ResultSet rs = psq.executeQuery();
			if(rs.next() == true){
				time = rs.getDate("time").getTime();
			}
			return time;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Db.closeConnection(conn);
		}
		return time;
		
	}
	
	

	/**
	 * 添加btc服务器的交易记录
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public boolean addTxListFromBtc(){
		
		List<List> list = HttpsUtil.getTxDataFromBTC(ANDA_SERVER_BTC_ADDRESS);
		long time = getTimeRecoreServer();
		if(!list.isEmpty()){
			for(List<Map> l : list){
				for(Map<String,Object> map : l){
					if(Long.parseLong(map.get("time").toString()) > time){
						addBtcServerRecord(map);
						return true;
					}
				}
			}
		}
		return false;
		
		
	}
	/**
	 * 对比服务器交易记录与客户端上传的记录
	 * @throws Exception 
	 */
	/*public boolean compareTx() throws Exception{
		boolean flag = false;
		List<TxRecord> listServerTx = getTxRecoreServer();//服务器保存的交易记录
		List<TxRecord> listClientTx = getTxRecoreClient();//客户端上传的交易记录
		if(listClientTx.isEmpty() && listServerTx.isEmpty()){
			return false;
		}
		for( TxRecord tx_server : listServerTx){
			for(TxRecord tx_client : listClientTx){
				flag = tx_client.equals(tx_server);//判断是否一样(不必比较ID)
				System.out.println(tx_client.toString() + "----------" + tx_server.toString());
				System.out.println("判断:" + flag);
				if(flag){
					//修改server,client的完成标志为Y
					Map<String, String> map = new HashMap<>();
					map.put("client_id", tx_client.getId());
					map.put("server_id", tx_server.getId());
					boolean b = updateSfwc(map);
					if(b){
						//transActionRun(tx_client.getAndaAddress(), DaoUtil.exchangeRate(tx_server.getAmount()));
						logSaveCom(tx_server.getId(),tx_client.getId(),SFWC_TRUE,tx_server.getSender(), tx_server.getRecipient(), tx_server.getAmount());
						break;
					}else{
						//插入日志
						logSaveCom(tx_server.getId(),tx_client.getId(),SFWC_FALSE,tx_server.getSender(), tx_server.getRecipient(), tx_server.getAmount());
					}
					//结束循环
				}
			}
		}
		return true;
	}*/
	/**
	 * 修改是否完成为Y
	 * @return
	 * @throws Exception 
	 */
	public static boolean updateSfwc(Map<String, String> map) throws Exception{
		Connection conn = null;
		conn = Db.getConnection();
		/*String sqlClient = "SELECT id,andaAddress FROM (SELECT * FROM clientupload WHERE sender = ?,recipient = ?,amount = ? ORDER BY id) WHERE rownum=1;";
		String sqlServer = "SELECT id FROM (SELECT * FROM btcservertxrecord WHERE sender = ?,recipient = ?,amount = ? ORDER BY id) WHERE rownum=1; ";
*/		try {
			String sql_client = "UPDATE clientupload SET sfwc = ? WHERE id = ?";
			PreparedStatement psq_client = conn.prepareStatement(sql_client);
			psq_client.setString(1, SFWC_TRUE);
			psq_client.setString(2, map.get("id").toString());
			Integer i = psq_client.executeUpdate();
			if(i == 1){
				return true;
			}
			else{
				return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Db.closeConnection(conn);
		}
		return false;
	}
	
	/**
	 * 插入log记录
	 * @param id 
	 * @param result 是否完成
	 * @param reason 原因
	 * @param sql 语句
	 */
	public void logSaveJl(String id,String result,String reason,String table){
		Connection conn = null;
		conn = Db.getConnection();
		String sql = "insert into " + table 
				+ "(id,result,reason,time) values (?,?,?,?)";
		try {
			PreparedStatement psq = conn.prepareStatement(sql);
			psq.setString(1, id);
			psq.setString(2, result);
			psq.setString(3, reason);
			psq.setDate(4, new Date(System.currentTimeMillis()));
			psq.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Db.closeConnection(conn);
		}
	}
	
	/**
	 * 记录比较log
	 * @param id
	 * @param result 结果
	 * @param sender 发送地址
	 * @param recipient 接收地址
	 * @param amount 金额
	 */
	public void logSaveCom(String idserver,String idclient,String result,String sender,String recipient,int amount){
		Connection conn = null;
		conn = Db.getConnection();
		String sql = "insert into log_clientcomserver(idserver,idclient,result,sender,recipient,amount,time) values (?,?,?,?,?,?,?)";
		try {
			PreparedStatement psq = conn.prepareStatement(sql);
			psq.setString(1, idserver);
			psq.setString(2, idclient);
			psq.setString(3, result);
			psq.setString(4, sender);
			psq.setString(5, recipient);
			psq.setInt(6, amount);
			psq.setDate(7, new Date(System.currentTimeMillis()));
			psq.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Db.closeConnection(conn);
		}
	}
	
	/**
	 * 安达交易日志
	 * @param map
	 */
	public static void andaTxLog(Map<String, Object> map){
		Connection conn = null;
		conn = Db.getConnection();
		//data,code,message,status,errorMessage,sender,recipient,amount,publicKey,txHash,sign,timestamp
		String sql = "insert into log_andatx(id,code,message,status,errorMessage,sender,recipient,amount,publicKey,txHash,sign,time) values (?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			PreparedStatement psq = conn.prepareStatement(sql);
			psq.setString(1, map.get("data").toString());
			psq.setString(2, map.get("code").toString());
			psq.setString(3, map.get("message").toString());
			psq.setString(4, map.get("status").toString());
			psq.setString(5, map.get("errorMessage")+"");
			psq.setString(6, map.get("sender").toString());
			psq.setString(7, map.get("recipient").toString());
			psq.setInt(8, Integer.parseInt(map.get("amount").toString()));
			psq.setString(9, map.get("publicKey").toString());
			psq.setString(10,map.get("txHash").toString());
			psq.setString(11,map.get("sign").toString());
			psq.setDate(12, new Date(Long.parseLong(map.get("timestamp").toString())));
			psq.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Db.closeConnection(conn);
		}
	}
	
	/**
	 * 通过交易hash查询交易记录
	 * @param txhash
	 * @return
	 */
	public Map<String, Object> selectTransactionByHash(String txhash)
	  {
	    Connection conn = null;
	    conn = Db.getConnection();
	    Map<String, Object> map = new HashMap<String, Object>();
	    String sql = "SELECT sender,recipient,amount,txHash,time FROM client WHERE txHash = ?;";
	    try {
	      PreparedStatement psq = conn.prepareStatement(sql);
	      psq.setString(1, txhash);
	      ResultSet rs = psq.executeQuery();
	      if (rs.next()) {
	        map.put("sender", rs.getString("sender"));
	        map.put("recipient", rs.getString("recipient"));
	        map.put("amount", rs.getString("amount"));
	        map.put("txHash", rs.getString("txHash"));
	        map.put("time", rs.getString("time"));
	      }
	    }
	    catch (SQLException e) {
	      e.printStackTrace();
	    } finally {
	      Db.closeConnection(conn);
	    }
	    return map;
	  }
	  

	/**
	 * 通过安达地址查询交易记录
	 * @param andaAddress
	 * @return
	 */
	  public Map<String, List<Map<String, Object>>> selectTransactionByAndaAddress(String andaAddress)
	  {
	    Connection conn = null;
	    conn = Db.getConnection();
	    
	    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	    Map<String, List<Map<String, Object>>> record = new HashMap<String, List<Map<String, Object>>>();
	    String sql = "SELECT sender,recipient,amount,txHash,time FROM client WHERE sender = ? or recipient = ? order by time desc;";
	    try
	    {
	      PreparedStatement psq = conn.prepareStatement(sql);
	      psq.setString(1, andaAddress);
	      psq.setString(2, andaAddress);
	      ResultSet rs = psq.executeQuery();
	      while (rs.next()) {
	        Map<String, Object> map = new HashMap<String, Object>();
	        map.put("sender", rs.getString("sender"));
	        map.put("recipient", rs.getString("recipient"));
	        map.put("amount", rs.getString("amount"));
	        map.put("txHash", rs.getString("txHash"));
	        map.put("time", rs.getString("time"));
	        if (rs.getString("sender").equals(andaAddress)) {
	          map.put("revenueOrExpenditure", "expenditure");
	        } else if (rs.getString("recipient").equals(andaAddress)) {
	          map.put("revenueOrExpenditure", "revenue");
	        }
	        list.add(map);
	      }
	    }
	    catch (SQLException e) {
	      e.printStackTrace();
	    } finally {
	      Db.closeConnection(conn);
	    }
	    record.put("record", list);
	    return record;
	  }
	  

	  /**
	   * 根据币类型查询汇率并返回
	   * @param currency
	   * @return
	   * @author Kelly
	   */
	  public Map<String, String> exchangeRate(String currency)
	  {
	    BigDecimal exchangeRate = null;
	    Map<String, String> resultMap = new HashMap<String, String>();
	    if ((!currency.equals("CNY")) && (!currency.equals("USD"))) {
	      resultMap.put("exchangeRate", "币类型错误");
	    } else {
	      if (currency.equals("CNY")) {
	        exchangeRate = new BigDecimal(TransactionConstants.CNY_EXCHANGE_RATE);
	      } else if (currency.equals("USD")) {
	        exchangeRate = new BigDecimal(TransactionConstants.USD_EXCHANGE_RATE);
	      }
	      resultMap.put("exchangeRate", exchangeRate.toString());
	    }
	    return resultMap;
	  }
	  

	  /**
	   * 增加paypal支付兑换记录
	   * @param tx
	   * @return
	   */
	  public boolean addPaypalExchangeRecord(Transaction tx)
	  {
	    Connection conn = null;
	    conn = Db.getConnection();
	    String sql = "insert into paypal_exchange_record (sender,recipient,amount,data,public_key,sign,timestamp,tx_hash) values (?,?,?,?,?,?,?,?)";
	    try {
	      PreparedStatement psq = conn.prepareStatement(sql);
	      psq.setString(1, tx.getSender());
	      psq.setString(2, tx.getRecipient());
	      psq.setBigDecimal(3, tx.getAmount());
	      psq.setString(4, tx.getData().toString());
	      psq.setString(5, tx.getPublicKey());
	      psq.setString(6, tx.getSign());
	      Long time = tx.getTimestamp();
	      SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	      String dateStr = dateformat.format(time);
	      psq.setString(7, dateStr);
	      psq.setString(8, tx.getTxHash());
	      Integer i = Integer.valueOf(psq.executeUpdate());
	      System.out.println("20181115--------" + i);
	      boolean bool; if (i.intValue() == 1) {
	        logSaveJl(tx.getTimestamp() + "", "Y", "SUCCESS", "log_clientuploadjl");
	        return true;
	      }
	      
	      logSaveJl(tx.getTimestamp() + "", "N", "FAIL", "log_clientuploadjl");
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
	   * 增加微信支付兑换记录（未使用，也未测试）
	   * @param tx
	   * @return
	   */
	  public boolean addWXExchangeRecord(Transaction tx)
	  {
	    Connection conn = null;
	    conn = Db.getConnection();
	    String sql = "insert into wx_exchange_record (sender,recipient,amount,data,public_key,sign,timestamp,tx_hash) values (?,?,?,?,?,?,?,?)";
	    try {
	      PreparedStatement psq = conn.prepareStatement(sql);
	      psq.setString(1, tx.getSender());
	      psq.setString(2, tx.getRecipient());
	      psq.setBigDecimal(3, tx.getAmount());
	      psq.setString(4, tx.getData().toString());
	      psq.setString(5, tx.getPublicKey());
	      psq.setString(6, tx.getSign());
	      Long time = tx.getTimestamp();
	      SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	      String dateStr = dateformat.format(time);
	      psq.setString(7, dateStr);
	      psq.setString(8, tx.getTxHash());
	      Integer i = Integer.valueOf(psq.executeUpdate());
	      System.out.println("20181115--------" + i);
	      boolean bool; if (i.intValue() == 1) {
	        logSaveJl(tx.getTimestamp() + "", "Y", "SUCCESS", "log_clientuploadjl");
	        return true;
	      }
	      
	      logSaveJl(tx.getTimestamp() + "", "N", "FAIL", "log_clientuploadjl");
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
	   * 增加交易Hash记录	
	   * @author Kelly
	   */
	  public void addTxHash(String type, String btcTxHash, String exchangeStatus, String txHash)
	  {
	    Connection conn = null;
	    conn = Db.getConnection();
	    String sql = "insert into tx_hash_record(time,type,btc_tx_hash,exchange_status,tx_hash) values(?,?,?,?,?)";
	    try {
	      PreparedStatement psq = conn.prepareStatement(sql);
	      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	      String time = format.format(Long.valueOf(System.currentTimeMillis()));
	      psq.setString(1, time);
	      psq.setString(2, type);
	      psq.setString(3, btcTxHash);
	      psq.setString(4, exchangeStatus);
	      psq.setString(5, txHash);
	      Integer i = Integer.valueOf(psq.executeUpdate());
	      if (i.intValue() == 1) {
	        logSaveJl(btcTxHash, "Y", "SUCCESS", "log_clientuploadjl");
	      }
	      else
	      {
	        logSaveJl(btcTxHash, "N", "FAIL", "log_clientuploadjl");
	      }
	    }
	    catch (SQLException e)
	    {
	      e.printStackTrace();
	    } finally {
	      Db.closeConnection(conn);
	    }
	  }
	  

	  /**
	   * 通过比特币交易hash更新数据库中交易hash记录
	   * @param type
	   * @param btcTxHash
	   * @param exchangeStatus
	   * @param txHash
	   */
	  public void updateTxHashBybtcTxHash(String type, String btcTxHash, String exchangeStatus, String txHash)
	  {
	    Connection conn = null;
	    conn = Db.getConnection();
	    String sql = "update tx_hash_record set time = ?,exchange_status = ?,tx_hash = ? where type = ? and btc_tx_hash = ?";
	    try {
	      PreparedStatement psq = conn.prepareStatement(sql);
	      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	      String time = format.format(Long.valueOf(System.currentTimeMillis()));
	      psq.setString(1, time);
	      psq.setString(2, exchangeStatus);
	      psq.setString(3, txHash);
	      psq.setString(4, type);
	      psq.setString(5, btcTxHash);
	      Integer i = Integer.valueOf(psq.executeUpdate());
	      if (i.intValue() == 1) {
	        logSaveJl(btcTxHash, "Y", "SUCCESS", "log_clientuploadjl");
	      }
	      else
	      {
	        logSaveJl(btcTxHash, "N", "FAIL", "log_clientuploadjl");
	      }
	    }
	    catch (SQLException e)
	    {
	      e.printStackTrace();
	    } finally {
	      Db.closeConnection(conn);
	    }
	  }
	  

	  /**
	   * 通过交易hash更新数据库中交易hash记录
	   * @param type
	   * @param exchangeStatus
	   * @param txHash
	   */
	  public void updateTxHashByTxHash(String type, String exchangeStatus, String txHash)
	  {
	    Connection conn = null;
	    conn = Db.getConnection();
	    String sql = "update tx_hash_record set time = ?,exchange_status = ? where type = ? and tx_hash = ?";
	    try {
	      PreparedStatement psq = conn.prepareStatement(sql);
	      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	      String time = format.format(Long.valueOf(System.currentTimeMillis()));
	      psq.setString(1, time);
	      psq.setString(2, exchangeStatus);
	      psq.setString(3, type);
	      psq.setString(4, txHash);
	      Integer i = Integer.valueOf(psq.executeUpdate());
	      if (i.intValue() == 1) {
	        logSaveJl(txHash, "Y", "SUCCESS", "log_clientuploadjl");
	      }
	      else
	      {
	        logSaveJl(txHash, "N", "FAIL", "log_clientuploadjl");
	      }
	    }
	    catch (SQLException e)
	    {
	      e.printStackTrace();
	    } finally {
	      Db.closeConnection(conn);
	    }
	  }
	  

	  /**
	   * 通过比特币交易hash查询库中是否有此交易hash
	   * @param type
	   * @param btcTxHash
	   * @return
	   */
	  public boolean selectTxHashByBtcTxHash(String type, String btcTxHash)
	  {
	    Connection conn = null;
	    conn = Db.getConnection();
	    String sql = "select exchange_status from tx_hash_record where type = ? and btc_tx_hash = ?";
	    boolean result = false;
	    String exchangeStatus = "";
	    try {
	      PreparedStatement psq = conn.prepareStatement(sql);
	      
	      psq.setString(1, type);
	      psq.setString(2, btcTxHash);
	      
	      ResultSet rs = psq.executeQuery();
	      while (rs.next()) {
	        exchangeStatus = rs.getString("exchange_status");
	      }
	    }
	    catch (SQLException e) {
	      e.printStackTrace();
	    } finally {
	      Db.closeConnection(conn);
	    }
	    
	    if (exchangeStatus.equals("true")) {
	      return true;
	    }
	    return false;
	  }
	
}
