package com.test;

import java.util.HashMap;
import java.util.Map;

import com.aizone.blockchain.dao.DaoUtil;
import com.aizone.blockchain.web.controller.BlockController;

public class TransactionTest {

	public static void main(String[] args) {
//		String a = "5ca7793f385deb25a34577c672d04802f0b1b8a0c5e9c229806a531c9e040909";
//		String b = "Optional.of(5ca7793f385deb25a34577c672d04802f0b1b8a0c5e9c229806a531c9e040909)";
//		int startIndex = b.indexOf("(")+1;
//		int endIndex = b.indexOf(")");
//		String c = b.substring(startIndex,endIndex);
//		System.out.println(c);
//		System.out.println(a.equals(c));

		
		 Map<String,Object> map = new HashMap<String,Object>();
	     map.put("AndaAddress","qpxwq48Ys5C2AneS3K893M4sissPBAsZL6");//
	     map.put("Amount","1000");
	     map.put("id", "5da7793f385deb25a34577c672d04802f0b1b8a0c5e9c229806a531c9e040909");
	     map.put("TxHash", "5ca7793f385deb25a34577c672d04802f0b1b8a0c5e9c229806a531c9e040909");
	   
	     String result = null;
			try {
				//result = DaoUtil.post("http://3.0.327.114:8081/chain/transactions/addTxRecord", map);
				result = DaoUtil.post("http://3.0.88.3:8081/chain/transactions/addTxRecord1", map);
				
				System.out.println(result);
				
	
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}
}
