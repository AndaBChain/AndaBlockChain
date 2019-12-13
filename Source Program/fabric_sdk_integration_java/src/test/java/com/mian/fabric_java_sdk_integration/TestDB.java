package com.mian.fabric_java_sdk_integration;

import com.mian.fabric_java_sdk_integration.entity.CashBase;
import com.mian.fabric_java_sdk_integration.service.TestService;
import com.mian.fabric_java_sdk_integration.util.DBUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
* @Description: 测试数据库相关
* @Param:
* @return:
* @Author: 继鹏
* @Date: 2019/12/13
*/
public class TestDB {
    public static void main(String[] args) throws Exception {
        TestService testService= new TestService();

        Connection con = DBUtils.getConnection();
        System.out.println("连接成功");
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM [dbo].[CashBase]");

        //遍历结果集

        Map<String, Object> dataMap=new HashMap<String, Object>();//结果集
        String it = null;//循环内的每个参数
        int x = 2;//计数器，显示行号
        String Primary_key = null;//键
        List value = new ArrayList();//值

//        if (rs.next()) {
//            ResultSetMetaData rsMeta=rs.getMetaData();
//            int columnCount=rsMeta.getColumnCount();
//            for (int i=1; i<=columnCount; i++) {
//                it = rs.getString(i);
//                dataMap.put(rsMeta.getColumnLabel(i), it);
//            }
//            System.out.println("**原来第一行的dataMap = " + dataMap);
//            while(rs.next()){
//                for (int i=1; i<=columnCount; i++) {
//                    it = rs.getString(i);
//                    dataMap.put(rsMeta.getColumnLabel(i), it);
//                }
//                System.out.println("**原来 第" +x+ "行的dataMap = " + dataMap);
//                x++;
//            }
//        }

        if (rs.next()) {
            //获取元数据
            ResultSetMetaData rsMeta = rs.getMetaData();
            //列数
            int columnCount = rsMeta.getColumnCount();
            //遍历列
            for (int i=1; i <= columnCount; i++) {
                if ("ID".equals(rsMeta.getColumnLabel(i))){
                    Primary_key = rsMeta.getColumnLabel(i) + "="+rs.getString(i);
                    continue;
                }
                it = rsMeta.getColumnLabel(i) + "="+rs.getString(i);
                value.add(it);
            }
            //调用fabric存储
//            String[] values = new String[]{Primary_key,value.toString()};
//            testService.dylm("add",values);

            dataMap.put(Primary_key,value);
            System.out.println("****转换后的第一行的数据 = " + dataMap);
            //清空暂存的数据
            Primary_key = null;
            value.clear();
            dataMap.clear();
//            values = null;

            while(rs.next()){
                //遍历列
                for (int i=1; i <= columnCount; i++) {
                    if ("ID".equals(rsMeta.getColumnLabel(i))){
                        Primary_key = rsMeta.getColumnLabel(i) +"="+rs.getString(i);
                        continue;
                    }
                    it = rsMeta.getColumnLabel(i) + "="+rs.getString(i);
                    value.add(it);
                }
                //调用fabric存储
//                values = new String[]{Primary_key,value.toString()};
//                testService.dylm("add",values);

                dataMap.put(Primary_key,value);
                System.out.println("**转换后的 第" +x+ "行的dataMap = " + dataMap);
                //清空暂存的数据
                Primary_key = null;
                value.clear();
                dataMap.clear();
//                values=null;
                x++;
            }
        }




        //关闭连接
        DBUtils.close(con, null, null);


    }
}
