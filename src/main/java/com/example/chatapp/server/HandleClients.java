package com.example.chatapp.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HandleClients implements Runnable {
    private String clientID;
    private Socket socket;
    final BufferedReader in;
    final BufferedWriter out;
    private String nickname = "";
    boolean isActive;
    private final String EXIT = "_exit";
    private final String BYE = "_bye";
    private final String FIND_NEW_CHAT = "_findnew";

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
            String dataReceived;
            // check nickname
            while ((nickname = in.readLine()) != null) {
                if (checkNickname(nickname)) {
                    System.out.println("This: " + this);
                    valueClient.put(nickname, this);
                    clientNickname.add(nickname);
                    System.out.println("trả kết quả dki tên thành công");
                    out.write("success");
                    out.newLine();
                    out.flush();
                    break;
                } else {
                    System.out.println("trả kết quả thất bại");
                    out.write("fail");
                    out.newLine();
                    out.flush();
                }
            }

            //ghép đôi
            pairing(this);
            System.out.println("couple :" + chatCouple);

            // gửi tin nhắn tới client được ghép
            while(true) {

                dataReceived = in.readLine();
                System.out.println("Server received: " + dataReceived + " from " + socket.toString() + " # Client " + nickname);
//                    if (flag) {
                        if (dataReceived.equalsIgnoreCase(EXIT)) {
                            clientNickname.remove(this.nickname);
                            out.write("_byeClient\n");
                            out.flush();
                            break;
                        }
                        else if (dataReceived.equalsIgnoreCase(BYE)) {
                            System.out.println("đang là client : " +this.nickname );
                            if (chatCouple.containsKey(this)) {
                                chatCouple.get(this).out.write(this.nickname + "_EXIT" + '\n');
                                chatCouple.get(this).out.flush();
                                chatCouple.remove(this);
                                this.isActive = false;
//                            this.socket.close();
                            } else {
                                HandleClients getKeyChatting = getKeyByValues(chatCouple, this);
                                System.out.println("lấy key:" +getKeyChatting.nickname);
                                getKeyChatting.out.write(nickname + "_EXIT" + '\n');
                                getKeyChatting.out.flush();
                                chatCouple.remove(getKeyChatting);
                            }


                        }
                        else if(dataReceived.equalsIgnoreCase(FIND_NEW_CHAT)) {
                            pairing(this);
                        }
                        else {
                            for (HandleClients couple : chatCouple.keySet()) {
                                if (clientID.equals(couple.clientID)) {
                                    chatCouple.get(couple).out.write(couple.nickname+": "+dataReceived+'\n');
                                    chatCouple.get(couple).out.flush();
                                    System.out.println("Server write 1: " + dataReceived + " to " + chatCouple.get(couple).clientID);
                                    break;
                                }
                                if (clientID.equals(chatCouple.get(couple).clientID)) {
                                    couple.out.write(chatCouple.get(couple).nickname + ": " + dataReceived+'\n');
                                    couple.out.flush();
                                    System.out.println("Server write 2: " + dataReceived + " to " + couple.clientID);
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
        }
    }

    public void pairing(HandleClients client) throws IOException {
        System.out.println("valueClient: " + valueClient.size());
        boolean flag = false;
        if (waitList.isEmpty()) {
            System.out.println("nick name:" +nickname);
            waitList.add(client);
            out.write("Đang tìm người mới");
            out.newLine();
            out.flush();
            System.out.println(nickname + " đang trong hàng đợi");

        } else {
            for (int i = 0; i < waitList.size(); i++) {
                try {
                    out.write("Chấp nhận kết nối: " + waitList.get(i).nickname + " Y/N?\n");
                    out.flush();
                    System.out.println("server gửi yêu cầu accept tới:" + waitList.get(i).nickname);

                    String responeFormClient = in.readLine();

                    if (responeFormClient.equalsIgnoreCase("y")) {
                        out.write(nickname + "-" + waitList.get(i).nickname);
                        out.newLine();
                        out.flush();

                        waitList.get(i).out.write(waitList.get(i).nickname + "-" + nickname+'\n');
                        waitList.get(i).out.flush();
                        chatCouple.put(valueClient.get(nickname), waitList.get(i));
                        waitList.remove(i);
                        System.out.println("sau khi xóa: " +waitList);
//                        System.out.println("" + chatCouple.);
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
                out.write("Đang tìm người mới"+'\n');
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
