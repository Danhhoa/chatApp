package com.example.chatapp.controllers;

import com.example.chatapp.client.Client;
import com.example.chatapp.utils.AlertUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.net.URL;
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

    @FXML
            Label lbName;


    Client client = MainController.client;
    String dataReceive;
    String EXIT = "_EXIT";
    String FOUND_SUCCESS = "[a-zA-Z0-9 \\u0080-\\u9fff]*-[a-zA-Z0-9 \\u0080-\\u9fff]*";
    String ACCEPT_USER = "Y/N";
    public void receive() {
        Thread thread = new Thread(() -> {
            while ((dataReceive = client.getRecv().receiveFromServer()) != null) {
//                dataReceive = client.getRecv().receiveFromServer();
                System.out.println("Nhận: " + dataReceive);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (dataReceive.contains(EXIT)) {
                            AlertUtils.showNotification(dataReceive + " khỏi phòng chat\n" + "Chúng tôi sẽ tìm người khác ngay sau đây" );
                            try {
                                client.getSend().sendToServer("_findnew");

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        else if (dataReceive.contains(ACCEPT_USER)) {
                            Alert alert = AlertUtils.alert(Alert.AlertType.CONFIRMATION, dataReceive);
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isEmpty() || result.get() != ButtonType.OK) {
                                try {
                                    client.getSend().sendToServer("N");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                try {
                                    client.getSend().sendToServer("Y");
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
                            showMessage(dataReceive, "#ffffff", "#0098c7");
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
            client.getSend().sendToServer(message);
            showMessage(message, "#000000", "white");
            tfMessage.clear();
        }

    }

    public void showMessage(String message, String textColor, String backgroundColor) {

        Text text = new Text(message);
        textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-color: " + textColor + "; -fx-background-color: " + backgroundColor + "; -fx-background-radius: 20px;");

        textFlow.setPadding(new Insets(5, 10, 5, 10));
        text.setFill(Color.BLACK);

        text.setFont(Font.font("Segoe UI Historic", 15));
        messageBox.getChildren().add(textFlow);
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
            client.getSend().sendToServer("_bye");
            client.getSend().sendToServer("_findnew");

        }
    }

    public static void shutdown() {
        Thread thisThread = Thread.currentThread();
        thisThread.interrupt();
    }
}
