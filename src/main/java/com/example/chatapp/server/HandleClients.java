package com.example.chatapp.server;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.example.chatapp.utils.Decryption;
import com.example.chatapp.utils.Encryption;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.spec.SecretKeySpec;

public class HandleClients implements Runnable {
    private String clientID;
    private Socket socket;
    final BufferedReader in;
    final BufferedWriter out;
    boolean isActive;
    private final String EXIT = "_exit";
    private final String BYE = "_bye";
    private final String FIND_NEW_CHAT = "_findnew";
    private String nickname = "";
    String decryptData;
    private SecretKeySpec skeySpec;
    private PrivateKey priKey;

    public static ArrayList<String> clientNickname = new ArrayList<>();
    public static  ArrayList<HandleClients> waitList= new ArrayList<>();
    public static HashMap<HandleClients, HandleClients> chatCouple = new HashMap<>();
    public static HashMap<String, HandleClients> valueClient = new HashMap<>();

    public HandleClients(Socket s, String clientID, BufferedReader in, BufferedWriter out) throws IOException {
        this.socket = s;
        this.clientID = clientID;
        this.in = in;
        this.out = out;
        this.isActive = true;
    }

    @Override
    public void run() {
        System.out.println("Client " + socket.toString() + " accepted");


        try {
            SecureRandom sr = new SecureRandom();
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048, sr);

            // Key Pair Initialize
            KeyPair kp = kpg.genKeyPair();

            // PublicKey
            PublicKey publicKey = kp.getPublic();

            // PrivateKey
            PrivateKey privateKey = kp.getPrivate();

            //Generator private key
            PKCS8EncodedKeySpec spec_en = new PKCS8EncodedKeySpec(privateKey.getEncoded());
            KeyFactory factory_en = KeyFactory.getInstance("RSA");
            priKey = factory_en.generatePrivate(spec_en);

            //Generator public key
            X509EncodedKeySpec spec_de = new X509EncodedKeySpec(publicKey.getEncoded());
            KeyFactory factory_de = KeyFactory.getInstance("RSA");
            PublicKey pubKey = factory_de.generatePublic(spec_de);

            String pubKeyEncode = Base64.getEncoder().encodeToString(pubKey.getEncoded());

            //Server put public Key into JSONObject
            JSONObject json = new JSONObject();
            json.put("publicKey", pubKeyEncode);
            String publicKeyTrans = json.toString();

            //Server send public Key to Client
            out.write(publicKeyTrans);
            out.newLine();
            out.flush();

            String secretKeyFromClient = in.readLine();
            System.out.println("secretKeyFromClient: "+ secretKeyFromClient);
            JSONObject jsonObject = new JSONObject(secretKeyFromClient);
            String secretKeyEncrypt = jsonObject.get("secretKey").toString();
            System.out.println("SecretKey: " + secretKeyEncrypt);

            //Server decrpyt secret Key by private Key from Client
            String decryptOut = Decryption.decryptDataByRSA(secretKeyEncrypt, priKey);

            //secretKey server to encrypt data send client
            skeySpec = new SecretKeySpec(decryptOut.getBytes(), "AES");


            // check nickname

            while ((nickname = in.readLine()) != null) {
                System.out.println("nicknameBeforeDecrypt: "+ nickname);
                //Decrypt first time by RSA
                decryptData = Decryption.decryptDataByRSA(nickname, priKey);
                //Decrypt second time by AES
                decryptData = Decryption.decryptDataByAES(decryptData, skeySpec);
                nickname = decryptData;
                System.out.println("nicknameAfterDecrypt: "+ nickname);
                if (checkNickname(nickname)) {
                    System.out.println("This: " + this);
                    valueClient.put(nickname, this);
                    clientNickname.add(nickname);
                    System.out.println("trả kết quả dki tên thành công");
                    //Encrypt data by AES
                    String success = Encryption.encryptDataByAES("success", skeySpec);
                    out.write(success);
                    out.newLine();
                    out.flush();
                    break;
                } else {
                    System.out.println("trả kết quả thất bại");
                    //Encrypt data by AES
                    String fail = Encryption.encryptDataByAES("fail", skeySpec);
                    out.write(fail);
                    out.newLine();
                    out.flush();
                }
            }

            //ghép đôi
            pairing(this);
            System.out.println("couple :" + chatCouple);

            // gửi tin nhắn tới client được ghép
            while(true) {
                String messageEncrypt = in.readLine();
                //Decrypt first time by RSA
                decryptData = Decryption.decryptDataByRSA(messageEncrypt, priKey);
                //Decrypt second time by AES
                decryptData = Decryption.decryptDataByAES(decryptData, skeySpec);
                String dataReceived = decryptData;
                System.out.println("Server received: " + dataReceived + " from " + socket.toString() + " # Client " + nickname);
                String tmp;
                        if (dataReceived.equalsIgnoreCase(EXIT)) {
                            clientNickname.remove(this.nickname);
                            valueClient.remove(this.nickname);
                            break;
                        }
                        else if (dataReceived.equalsIgnoreCase(BYE)) {
                            System.out.println("đang là client : " +this.nickname );
                            if (chatCouple.containsKey(this)) {
                                //Encrypt data by AES
                                tmp = Encryption.encryptDataByAES(this.nickname+"_EXIT", skeySpec);
                                chatCouple.get(this).out.write(tmp);
                                chatCouple.get(this).out.newLine();
                                chatCouple.get(this).out.flush();
                                chatCouple.remove(this);
                            } else {
                                HandleClients getKeyChatting = getKeyByValues(chatCouple, this);
                                System.out.println("lấy key:" +getKeyChatting.nickname);
                                //Encrypt data by AES
                                tmp = Encryption.encryptDataByAES(nickname + "_EXIT", skeySpec);
                                getKeyChatting.out.write(tmp);
                                getKeyChatting.out.newLine();
                                getKeyChatting.out.flush();
                                chatCouple.remove(getKeyChatting);
                            }


                        }
                        else if(dataReceived.equalsIgnoreCase(FIND_NEW_CHAT)) {
                            pairing(this);
                        }
                        else {
                            String message;
                            for (HandleClients couple : chatCouple.keySet()) {

                                if (clientID.equals(couple.clientID)) {
                                    //Encrypt data by AES
                                    message = Encryption.encryptDataByAES(couple.nickname+": " + dataReceived, skeySpec);
                                    chatCouple.get(couple).out.write(message);
                                    chatCouple.get(couple).out.newLine();
                                    chatCouple.get(couple).out.flush();
                                    System.out.println("Server write 1: " + message + " to " + chatCouple.get(couple).clientID);
                                    break;
                                }
                                if (clientID.equals(chatCouple.get(couple).clientID)) {
                                    //Encrypt data by AES
                                    message = Encryption.encryptDataByAES(chatCouple.get(couple).nickname + ": " + dataReceived, skeySpec);
                                    couple.out.write(message);
                                    couple.out.newLine();
                                    couple.out.flush();
                                    System.out.println("Server write 2: " + message + " to " + couple.clientID);
                                    break;
                                }

                            }
                        }
            }
            System.out.println("Closed socket for client " + clientID + " " + socket.toString());
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println(e);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public void pairing(HandleClients client) throws IOException {
        System.out.println("valueClient: " + valueClient.size());
        String FOUND_PATNER = Encryption.encryptDataByAES("_founding", skeySpec);;
        boolean flag = false;
        if (waitList.isEmpty()) {
            System.out.println("nick name:" +nickname);
            waitList.add(client);
            out.write(FOUND_PATNER);
            out.newLine();
            out.flush();
            System.out.println(nickname + " đang trong hàng đợi");

        } else {
            for (int i = 0; i < waitList.size(); i++) {
                try {
                    String tmp = "Chấp nhận kết nối: " + waitList.get(i).nickname + " Y/N?\n";
                    String ACCEPT_PATNER = Encryption.encryptDataByAES(tmp, skeySpec);
                    out.write(ACCEPT_PATNER);
                    out.newLine();
                    out.flush();
                    System.out.println("server gửi yêu cầu accept tới:" + waitList.get(i).nickname);

                    String responeFormClient = in.readLine();
                    //Decrypt first time by RSA
                    decryptData = Decryption.decryptDataByRSA(responeFormClient, priKey);
                    //Decrypt second time by AES
                    decryptData = Decryption.decryptDataByAES(decryptData, skeySpec);
                    responeFormClient = decryptData;

                    if (responeFormClient.equalsIgnoreCase("y")) {
                        String ACCEPT = Encryption.encryptDataByAES(nickname + "-" + waitList.get(i).nickname, skeySpec);
                        out.write(ACCEPT);
                        out.newLine();
                        out.flush();

                        String ACCEPT_2 = Encryption.encryptDataByAES(waitList.get(i).nickname + "-" + nickname, skeySpec);
                        waitList.get(i).out.write(ACCEPT_2);
                        waitList.get(i).out.newLine();
                        waitList.get(i).out.flush();
                        chatCouple.put(valueClient.get(nickname), waitList.get(i));
                        waitList.remove(i);
                        System.out.println("sau khi xóa: " +waitList);
                        flag = true;
                        break;
                    } else {
                        flag = false;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(!flag) {
                out.write(FOUND_PATNER);
                out.flush();
                waitList.add(this);
            }
        }
    }

    public boolean checkNickname(String nickName) throws IOException {
        if (!clientNickname.contains(nickName)) {
            return true;
        } else {
            return false;
        }
    }

    public HandleClients getKeyByValues(HashMap<HandleClients, HandleClients> map, HandleClients value) {
        for (Map.Entry<HandleClients, HandleClients> entry : map.entrySet()) {
            if(entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
