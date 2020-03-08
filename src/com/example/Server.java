package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import com.example.myapplication.User;

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
        //while(true) {
        try {
//				Reader reader = new InputStreamReader(input, Charset.forName("utf-8"));
//				BufferedReader br = new BufferedReader(reader);
//				String line = br.readLine();
//				if(line == null) {
//					System.out.println("服务器未收到消息");
//				} else {
//					System.out.println("服务器线程" + Thread.currentThread().getId() + "收到消息：" + line);
//				}
//				if(line == "close socket") {
//					break;
//				}
            ObjectInputStream ois = new ObjectInputStream(clientConnection.getInputStream());
            User user = (User)ois.readObject();
            System.out.println("服务器端接收到对象，账号为" + user.getAccount() + ",密码为" + user.getPassword());

//				StringBuffer result = new StringBuffer();
//				result.append("HTTP /1.1 200 ok \r\n");
//				result.append("Content-Type:text/html \r\n");
//				result.append("Content-Length:" + file.length() + "\r\n");
//				result.append("\r\n:" + sb.toString());
//				output.write(result.toString().getBytes());
//				output.flush();
//				output.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("异常信息" + e);
        }
        //}
        try {
            clientConnection.close();
            System.out.println("服务端Socket关闭");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
