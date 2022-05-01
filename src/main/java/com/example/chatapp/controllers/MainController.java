package com.example.chatapp.controllers;

import com.example.chatapp.Main;
import com.example.chatapp.client.Client;
import com.example.chatapp.utils.AlertUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.control.Notifications;
import org.json.JSONObject;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;
import com.example.chatapp.utils.Encryption;
import com.example.chatapp.utils.Decryption;


public class MainController {

    @FXML
    TextField tfName;
    @FXML
    Button btnName;

    static Client client;

    static {
        try {
            client = new Client("localhost", 8085);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String receive;
    String WAIT_TO_FOUND="_founding";
    String FOUND_SUCCESS = "[a-zA-Z0-9 \\u0080-\\u9fff]*-[a-zA-Z0-9 \\u0080-\\u9fff]*";
    String ACCEPT_USER = "Y/N";
    public String PUBLIC_KEY = client.PUBLIC_KEY;
    public final String SECRET_KEY = "rsa.aes.hybrid.d";
    public static PublicKey pubKey;
    public static SecretKeySpec skeySpec;


    public MainController() throws IOException {
    }

    public void onActionEnterName(ActionEvent e) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String nickName = tfName.getText();

        if (nickName.trim().isEmpty()) {
            Notifications.create()
                    .title("Thông báo")
                    .text("Vui lòng nhập nickname")
                    .showWarning();
        } else {
            client.run();
            skeySpec = client.generateAESkey(SECRET_KEY);

            String publicKeyFromSV = client.getRecv().receiveFromServer();
            System.out.println("PublicKey: " + publicKeyFromSV);
            pubKey = client.RSAPublicKey(publicKeyFromSV);
            System.out.println("pubkey: "+ pubKey);

            //use publickey from sv Encrypt secretKey by RSA
            String strEncrypt = Encryption.encryptDataByRSA(SECRET_KEY, pubKey);
            String encryptSecretKey = strEncrypt;
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("secretKey", encryptSecretKey);
            encryptSecretKey = jsonObject1.toString();
            //client send secretKey which encrypted by publicKey from server to server
            client.getSend().sendToServer(encryptSecretKey);

            String encryptedMessage;
            encryptedMessage = Encryption.encryptDataByAES(nickName, skeySpec);

            //Encrypt second time by publicKey RSA
            encryptedMessage = Encryption.encryptDataByRSA(encryptedMessage, pubKey);
            //nickname encrypted
            client.getSend().sendToServer(encryptedMessage);
            boolean flag = false;
            while (true) {
                receive = client.getRecv().receiveFromServer();
                //Decrypt data by AES
                String dataDecrypt = Decryption.decryptDataByAES(receive, skeySpec);
                System.out.println("nhận 1:" + dataDecrypt);
                if (dataDecrypt.equalsIgnoreCase("success")) {
                    btnName.setDisable(true);
                    AlertUtils.showNotification("Đăng ký nickname thành công " +nickName );

                }
                else if (dataDecrypt.equals("fail")) {
                    AlertUtils.showWarning("Nickname đã tồn tại. Hãy nhập nickname khác");
                    client.close();
                    client = new Client(Client.getHost(), 8085);
                    return;
                }
                else if (dataDecrypt.equalsIgnoreCase(WAIT_TO_FOUND)) {
                    flag = true;
                    AlertUtils.showNotification("Đang tìm người ghép đôi");
                }
                else if (dataDecrypt.contains(ACCEPT_USER)) {
                    Alert alert = AlertUtils.alert(Alert.AlertType.CONFIRMATION, dataDecrypt);
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        String sendNo = "N";
                        encryptedMessage = Encryption.encryptDataByAES(sendNo, skeySpec);
                        //Encrypt second time by publicKey RSA
                        encryptedMessage = Encryption.encryptDataByRSA(encryptedMessage, pubKey);
                        client.getSend().sendToServer(encryptedMessage);
                    }
                    else {
                        String sendYes = "Y";
                        encryptedMessage = Encryption.encryptDataByAES(sendYes, skeySpec);
                        //Encrypt second time by publicKey RSA
                        encryptedMessage = Encryption.encryptDataByRSA(encryptedMessage, pubKey);
                        client.getSend().sendToServer(encryptedMessage);
//                        break;
                    }
                }
                else if (dataDecrypt.matches(FOUND_SUCCESS)){
                    AlertUtils.showNotification("Bạn đã được ghép đôi\n" + "Đi đến phòng chat: " + dataDecrypt);
                    break;
                }
            }
            gotoChatroom();
            closeStage();
        }
    }
    public void gotoChatroom() throws IOException {
        System.out.println("chat room");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/chatapp/chat-box.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(getClass().getResource("/com/example/chatapp/mainCSS.css").toExternalForm());
        Stage stage = new Stage();
        ChatBoxController chatBoxController = new ChatBoxController();
        fxmlLoader.setController(chatBoxController);
        stage.setTitle("Phòng chat");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                try {
                    Alert alert = AlertUtils.alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn thoát không?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isEmpty() || result.get() == ButtonType.CANCEL) {
                        System.out.println("NO exit");
                    }
                    if (result.get() == ButtonType.OK) {
                        client.getSend().sendToServer("_bye");
                        client.getSend().sendToServer("_exit");
                        client.close();
                        Platform.exit();
                        System.exit(0);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void closeStage() {
        Stage stage = (Stage) btnName.getScene().getWindow();
        stage.close();
    }

    PublicKey publicKey (String publicKey) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = factory.generatePublic(spec);
            return pubKey;
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
