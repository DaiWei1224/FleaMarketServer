package com.example.fleamarket.database;

import java.io.File;
import java.sql.*;

public class TableCreater {
    public static void main(String[] args){
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
//            connection = DriverManager.getConnection("jdbc:sqlite:datebase/user.db");
            connection = DriverManager.getConnection("jdbc:sqlite:datebase/invitation_code.db");
            statement = connection.createStatement();
            rs = statement.executeQuery("select * from InvitationCode");
            while (rs.next()){
                if(rs.getString("Code").equals("VI920P")){
                    System.out.println("334");
                    break;
                }
            }
//            statement = connection.createStatement();

//            statement.executeUpdate("drop table User");
//            statement.executeUpdate("create table User(" +
//                    "ID integer primary key," +
//                    "Password text not null," +
//                    "Nickname text)");
//            statement.executeUpdate("v");

//            int rowAffected = statement.executeUpdate("insert into User(ID,Password)" + "values(12345,'david666')");
//            if(rowAffected > 0){
//                    // 数据成功插入
////                    executeQuery(rs, statement, "select * from test");
//                }else{
//                    System.err.println("插入操作失败");
//            }
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
}
