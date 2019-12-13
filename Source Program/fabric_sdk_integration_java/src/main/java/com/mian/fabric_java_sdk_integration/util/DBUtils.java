package com.mian.fabric_java_sdk_integration.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
/**
* @Description: 测试用数据库类
* @Param:
* @return:
* @Author: 继鹏
* @Date: 2019/12/13
*/

public class DBUtils {
    private static String url = "jdbc:sqlserver://localhost:1433;DatabaseName=ZhengHeCTP";//严格区分大小写
    private static String username = "sa";
    private static String password = "123";
    private static String driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";//严格区分大小写

    static {
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static void close(Connection con, Statement sm, ResultSet rs) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (sm != null) {
            try {
                sm.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}