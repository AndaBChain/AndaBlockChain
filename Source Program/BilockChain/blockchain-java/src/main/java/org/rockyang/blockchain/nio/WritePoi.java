package org.rockyang.blockchain.nio;

import java.io.*;
import java.util.*;



import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.rockyang.blockchain.db.MySql.MySQL;




/**
 *
 * 
 * @author Wang HaiTian
 *
 */
public class WritePoi {

	/**
	 * 准备数据
	 * @return
	 */
	public List<Map<String,Object>> data(){
		MySQL a = new MySQL();
		List<Object> TIME = a.examineTime();
		List<Object> PeerIp = a.examinePeerIp();
		List<Object> PeerAddress = a.examinePeerAddress();
		List<Map<String, Object>> list = new ArrayList<>();
		for (int b=0;b<=TIME.size()-1;b++ ) {
			Map<String, Object> map = new HashMap<>();
			map.put("PeerAddress", PeerAddress.get(b));
			map.put("PeerIp", PeerIp.get(b));
			map.put("time", TIME.get(b));
			list.add(map);

		}

		return list;
	}


	public void writeData2Excel(){
		String resultPath = "E:/KEY/钱包信息表.xls";
		String[] title={"PeerAddress","PeerIp","time"}; //表头
		//创建工作簿
		HSSFWorkbook workbook=new HSSFWorkbook();
		//创建sheet
		HSSFSheet sheet=workbook.createSheet("sheet");
		//创建第一行
		HSSFRow row=sheet.createRow(0);
		HSSFCell cell=null;
		//插入第一行数据的表头
		for(int i=0;i<title.length;i++){
			cell=row.createCell(i);
			cell.setCellValue(title[i]);
		}
		List<Map<String,Object>> data = data();
		for(int i=1;i<=data.size();i++){
			HSSFRow row2=sheet.createRow(i);
			HSSFCell cell0=row2.createCell(0);
			cell0.setCellValue(data.get(i-1).get("PeerAddress").toString());
			cell0=row2.createCell(1);
			cell0.setCellValue(data.get(i-1).get("PeerIp").toString());
			cell0=row2.createCell(2);
			cell0.setCellValue(data.get(i-1).get("time").toString());
		}
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(resultPath);
			workbook.write(fileOutputStream);
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void up() {
		WritePoi w = new WritePoi();
		w.writeData2Excel();
		System.out.println("完成");
	}
}

