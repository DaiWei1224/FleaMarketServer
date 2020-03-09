package com.example.fleamarket;

import java.sql.*;

public class TestSqlLite {
    public static void main(String[] args){
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            // 创建数据库连接
            connection = DriverManager.getConnection("jdbc:sqlite:datebase/user.db");
            // 准备创建操作与存取的数据库命令
            statement = connection.createStatement();
            executeQuery(rs, statement, "select * from User");

            // 插入数据时，可以不指定自增字段的值，而由数据库自己生成
            int rowAffected = statement.executeUpdate("insert into User(ID,Password,Nickname)" + "values('1120161648','233','超哥')");
            if(rowAffected > 0){
                // 数据成功插入
                executeQuery(rs, statement, "select * from User");
            }else{
                System.err.println("插入操作失败");
            }

            rowAffected = statement.executeUpdate("update User set Password=234 where Nickname='超哥'");
            if(rowAffected > 0){
                // 数据成功更新
                executeQuery(rs, statement, "select * from User");
            }else{
                System.err.println("更新操作失败");
            }

            rowAffected = statement.executeUpdate("delete from User where Nickname='超哥'");
            if(rowAffected > 0){
                // 数据成功删除
                executeQuery(rs, statement, "select * from User");
            }else{
                System.err.println("删除操作失败");
            }

        } catch (SQLException e){
            System.err.println(e.getMessage());
        } finally {
            try { // 确保各种数据库对象都被关闭
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
                System.err.println(e);
            }
        }
    }

    public static void executeQuery(ResultSet rs, Statement statement, String sql){
        // 查询表中保存的护具
        try {
            rs = statement.executeQuery(sql);
            System.out.println("==============start==============");
            while (rs.next()){
                // 输出查询到的数据
                System.out.println("id = " + rs.getInt("ID"));
                System.out.println("password = " + rs.getString("Password"));
                System.out.println("nickname = " + rs.getString("Nickname"));
                System.out.println(" ");
            }
            System.out.println("==============end==============");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
