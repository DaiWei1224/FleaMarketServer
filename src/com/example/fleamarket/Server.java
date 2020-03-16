package com.example.fleamarket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

import com.example.fleamarket.net.MessageType;
import com.example.fleamarket.net.NetMessage;

public class Server {

    public final static int PORT = 1224;

    public static void main(String[] args) {
        ServerSocket socket;
        try {
            socket = new ServerSocket(PORT);
            while(true) {
                Socket clientConnection = socket.accept();
                new ServerThread(clientConnection).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//		try(ServerSocket socket = new ServerSocket(PORT)) {
//			while(true) {
//				Socket clientConnection = socket.accept();
//				Writer out = new OutputStreamWriter(
//						clientConnection.getOutputStream(),
//						Charset.forName("utf-8"));
//				out.write("TEMPERATURE = 60, HUMIDITY = 0, LIGHT = 1\r\n");
//				out.flush();
//
//				Reader reader = new InputStreamReader(
//						socket.getInputStream(),
//						Charset.forName("utf-8"));
//				BufferedReader br = new BufferedReader(reader);
//				String line = br.readLine();
//				System.out.println("Server receive message：" + line);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
    }
}

class ServerThread extends Thread{

    private Socket clientConnection;
    //输入流对象，读取浏览器请求
    private InputStream input;
    //输出流对象，响应内容给浏览器
    private OutputStream output;

    public ServerThread(Socket socket){
        this.clientConnection = socket;
        try {
            this.input = socket.getInputStream();
            this.output = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("有客户端接入");
        try {
            ObjectInputStream ois = new ObjectInputStream(clientConnection.getInputStream());
            NetMessage message = (NetMessage) ois.readObject();
            MessageType type = message.getType();
            switch (type){
                case LOGIN:{
                    String id = message.getId();
                    String pw = message.getPw();
                    Connection connection = null;
                    Statement statement = null;
                    ResultSet rs = null;
                    try {
                        connection = DriverManager.getConnection("jdbc:sqlite:datebase/user.db");
                        statement = connection.createStatement();
                        rs = statement.executeQuery("select * from User");
                        boolean loginSuccess = false;
                        while (rs.next()){
                            if(rs.getString("ID").equals(id)){
                                if(rs.getString("Password").equals(pw)){
                                    loginSuccess = true;
                                    break;
                                }
                            }
                        }
                        Writer out = new OutputStreamWriter(clientConnection.getOutputStream()/*, Charset.forName("utf-8")*/);
                        if(loginSuccess){
                            out.write("login success\r\n");
                        }else{
                            out.write("login failure\r\n");
                        }
                        out.flush();

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
                } break;
                case REGISTER:{
                    String invitationCode = message.getId();
                    String pw = message.getPw();
                    String id = null;
                    Connection connection = null;
                    Statement statement = null;
                    ResultSet rs = null;
                    try {
                        connection = DriverManager.getConnection("jdbc:sqlite:datebase/invitation_code.db");
                        statement = connection.createStatement();
                        rs = statement.executeQuery("select * from InvitationCode");
                        boolean invitationCodeExist = true;
                        while (rs.next()){
                            if(rs.getString("Code").equals(invitationCode) && rs.getBoolean("Use") == false){
                                id = rs.getString("ID");
                                invitationCodeExist = false;
                                break;
                            }
                        }
                        Writer out = new OutputStreamWriter(clientConnection.getOutputStream()/*, Charset.forName("utf-8")*/);
                        if(invitationCodeExist){
                            out.write("register failure\r\n");
                        }else{
                            // 设置邀请码为已经使用
                            statement.executeUpdate("update InvitationCode set Use=true where Code='" + invitationCode+"'");
                            // 将该用户信息插入到User数据库中
                            connection = DriverManager.getConnection("jdbc:sqlite:datebase/user.db");
                            statement = connection.createStatement();
                            statement.executeUpdate("insert into User(ID,Password,Nickname)" + "values("+id+",'"+pw+"','用户_"+id+"')");
                            // 创建用户文件夹
                            File file = new File("./datebase/User/" + id);
                            file.mkdir();
                            // 将注册成功后的账号发送给客户端
                            out.write(id + "\r\n");
                        }
                        out.flush();

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
                } break;
                    default:
            }
//				StringBuffer result = new StringBuffer();
//				result.append("HTTP /1.1 200 ok \r\n");
//				result.append("Content-Type:text/html \r\n");
//				result.append("Content-Length:" + file.length() + "\r\n");
//				result.append("\r\n:" + sb.toString());
//				output.write(result.toString().getBytes());
//				output.flush();
//				output.close();
            clientConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
