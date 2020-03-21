package com.example.fleamarket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

import com.example.fleamarket.database.DBHelper;
import com.example.fleamarket.net.MessageType;
import com.example.fleamarket.net.NetImage;
import com.example.fleamarket.net.NetMessage;
import com.example.fleamarket.utils.MyUtil;

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
                // 登录
                case LOGIN:{
                    String id = message.getId();
                    String pw = message.getPw();
                    String nickname = null;
                    // 查询用户表
                    ResultSet rs = DBHelper.query("jdbc:sqlite:database/user.db", "select * from User");
                    boolean loginSuccess = false;
                    while (rs.next()){
                        if(rs.getString("ID").equals(id)){
                            if(rs.getString("Password").equals(pw)){
                                loginSuccess = true;
                                nickname = rs.getString("Nickname");
                                break;
                            }
                        }
                    }
                    ObjectOutputStream oos= new ObjectOutputStream(clientConnection.getOutputStream());
                    NetMessage returnMessage = new NetMessage();
                    if(loginSuccess){
                        returnMessage.setType(MessageType.SUCCESS);
                        returnMessage.setId(id);
                        returnMessage.setNickname(nickname);
                        returnMessage.setPw(pw);
                        File file = new File("./database/avatar/avatar_" + id + ".jpg");
                        if (file.exists()) {
                            NetImage netImage = new NetImage();
                            netImage.setData(MyUtil.loadImageFromFile(file));
                            returnMessage.setAvatar(netImage);
                        }
                    }else{
                        returnMessage.setType(MessageType.FAILURE);
                    }
                    oos.writeObject(returnMessage);
                    DBHelper.close();
                } break;
                // 注册
                case REGISTER:{
                    String invitationCode = message.getId();
                    String pw = message.getPw();
                    String id = null;
                    // 查询邀请码表
                    ResultSet rs = DBHelper.query("jdbc:sqlite:database/invitation_code.db",
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
                    ObjectOutputStream oos= new ObjectOutputStream(clientConnection.getOutputStream());
                    NetMessage returnMessage = new NetMessage();
                    if(invitationCodeExist){
                        returnMessage.setType(MessageType.FAILURE);
                    }else{
                        // 设置邀请码为已经使用
                        DBHelper.update("jdbc:sqlite:database/invitation_code.db",
                                "update InvitationCode set Use=true where Code='" + invitationCode+"'");
                        // 将该用户信息插入到User数据库中
                        DBHelper.update("jdbc:sqlite:database/user.db",
                                "insert into User(ID,Password,Nickname)" + "values("+id+",'"+pw+"','用户_"+id+"')");
                        DBHelper.close();
                        // 创建用户文件夹
                        File file = new File("./database/User/" + id);
                        file.mkdir();
                        // 将注册成功后的账号发送给客户端
                        returnMessage.setType(MessageType.SUCCESS);
                        returnMessage.setId(id);
                    }
                    oos.writeObject(returnMessage);
                } break;
                // 修改昵称
                case CHANGE_NICKNAME:{
                    String id = message.getId();
                    String newNickname = message.getNickname();
                    int rowAffected = DBHelper.update("jdbc:sqlite:database/user.db",
                            "update User set Nickname='" + newNickname + "' where ID=" + id);
                    DBHelper.close();
                    ObjectOutputStream oos= new ObjectOutputStream(clientConnection.getOutputStream());
                    NetMessage returnMessage = new NetMessage();
                    if (rowAffected > 0) {
                        returnMessage.setType(MessageType.CHANGE_NICKNAME);
                        returnMessage.setNickname(newNickname);
                    } else {
                        returnMessage.setType(MessageType.FAILURE);
                    }
                    oos.writeObject(returnMessage);
                } break;
                // 修改密码
                case CHANGE_PASSWORD:{
                    String id = message.getId();
                    String newPassword = message.getPw();
                    int rowAffected = DBHelper.update("jdbc:sqlite:database/user.db",
                            "update User set Password='" + newPassword + "' where ID=" + id);
                    DBHelper.close();
                    ObjectOutputStream oos= new ObjectOutputStream(clientConnection.getOutputStream());
                    NetMessage returnMessage = new NetMessage();
                    if (rowAffected > 0) {
                        returnMessage.setType(MessageType.CHANGE_PASSWORD);
                        returnMessage.setPw(newPassword);
                    } else {
                        returnMessage.setType(MessageType.FAILURE);
                    }
                    oos.writeObject(returnMessage);
                } break;
                // 保存头像
                case SAVE_AVATAR:{
                    byte[] data = message.getAvatar().getData();
                    File image = new File("./database/avatar/avatar_" + message.getId() + ".jpg");
                    try {
                        if (image.exists()){
                            image.delete();
                        }
                        image.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    OutputStream os = new FileOutputStream(image);
                    os.write(data);
                    os.close();

                    ObjectOutputStream oos= new ObjectOutputStream(clientConnection.getOutputStream());
                    NetMessage returnMessage = new NetMessage();
                    returnMessage.setType(MessageType.SAVE_AVATAR);
                    oos.writeObject(returnMessage);
                } break;
                    default:
            }
            clientConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
