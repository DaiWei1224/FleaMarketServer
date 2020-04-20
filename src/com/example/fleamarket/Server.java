package com.example.fleamarket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public final static int PORT = 1224;
    public final static int CHAT_PORT = 1225;

    public static void main(String[] args) {
        ServerSocket socket;
        // 聊天服务线程
        new Thread(() -> {
                try {
                    ServerSocket chatSocket = new ServerSocket(CHAT_PORT);
                    while (true) {
                        Socket chatConnection = chatSocket.accept();
                        new ChatThread(chatConnection).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        // 其他服务线程
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


