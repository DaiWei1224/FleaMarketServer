package com.example.fleamarket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

import com.example.fleamarket.database.DBHelper;
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
    }
}

class ServerThread extends Thread{

    private Socket clientConnection;
    private InputStream input;
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
        System.out.println("客户端接入");
        try {
            ObjectInputStream ois = new ObjectInputStream(clientConnection.getInputStream());
            NetMessage message = (NetMessage) ois.readObject();
            MessageType type = message.getType();
            switch (type){
                case LOGIN:{
                    String id = message.getId();
                    String pw = message.getPw();
                    // 查询用户表
                    ResultSet rs = DBHelper.query("jdbc:sqlite:datebase/user.db", "select * from User");
                    boolean loginSuccess = false;
                    while (rs.next()){
                        if(rs.getString("ID").equals(id)){
                            if(rs.getString("Password").equals(pw)){
                                loginSuccess = true;
                                break;
                            }
                        }
                    }
                    DBHelper.close();
                    Writer out = new OutputStreamWriter(clientConnection.getOutputStream()/*, Charset.forName("utf-8")*/);
                    if(loginSuccess){
                        out.write("login success\r\n");
                    }else{
                        out.write("login failure\r\n");
                    }
                    out.flush();
                } break;
                case REGISTER:{
                    String invitationCode = message.getId();
                    String pw = message.getPw();
                    String id = null;
                    // 查询邀请码表
                    ResultSet rs = DBHelper.query("jdbc:sqlite:datebase/invitation_code.db",
                            "select * from InvitationCode");
                    boolean invitationCodeExist = true;
                    while (rs.next()){
                        if(rs.getString("Code").equals(invitationCode) && rs.getBoolean("Use") == false){
                            id = rs.getString("ID");
                            invitationCodeExist = false;
                            break;
                        }
                    }
                    DBHelper.close();
                    Writer out = new OutputStreamWriter(clientConnection.getOutputStream()/*, Charset.forName("utf-8")*/);
                    if(invitationCodeExist){
                        out.write("register failure\r\n");
                    }else{
                        // 设置邀请码为已经使用
                        DBHelper.update("jdbc:sqlite:datebase/invitation_code.db",
                                "update InvitationCode set Use=true where Code='" + invitationCode+"'");
                        // 将该用户信息插入到User数据库中
                        DBHelper.update("jdbc:sqlite:datebase/user.db",
                                "insert into User(ID,Password,Nickname)" + "values("+id+",'"+pw+"','用户_"+id+"')");
                        DBHelper.close();
                        // 创建用户文件夹
                        File file = new File("./datebase/User/" + id);
                        file.mkdir();
                        // 将注册成功后的账号发送给客户端
                        out.write(id + "\r\n");
                    }
                    out.flush();
                } break;
                    default:
            }
            clientConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
