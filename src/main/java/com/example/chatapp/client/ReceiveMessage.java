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
}
