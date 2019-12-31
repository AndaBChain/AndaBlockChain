package com.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;

public class FileTool {

	public static void main(String[] args) {
//        writeToFile("123");
        String ss=readString();
//        String sss="10000";
        String sss=ss.trim();
        Integer i=Integer.parseInt(sss);
		if (i>0) {
			BigDecimal cc=new BigDecimal(sss).subtract(new BigDecimal(1));
			writeToFile(cc+"");
		}
		System.out.println(readString());
    }

    /**
     * 在文件中添加数据
     * @param filePath 文件路径
     * @param data 添加数据
     * @return
     */
    public static String writeToFile(String data){
        FileOutputStream out = null;
        String path = "E:\\file\\test.txt";
        try
        {
            //目标文件
            File file=new File(path);
            //若不存在即创建文件
            if(!file.exists()) {
                if (!file.getParentFile().exists()) {   //如果父文件夹不存在
                    file.getParentFile().mkdirs();   //新建多层文件夹
                }
                file.createNewFile();
            }
            //创建文件输入流
            out =new FileOutputStream(file,false); //如果追加方式用true是写入，false是覆盖
            //写入内容
            StringBuffer sb=new StringBuffer();
            sb.append(data+"  ");
            //写入
            out.write(sb.toString().getBytes("utf-8"));//注意需要转换对应的字符集
            return "success";
        }
        catch(IOException ex)
        {
            System.out.println(ex.getStackTrace());
        }finally {
            try {
                if(out!=null){
                    out.close();   //关闭流
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "false";
    }

    
    public static String readString(){
    	String path = "E:\\file\\test.txt";
//    	String path = "/opt/monichain/file1/num.txt";
    	File file=new File(path);
        StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            String s = null;
            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
                result.append(System.lineSeparator()+s);
            }
            br.close();    
        }catch(Exception e){
            e.printStackTrace();
        }
        return result.toString();
    }
}
