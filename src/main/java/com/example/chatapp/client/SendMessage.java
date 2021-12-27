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
    public void run() {
        try {
            while(true) {
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
                String data = stdIn.readLine();
                System.out.println("Input from client: " + data);
                out.write(data+'\n');
                out.flush();
                if(data.equals("bye"))
                    break;
            }
            System.out.println("Client closed connection");
            out.close();
            socket.close();
        } catch (IOException e) {}
    }
}
