package com.tgy.rtls.web.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    private static final String MYSQL_URL = "jdbc:mysql://192.168.1.95:3306/park1?useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASSWORD = "tuguiyao";

//    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/park?useSSL=false&serverTimezone=Asia/Shanghai";
//    private static final String MYSQL_USER = "root";
//    private static final String MYSQL_PASSWORD = "123456";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}