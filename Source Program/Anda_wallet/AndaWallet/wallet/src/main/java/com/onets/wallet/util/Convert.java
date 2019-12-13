package com.onets.wallet.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 字符转换类
 */
public class Convert {

    /**
     * 字符串转十六进制
     * @param str
     * @return
     */
    public String convertStringToHex(String str){
        //把字符串转换成char数组
        char[] chars = str.toCharArray();
        //新建一个字符串缓存类
        StringBuffer hex = new StringBuffer();
        //循环每一个char
        for(int i = 0; i < chars.length; i++){
            //把每一个char都转换成16进制的，然后添加到字符串缓存对象中
            hex.append(Integer.toHexString((int)chars[i]));
        }
        //最后返回字符串就是16进制的字符串
        return hex.toString();
    }

    public File writeToFile(String str,File txt){
        if(!txt.exists()){
            try {
                txt.createNewFile();
                byte bytes[]=new byte[10240];
                bytes=str.getBytes();
                int b=bytes.length;  //是字节的长度，不是字符串的长度
                FileOutputStream fos=new FileOutputStream(txt);
                fos.write(bytes,0,b);
                fos.write(bytes);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return txt;
    }
}
