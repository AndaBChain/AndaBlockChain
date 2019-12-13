package com.onets.core.wallet.transaction;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * hash工具类
 */
public class HashUtils {
    /**
     * 使用 sha256 算法加密
     * @param input
     * @return
     */
    public static String sha256Hex(String input) {
        return DigestUtils.sha256Hex(input);
    }

    /**
     * 使用 sha256 hash 算法加密，返回一个 64 位的字符串 hash
     * @param input
     * @return
     */
    public static String sha256Hex(byte[] input) {
        return org.spongycastle.util.encoders.Hex.toHexString(DigestUtils.sha256(input));
    }

    public static byte[] sha256(String input) {
        return DigestUtils.sha256(input);
    }

    public static byte[] sha256(byte[] input) {
        return DigestUtils.sha256(input);
    }

    /**
     * 地址验证---暂时不做验证
     * @param address
     * @return
     */
    public static boolean verifyAddress(String address){
        return true;
    }
}
