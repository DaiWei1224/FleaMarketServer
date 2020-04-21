package com.example.fleamarket;

import com.example.fleamarket.database.DBHelper;
import com.example.fleamarket.net.*;
import com.example.fleamarket.utils.MyUtil;

import java.io.*;
import java.net.Socket;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

class ServerThread extends Thread{
    private Socket clientConnection;
//    private InputStream input;
//    private OutputStream output;

    public ServerThread(Socket socket){
        this.clientConnection = socket;
//        try {
//            this.input = socket.getInputStream();
//            this.output = socket.getOutputStream();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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
                        DBHelper.close();
                        // 将该用户信息插入到User数据库中
                        DBHelper.update("jdbc:sqlite:database/user.db",
                                "insert into User(ID,Password,Nickname)" + "values("+id+",'"+pw+"','用户_"+id+"')");
                        // 创建用户文件夹
//                        File file = new File("./database/user/" + id);
//                        file.mkdir();
//                        DBHelper.update("jdbc:sqlite:database/user/" + id + "/message_queue.db",
//                                "create table MessageQueue(" +
                        DBHelper.update("jdbc:sqlite:database/message_queue.db",
                                "create table MessageQueue_" + id + "(" +
                                        "SenderID text not null," +
                                        "SenderName text not null," +
                                        "SendTime text not null," +
                                        "Content text not null)");
                        DBHelper.close();
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
                    // 同时更新商品数据库
                    DBHelper.update("jdbc:sqlite:database/commodity.db",
                            "update Commodity set SellerName='" + newNickname + "' where SellerID=" + id);
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
                    returnMessage.setAvatar(message.getAvatar());
                    oos.writeObject(returnMessage);
                } break;
                // 发布商品
                case POST_COMMODITY:{
                    Commodity commodity = message.getCommodity();
                    int rowAffected = DBHelper.update("jdbc:sqlite:database/commodity.db",
                            "insert into Commodity(CommodityID,CommodityName,Price,HavePhoto,PostTime,PostTimeString,SellerID,SellerName,Area,CommodityDetail) values('"
                                    + commodity.getCommodityID() + "','"
                                    + commodity.getCommodityName() + "','"
                                    + commodity.getPrice() + "',"
                                    + commodity.isHavePhoto() + ",'"
                                    + commodity.getPostTime() + "','"
                                    + commodity.getPostTimeString() + "','"
                                    + commodity.getSellerID() + "','"
                                    + commodity.getSellerName() + "','"
                                    + commodity.getArea() + "','"
                                    + commodity.getCommodityDetail() + "')");
                    DBHelper.close();
                    ObjectOutputStream oos= new ObjectOutputStream(clientConnection.getOutputStream());
                    NetMessage returnMessage = new NetMessage();
                    if (rowAffected > 0) {
                        if (commodity.isHavePhoto()) {
                            // 将商品图片保存到本地
                            byte[] data = commodity.getCommodityPhoto().getData();
                            File image = new File("./database/commodity/" + commodity.getCommodityID() + ".jpg");
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
                        }
                        returnMessage.setType(MessageType.SUCCESS);
                    } else {
                        returnMessage.setType(MessageType.FAILURE);
                    }
                    oos.writeObject(returnMessage);
                } break;
                // 下发商品
                case GET_COMMODITY:{
                    int index = message.getCommodityNum();
                    ResultSet rs;
                    if (message.getId() == null) {
                        rs = DBHelper.query("jdbc:sqlite:database/commodity.db",
                                "select * from Commodity order by PostTimeString DESC");
                    } else {
                        rs = DBHelper.query("jdbc:sqlite:database/commodity.db",
                                "select * from Commodity where SellerID='" + message.getId() + "' order by PostTimeString DESC");
                    }
                    int i = 0;
                    int count = 0;
                    List<Commodity> commodityList = new ArrayList<>();
                    Commodity commodity;
                    while (rs.next()){
                        if (i >= index && i < index + 20) {
                            commodity = new Commodity();
                            commodity.setCommodityID(rs.getString("CommodityID"));
                            commodity.setCommodityName(rs.getString("CommodityName"));
                            commodity.setCommodityDetail(rs.getString("CommodityDetail"));
                            commodity.setPrice(rs.getString("Price"));
                            commodity.setSellerID(rs.getString("SellerID"));
                            commodity.setSellerName(rs.getString("SellerName"));
                            commodity.setArea(rs.getString("Area"));
                            commodity.setPostTimeString(rs.getString("PostTimeString"));
                            commodity.setHavePhoto(rs.getBoolean("HavePhoto"));
                            if (commodity.isHavePhoto()) {
                                // 添加商品照片
                                File file = new File("./database/commodity/" + commodity.getCommodityID() + ".jpg");
                                if (file.exists()) {
                                    NetImage netImage = new NetImage();
                                    netImage.setData(MyUtil.loadImageFromFile(file));
                                    commodity.setCommodityPhoto(netImage);
                                }
                            }
                            // 添加头像
                            File file = new File("./database/avatar/avatar_" + commodity.getSellerID() + ".jpg");
                            if (file.exists()) {
                                NetImage netImage = new NetImage();
                                netImage.setData(MyUtil.loadImageFromFile(file));
                                commodity.setAvatar(netImage);
                            }
                            commodityList.add(commodity);
                            count++;
                        }
                        i++;
                    }
                    ObjectOutputStream oos= new ObjectOutputStream(clientConnection.getOutputStream());
                    NetMessage returnMessage = new NetMessage();
                    returnMessage.setType(MessageType.SUCCESS);
                    returnMessage.setCommodityNum(count);
                    if (count > 0) {
                        returnMessage.setCommodityList(commodityList);
                    }
                    oos.writeObject(returnMessage);
                    DBHelper.close();
                } break;
                // 编辑商品
                case EDIT_COMMODITY:{
                    Commodity commodity = message.getCommodity();
                    int rowAffected = DBHelper.update("jdbc:sqlite:database/commodity.db",
                            "update Commodity set CommodityName='" +commodity.getCommodityName() +
                                    "',Price='" + commodity.getPrice() +
                                    "',HavePhoto=" + commodity.isHavePhoto() +
                                    ",Area='" + commodity.getArea() +
                                    "',CommodityDetail='" + commodity.getCommodityDetail() +
                                    "' where CommodityID='" + commodity.getCommodityID() + "'");
                    // 修改图片文件或删除图片文件
                    File image = new File("./database/commodity/" + commodity.getCommodityID() + ".jpg");
                    if (image.exists()){
                        image.delete();
                    }
                    if (commodity.isHavePhoto()) {
                        image.createNewFile();
                        // 将商品图片保存到本地
                        byte[] data = commodity.getCommodityPhoto().getData();
                        OutputStream os = new FileOutputStream(image);
                        os.write(data);
                        os.close();
                    }
                    ObjectOutputStream oos= new ObjectOutputStream(clientConnection.getOutputStream());
                    NetMessage returnMessage = new NetMessage();
                    if (rowAffected > 0) {
                        returnMessage.setType(MessageType.EDIT_COMMODITY);
                    } else {
                        returnMessage.setType(MessageType.FAILURE);
                    }
                    oos.writeObject(returnMessage);
                    DBHelper.close();
                } break;
                // 删除商品
                case DELETE_COMMODITY:{
                    String commodityID = message.getId();
                    int rowAffected = DBHelper.update("jdbc:sqlite:database/commodity.db",
                            "delete from Commodity where CommodityID='" + commodityID + "'");
                    File image = new File("./database/commodity/" + commodityID + ".jpg");
                    if (image.exists()){
                        image.delete();
                    }
                    ObjectOutputStream oos= new ObjectOutputStream(clientConnection.getOutputStream());
                    NetMessage returnMessage = new NetMessage();
                    if (rowAffected > 0) {
                        returnMessage.setType(MessageType.DELETE_COMMODITY);
                    } else {
                        returnMessage.setType(MessageType.FAILURE);
                    }
                    oos.writeObject(returnMessage);
                    DBHelper.close();
                } break;
                case GET_UNREAD_MESSAGE:{
                    ResultSet rs = DBHelper.query("jdbc:sqlite:database/message_queue.db",
                            "select * from MessageQueue_" + message.getId() + " order by SendTime ASC");
                    List<Chat> messageList = new ArrayList<Chat>();
                    Chat chat;
                    while (rs.next()){
                        chat = new Chat();
                        chat.setSenderID(rs.getString("SenderID"));
                        chat.setSenderName(rs.getString("SenderName"));
                        chat.setSendTime(rs.getString("SendTime"));
                        chat.setContent(rs.getString("Content"));
                        messageList.add(chat);
                    }
                    ObjectOutputStream oos= new ObjectOutputStream(clientConnection.getOutputStream());
                    NetMessage returnMessage = new NetMessage();
                    if (messageList.size() > 0) {
                        returnMessage.setType(MessageType.GET_UNREAD_MESSAGE);
                        returnMessage.setMessageList(messageList);
                        // 删除服务器缓存的消息
                        DBHelper.update("jdbc:sqlite:database/message_queue.db",
                                "delete from MessageQueue_" + message.getId());
                    } else {
                        returnMessage.setType(MessageType.FAILURE);
                    }
                    oos.writeObject(returnMessage);
                    DBHelper.close();
                } break;
                default:
                    break;
            }
            clientConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
