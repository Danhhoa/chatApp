package com.example.chatapp.controllers;

import com.example.chatapp.client.Client;
import com.example.chatapp.utils.AlertUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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


    Client client = MainController.client;
    String dataReceive;
    String EXIT = "_EXIT";
    String FOUND_SUCCESS = "[a-zA-Z0-9 \\u0080-\\u9fff]*-[a-zA-Z0-9 \\u0080-\\u9fff]*";
    String ACCEPT_USER = "Y/N";
    String WAIT_TO_FOUND = "_founding";
    public void receive() {
        Thread thread = new Thread(() -> {
            while ((dataReceive = client.getRecv().receiveFromServer()) != null) {
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
                        else if (dataReceive.equalsIgnoreCase(WAIT_TO_FOUND)) {
                            AlertUtils.showNotification("Đang tìm người ghép đôi");
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
            client.getSend().sendToServer(message);
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
            client.getSend().sendToServer("_bye");
            client.getSend().sendToServer("_findnew");

        }
    }

    public static void shutdown() {
        Thread thisThread = Thread.currentThread();
        thisThread.interrupt();
    }
}
