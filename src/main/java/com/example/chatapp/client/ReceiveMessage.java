package com.example.chatapp.client;

import java.io.*;
import java.net.*;


public class ReceiveMessage implements Runnable {
    private BufferedReader in;
    private Socket socket;
    public ReceiveMessage(Socket s, BufferedReader i) {
        this.socket = s;
        this.in = i;
    }

    public String receiveFromServer() {
        try {
            String data = in.readLine();
            return data;

            }
        catch (IOException e) {
            System.err.println(e);
        }
        return null;
    }

    public void close() throws IOException {
        in.close();
        socket.close();
    }

    @Override
    public void run() {

    }
//    public void run() {
//        try {
//            String data = "";
//            while(data != null) {
//                if (data.equalsIgnoreCase("success")) {
//                    System.out.println("Tạo tài khoản thành công");
//                }
//                if (data.equalsIgnoreCase("fail")) {
//                    System.out.println("tên đã được sử dụng. hãy nhập lại:");
//                }
//                if (!data.equalsIgnoreCase("success") && !data.equalsIgnoreCase("fail")) {
//                    System.out.println("Receive: " + data);
//                }
//
//                data = in.readLine();
//            }
//        } catch (IOException e) {
//            System.err.println(e);
//        }
//    }
}
