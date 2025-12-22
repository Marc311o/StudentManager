package com.studentmanager.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {
        ClientConnection.connect();
    } catch (Exception e) {
        e.printStackTrace();
    }
    
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApp.class.getResource("/intro.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 400);

        Image icon = new Image("logo_square.jpg");
        stage.getIcons().add(icon);

        stage.setTitle("StudentManager Client");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}