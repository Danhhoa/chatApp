package com.example.chatapp.client;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Client {

    private static String host = "localhost";
    private static int port = 8085;
    private static Socket socket;

    private static BufferedWriter out;
    private static BufferedReader in;

    public static void main(String[] args) throws IOException, InterruptedException {
        socket = new Socket(host, port);
        System.out.println("Client connected");
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        SendMessage send = new SendMessage(socket, out);
        ReceiveMessage recv = new ReceiveMessage(socket, in);
        executor.execute(send);
        executor.execute(recv);
    }
}
