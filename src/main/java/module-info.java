module com.example.chatapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.controlsfx.controls;


    opens com.example.chatapp to javafx.fxml;
    exports com.example.chatapp;
    exports com.example.chatapp.controllers;
    opens com.example.chatapp.controllers to javafx.fxml;
}