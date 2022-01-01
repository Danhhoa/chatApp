package com.example.chatapp.client;

import java.io.*;
import java.net.*;

public class SendMessage implements Runnable {
    private BufferedWriter out;
    private Socket socket;
    public SendMessage(Socket s, BufferedWriter o) {
        this.socket = s;
        this.out = o;
    }

    public void sendToServer(String data) throws IOException {
        out.write(data);
        out.newLine();
        out.flush();
    }

    public void close () throws IOException {
        System.out.println("Sendmessage close");
        out.close();
        socket.close();
    }

    @Override
    public void run() {

    }
//    public void run() {
//        try {
//            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
//            System.out.println("Enter Nickname: ");
//            String nickname = stdIn.readLine();
//            System.out.println("Nickname: " +nickname);
//            out.write(nickname+'\n');
//            out.flush();
//
//            while(true) {
//                String data = stdIn.readLine();
//                System.out.println("Input from client: " + data);
//                out.write(data+'\n');
//                out.flush();
//                if(data.equals("bye"))
//                    break;
//            }
//            System.out.println("Client closed connection");
//            out.close();
//            socket.close();
//        } catch (IOException e) {}
//    }
}
