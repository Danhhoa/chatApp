package com.example.chatapp.server;

import java.io.*;
import java.net.Socket;

public class HandleClients implements Runnable {
    private String myName;
    private Socket socket;
    BufferedReader in;
    BufferedWriter out;


    public HandleClients(Socket s, String name) throws IOException {
        this.socket = s;
        this.myName = name;
        this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

    }

    public static void add(HandleClients client) {
    }

    @Override
    public void run() {
        System.out.println("Client " + socket.toString() + " accepted");
        try {
            String input = "";
            while(true) {
                input = in.readLine();
                System.out.println("Server received: " + input + " from " + socket.toString() + " # Client " + myName);
                if(input.equals("bye"))
                    break;
                for(HandleClients worker : Server.workers) {
                    if(!myName.equals(worker.myName)) {
                        worker.out.write(input + '\n');
                        worker.out.flush();
                        System.out.println("Server write: " + input + " to " + worker.myName);
                        break;
                    }
                }
            }
            System.out.println("Closed socket for client " + myName + " " + socket.toString());
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
