package com.example.chatapp.server;

import java.io.IOException;
import java.net.*;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static int port = 8085;
    public static int numThread = 2;
    private static ServerSocket server = null;
    public static Vector<HandleClients> workers = new Vector<>();

    public static void main(String[] args) throws IOException {
        int i = 0;
        ExecutorService executor = Executors.newFixedThreadPool(numThread);
        try {
            server = new ServerSocket(port);
            System.out.println("Server binding at port " + port);
            System.out.println("Waiting for client...");
            while(true) {
                i++;
                Socket socket = server.accept();
                HandleClients client = new HandleClients(socket, Integer.toString(i));
                workers.add(client);
                executor.execute(client);
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            if(server!=null)
                server.close();
        }
    }
}
