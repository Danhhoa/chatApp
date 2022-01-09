package com.example.chatapp.controllers;

import com.example.chatapp.Main;
import com.example.chatapp.client.Client;
import com.example.chatapp.utils.AlertUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.util.Optional;

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

    public MainController() throws IOException {
    }

    public void onActionEnterName(ActionEvent e) throws IOException {
        String nickName = tfName.getText();

        if (nickName.trim().isEmpty()) {

            Notifications.create()
                    .title("Thông báo")
                    .text("Vui lòng nhập nickname")
                    .showWarning();
        } else {
            client.run();
            client.getSend().sendToServer(nickName);
            boolean flag = false;
            while (true) {
                receive = client.getRecv().receiveFromServer();
                System.out.println("nhận 1:" + receive);
                if (receive.equalsIgnoreCase("success")) {
                    btnName.setDisable(true);
                    AlertUtils.showNotification("Đăng ký nickname thành công " +nickName );

                }
                else if (receive.equals("fail")) {
                    AlertUtils.showWarning("Nickname đã tồn tại. Hãy nhập nickname khác");
                    client.close();
                    client = new Client(Client.getHost(), 8085);
                    return;
                }
                else if (receive.equalsIgnoreCase(WAIT_TO_FOUND)) {
                    flag = true;
                    AlertUtils.showNotification("Đang tìm người ghép đôi");
                }
                else if (receive.contains(ACCEPT_USER)) {
                    Alert alert = AlertUtils.alert(Alert.AlertType.CONFIRMATION, receive);
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        client.getSend().sendToServer("N");
                    }
                    else {
                        client.getSend().sendToServer("Y");
//                        break;
                    }
                }
                else if (receive.matches(FOUND_SUCCESS)){
                    AlertUtils.showNotification("Bạn đã được ghép đôi\n" + "Đi đến phòng chat: " + receive);
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
}
