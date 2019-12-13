package com.onets.wallet.util;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Yu K.Q.
 *
 * This class checks the quality of the provided password, for example if it is too common.
 *
 * The common passwords list is provided by Mark Burnett
 * https://xato.net/passwords/more-top-worst-passwords/
 *
 * 密码质量检测器
 */
public class PasswordQualityChecker {
    private static final Logger log = LoggerFactory.getLogger(PasswordQualityChecker.class);
    public static final int DEFAULT_MIN_PASSWORD_LENGTH = 10;//默认的密码最小长度
    private static final String COMMON_PASSWORDS_TXT = "common_passwords.txt";//常见的密码TXT文件
    private final HashSet<String> passwordList;//密码列表
    private final int minPasswordLength;//最小密码长度

    public PasswordQualityChecker(Context context) {
        this(context, DEFAULT_MIN_PASSWORD_LENGTH);
    }

    public PasswordQualityChecker(Context context, int minPassLength) {
        this.minPasswordLength = minPassLength;
        this.passwordList = new HashSet<>(10000);

        try {
            InputStream stream = context.getAssets().open(COMMON_PASSWORDS_TXT);
            BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String word;
            while ((word = br.readLine()) != null) {
                this.passwordList.add(word);
            }
            br.close();
        } catch (IOException e) {
            log.error("Could not open common passwords file.", e);
        }
    }

    /**
     * Check if the password meets some quality criteria, like length and how common it is
     * 检查密码是否符合一些质量标准，比如长度以及它的常见程度
     */
    public void checkPassword(String password) throws PasswordTooCommonException, PasswordTooShortException, PasswordTooSingleException {
        if (passwordList.contains(password)) throw new PasswordTooCommonException(password);

        if (password.length() < minPasswordLength) {
            throw new PasswordTooShortException("Password length is too short: " + password.length());
        }
        if(!isLetterDigit(password)){
            throw new PasswordTooSingleException("Password is too single: " + password);
        }
    }

    /**
     * Gets the password list this code uses.
     * 获取该代码使用的密码列表。
     */
    public Set<String> getWordList() {
        return passwordList;
    }

    /*获取密码的最小长度*/
    public int getMinPasswordLength() {
        return minPasswordLength;
    }

    public class PasswordTooCommonException extends Exception {
        public PasswordTooCommonException(String commonPassword) {
            super(commonPassword);
        }
    }

    public class PasswordTooShortException extends Exception {
        public PasswordTooShortException(String detailMessage) {
            super(detailMessage);
        }
    }

    public class PasswordTooSingleException extends Exception{
        public PasswordTooSingleException(String detailMessage){
            super(detailMessage);
        }
    }

    /**
     * 规则2：至少包含大小写字母及数字中的两种
     * 是否包含
     *
     * @param str
     * @return
     */
    public static boolean isLetterDigit(String str) {
        boolean isDigit = false;//定义一个boolean值，用来表示是否包含数字
        boolean isLetter = false;//定义一个boolean值，用来表示是否包含字母
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {   //用char包装类中的判断数字的方法判断每一个字符
                isDigit = true;
            } else if (Character.isLetter(str.charAt(i))) {  //用char包装类中的判断字母的方法判断每一个字符
                isLetter = true;
            }
        }
        String regex = "^[a-zA-Z0-9]+$";
        boolean isRight = isDigit && isLetter && str.matches(regex);
        return isRight;
    }
}
