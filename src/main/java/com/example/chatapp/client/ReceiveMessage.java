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
    public void run() {
        try {
            while(true) {
                String data = in.readLine();
                System.out.println("Receive: " + data);
            }
        } catch (IOException e) {}
    }
}
