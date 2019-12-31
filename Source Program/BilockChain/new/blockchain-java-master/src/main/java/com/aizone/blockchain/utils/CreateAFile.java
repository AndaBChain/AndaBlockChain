package com.aizone.blockchain.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CreateAFile {

	/***
	 * 文件覆盖
	 * @param 地址
	 * @param 文件名字
	 * @throws IOException
	 */
	public void CreateFile(String s,String ss) throws IOException {

		/* 先写好一个路径path，再看最后一段test.txt，
         * 如果在file文件夹中不存在test.txt这个文件的话，从第14行到19行就会创建 */
		//String path = "E:\\file\\"+ss+".txt";
		String path = "/opt/monichain/file/"+ss+".txt"; 
		File file = new File(path); 
		if(!file.exists()){
			file.getParentFile().mkdirs();
		}
		file.createNewFile(); 
		// write 向test.txt中写入文件； 
		//如果FileWriter的构造参数为true，那么就进行内容追加; 
		//如果FileWriter的构造参数为false,那么就进行内容的覆盖; 
		FileWriter fw = new FileWriter(file,false); 
		BufferedWriter bw = new BufferedWriter(fw); 
		bw.write(s); 
		bw.flush(); 
		bw.close(); 
		fw.close();
//		return path;
	}
	
	/***
	 * 文件覆盖
	 * @param 地址
	 * @param 文件名字
	 * @throws IOException
	 */
	public void CreateFile1(String s,String ss) throws IOException {

		/* 先写好一个路径path，再看最后一段test.txt，
         * 如果在file文件夹中不存在test.txt这个文件的话，从第14行到19行就会创建 */
		//String path = "E:\\file\\"+ss+".txt";
		String path = "/opt/monichain/file1/"+ss+".txt"; 
		File file = new File(path); 
		if(!file.exists()){
			file.getParentFile().mkdirs();
		}
		file.createNewFile(); 
		// write 向test.txt中写入文件； 
		//如果FileWriter的构造参数为true，那么就进行内容追加; 
		//如果FileWriter的构造参数为false,那么就进行内容的覆盖; 
		FileWriter fw = new FileWriter(file,false); 
		BufferedWriter bw = new BufferedWriter(fw); 
		bw.write(s); 
		bw.flush(); 
		bw.close(); 
		fw.close();
//		return path;
	}
	
	
	/**
	 * 文件中插入JSON
	 * @param 文件地址
	 * @return
	 */
	public  String writeToFile(String data){
        FileOutputStream out = null;
//        String path = "E:\\file\\test.txt";
        String path = "/opt/monichain/file1/num.txt";
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
            out =new FileOutputStream(file,false); //如果追加方式用true
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
	
	
    /**
     * 读取txt文件的内容
     * @param file 想要读取的文件对象
     * @return 返回文件内容
     */
    public String readString(){
    	//String path = "E:\\file\\test.txt";
    	String path = "/opt/monichain/file1/num.txt";
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

    /**
     * 读取文件记录内容（未使用）
     * @param timestamp
     * @return
     * @author Kelly
     */
    public String readTransactionRecord(String timestamp)
    {
      String path = "E:\\" + timestamp + ".txt";
      
      File file = new File(path);
      StringBuilder result = new StringBuilder();
      try {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String s = null;
        while ((s = br.readLine()) != null) {
          result.append(System.lineSeparator() + s);
        }
        br.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return result.toString();
    }
  
}
