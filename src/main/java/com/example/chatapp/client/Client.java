package com.example.chatapp.client;

import org.json.JSONObject;

import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.*;

public class Client {

    private static String host = "localhost";
    private static int port = 8085;
    private static Socket socket;

    private static BufferedWriter out;
    private static BufferedReader in;
    SendMessage send;
    ReceiveMessage recv;
    public String PUBLIC_KEY;

    public Client (String host, int port) throws IOException {
        socket = new Socket(host, port);
        System.out.println("Client connected");
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        send = new SendMessage(socket, out);
        recv = new ReceiveMessage(socket, in);
    }

    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(send);
        executor.execute(recv);
    }

    public SecretKeySpec generateAESkey(String secretKey) {
        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
        return skeySpec;
    }
    public PublicKey RSAPublicKey (String publicKeyFromSV) throws NoSuchAlgorithmException, InvalidKeySpecException {
        JSONObject jsonObject = new JSONObject(publicKeyFromSV);

        String publicKey = jsonObject.get("publicKey").toString();

        PUBLIC_KEY = publicKey;
        byte[] decodedBytes = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedBytes);

        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = factory.generatePublic(spec);
        return pubKey;
    }

    public static String getHost() {
        return host;
    }

    public static void setHost(String host) {
        Client.host = host;
    }

    public SendMessage getSend() {
        return send;
    }

    public void setSend(SendMessage send) {
        this.send = send;
    }

    public ReceiveMessage getRecv() {
        return recv;
    }

    public void setRecv(ReceiveMessage recv) {
        this.recv = recv;
    }

    public void close() throws IOException {
        socket.close();
        in.close();
        out.close();
        send.close();
        recv.close();
    }
    //    public static void main(String[] args) throws IOException {
//       new Client(host, port);
//    }
}
