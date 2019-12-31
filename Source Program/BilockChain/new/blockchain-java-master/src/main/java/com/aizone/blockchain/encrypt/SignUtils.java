package com.aizone.blockchain.encrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bitcoinj.core.ECKey;
import org.spongycastle.util.encoders.Hex;

/**
 * 签名工具类
 * 
 * @author yangjian
 * @since 18-4-10
 */
public class SignUtils {

	/**
	 * 使用公钥验证签名
	 * 
	 * @param publicKey
	 * @param sign
	 * @param data
	 * @return
	 */
	public static boolean verify(String publicKey, String sign, String data) throws Exception {
		//data = data + "2";测试修改参数后返回值
		//sign = sign + "0";
		//publicKey = publicKey + "0";
		System.out.println("---------------------data---" + data);
		//System.out.println(publicKey);
		System.out.println("----------------------sign--"+sign);
		org.bitcoinj.core.ECKey eckey = org.bitcoinj.core.ECKey.fromPublicOnly(Hex.decode(publicKey));
		System.out.println("eckey:"+eckey);
		boolean b = false;
		try {
			//b =  ECKey.verify(data.getBytes(), Hex.decode(sign), Hex.decode(publicKey));
			org.bitcoinj.core.ECKey eckey2 = org.bitcoinj.core.ECKey.fromPublicOnly(Hex.decode(publicKey));
			eckey2.verifyMessage(data, sign);
			System.out.println(eckey2);
			b = true;
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			b = false;
		}		
		System.out.println("---------------------------------------------" + b);	

		return b;
		// return true;
	}

	/**
	 * 暂时修改，任意签名都通过
	 *//*
		 * public static boolean verify(PublicKey publicKey, String sign, String
		 * data) throws Exception {
		 * 
		 * return true; }
		 */

}
