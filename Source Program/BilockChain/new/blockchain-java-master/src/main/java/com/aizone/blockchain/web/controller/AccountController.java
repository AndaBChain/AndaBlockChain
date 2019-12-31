package com.aizone.blockchain.web.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.aizone.blockchain.core.Transaction;
import com.aizone.blockchain.wallet.PersonalByPersonal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aizone.blockchain.db.DBAccess;
import com.aizone.blockchain.utils.JsonVo;
import com.aizone.blockchain.wallet.Account;
import com.aizone.blockchain.wallet.AnyAccount;
import com.aizone.blockchain.wallet.Personal;
import com.google.common.base.Optional;

/**
 * @author yangjian
 * @since 18-4-8
 */
@SuppressWarnings("unused")
@RestController
@RequestMapping("/account")
public class AccountController {
	@Autowired
	private PersonalByPersonal personalByPersonal;
	@Autowired
	private Personal personal;
	@Autowired
	private DBAccess dbAccess;

	/**
	 * 创建账户
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/new")
	public JsonVo newAccount(@RequestBody HttpServletRequest request) throws Exception {
			
		//String password = request.getParameter("password");
		
		/**
		 * 传入的格式为json标准格式
		 */
		/*JSONObject jsonObj = new JSONObject(getinputStream(request));
        JSONObject  password = jsonObj.getJSONObject("password");*/
		//System.out.println(password);
		
		Account account = personal.newAccount();
		return new JsonVo(JsonVo.CODE_SUCCESS, "New account created, please remember your Address and Private Key.",
				account);
	}
	/**
	 * 创建账户
	 * 交易所用
	 * @param request
	 * @return
	 */
	@PostMapping("/newbybourse")
	public JsonVo newAccountbybourse(@RequestBody HttpServletRequest request) throws Exception {

		//String password = request.getParameter("password");

		/**
		 * 传入的格式为json标准格式
		 */
		/*JSONObject jsonObj = new JSONObject(getinputStream(request));
        JSONObject  password = jsonObj.getJSONObject("password");*/
		//System.out.println(password);

		Account account = personalByPersonal.newAccount();
		return new JsonVo(JsonVo.CODE_SUCCESS, "New account created, please remember your Address and Private Key.",
				account);
	}
	/**
	 * 客户端提交已经生成的账户
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/commitnew")
	public JsonVo commitAccount(@RequestBody Map<String, String> request) throws Exception {
		/**
		 * 无法使用request.getParameter()来获取参数
		 * 当form表单内容采用enctype=multipart/form-data编码时，即使先调用request.getParameter()也得不到数据
		 */
		
		String address = request.get("address");
		String publicKey = request.get("publicKey");
		String password = request.get("password");
		String privateKey = request.get("privateKey");
		
		/**
		 *格式为json格式时使用
		 */
		/*JSONObject jsonObj = new JSONObject(getinputStream(request));
        JSONObject  address = jsonObj.getJSONObject("address");
        JSONObject  publicKey = jsonObj.getJSONObject("publicKey");
        JSONObject  password = jsonObj.getJSONObject("password");
        JSONObject  privateKey = jsonObj.getJSONObject("privateKey");*/
		
	/*	Map<String, String> map_1 = getMap(getinputStream(request));
		String address = map_1.get("address");
		String publicKey = map_1.get("publicKey");
		String privateKey = map_1.get("privateKey");*/
		System.out.println("获取到的信息" + address +"_+_" + publicKey + "_+_" + privateKey);
		//System.out.println(address +"------" +publicKey+"-------"+privateKey);
		personal.commitAccount(address, publicKey, privateKey);
		return new JsonVo(JsonVo.CODE_SUCCESS, "account Commit Success");
	}

	/**
	 * 获取挖矿账号
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("/coinbase")
	public JsonVo coinbase(HttpServletRequest request) {

		Optional<Account> coinBaseAccount = dbAccess.getCoinBaseAccount();
		JsonVo success = JsonVo.success();
		if (coinBaseAccount.isPresent()) {
			success.setItem(coinBaseAccount.get());
		} else {
			success.setMessage("CoinBase Account is not created");
		}
		return success;
	}

	/**
	 * 列出所有的账号
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("/list")
	public JsonVo listAccounts(HttpServletRequest request) {

		List<Account> accounts = dbAccess.listAccounts();
		JsonVo success = JsonVo.success();
		success.setItem(accounts);
		return success;
	}
	/**
	 * 列出所有的账号
	 *
	 * @param request
	 * @return
	 *//*
	@GetMapping("/list")
	public JsonVo listAccounts(HttpServletRequest request) {
		List<Account> accounts = dbAccess.listAccounts();
		JsonVo success = JsonVo.success();
		success.setItem(accounts);
		return success;
	}*/
	
	/**
	 * 安达币交易测试
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("/mine1mine1")
	public String mine1(@RequestBody Map<String, String> request) throws Exception {
		
		String s1 = request.get("recipient");
		String s2 = request.get("sender");
		String s3 = request.get("amount");
		//接收账户
		Optional<Account> recipient = dbAccess.getAccount(s1);
		System.out.println(recipient);
		//账户转账
		Optional<Account> sender = dbAccess.getAccount(s2);
		System.out.println(sender);
		//执行转账操作,更新账户余额
		BigDecimal b=new BigDecimal(s3);
		System.out.println("sender"+sender.get().getBalance());
		System.out.println("recipient"+recipient.get().getBalance());
		sender.get().setBalance(sender.get().getBalance().subtract(b));
		recipient.get().setBalance(recipient.get().getBalance().add(b));
		System.out.println("--转账--");
		dbAccess.putAccount(sender.get());
		dbAccess.putAccount(recipient.get());
		System.out.println("--成功--");
		return "success";
	}
	
	/**
	 * 设置账号金额
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("/mine2mine2")
	public String mine2(@RequestBody Map<String, String> request) throws Exception {
		
		String s1 = request.get("recipient");
		String s2 = request.get("amount");
		//接收账户
		Optional<Account> recipient = dbAccess.getAccount(s1);
		System.out.println(recipient);
		//执行操作,更新账户余额
		BigDecimal b=new BigDecimal(s2);
		System.out.println("recipient"+recipient.get().getBalance());
		recipient.get().setBalance(b);
		dbAccess.putAccount(recipient.get());
		return "success";
	}

	/**
	 * 账户余额查询
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/getAccountBalance")
	//HttpServletRequest
	public JsonVo getAccountBalance(@RequestBody Map<String, String> request) {
		JsonVo success = null;
		String address = request.get("address");//request.getParameter("address");
		System.out.println("2018928" + address);
		if (address == null || "".equals(address)) {
			success = JsonVo.fail();
		} else {
			Optional<Account> account = dbAccess.getAccount(address);
			System.out.println(account);
			System.out.println(account.isPresent());
			if (!account.isPresent()) {
				success = JsonVo.fail();
			}else{
				BigDecimal balance = account.get().getBalance();
				success = JsonVo.success();
				success.setItem(balance.toString());			
			}
		}
		return success;
	}
	
    /**
     * 将截取的JSON类型的字符串转换为map形式
     * @param str
     * @return
     */
    public static Map<String, String> getMap(String str){
    	String[] array_1 = str.trim().split(",");
		Map<String, String> map_1 = new HashMap<String, String>();
		for(int i = 0 ; i<array_1.length ; i++){
			String[] array_2 = array_1[i].replace("\"", "").replace("{", "").replace("}", "").replace(" ", "").split(":");
			map_1.put(array_2[0], array_2[1]);
		}
		return map_1;
    }
    


    /**
     * IOS恢复证包用,提交账户信息（加密的种子密码、加密的私钥、公钥、加密的keystore、钱包地址）
     * @param request
     * @return
     * @throws Exception
     * @author Kelly
     */
    @PostMapping({"/commitAnyAccount"})
    public JsonVo commitAccount(HttpServletRequest request)
      throws Exception
    {
      String encryptedSeed = request.getParameter("encryptedSeed");
      String encryptedPrivateKey = request.getParameter("encryptedPrivateKey");
      String publicKey = request.getParameter("publicKey");
      String encryptedKeyStore = request.getParameter("encryptedKeyStore");
      String address = request.getParameter("address");
      AnyAccount account = personal.commitAnyAccount(encryptedSeed, encryptedPrivateKey, publicKey, encryptedKeyStore, address);
      return new JsonVo(200, "anyAccount Commit Success", account);
    }
	/**
	 * 当前块高
	 * @return
	 */
	@GetMapping("/block-lndex")
	public JsonVo getblocklndex()
	{
		Object accounts = dbAccess.getLastBlockIndex();
		JsonVo success = JsonVo.success();
		success.setItem(accounts);
		return success;
	}

	/**
	 * 根据哈希头找寻区块
	 * @return
	 */
	@PostMapping("/getblock")
	public JsonVo getblock(@RequestBody Map<String, String> request)
	{
		String blockIndex = request.get("blockIndex");
		Object accounts = dbAccess.getBlock(blockIndex);
		JsonVo success = JsonVo.success();
		success.setItem(accounts);
		return success;
	}


	/**
     * IOS恢复证包用（获取账户信息）
     * @param request
     * @return
     * @throws Exception
     * @author Kelly
     */
    @PostMapping({"/getAnyAccount"})
    public JsonVo getAnyAccount(HttpServletRequest request)
      throws Exception
    {
      String encryptedSeed = request.getParameter("encryptedSeed");
      Optional<AnyAccount> account = dbAccess.getAnyAccount(encryptedSeed);
      if (account.isPresent()) {
        AnyAccount anyAccount = new AnyAccount();
        Map<String, String> map = anyAccount.AnyAccountToMap((AnyAccount)account.get());
        return new JsonVo(200, "Account Message", map);
      }
      return new JsonVo(400, "The address you entered is incorrect.");
    }
}
