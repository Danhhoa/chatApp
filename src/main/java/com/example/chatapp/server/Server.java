package com.example.chatapp.server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static int port = 8085;
    public static int numThread = 6;
    private static ServerSocket server = null;
    public static Vector<HandleClients> workers = new Vector<>();


    public static void main(String[] args) throws IOException {
        int i = 0;
        ExecutorService executor = Executors.newFixedThreadPool(numThread);
        try {
            server = new ServerSocket(port);
            Socket socket;
            System.out.println("Server binding at port " + port);
            System.out.println("Waiting for client...");
            while(true) {
                i++;
                socket = server.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                HandleClients client = new HandleClients(socket,"client "+ i, in, out);
                workers.add(client);
                System.out.println("add " +workers);
                executor.execute(client);
            }
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if(server!=null)
                server.close();
        }
    }
}
