package com.example.fleamarket;

import java.sql.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TableCreater {
    public static void main(String[] args){
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
//        float a = Float.parseFloat("999034249.37");
//        DecimalFormat decimalFormat=new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
//        String p=decimalFormat.format(a);//format 返回的是字符串
//        System.out.println(p + "");
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:database/commodity.db");
            statement = connection.createStatement();

//            statement.executeUpdate("drop table Commodity");
//
//            statement.executeUpdate("create table Commodity(" +
//                    "CommodityID text primary key," +
//                    "CommodityName text not null," +
//                    "Price text not null," +
//                    "HavePhoto boolean not null," +
//                    "PostTime datetime not null," +
//                    "PostTimeString text not null," +
//                    "SellerID text not null," +
//                    "SellerName text not null," +
//                    "Area text not null," +
//                    "CommodityDetail text not null)");

//            statement.executeUpdate("update Commodity set CommodityDetail='emmm' where CommodityName='二手黄瓜'");
//            statement.executeUpdate(
//                    "delete from Commodity where HavePhoto=1");
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
