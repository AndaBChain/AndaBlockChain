package org.rockyang.blockchain.db.MySql;

import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
/**
 * @author Wang HaiTian
 */
@Component
public class MySQL {

    // MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL
    //static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    //static final String DB_URL = "jdbc:mysql://localhost:3306/RUNOOB";

    // MySQL 8.0 以上版本 - JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/andawebsite?useSSL=false&serverTimezone=UTC&serverTimezone=GMT%2B8\n" +
            "\n";


    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "root";
    static final String PASS = "root";

    public static List<String>  examineNior() {
        Connection conn = null;
        Statement stmt = null;
        List<String> alisr = new ArrayList<String>();
        try{
            // 注册 JDBC 驱动
           /* Class.forName("com.mysql.jdbc.Driver");*/
            Class.forName(JDBC_DRIVER);
            // 打开链接
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT peerip,PeerAddress FROM PEER";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                String peerip = rs.getString("peerip");
                String peerAddress = rs.getString("PeerAddress");
                alisr.add(peerip);
                alisr.add(peerAddress);
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return alisr;
    }
    public static List<String>  examine() {
        Connection conn = null;
        Statement stmt = null;
        List<String> alisr = new ArrayList<String>();
        try{
            // 注册 JDBC 驱动
            /* Class.forName("com.mysql.jdbc.Driver");*/
            Class.forName(JDBC_DRIVER);
            // 打开链接
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "select PeerIp from (select * from peer order by time desc ) tem  group by PeerAddress order by time desc";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                String peerip = rs.getString("peerip");
                alisr.add(peerip);
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return alisr;
    }
    public static List<String>  examineAddress() {
        Connection conn = null;
        Statement stmt = null;
        List<String> alisr = new ArrayList<String>();
        try{
            // 注册 JDBC 驱动
            /* Class.forName("com.mysql.jdbc.Driver");*/
            Class.forName(JDBC_DRIVER);
            // 打开链接
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT PeerAddress FROM PEER";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                String PeerAddress = rs.getString("PeerAddress");
                alisr.add(PeerAddress);
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return alisr;
    }
    public static List<Object>  examineTime() {
        Connection conn = null;
        Statement stmt = null;
        List<Object> alisr = new ArrayList<Object>();
        try{
            // 注册 JDBC 驱动
            /* Class.forName("com.mysql.jdbc.Driver");*/
            Class.forName(JDBC_DRIVER);
            // 打开链接
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT time FROM PEER tem  group by PeerAddress order by time desc";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                //String PeerAddress = rs.getString("PeerAddress");
                //String PeerIp = rs.getString("PeerIp");
                Timestamp Time = rs.getTimestamp("Time");
                //alisr.add(PeerAddress);
                //alisr.add(PeerIp);
                alisr.add(Time);
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return alisr;
    }
    public static List<Object>  examinePeerAddress() {
        Connection conn = null;
        Statement stmt = null;
        List<Object> alisr = new ArrayList<Object>();
        try{
            // 注册 JDBC 驱动
            /* Class.forName("com.mysql.jdbc.Driver");*/
            Class.forName(JDBC_DRIVER);
            // 打开链接
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT PeerAddress FROM PEER tem  group by PeerAddress order by time desc";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                String PeerAddress = rs.getString("PeerAddress");
                //String PeerIp = rs.getString("PeerIp");
                //Timestamp Time = rs.getTimestamp("Time");
                alisr.add(PeerAddress);
                //alisr.add(PeerIp);
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return alisr;
    }
    public static List<Object>  examinePeerIp() {
        Connection conn = null;
        Statement stmt = null;
        List<Object> alisr = new ArrayList<Object>();
        try{
            // 注册 JDBC 驱动
            /* Class.forName("com.mysql.jdbc.Driver");*/
            Class.forName(JDBC_DRIVER);
            // 打开链接
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT PeerIp FROM PEER tem  group by PeerAddress order by time desc";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                //String PeerAddress = rs.getString("PeerAddress");
                String PeerIp = rs.getString("PeerIp");
                //Timestamp Time = rs.getTimestamp("Time");
                //alisr.add(PeerAddress);
                alisr.add(PeerIp);
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return alisr;
    }

    public static void upnew(String ip,String adderss,Timestamp time) {
        Connection conn = null;
        Statement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL, USER, PASS);


            // 执行储存
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "INSERT INTO PEER (PeerIp,PeerAddress,time)VALUES(?,?,?);";
            PreparedStatement sta = conn.prepareStatement(sql);
            sta.setString(1, ip);
            sta.setString(2, adderss);
            sta.setTimestamp(3, time);
            sta.executeUpdate();

            // 完成后关闭
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        System.out.println("Goodbye!");
    }
    public static void upold(String adderss,Timestamp time) {
        Connection conn = null;
        Statement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL, USER, PASS);


            // 执行储存
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            /*sql = "INSERT INTO PEER (PeerIp,PeerAddress,time)VALUES(?,?,?);";*/
            sql= "UPDATE peer SET time = ? WHERE PeerAddress = ?";
            PreparedStatement sta = conn.prepareStatement(sql);
            sta.setTimestamp(1, time);
            sta.setString(2, adderss);
            sta.executeUpdate();

            // 完成后关闭
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        System.out.println("Goodbye!");
    }
}