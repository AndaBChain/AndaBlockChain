package com.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConnectionTest {

	 public static Connection getConnection() {
	        // 定义连接
	        Connection connection = null;
	        
	        try {
	            // 加载驱动
	            Class.forName("com.mysql.cj.jdbc.Driver");
	            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/btctxdata?"
	            		+ "Unicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8",
	            		"root", "123456");
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return connection;
	    }
	    
	    public static List<HashMap<String, Object>> getMysqlData() {
	        Connection connection = null;
	        // 预执行加载
	        PreparedStatement psp = null;
	        // 结果集
	        ResultSet resultSet = null;
	        
	        connection = getConnection();
	        
	        String sql = "select * from test";
	        
	        List<HashMap<String, Object>> list = new ArrayList<HashMap<String,Object>>();
	        
	        try {
	            psp = connection.prepareStatement(sql);
	            resultSet = psp.executeQuery();
	            HashMap<String, Object> map = null;
	            while (resultSet.next()) {
	                map = new HashMap<String, Object>();
	                map.put("name", resultSet.getString("id"));
	                list.add(map);
	            }
	         } catch (SQLException e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                if (resultSet != null) {
	                    resultSet.close();
	                }
	                if (psp != null) {
	                    psp.close();
	                }
	                if (connection != null) {
	                    connection.close();
	                }
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	        return list;
	    }
	    public static List<HashMap<String, Object>> getMysqlData1() {
	        Connection connection = null;
	        // 预执行加载
	        PreparedStatement psp = null;
	        // 结果集
	        
	        connection = getConnection();
	        
//	        String sql = "select * from test";
	        String sql = "insert into ethupload(id,andaAddress,amount) values (?,?,?)";
	        
	        List<HashMap<String, Object>> list = new ArrayList<HashMap<String,Object>>();
	        
	        try {
	            psp = connection.prepareStatement(sql);
	            psp.setString(1, "111");
	            psp.setString(2, "222");
	            psp.setString(3, "333");
	            int i = psp.executeUpdate();
	           
	         } catch (SQLException e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                if (psp != null) {
	                    psp.close();
	                }
	                if (connection != null) {
	                    connection.close();
	                }
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	        return list;
	    }

	    public static void main(String[] args) {
	    	
	    	//查找
	        List<HashMap<String, Object>> mysqlData = getMysqlData();
	        //插入
	        List<HashMap<String, Object>> mysqlData1 = getMysqlData1();
	        for(HashMap<String, Object> map : mysqlData) {
	            System.out.println(map.get("2"));
	        }
	    }
	
}
