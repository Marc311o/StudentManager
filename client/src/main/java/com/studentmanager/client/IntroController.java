package com.studentmanager.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert; // Dodaj import Alert
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.util.Objects;

public class IntroController {

    public void setGoBtnRelease(ActionEvent event) { // Usuń 'throws IOException' stąd, obsłużymy to wewnątrz
        try {
            // 1. Sprawdzamy, czy mamy połączenie. Jeśli nie (service == null), próbujemy się połączyć.
            if (ClientConnection.getService() == null) {
                System.out.println("Próba nawiązania połączenia z serwerem...");
                ClientConnection.connect();
            }

            // 2. Jeśli połączenie jest aktywne (lub właśnie się udało), ładujemy widok studentów
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/management.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            // 3. Jeśli połączenie się nie uda, wyświetlamy komunikat i NIE zmieniamy sceny
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd połączenia");
            alert.setHeaderText("Nie można połączyć z serwerem");
            alert.setContentText("Upewnij się, że serwer jest uruchomiony, a następnie spróbuj ponownie.");
            alert.showAndWait();
        }
    }
}