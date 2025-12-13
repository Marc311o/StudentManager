package com.studentmanager.client;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class ManagementController {

    // table 1 students
    @FXML private TableView<TempStudent> studentTable;
    @FXML private TableColumn<TempStudent, String> firstnameCol;
    @FXML private TableColumn<TempStudent, String> surnameCol;
    @FXML private TableColumn<TempStudent, Number> indCol;

    // table 2 grades
    @FXML private TableView<TempPrzedmiot> gradeTable;
    @FXML private TableColumn<TempPrzedmiot, String> nameCol;
    @FXML private TableColumn<TempPrzedmiot, Number> gradeCol;

    // buttons
    @FXML private Button deleteStudentBtn;
    @FXML private Button deleteGradeBtn;

    @FXML
    public void initialize(){

        configureColumns();

        studentTable.setPlaceholder(new javafx.scene.control.Label("Brak studentów w bazie"));
        gradeTable.setPlaceholder(new javafx.scene.control.Label("Wybierz studenta, aby zobaczyć oceny"));

        // TODO: repalce subsequent line with server data
        //  also make sure that all fields from final data model responds to this
        studentTable.setItems(prepareDummyData()); // this one btw

        studentTable.getSelectionModel().clearSelection();

        deleteStudentBtn.visibleProperty().bind(studentTable.getSelectionModel().selectedItemProperty().isNotNull());
        deleteGradeBtn.visibleProperty().bind(gradeTable.getSelectionModel().selectedItemProperty().isNotNull());

        studentTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldStudent, newStudent) -> {

                    if (newStudent != null) {
                        showStudentsGrades(newStudent);
                    } else {
                        gradeTable.getItems().clear();
                    }
                });

    }

    private void showStudentsGrades(TempStudent student) {

        ArrayList<TempPrzedmiot> listaOcen = student.getPrzedmioty();

        if (listaOcen == null || listaOcen.isEmpty()) {

            Label emptyLabel = new Label("Student " + student.getImie() + " nie ma żadnych ocen");
            gradeTable.setPlaceholder(emptyLabel);
            gradeTable.getItems().clear();
        } else {
            gradeTable.setItems(FXCollections.observableArrayList(listaOcen));
        }
    }

    private void configureColumns() {

        // student table config
        firstnameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getImie()));

        surnameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNazwisko()));

        indCol.setCellValueFactory(cellData ->
                new SimpleLongProperty(cellData.getValue().getIndeks()));

        // grades table config
        nameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNazwa()));

        gradeCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getOcena()));
    }

    public ObservableList<TempStudent> prepareDummyData() {

        ObservableList<TempStudent> listaStudentow = FXCollections.observableArrayList();

        ArrayList<TempPrzedmiot> przedmioty1 = new ArrayList<>();
        przedmioty1.add(new TempPrzedmiot("ZPO", 5));
        przedmioty1.add(new TempPrzedmiot("RiOBD", 4));
        przedmioty1.add(new TempPrzedmiot("SCR", 3));
        listaStudentow.add(new TempStudent("Jan", "Kowalski", 123456L, przedmioty1));

        ArrayList<TempPrzedmiot> przedmioty2 = new ArrayList<>();
        przedmioty2.add(new TempPrzedmiot("Matematyka Dyskretna", 5));
        przedmioty2.add(new TempPrzedmiot("Angielski", 5));
        listaStudentow.add(new TempStudent("Anna", "Nowak", 654321L, przedmioty2));


        listaStudentow.add(new TempStudent("Piotr", "Zieliński", 676767L, new ArrayList<>()));

        return listaStudentow;
    }

    // data deletion
    private boolean confirmDeletion(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm()
        );

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/logo_square.jpg")).toString()));

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Usuń");
        okButton.getStyleClass().add("danger-button");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Anuluj");

        Optional<ButtonType> res = alert.showAndWait();

        return res.isPresent() && res.get() == ButtonType.OK;
    }

    @FXML
    public void deleteStudentAction() {

        TempStudent wybranyStudent = studentTable.getSelectionModel().getSelectedItem();

        if (wybranyStudent == null) return;

        boolean agreement = confirmDeletion("Usuwanie studenta",
                "Czy na pewno chcesz usunąć studenta " + wybranyStudent.getImie() + " " + wybranyStudent.getNazwisko() + "?");

        if (agreement) {
            studentTable.getItems().remove(wybranyStudent);
            gradeTable.getItems().clear();

            // TODO: server data integration
            System.out.println("Usunięto studenta: " + wybranyStudent.getImie() + " " + wybranyStudent.getNazwisko());
        }
    }

    @FXML
    public void deleteGradeAction() {

        TempPrzedmiot wybranaOcena = gradeTable.getSelectionModel().getSelectedItem();
        TempStudent wybranyStudent = studentTable.getSelectionModel().getSelectedItem();

        if (wybranaOcena == null || wybranyStudent == null) return;

        boolean zgoda = confirmDeletion("Usuwanie oceny",
                "Czy chcesz usunąć ocenę " + wybranaOcena.getOcena() + " z przedmiotu " + wybranaOcena.getNazwa() + "?");

        if (zgoda) {
            wybranyStudent.getPrzedmioty().remove(wybranaOcena);
            gradeTable.getItems().remove(wybranaOcena);

            // TODO: Server data integration here

            if (gradeTable.getItems().isEmpty()) showStudentsGrades(wybranyStudent);
        }
    }

    // data addition
    @FXML
    public void addStudentAction() {

        Dialog<TempStudent> dialog = new Dialog<>();
        dialog.setTitle("Nowy Student");
        dialog.setHeaderText("Wprowadź dane studenta");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/logo_square.jpg")).toString()));

        ButtonType saveBtnType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField firstnameField = new TextField();
        firstnameField.setPromptText("Imię");
        TextField surnameField = new TextField();
        surnameField.setPromptText("Nazwisko");
        TextField indField = new TextField();
        indField.setPromptText("123456");

        grid.add(new Label("Imię:"), 0, 0);
        grid.add(firstnameField, 1, 0);
        grid.add(new Label("Nazwisko:"), 0, 1);
        grid.add(surnameField, 1, 1);
        grid.add(new Label("Indeks:"), 0, 2);
        grid.add(indField, 1, 2);

        dialog.getDialogPane().setContent(grid);


        Node saveBtn = dialogPane.lookupButton(saveBtnType);
        saveBtn.getStyleClass().add("btn-success");
        saveBtn.setDisable(true);

        saveBtn.disableProperty().bind(
                firstnameField.textProperty().isEmpty()
                        .or(surnameField.textProperty().isEmpty())
                        .or(indField.textProperty().isEmpty())
        );

        saveBtn.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Long.parseLong(indField.getText());
            } catch (NumberFormatException e) {
                event.consume();


                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Błąd danych");
                alert.setHeaderText("Nieprawidłowy format indeksu");
                alert.setContentText("Numer indeksu musi składać się wyłącznie z cyfr!");

                alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
                Stage inn_stage = (Stage) alert.getDialogPane().getScene().getWindow();
                inn_stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/logo_square.jpg")).toString()));

                alert.showAndWait();
            }
        });


        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                return new TempStudent(
                        firstnameField.getText(),
                        surnameField.getText(),
                        Long.parseLong(indField.getText()),
                        new ArrayList<>()
                );
            }
            return null;
        });

        Optional<TempStudent> result = dialog.showAndWait();

        result.ifPresent(student -> {
            studentTable.getItems().add(student);
            System.out.println("Dodano studenta: " + student.getNazwisko());
            // TODO: server integration
        });
    }

    @FXML
    public void addGradeAction() {

        TempStudent chosenStudent = studentTable.getSelectionModel().getSelectedItem();
        if (chosenStudent == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Błąd");
            alert.setHeaderText("Nie wybrano studenta");
            alert.setContentText("Aby dodać ocenę, najpierw zaznacz studenta z listy.");
            alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/logo_square.jpg")).toString()));
            alert.showAndWait();
            return;
        }

        Dialog<TempPrzedmiot> dialog = new Dialog<>();
        dialog.setTitle("Nowa Ocena");
        dialog.setHeaderText("Dodawanie oceny dla: " + chosenStudent.getImie());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());

        dialogPane.getStyleClass().add("dialog-pane");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/logo_square.jpg")).toString()));

        ButtonType zapiszBtnType = new ButtonType("Dodaj", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(zapiszBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Np. Programowanie");

        ComboBox<Integer> ocenaBox = new ComboBox<>();
        ocenaBox.getItems().addAll(2, 3, 4, 5);
        ocenaBox.setValue(3);

        grid.add(new Label("Przedmiot:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Ocena:"), 0, 1);
        grid.add(ocenaBox, 1, 1);

        dialogPane.setContent(grid);

        Node addBtn = dialogPane.lookupButton(zapiszBtnType);
        addBtn.setDisable(true);
        addBtn.getStyleClass().add("btn-success");

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            addBtn.setDisable(newValue.trim().isEmpty());
        });


        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == zapiszBtnType) return new TempPrzedmiot(nameField.getText(), ocenaBox.getValue());
            return null;
        });

        // TODO: fix class to permanent datamodel
        Optional<TempPrzedmiot> result = dialog.showAndWait();

        result.ifPresent(przedmiot -> {
            chosenStudent.getPrzedmioty().add(przedmiot);
            showStudentsGrades(chosenStudent);

            // TODO: (yeah you guessed it) server data integration
            System.out.println("Dodano ocenę z przedmiotu: " + przedmiot.getNazwa());
        });
    }

    // back to main menu
    public void goBackToMenuBtnRelease(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/intro.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

    }

}
