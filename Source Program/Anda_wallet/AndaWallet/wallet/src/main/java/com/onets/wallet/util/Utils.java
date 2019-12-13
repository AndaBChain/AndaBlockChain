package com.onets.wallet.util;

import android.os.StrictMode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 工具类
 */
public class Utils {
    /**
     * 从输入流获取字符串
     * @param is
     * @return
     */
    public static String getTextFromStream(InputStream is){

        byte[] b = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            while((len = is.read(b)) != -1){
                bos.write(b, 0, len);
            }
            String text = new String(bos.toByteArray());
            bos.close();
            return text;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
