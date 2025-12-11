package com.studentmanager.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ClientController {

    @FXML
    private TextArea messageArea;

    @FXML
    private TextField inputField;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        messageArea.appendText("Witaj w aplikacji klienckiej!\n");
    }


    @FXML
    protected void onSendAction() {
        String message = inputField.getText();

        if (!message.isEmpty()) {

            messageArea.appendText("Ja: " + message + "\n");
            inputField.clear();
        }
    }


    public void appendMessage(String msg) {
        messageArea.appendText("Serwer: " + msg + "\n");
    }
}