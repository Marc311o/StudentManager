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

    // przyciski
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
        stage.getIcons().add(new Image(getClass().getResource("/logo_square.jpg").toString()));

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


    public void goBackToMenuBtnRelease(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/intro.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

    }

}
