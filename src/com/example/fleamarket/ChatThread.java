package com.example.fleamarket;

import com.example.fleamarket.net.Chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class ChatThread extends Thread{
    private Socket clientConnection;
    private static HashMap<String, Socket> socketManager = new HashMap<>();

    public ChatThread(Socket socket){
        this.clientConnection = socket;
    }
    @Override
    public void run() {
        System.out.println("聊天端口接入");
        try {
            while (clientConnection != null) {
                ObjectInputStream ois = new ObjectInputStream(clientConnection.getInputStream());
                Chat chat = (Chat) ois.readObject();
                // 将次socket添加到hashmap
                socketManager.put(chat.getSenderID(), clientConnection);
                Socket forwardSocket = socketManager.get(chat.getReceiverID());
                boolean forwardSuccess = false;
                if (forwardSocket != null) {
                    try {
                        ObjectOutputStream oos= new ObjectOutputStream(forwardSocket.getOutputStream());
                        oos.writeObject(chat);
                        forwardSuccess = true;
                        System.out.println("消息:" + chat.getContent() + " 已转发给用户" + chat.getReceiverID());
                    } catch (IOException e) {
                        e.printStackTrace();
                        socketManager.remove(chat.getReceiverID());
                    }
                }
                // 消息转发失败，将消息保存到消息列表数据库
                if (!forwardSuccess) {
                    System.out.println("消息:" + chat.getContent() + "已保存到数据库");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                clientConnection.close();
                clientConnection = null;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
