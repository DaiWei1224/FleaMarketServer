package com.example.fleamarket;

import java.sql.*;

public class TableCreater {
    public static void main(String[] args){
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:datebase/user.db");
            statement = connection.createStatement();
//            statement.executeUpdate("drop table User");

//            statement.executeUpdate("create table User(" +
//                    "ID integer primary key," +
//                    "Password text not null," +
//                    "Nickname text)");

//            statement.executeUpdate("insert into User(ID,Password)" + "values(12345,'david666')");
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
