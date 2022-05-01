package com.example.chatapp.controllers;

import com.example.chatapp.client.Client;
import com.example.chatapp.utils.AlertUtils;
import com.example.chatapp.utils.Decryption;
import com.example.chatapp.utils.Encryption;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.controlsfx.control.Notifications;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;
import java.util.Optional;
import java.util.ResourceBundle;

public class ChatBoxController implements Initializable {

    @FXML
    Button btnSend;

    @FXML
    public TextField tfMessage;

    @FXML
    public VBox messageBox;

    @FXML
    Button btnOutChat;

    @FXML
    TextFlow textFlow;

    Client client = MainController.client;


    String EXIT = "_EXIT";
    String FOUND_SUCCESS = "[a-zA-Z0-9 \\u0080-\\u9fff]*-[a-zA-Z0-9 \\u0080-\\u9fff]*";
    String ACCEPT_USER = "Y/N";
    String WAIT_TO_FOUND = "_founding";
    String FIND_NEW = "_findnew";
    PublicKey pubKey = MainController.pubKey;
    SecretKeySpec skeySpec = MainController.skeySpec;
    String dataReceive;
    String dataEncrypt;
    public void receive() {
        Thread thread = new Thread(() -> {
            System.out.println(pubKey);
            System.out.println(skeySpec);
            while ((dataEncrypt = client.getRecv().receiveFromServer()) != null) {
                //Decrypt data by AES
                dataReceive = Decryption.decryptDataByAES(dataEncrypt, skeySpec);
                System.out.println("Nhận: " + dataReceive);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        String encryptedMessage;
                        if (dataReceive.contains(EXIT)) {
                            AlertUtils.showNotification(dataReceive + " khỏi phòng chat\n" + "Chúng tôi sẽ tìm người khác ngay sau đây" );
                            try {
                                encryptedMessage = Encryption.encryptDataByAES(FIND_NEW, skeySpec);
                                //Encrypt second time by publicKey RSA
                                encryptedMessage = Encryption.encryptDataByRSA(encryptedMessage, pubKey);
                                client.getSend().sendToServer(encryptedMessage);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        else if (dataReceive.equalsIgnoreCase(WAIT_TO_FOUND)) {
                            AlertUtils.showNotification("Đang tìm người ghép đôi");
                        }
                        else if (dataReceive.contains(ACCEPT_USER)) {
                            Alert alert = AlertUtils.alert(Alert.AlertType.CONFIRMATION, dataReceive);
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isEmpty() || result.get() != ButtonType.OK) {
                                try {
                                    String sendCancel = "N";
                                    encryptedMessage = Encryption.encryptDataByAES(sendCancel, skeySpec);
                                    //Encrypt second time by publicKey RSA
                                    encryptedMessage = Encryption.encryptDataByRSA(encryptedMessage, pubKey);
                                    client.getSend().sendToServer(encryptedMessage);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                try {
                                    String sendYes = "Y";
                                    encryptedMessage = Encryption.encryptDataByAES(sendYes, skeySpec);
                                    //Encrypt second time by publicKey RSA
                                    encryptedMessage = Encryption.encryptDataByRSA(encryptedMessage, pubKey);
                                    client.getSend().sendToServer(encryptedMessage);
                                    messageBox.getChildren().clear();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        else if (dataReceive.matches(FOUND_SUCCESS)){
                            AlertUtils.showNotification("Bạn đã được ghép đôi\n" + "Đi đến phòng chat: " + dataReceive);
                            messageBox.getChildren().clear();
                        }
                        else  {
                            showMessage(dataReceive, "#FFFFFF", "#839EFB", false);
                        }

                    }
                });
            }
        });

    thread.start();
    }


    public void onActionSend(ActionEvent actionEvent) throws IOException {
        String message = tfMessage.getText();
        if (message.isEmpty()) {
            Notifications.create()
                    .title("Thông báo")
                    .text("Hãy nhập tin nhắn")
                    .showWarning();
        }
        else {
            String encryptedMessage;
            encryptedMessage = Encryption.encryptDataByAES(message, skeySpec);

            //Encrypt second time by publicKey RSA
            encryptedMessage = Encryption.encryptDataByRSA(encryptedMessage, pubKey);

            client.getSend().sendToServer(encryptedMessage);
            showMessage(message, "black", "#FFFFFF", true);
            tfMessage.clear();
        }

    }

    public void showMessage(String message, String textColor, String backgroundColor, boolean right) {
        Text text = new Text(message);
        text.setFont(Font.font("Segoe UI Historic", 15));
        textFlow = new TextFlow(text);
        textFlow.setMaxWidth(180);
        textFlow.setPadding(new Insets(5, 10, 5, 10));
        textFlow.setStyle("-fx-fill: " + textColor + "; -fx-background-color: " + backgroundColor + "; -fx-background-radius: 20px; -fx-margin: 0,0,10,0;");
        HBox pane = new HBox(textFlow);
        pane.setMargin(textFlow, new Insets(5,0,2,0));
        pane.setAlignment(right ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageBox.getChildren().add(pane);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        receive();

    }

    public void onOutChat(ActionEvent actionEvent) throws IOException {
        Alert alert = AlertUtils.alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn thoát không?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            System.out.println("cancel");
        }
        else {
            String sendBye = "_bye",
                    encryptedMessage,
                    sendFindNew = "_findnew";
            encryptedMessage = Encryption.encryptDataByAES(sendBye, skeySpec);
            //Encrypt second time by publicKey RSA
            encryptedMessage = Encryption.encryptDataByRSA(encryptedMessage, pubKey);
            client.getSend().sendToServer(encryptedMessage);

            encryptedMessage = Encryption.encryptDataByAES(sendFindNew, skeySpec);
            //Encrypt second time by publicKey RSA
            encryptedMessage = Encryption.encryptDataByRSA(encryptedMessage, pubKey);
            client.getSend().sendToServer(encryptedMessage);

        }
    }

    public static void shutdown() {
        Thread thisThread = Thread.currentThread();
        thisThread.interrupt();
    }
}
