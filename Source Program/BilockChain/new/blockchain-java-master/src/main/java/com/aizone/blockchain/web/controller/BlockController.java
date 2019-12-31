package com.aizone.blockchain.web.controller;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SignatureException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.params.TestNet3Params;
import org.bouncycastle.asn1.dvcs.Data;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aizone.blockchain.bit.AssessJSON;
import com.aizone.blockchain.bit.HashToString;
import com.aizone.blockchain.conf.Settings;
import com.aizone.blockchain.core.Block;
import com.aizone.blockchain.core.BlockChain;
import com.aizone.blockchain.core.Transaction;
import com.aizone.blockchain.dao.DaoUtil;
import com.aizone.blockchain.dao.TransactionRecordDao;
import com.aizone.blockchain.dao.Wallet;
import com.aizone.blockchain.db.DBAccess;
import com.aizone.blockchain.net.base.Node;
import com.aizone.blockchain.utils.BitcoinJUtil;
import com.aizone.blockchain.utils.CreateAFile;
import com.aizone.blockchain.utils.HttpsUtil;
import com.aizone.blockchain.utils.JsonVo;
import com.aizone.blockchain.wallet.Account;
import com.aizone.blockchain.web.vo.TransactionVo;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;

/**
 * @author yangjian
 * @since 2018-04-07 上午10:50.
 */
@RestController
@RequestMapping("/chain")
public class BlockController {

	/**
	 * 收款人私钥，测试数据
	 */
	@SuppressWarnings("unused")
	private static final String SENDER_PRIVATE_KEY = "";
	/**
	 * 收款人公钥，测试数据
	 */
	@SuppressWarnings("unused")
	private static final String SENDER_PUBLIC_KEY = "";

	// 测试地址
	/**
	 * 服务器安达钱包地址
	 */
	//private final static String ANDA_SERVER_ANDA_ADDRESS = "cf87c2e720f9019399a80f9661e0d450e4997e68";
	//private final static String ANDA_SERVER_ANDA_ADDRESS = "fa15d39b7b2c9ba71d9faff015b4f6dc9031f85b";
	//8f863ab09ab147df18755ea64a679294594a1266
	private final static String ANDA_SERVER_ANDA_ADDRESS = "8f863ab09ab147df18755ea64a679294594a1266";
	/**
	 * 服务器对外公布唯一地址
	 */
	private static final String BTC_SERVER_RECIPENT_ADDRESS = "";
	@Autowired
	private DBAccess dbAccess;

	@Autowired
	private BlockChain blockChain;

	@Autowired
	private Settings settings;
	@Autowired
	private TransactionRecordDao txRecord;
		

	@GetMapping({ "", "/", "index" })
	public JsonVo index(HttpServletRequest request) {
		Optional<Object> o = dbAccess.getLastBlockIndex();
		JsonVo success = JsonVo.success();
		success.setItem(o.get());
		return success;
	}

	/**
	 * 挖矿
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("/mine")
	public JsonVo mine(HttpServletRequest request) throws Exception {
//		List<JsonVo> l=new ArrayList<>();
		Block block = blockChain.mining();
		JsonVo vo = new JsonVo();
		vo.setCode(JsonVo.CODE_SUCCESS);
		vo.setMessage("Create a new block");
		vo.setItem(block);
//		l.add(vo);
		return vo;
//		return l;
	}
	/**
	 * 安达币交易测试
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("/mine1")
	public String mine1(String rec,String mon) throws Exception {
		Optional<Account> recipient = dbAccess.getAccount("b7578430db8b62e1a71c5350ce6d8dc74d72d9aa");
		System.out.println(recipient);
		//账户转账
		Optional<Account> sender = dbAccess.getAccount("qgub3qKXSsYRSoPY1vPjdpURsCVJgVhf2W地址");
		//执行转账操作,更新账户余额
		BigDecimal b=new BigDecimal("200");
		sender.get().setBalance(sender.get().getBalance().subtract(b));
		recipient.get().setBalance(recipient.get().getBalance().add(b));
		dbAccess.putAccount(sender.get());
		dbAccess.putAccount(recipient.get());
		return "success";
	}

	/**
	 * 浏览区块
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("/block/view")
	public JsonVo viewChain(HttpServletRequest request) {

		Optional<Block> block = dbAccess.getLastBlock();
		JsonVo success = JsonVo.success();
		if (block.isPresent()) {
			success.setItem(block.get());
		}
		return success;

	}

	/**
	 * 发送anda To anda
	 * 
	 * @param txVo
	 * @return
	 */
	@PostMapping("/transactions/new")
	public JsonVo sendTransaction(@RequestBody TransactionVo txVo) throws Exception {

		Preconditions.checkNotNull(txVo.getSender(), "Sender is needed.");
		Preconditions.checkNotNull(txVo.getRecipient(), "Recipient is needed.");
		Preconditions.checkNotNull(txVo.getAmount(), "Amount is needed.");
		Preconditions.checkNotNull(txVo.getPublicKey(), "PublicKey is needed.");
		Preconditions.checkNotNull(txVo.getSign(), "Sign is needed.");
		Preconditions.checkNotNull(txVo.getTimestamp(), "TimeStamp is needed.");
		Preconditions.checkNotNull(txVo.getTxHash(), "TxHash is needed.");

		String amount = txVo.getAmount();
		// Integer amount_int = Integer.valueOf(amount) / 2;
//		BigDecimal amount1 = BigDecimal.valueOf(Double.valueOf(amount));
		BigDecimal amount1 = new BigDecimal(amount);
		Transaction tx = new Transaction();
		tx.setSender(txVo.getSender());
		tx.setRecipient(txVo.getRecipient());
		tx.setAmount(amount1);
		tx.setData(txVo.getData());
		tx.setPublicKey(txVo.getPublicKey());
		tx.setSign(txVo.getSign());
		tx.setTimestamp(Long.valueOf(txVo.getTimestamp()));
		tx.setTxHash(txVo.getTxHash());
		// BeanUtils.copyProperties(txVo, tx);
		tx.setCoinSendAddress(txVo.getCoinSendAddress());
		Transaction transaction = blockChain.sendTransaction(tx);
		transaction.getSign();
		System.out.println("-签名-"+transaction.getSign()+"--");
		//存储数据/
		txRecord.add1(transaction);
		
		// 如果开启了自动挖矿，则直接自动挖矿
		if (settings.isAutoMining()) {
			blockChain.mining();
		} else {
			blockChain.runTransaction(tx);
			// System.out.println("----------------------------------------------------------------");
		}

		JsonVo success = JsonVo.success();
		success.setItem(transaction);
		return success;
	}/**
	 * 发送交易比特币
	 * 
	 * @param txVo
	 * @return
	 */
	@PostMapping("/transactions/new2")
	public JsonVo sendTransaction2(@RequestBody TransactionVo txVo) throws Exception {

		Preconditions.checkNotNull(txVo.getSender(), "Sender is needed.");
		Preconditions.checkNotNull(txVo.getRecipient(), "Recipient is needed.");
		Preconditions.checkNotNull(txVo.getAmount(), "Amount is needed.");
		Preconditions.checkNotNull(txVo.getPublicKey(), "PublicKey is needed.");
		Preconditions.checkNotNull(txVo.getSign(), "Sign is needed.");
		Preconditions.checkNotNull(txVo.getTimestamp(), "TimeStamp is needed.");
		Preconditions.checkNotNull(txVo.getTxHash(), "TxHash is needed.");

		String amount = txVo.getAmount();
		// Integer amount_int = Integer.valueOf(amount) / 2;
//		BigDecimal amount1 = BigDecimal.valueOf(Double.valueOf(amount));
		BigDecimal amount1 = new BigDecimal(amount);
		Transaction tx = new Transaction();
		tx.setSender(txVo.getSender());
		tx.setRecipient(txVo.getRecipient());
		tx.setAmount(amount1);
		tx.setData(txVo.getData());
		tx.setPublicKey(txVo.getPublicKey());
		tx.setSign(txVo.getSign());
		tx.setTimestamp(Long.valueOf(txVo.getTimestamp()));
		tx.setTxHash(txVo.getTxHash());
		// BeanUtils.copyProperties(txVo, tx);
		tx.setCoinSendAddress(txVo.getCoinSendAddress());
		Transaction transaction = blockChain.sendTransaction(tx);
		transaction.getSign();
		System.out.println("-签名-"+transaction.getSign()+"--");
		//存储数据/
		txRecord.add1(transaction);
		
		// 如果开启了自动挖矿，则直接自动挖矿
		if (settings.isAutoMining()) {
			Block block=blockChain.mining();
			//转为json存储	
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Map<String, Object> map=new HashMap<>();
			Map<String, Object> map1=new HashMap<>();
			Map<String, Object> map2=new HashMap<>();
			map1.put("index", block.getHeader().getIndex());
			map1.put("hash", block.getHeader().getHash());
			map1.put("previousHash", block.getHeader().getPreviousHash());
			map1.put("difficulty", block.getHeader().getDifficulty());
			map1.put("nonce", block.getHeader().getNonce());
			map.put("BlockHeader", map1);
			//[Transaction{sender='null', recipient='', amount=旷工奖励数值, timestamp=,txHash:交易hash data=}]
			String BTC="3BdQVmnxMHmJjRi6qiZNNfRJ87uLGmuxZN";
			map2.put("recipient", BTC);//sender
			map2.put("sender", block.getBody().getTransactions().get(1).getRecipient());
			map2.put("BTCsender", txVo.getCoinSendAddress());
			map2.put("publicKey", block.getBody().getTransactions().get(1).getPublicKey());
			map2.put("txHash", block.getBody().getTransactions().get(1).getTxHash());
			map2.put("BTC", new BigDecimal(block.getBody().getTransactions().get(1).getAmount()+"").divide(new BigDecimal("3998")));
			map2.put("amount", block.getBody().getTransactions().get(1).getAmount());
			map2.put("time", format.format(block.getBody().getTransactions().get(1).getTimestamp()));
			map.put("Body", map2);
			Gson gson=new Gson();
			String ss= gson.toJson(map);
			//存储json
			CreateAFile cc =new CreateAFile();
			System.out.println("块"+(format.format(block.getBody().getTransactions().get(1).getTimestamp())+""));
			cc.CreateFile(ss,format.format(block.getBody().getTransactions().get(1).getTimestamp())+"");
			 System.out.println("*********************打印快************************");
		} else {
			blockChain.runTransaction(tx);
			// System.out.println("----------------------------------------------------------------");
		}
		
		//交易hash入库
		 String txHash = transaction.getTxHash();
		 this.txRecord.updateTxHashByTxHash("BTC", "true", txHash);

		JsonVo success = JsonVo.success();
		success.setItem(transaction);
		return success;
	}
	
	/**
	 * 发送交易以太坊
	 * 
	 * @param txVo
	 * @return
	 */
	@PostMapping("/transactions/new1")
	public JsonVo sendTransaction1(@RequestBody TransactionVo txVo) throws Exception {
		
		String amount0 = txVo.getAmount();
		System.out.println("****验证是否为空*****"+amount0);
		Preconditions.checkNotNull(txVo.getSender(), "Sender is needed.");
		Preconditions.checkNotNull(txVo.getRecipient(), "Recipient is needed.");
		Preconditions.checkNotNull(txVo.getAmount(), "Amount is needed.");
		Preconditions.checkNotNull(txVo.getPublicKey(), "PublicKey is needed.");
		Preconditions.checkNotNull(txVo.getSign(), "Sign is needed.");
		Preconditions.checkNotNull(txVo.getTimestamp(), "TimeStamp is needed.");
		Preconditions.checkNotNull(txVo.getTxHash(), "TxHash is needed.");
		String amount = txVo.getAmount();
		// Integer amount_int = Integer.valueOf(amount) / 2;
//		BigDecimal amount1 = BigDecimal.valueOf(Double.valueOf(amount));
		BigDecimal amount1 = new BigDecimal(amount);
		Transaction tx = new Transaction();
		tx.setSender(txVo.getSender());
		tx.setRecipient(txVo.getRecipient());
		tx.setAmount(amount1);
		tx.setData(txVo.getData());
		tx.setPublicKey(txVo.getPublicKey());
		tx.setSign(txVo.getSign());
		tx.setTimestamp(Long.valueOf(txVo.getTimestamp()));
		tx.setTxHash(txVo.getTxHash());
		tx.setCoinSendAddress(txVo.getCoinSendAddress());
		// BeanUtils.copyProperties(txVo, tx);
		Transaction transaction = blockChain.sendTransaction(tx);
		transaction.getSign();
		System.out.println("-签名-"+transaction.getSign()+"--");
		//存储数据*
		txRecord.add2(transaction);
		// 如果开启了自动挖矿，则直接自动挖矿
		if (settings.isAutoMining()) {
			Block block=blockChain.mining();
			//转为json存储	
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Map<String, Object> map=new HashMap<>();
			Map<String, Object> map1=new HashMap<>();
			Map<String, Object> map2=new HashMap<>();
			map1.put("index", block.getHeader().getIndex());
			map1.put("hash", block.getHeader().getHash());
			map1.put("previousHash", block.getHeader().getPreviousHash());
			map1.put("difficulty", block.getHeader().getDifficulty());
			map1.put("nonce", block.getHeader().getNonce());
			map.put("BlockHeader", map1);
			//[Transaction{sender='null', recipient='', amount=旷工奖励数值, timestamp=,txHash:交易hash data=}]
			String ETH="0x7BF470fe5d7553e444989229721b47375CB9B695";
			map2.put("recipient", ETH);//sender
			map2.put("sender", block.getBody().getTransactions().get(1).getRecipient());
			map2.put("BTCsender", txVo.getCoinSendAddress());
			map2.put("publicKey", block.getBody().getTransactions().get(1).getPublicKey());
			map2.put("txHash", block.getBody().getTransactions().get(1).getTxHash());
			map2.put("ETH", new BigDecimal(block.getBody().getTransactions().get(1).getAmount()+"").divide(new BigDecimal("99.95")));
			map2.put("amount", block.getBody().getTransactions().get(1).getAmount());
			map2.put("time", format.format(block.getBody().getTransactions().get(1).getTimestamp()));
			map.put("Body", map2);
			Gson gson=new Gson();
			String ss= gson.toJson(map);
			//存储json
			CreateAFile cc =new CreateAFile();
			System.out.println("块"+(format.format(block.getBody().getTransactions().get(1).getTimestamp())+""));
			cc.CreateFile(ss,(format.format(block.getBody().getTransactions().get(1).getTimestamp())+""));
			 System.out.println("*********************打印块*****************************");
		} else {
			blockChain.runTransaction(tx);
			// System.out.println("----------------------------------------------------------------");
		}

		//交易Hash入库
		String txHash = transaction.getTxHash();
		this.txRecord.updateTxHashByTxHash("ETH", "true", txHash);
		    
		JsonVo success = JsonVo.success();
		success.setItem(transaction);
		return success;
	}

	/**
	 * 添加节点
	 * 
	 * @param node
	 * @return
	 * @throws Exception			
	 */
	@PostMapping("/node/add")
	public JsonVo addNode(@RequestBody Map<String, Object> node) throws Exception {

		Preconditions.checkNotNull(node.get("ip"), "server ip is needed.");
		Preconditions.checkNotNull(node.get("port"), "server port is need.");

		blockChain.addNode(String.valueOf(node.get("ip")), (Integer) node.get("port"));
		return JsonVo.success();
	}

	/**
	 * 查看节点列表
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("node/view")
	public JsonVo nodeList(HttpServletRequest request) {

		Optional<List<Node>> nodeList = dbAccess.getNodeList();
		JsonVo success = JsonVo.success();
		if (nodeList.isPresent()) {
			success.setItem(nodeList.get());
		}
		return success;
	}

	/**
	 * 获取前台传递来的参数,进行比特币与安达币之间的转换 1.比特币交易需由前台完成 :
	 * 因无法通过两个地址进行比特币交换(如果两个地址能交换比特币,就相当于只要持有某人的银行卡就可以进行转账), 比特币交易需通过发送者钱包进行
	 * 2.前台传递给后台的值为 : 比特币交易金额(amount) 发送者的安达币地址(sender) 3.后台通过汇率将比特币转换为安达币
	 * 4.成功后将信息返回给前台
	 * 
	 * @throws Exception
	 */
	@PostMapping("/transactions/addTxRecord")
	public String addTxRecord(@RequestBody Map<String, Object> map) throws Exception {
//		String txHsah = map.get("id")+"";//获取Hash值
		String txHsah = map.get("TxHash")+"";
		//若此笔交易hash与上一笔交易hash值不同时，则继续进行交易
		String txHashNewSaved = String.valueOf(dbAccess.get("txHashNewSavedBTC"));
		int startIndex = txHashNewSaved.indexOf("(")+1;
		int endIndex = txHashNewSaved.indexOf(")");
		String txHashSaved = txHashNewSaved.substring(startIndex,endIndex);
		//查询交易Hash入库情况
		boolean exchangeStatus = txRecord.selectTxHashByBtcTxHash("BTC", txHsah);
		if ((!txHsah.equals(txHashSaved)) && (!exchangeStatus)) {
			dbAccess.put("txHashNewSavedBTC", txHsah);
			//交易Hash入库
			txRecord.addTxHash("BTC", txHsah, "false", "null");
			
			System.out.println("hash:"+txHsah);
			System.out.println("add:"+map.get("AndaAddress"));
			System.out.println("amount:"+map.get("Amount"));
			System.out.println("id:"+map.get("id"));
		
			//hash->string
			String urlPath = "https://chain.api.btc.com/v3/tx/" + txHsah + "?verbose=2";
			//访问网络，获取json字符串
			HashToString httpClientPost = new HashToString();
			System.out.println("**/**");
			String jsonString = httpClientPost.searchRequest(urlPath);
			//解析json字符串
			AssessJSON assessJSON = new AssessJSON();
			String coinSendAddress = assessJSON.parseJSON(jsonString);
			System.out.println("比特币地址："+coinSendAddress);
//			String Amount = Double.parseDouble(map.get("Amount")+"")/100000000 + "";// 转为比特币,不精确
			BigDecimal b=new BigDecimal((map.get("Amount")+""));
			BigDecimal c=new BigDecimal("100000000");
			String Amount =b.divide(c)+"";
			String AndaAddress = map.get("AndaAddress")+"";// 获得客户端安达地址
			//String hash = map.get("hash").toString();
//			String coinSendAddress = map.get("coinSendAddress")+"";
			System.out.println("客户端安达地址--------------------------" + AndaAddress);
			System.out.println(Amount);
			System.out.println("----------------------------------------");
			Map<String,Object> newMap = new HashMap<String,Object>();
			newMap.put("amount", Amount);
			newMap.put("id", map.get("id")+"");
			newMap.put("AndaAddress", AndaAddress);
			newMap.put("coinSendAddress", coinSendAddress+"");
			txRecord.addTxRecoreClient(newMap);// 将从客户端获取的信息保存到服务器上
			
			//交易Hash传递
			newMap.put("BTCTxHash", txHsah);
			//失败的信息
			if (Amount==null||AndaAddress.equals("")||coinSendAddress.equals("")) {
				CreateAFile cc =new CreateAFile();
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Gson gson=new Gson();
				Map<String, Object> map0=new HashMap<>();
				map0.put("bth", Amount);
				map0.put("id", map.get("id")+"");
				map0.put("AndaAddress", AndaAddress);
				map0.put("BTHAddress", coinSendAddress+"");
				String ss= gson.toJson(map0);
				cc.CreateFile1(ss, format.format(System.currentTimeMillis()));
				}
			
			// 完成交易(添加唯一标识)
			System.out.println("--over--");
			return transActionRun(newMap);

			// 检测交易记录(从比特币服务器),并添加到服务器
			// 将交易记录中的源地址,目标地址,金额,与本地服务器中的数据做对比		
		}else {
			return "FAIL";
		}
		
	}
	
	/**
	 * 获取前台传递来的参数,进行比特币与安达币之间的转换 1.比特币交易需由前台完成 :？？？？？？？？？？？
	 * 因无法通过两个地址进行比特币交换(如果两个地址能交换比特币,就相当于只要持有某人的银行卡就可以进行转账), 比特币交易需通过发送者钱包进行
	 * 2.前台传递给后台的值为 : 比特币交易金额(amount) 发送者的安达币地址(sender) 3.后台通过汇率将比特币转换为安达币
	 * 4.成功后将信息返回给前台
	 * 
	 * @throws Exception
	 */
	@PostMapping("/transactions/addTxRecord1")
	public String addTxRecord1(@RequestBody Map<String, Object> map) throws Exception {
//		String Amount = Double.parseDouble(map.get("Amount")+"")/1000000000 + "";// 将以太币兑换为  wei
//		String Amount =new BigDecimal(Double.parseDouble(map.get("Amount")+"")/1000000000) + "";
		
		String txHsah = map.get("id")+"";
		//若此笔交易hash与上一笔交易hash值不同时，则继续进行交易
		String txHashNewSaved = String.valueOf(dbAccess.get("txHashNewSavedETH"));
		int startIndex = txHashNewSaved.indexOf("(")+1;
		int endIndex = txHashNewSaved.indexOf(")");
		String txHashSaved = txHashNewSaved.substring(startIndex,endIndex);
		
		//查询交易Hash入库情况
		boolean exchangeStatus = this.txRecord.selectTxHashByBtcTxHash("ETH", txHsah);	    
	    if ((!txHsah.equals(txHashSaved)) && (!exchangeStatus)) {
	        dbAccess.put("txHashNewSavedETH", txHsah);
	        //交易Hash入库
	        txRecord.addTxHash("ETH", txHsah, "false", "null");
			BigDecimal b=new BigDecimal((map.get("Amount")+""));
			BigDecimal c=new BigDecimal("1000000000");
			String Amount =b.divide(c)+"";
			String AndaAddress = map.get("AndaAddress")+"";// 获得客户端安达地址
			String coinSendAddress = map.get("coinSendAddress")+"";
			System.out.println("客户端比特地址--------------------------" + AndaAddress);
			System.out.println(Amount);
			
			Map<String,Object> newMap = new HashMap<String,Object>();
			newMap.put("amount", Amount);
			newMap.put("id", map.get("id")+"");
			newMap.put("AndaAddress", AndaAddress);
			newMap.put("coinSendAddress", coinSendAddress+"");
			txRecord.addTxRecoreClient1(newMap);// 将从客户端获取的信息保存到服务器上,	
			//交易Hash传递
			newMap.put("ETHTxHash", txHsah);
			//失败的信息
			if (Amount==null||AndaAddress.equals("")||coinSendAddress.equals("")) {
				CreateAFile cc =new CreateAFile();
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Gson gson=new Gson();
				Map<String, Object> map0=new HashMap<>();
				map0.put("eth", Amount);
				map0.put("id", map.get("id")+"");
				map0.put("AndaAddress", AndaAddress);
				map0.put("ETHAddress", coinSendAddress+"");
				String ss= gson.toJson(map0);
				cc.CreateFile1(ss, format.format(System.currentTimeMillis()));
				}
			
			// 完成交易(添加唯一标识)
			System.out.println("----over----");
			return transActionRun1(newMap);

			// 检测交易记录(从比特币服务器),并添加到服务器
			// 将交易记录中的源地址,目标地址,金额,与本地服务器中的数据做对比	
		}else {
			return "FAIL";	
		}
		
	}

	/**
	 * 以太坊转安达交易执行方法
	 * @return
	 * @throws MnemonicException
	 */
	@PostMapping("/transactions/transActionRun")
	public String transActionRun(@RequestBody Map<String,Object> map_) throws MnemonicException{
		
		org.bitcoinj.core.Wallet wallet = new org.bitcoinj.core.Wallet(TestNet3Params.get());
		String data_ = UUID.randomUUID().toString();
		//创建交易
		String sendAddress = ANDA_SERVER_ANDA_ADDRESS  ;//服务器默认的发送地址
		Transaction tx = new Transaction();
		tx.setSender(sendAddress);
		tx.setRecipient(map_.get("AndaAddress").toString());//手机端接收的安达地址
//		tx.setAmount(new BigDecimal((Double.parseDouble((String) map_.get("amount"))*4000*0.9995)+""));//数值不精确
		tx.setAmount(new BigDecimal(map_.get("amount")+"").multiply(new BigDecimal("3998")));
		System.out.println(tx.getAmount());
		tx.setTimestamp(System.currentTimeMillis());
		tx.setData(data_);
		tx.setCoinSendAddress(map_.get("coinSendAddress")+"");
		String publicKey =Hex.toHexString(wallet.getWatchingKey().getPubKey());
		tx.setPublicKey(publicKey);
		String hash = tx.hash();
		tx.setTxHash(hash);
		org.bitcoinj.core.ECKey eckey1 = org.bitcoinj.core.ECKey
				.fromPrivate(new BigInteger(wallet.getWatchingKey().getPrivKeyBytes33()));
		String data = tx.toStringOragin();
		System.out.println(data);
		String sign = eckey1.signMessage(data);
		tx.setSign(sign);
		
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("sender",sendAddress);
		map.put("recipient", map_.get("AndaAddress").toString());
//		map.put("amount",new BigDecimal((Double.parseDouble((String) map_.get("amount"))*4000*0.9995)+""));
		map.put("amount",new BigDecimal(map_.get("amount")+"").multiply(new BigDecimal("3998")));
		map.put("publicKey", publicKey);
		map.put("data", data_);
		map.put("timestamp", tx.getTimestamp().toString());
		map.put("txHash", hash);
		map.put("sign", sign);
		map.put("coinSendAddress", map_.get("coinSendAddress")+"");
		//交易Hash更新入库
		String btcTxHash = map_.get("BTCTxHash").toString();
		txRecord.updateTxHashBybtcTxHash("BTC", btcTxHash, "false", hash);
		String result = null;
		try {
			result = DaoUtil.post("http://localhost:8081/chain/transactions/new2", map);
			System.out.println(result);
			
			//TransactionRecordDao.updateSfwc(map_);
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return "SUCCESS";
	}
	
	/**
	 * 以太坊转安达交易执行方法
	 * @return
	 * @throws MnemonicException
	 */
	@PostMapping("/transactions/transActionRun1")
	public String transActionRun1(@RequestBody Map<String,Object> map_) throws MnemonicException{
		org.bitcoinj.core.Wallet wallet = new org.bitcoinj.core.Wallet(TestNet3Params.get());
		String data_ = UUID.randomUUID().toString();
		//创建交易
		String sendAddress = ANDA_SERVER_ANDA_ADDRESS  ;//服务器默认的发送地址
		Transaction tx = new Transaction();
		tx.setSender(sendAddress);
		tx.setRecipient(map_.get("AndaAddress").toString());//手机端接收的安达地址
//		tx.setAmount(new BigDecimal((Double.parseDouble((String) map_.get("amount"))*100*0.9995)+""));
		tx.setAmount(new BigDecimal(map_.get("amount")+"").multiply(new BigDecimal("99.95")));
		System.out.println(tx.getAmount());
		tx.setTimestamp(System.currentTimeMillis());
		tx.setData(data_);
		tx.setCoinSendAddress(map_.get("coinSendAddress")+"");
		String publicKey =Hex.toHexString(wallet.getWatchingKey().getPubKey());
		tx.setPublicKey(publicKey);
		String hash = tx.hash();
		tx.setTxHash(hash);
		org.bitcoinj.core.ECKey eckey1 = org.bitcoinj.core.ECKey
				.fromPrivate(new BigInteger(wallet.getWatchingKey().getPrivKeyBytes33()));
		String data = tx.toStringOragin();
		System.out.println(data);
		String sign = eckey1.signMessage(data);
		tx.setSign(sign);
		
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("sender",sendAddress);
		map.put("recipient", map_.get("AndaAddress").toString());
//		map.put("amount",new BigDecimal((Double.parseDouble((String) map_.get("amount"))*100/1000000000*0.9995)+""));
		map.put("amount",new BigDecimal(map_.get("amount")+"").multiply(new BigDecimal("99.95")));
		map.put("publicKey", publicKey);
		map.put("data", data_);
		map.put("timestamp", tx.getTimestamp().toString());
		map.put("txHash", hash);
		map.put("sign", sign);
		map.put("coinSendAddress", map_.get("coinSendAddress")+"");
		
		//交易hash更新入库
	    String ethTxHash = map_.get("ETHTxHash").toString();
	    txRecord.updateTxHashBybtcTxHash("ETH", ethTxHash, "false", hash);
		String result = null;
		try {
			result = DaoUtil.post("http://localhost:8081/chain/transactions/new1", map);
			System.out.println(result);
			
			
			//TransactionRecordDao.updateSfwc(map_);
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return "SUCCESS";
	}

	/**
	 * 汇率接口
	 * @param request
	 * @return
	 * @author Kelly	  
	 */
	@GetMapping({"/transactions/exchangeRate"})
	public JsonVo exchangeRate(HttpServletRequest request)
	{
	    String currency = request.getParameter("currency");
	    TransactionRecordDao dao = new TransactionRecordDao();
	    Map<String, String> resultMap = dao.exchangeRate(currency);
	    JsonVo success = JsonVo.success();
	    success.setItem(resultMap);
	    return success;
	}
	
	/**
	 * 通过时间戳查询交易记录接口（未使用）
	 * @param request
	 * @return
	 * @author Kelly
	 */
	//@GetMapping({"/transactions/record"})
	  public JsonVo readTransactionRecord(HttpServletRequest request)
	  {
	    String timestamp = request.getParameter("timestamp");
	    CreateAFile a = new CreateAFile();
	    String record = a.readTransactionRecord(timestamp);
	    Map<String, String> resultMap = new HashMap<String, String>();
	    resultMap.put("record", record);
	    JsonVo success = JsonVo.success();
	    success.setItem(resultMap);
	    return success;
	  }
	  


	/**
	 * 通过交易Hash查询交易记录接口（未使用）
	 * @param request
	 * @return
	 * @author Kelly
	 */
	  @GetMapping({"/transactions/selectTransactionByHash"})
	  public JsonVo selectTransactionByHash(HttpServletRequest request)
	  {
	    String txHash = request.getParameter("txHash");
	    TransactionRecordDao bc = new TransactionRecordDao();
	    Map<String, Object> map = bc.selectTransactionByHash(txHash);
	    JsonVo success = JsonVo.success();
	    success.setItem(map);
	    return success;
	  }
	  

	  /**
	   * 通过安达地址查询交易记录接口（测试通过，但前端不可通过此接口查询交易记录，后期需注释掉）
	   * @param request
	   * @return
	   * @author Kelly
	   */
	  @GetMapping({"/transactions/selectTransactionByAndaAddress"})
	  public JsonVo selectTransactionByAndaAddress(HttpServletRequest request)
	  {
	    String andaAddress = request.getParameter("andaAddress");
	    TransactionRecordDao bc = new TransactionRecordDao();
	    Map<String, List<Map<String, Object>>> record = bc.selectTransactionByAndaAddress(andaAddress);
	    JsonVo success = JsonVo.success();
	    success.setItem(record);
	    return success;
	  }

}
