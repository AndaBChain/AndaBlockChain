package com.onets.wallet.AlipayAPI;

/**
 * 支付宝
 * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
 */
public class AlipayActivity {
    /*SDK调用前需要进行初始化*/
    //AlipayClient alipayClient = new DefaultAlipayClient(URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);

    /*支付宝支付业务：入参app_id*/
    public static final String APPID = "";

    /*支付宝账户登陆授权业务：入参pid值*/
    public static final String PID = "";
    /*支付宝账户登陆授权业务：入参target_id值*/
    public static final String TARGET_ID = "";

    /*商户私钥，pkcs8格式
    * RSA2_PRIVATE 或者 RSA_PRIVATE 只需要填入一个，优先使用 RSA2_PRIVATE
    * 获取 RSA2_PRIVATE ，使用支付宝提供的公私钥生成工具生成
    * 工具地址：https://doc.open.alipay.com/docs/doc.htm?treeId=291&articleId=106097&docType=1 */
    public static final String RSA2_PRIVATE = "";
    public static final String RSA_PRIVATE = "";

    public static final int SDK_PAY_FLAG = 1;
    public static final int SDK_AUTH_FLAG = 2;
}
