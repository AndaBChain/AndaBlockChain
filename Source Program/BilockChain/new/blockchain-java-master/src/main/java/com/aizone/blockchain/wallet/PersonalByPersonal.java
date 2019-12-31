package com.aizone.blockchain.wallet;

import com.aizone.blockchain.db.DBAccess;
import com.aizone.blockchain.event.CommitAnyAccountEvent;
import com.aizone.blockchain.event.NewAccountEvent;
import com.aizone.blockchain.net.ApplicationContextProvider;
import com.aizone.blockchain.sm.SM2;
import com.aizone.blockchain.utils.CreateAFile;
import com.aizone.blockchain.utils.ECKey;
import com.aizone.blockchain.utils.HashUtil;
import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 账户控制工具类, 锁定，解锁等操作
 * @author yangjian
 * @since 18-4-6
 */
@Component
public class PersonalByPersonal {

	@Autowired
	private DBAccess dbAccess;
	

	/**
	 * 创建一个默认账户
	 * @return
	 */
	public Account newAccount() throws Exception {

		//KeyPair keyPair = WalletUtils.generateKeyPair();
		ECKey eckey = new ECKey(new SM2());
		 
		//addr = Hex.toHexString(eckey.getAddress());
		Account account = new Account();
		System.out.println("---------------普通账户入库以下:");
		account = new Account(eckey.getPubKey(), eckey.getAddress());
		//不存储私钥
		dbAccess.putAccount(account);
		//发布同步账号事件
		ApplicationContextProvider.publishEvent(new NewAccountEvent(account));
		account.setPrivateKey(HashUtil.toHexString(eckey.getPrivKeyBytes()));
		//如果没有发现挖矿账号, 则优先创建挖矿账号
		Optional<Account> coinBaseAccount = dbAccess.getCoinBaseAccount();
		if (!coinBaseAccount.isPresent()) {
			dbAccess.putCoinBaseAccount(account);
		}
		return account;
	}
	
	/**
	 * 创建一个新账户
	 * @return
	 */
	public Account commitAccount(String address , String publicKey,String privateKey) throws Exception {
		
		System.out.println("---进方法--");
		Optional<Account> re = dbAccess.getAccount(address);
		System.out.println("---获取集合--"+re.isPresent());
		BigDecimal b = null;
		if (re.isPresent()) {
			System.out.println("---获取余额--");
			 b=re.get().getBalance();
		}
		
		Account account = new Account();
		System.out.println("---------------普通账户入库以下:");
		account = new Account();
		account.setAddress(address);
		account.setPrivateKey(privateKey);
		account.setPublicKey(publicKey.getBytes());
		if (b!=null) {
			account.setBalance(b);
		} else {
			//注册账号中发放10000个安达币
			CreateAFile a=new CreateAFile();
			String ss=a.readString().trim();
			System.out.println(ss);
			Integer i=Integer.parseInt(ss);
			if (i>0) {
				account.setBalance(BigDecimal.valueOf(0));
				BigDecimal cc=new BigDecimal(ss).subtract(new BigDecimal(1));
				a.writeToFile(cc+"");
			} else {
				
				account.setBalance(BigDecimal.valueOf(0));
			}
		}
		//不存储私钥
		dbAccess.putAccount(account);
		//发布同步账号事件
		ApplicationContextProvider.publishEvent(new NewAccountEvent(account));
		account.setPrivateKey(privateKey);
		//如果没有发现挖矿账号, 则优先创建挖矿账号
		Optional<Account> coinBaseAccount = dbAccess.getCoinBaseAccount();
		if (!coinBaseAccount.isPresent()) {
			dbAccess.putCoinBaseAccount(account);
		}
		return account;
	}
	
	/**
	 * IOS恢复证包用提交账户信息
	 * @param encryptedSeed
	 * @param encryptedPrivateKey
	 * @param publicKey
	 * @param encryptedKeyStore
	 * @param address
	 * @return
	 */
	public AnyAccount commitAnyAccount(String encryptedSeed, String encryptedPrivateKey, String publicKey, String encryptedKeyStore, String address)
	  {
	    AnyAccount anyAccount = new AnyAccount(encryptedSeed, encryptedPrivateKey, publicKey, encryptedKeyStore, address);
	    
	    //leveldb数据库中存放该账户信息
	    dbAccess.putAnyAccount(anyAccount);
	    
	    ApplicationContextProvider.publishEvent(new CommitAnyAccountEvent(anyAccount));
	    return anyAccount;
	  }
}
