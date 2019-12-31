package com.test;

import org.spongycastle.util.encoders.Base64;

import com.aizone.blockchain.sm.SM2;
import com.aizone.blockchain.sm.SM2Utils;
import com.aizone.blockchain.sm.Util;
import com.aizone.blockchain.utils.ECKey;
import com.aizone.blockchain.utils.HashUtil;

/**
 * 国密SM2密钥对测试生成以及签名和签名检测测试
 * @author wss
 *
 */
public class EckeyTest {
	public static void main(String[] args) throws Exception {
		ECKey eckey = new ECKey(new SM2());
		
		String address = HashUtil.toHexString(eckey.getAddress()) ;
		String privateKey = HashUtil.toHexString(eckey.getPrivKeyBytes());
		String publicKey = HashUtil.toHexString(eckey.getPubKey());
		System.out.println("地址"+address);
		System.out.println("--------------------");
		System.out.println("公钥"+privateKey);
		System.out.println("--------------------");
		System.out.println("私钥"+publicKey);
		System.out.println("----------------------");
		
		//密钥格式转换
		 String prikS = new String(Base64.encode(Util.hexToByte(privateKey)));
		 String pubkS = new String(Base64.encode(Util.hexToByte(publicKey)));
		String data = address;
		//对address数据作数字签名
		byte sign[] = SM2Utils.sign("".getBytes(), Base64.decode(prikS.getBytes()), data.getBytes());
		String signs = Util.getHexString(sign);
		System.out.println(signs);
		
		//签名验证
		boolean b = SM2Utils.verifySign("".getBytes(), Base64.decode(pubkS.getBytes()), data.getBytes(), Util.hexStringToBytes(signs));
		System.out.println(b);
	}
}
