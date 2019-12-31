package com.test;

import com.google.common.base.Joiner;

import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;

import java.util.ArrayList;
import java.util.List;

/**
 * core 核心
 * @author John L. Jegutanis
 */
public class CoreUtils {

    /**
     * 从String列表中获取助记词
     * @param mnemonic String列表的参数
     * @return 返回String格式
     */
    public static String getMnemonicToString(List<String> mnemonic) {
        return Joiner.on(' ').join(mnemonic);
    }

    /**
     * 从byte数组中获取助记词
     * @param bytes
     * @return String列表
     */
    public static List<String> bytesToMnemonic(byte[] bytes) {
        List<String> mnemonic;
        try {
            mnemonic = MnemonicCode.INSTANCE.toMnemonic(bytes);
        } catch (MnemonicException.MnemonicLengthException e) {
            throw new RuntimeException(e); // should not happen, we have 16bytes of entropy
        }
        return mnemonic;
    }

    /**
     * 从byte数组中获取助记词，返回String格式
     * @param bytes
     * @return
     */
    public static String bytesToMnemonicString(byte[] bytes) {
        return getMnemonicToString(bytesToMnemonic(bytes));
    }

    /**
     * 助记符解析
     * @param mnemonicString String
     * @return String的ArrayList
     */
    public static ArrayList<String> parseMnemonic(String mnemonicString) {
        ArrayList<String> seedWords = new ArrayList<>();
        for (String word : mnemonicString.trim().split(" ")) {
            if (word.isEmpty()) continue;
            seedWords.add(word);
        }
        return seedWords;
    }

}
