package com.aizone.blockchain.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * 数据库工具类
 * @author wss
 *
 */
public class Db {
	
	 public static List<String> getDbValue() {
			// TODO Auto-generated method stub
			List<String> l = new ArrayList<>();
			try {
				
				//这个是配置连接的，当时我改成这样了。
				//ResourceBundle resource = ResourceBundle.getBundle("db");
				String driver = "com.mysql.cj.jdbc.Driver";//resource.getString("driver");
				//btctxdata   andawebsite  autoReconnect=true  17524
				String url = "jdbc:mysql://localhost:3306/andawebsite?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC";//resource.getString("username");
				//iptest  123456
				String username ="root"; //resource.getString("password");
				String password = "root";//resource.getString("url");
				l.add(driver);
				l.add(username);
				l.add(password);
				l.add(url);	
				return l;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return null;
			}
			
		}
	/**
	 * 获取连接
	 * @return
	 */
	public static Connection getConnection() {
		// TODO Auto-generated method stub
		try {
			List<String> l = getDbValue();
			Class.forName(l.get(0));
            return DriverManager.getConnection(l.get(3), l.get(1), l.get(2));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}

	/**
	 * 关闭连接资源
	 * @param conn
	 */
	public static void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.isClosed();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
