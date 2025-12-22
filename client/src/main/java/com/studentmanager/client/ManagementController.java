package com.studentmanager.client;

import com.studentmanager.shared.GradeDTO;
import com.studentmanager.shared.StudentDTO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ManagementController {

    // --- Tabela studentów ---
    @FXML private TableView<StudentDTO> studentTable;
    @FXML private TableColumn<StudentDTO, String> firstnameCol;
    @FXML private TableColumn<StudentDTO, String> surnameCol;
    @FXML private TableColumn<StudentDTO, String> indCol;

    // --- Tabela ocen ---
    @FXML private TableView<GradeDTO> gradeTable;
    @FXML private TableColumn<GradeDTO, String> nameCol;
    @FXML private TableColumn<GradeDTO, Double> gradeCol;

    // --- Przyciski ---
    @FXML private Button deleteStudentBtn;
    @FXML private Button deleteGradeBtn;

    @FXML
    public void initialize() {
        configureColumns();

        studentTable.setPlaceholder(new Label("Ładowanie danych..."));
        gradeTable.setPlaceholder(new Label("Wybierz studenta, aby zobaczyć oceny"));

        // Pobranie listy studentów na starcie
        refreshStudentList();

        // Obsługa przycisków usuwania (aktywne tylko gdy coś zaznaczono)
        deleteStudentBtn.visibleProperty().bind(studentTable.getSelectionModel().selectedItemProperty().isNotNull());
        deleteGradeBtn.visibleProperty().bind(gradeTable.getSelectionModel().selectedItemProperty().isNotNull());

        // Listener wyboru studenta -> pobierz jego oceny z serwera
        studentTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldStudent, newStudent) -> {
                    if (newStudent != null) {
                        fetchGradesForStudent(newStudent.getId());
                    } else {
                        gradeTable.getItems().clear();
                        gradeTable.setPlaceholder(new Label("Wybierz studenta"));
                    }
                });
    }

    // --- Konfiguracja kolumn tabel ---
    private void configureColumns() {
        firstnameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFirstName()));
        surnameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLastName()));
        indCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getIndexNumber()));

        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCourseName()));
        gradeCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getValue()));
    }

    // --- Pobieranie danych z serwera (Asynchronicznie) ---

    private void refreshStudentList() {
        Task<List<StudentDTO>> task = new Task<>() {
            @Override
            protected List<StudentDTO> call() throws Exception {
                return ClientConnection.getService().getAllStudents();
            }
        };

        task.setOnSucceeded(e -> {
            studentTable.setItems(FXCollections.observableArrayList(task.getValue()));
            if (studentTable.getItems().isEmpty()) {
                studentTable.setPlaceholder(new Label("Brak studentów w bazie"));
            }
        });

        task.setOnFailed(e -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Nie udało się pobrać listy studentów.");
            alert.show();
            e.getSource().getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private void fetchGradesForStudent(Long studentId) {
        gradeTable.setPlaceholder(new Label("Pobieranie ocen..."));
        
        Task<List<GradeDTO>> task = new Task<>() {
            @Override
            protected List<GradeDTO> call() throws Exception {
                return ClientConnection.getService().getGradesForStudent(studentId);
            }
        };

        task.setOnSucceeded(e -> {
            gradeTable.setItems(FXCollections.observableArrayList(task.getValue()));
            if (gradeTable.getItems().isEmpty()) {
                gradeTable.setPlaceholder(new Label("Brak ocen dla tego studenta"));
            }
        });

        task.setOnFailed(e -> {
            gradeTable.setPlaceholder(new Label("Błąd pobierania ocen"));
            e.getSource().getException().printStackTrace();
        });

        new Thread(task).start();
    }

    // --- Akcje Użytkownika ---

    @FXML
    public void addStudentAction() {
        // UI Dialogu (bez zmian logicznych w samym wyglądzie)
        Dialog<StudentDTO> dialog = new Dialog<>();
        dialog.setTitle("Nowy Student");
        dialog.setHeaderText("Wprowadź dane studenta");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        ButtonType saveBtnType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

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
        dialogPane.setContent(grid);

        Node saveBtn = dialogPane.lookupButton(saveBtnType);
        saveBtn.setDisable(true);
        saveBtn.disableProperty().bind(firstnameField.textProperty().isEmpty().or(surnameField.textProperty().isEmpty()).or(indField.textProperty().isEmpty()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                // Tworzymy DTO (bez listy ocen)
                return new StudentDTO(null, firstnameField.getText(), surnameField.getText(), indField.getText());
            }
            return null;
        });

        Optional<StudentDTO> result = dialog.showAndWait();

        result.ifPresent(dto -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    ClientConnection.getService().addStudent(dto);
                    return null;
                }
            };
            task.setOnSucceeded(e -> refreshStudentList());
            task.setOnFailed(e -> new Alert(Alert.AlertType.ERROR, "Błąd dodawania: " + e.getSource().getException().getMessage()).show());
            new Thread(task).start();
        });
    }

    @FXML
    public void addGradeAction() {
        StudentDTO selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            new Alert(Alert.AlertType.WARNING, "Wybierz studenta!").show();
            return;
        }

        Dialog<GradeDTO> dialog = new Dialog<>();
        dialog.setTitle("Nowa Ocena");
        dialog.setHeaderText("Dodaj ocenę dla: " + selectedStudent.getFirstName());
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        ButtonType saveBtnType = new ButtonType("Dodaj", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField nameField = new TextField(); nameField.setPromptText("Przedmiot");
        ComboBox<Integer> ocenaBox = new ComboBox<>();
        ocenaBox.getItems().addAll(2, 3, 4, 5);
        ocenaBox.setValue(3);

        grid.add(new Label("Przedmiot:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Ocena:"), 0, 1); grid.add(ocenaBox, 1, 1);
        dialogPane.setContent(grid);

        Node saveBtn = dialogPane.lookupButton(saveBtnType);
        saveBtn.setDisable(true);
        nameField.textProperty().addListener((observable, oldValue, newValue) -> saveBtn.setDisable(newValue.trim().isEmpty()));

        dialog.setResultConverter(button -> {
            if (button == saveBtnType) {
                // Use GradeDTO as a temporary form data carrier; ID is null because it will be assigned when the grade is saved on the server
                return new GradeDTO(null, nameField.getText(), Double.valueOf(ocenaBox.getValue()));
            }
            return null;
        });

        Optional<GradeDTO> result = dialog.showAndWait();
        result.ifPresent(gradeDto -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    ClientConnection.getService().addGrade(selectedStudent.getId(), gradeDto.getCourseName(), gradeDto.getValue().intValue());
                    return null;
                }
            };
            // Po dodaniu odświeżamy tabelę ocen dla aktualnie wybranego studenta
            task.setOnSucceeded(e -> fetchGradesForStudent(selectedStudent.getId()));
            task.setOnFailed(e -> new Alert(Alert.AlertType.ERROR, "Błąd dodawania oceny: " + e.getSource().getException().getMessage()).show());
            new Thread(task).start();
        });
    }

    @FXML
    public void deleteStudentAction() {
        StudentDTO selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Usunąć studenta " + selected.getLastName() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        ClientConnection.getService().removeStudent(selected.getId());
                        return null;
                    }
                };
                task.setOnSucceeded(e -> {
                    refreshStudentList();
                    gradeTable.getItems().clear();
                });
                new Thread(task).start();
            }
        });
    }

    @FXML
    public void deleteGradeAction() {
        StudentDTO selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        GradeDTO selectedGrade = gradeTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null || selectedGrade == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Usunąć ocenę z " + selectedGrade.getCourseName() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        ClientConnection.getService().removeGrade(selectedStudent.getId(), selectedGrade.getCourseName());
                        return null;
                    }
                };
                task.setOnSucceeded(e -> fetchGradesForStudent(selectedStudent.getId()));
                new Thread(task).start();
            }
        });
    }
    
    public void goBackToMenuBtnRelease(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/intro.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}