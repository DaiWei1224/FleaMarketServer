package com.example.fleamarket.database;

import java.sql.*;

public class DBHelper {
    private static Connection connection = null;
    private static Statement statement = null;
    private static ResultSet rs = null;

    // 数据库查询操作
    public static ResultSet query(String db, String sql){

        try {
            connection = DriverManager.getConnection(db);
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    // 数据库更新操作
    public static void update(String db, String sql){
        try {
            connection = DriverManager.getConnection(db);
            statement = connection.createStatement();
            int rowAffected = statement.executeUpdate(sql);
            if(rowAffected > 0){
                System.out.println("命令执行成功：" + sql);
            }else{
                System.err.println("命令执行失败：" + sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 确保各种数据库对象都被关闭
    public static void close(){
        try {
            if(connection != null){
                connection.close();
            }
            if(statement != null){
                statement.close();
            }
            if(rs != null){
                rs.close();
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
