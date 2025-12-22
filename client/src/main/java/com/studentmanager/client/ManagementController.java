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

/**
 * The controller class for the main management view of the client application.
 * <p>
 * This class handles the JavaFX UI logic, responding to user actions such as adding or removing
 * students and grades. It communicates with the backend server via the {@link ClientConnection}
 * class to perform CRUD operations asynchronously, ensuring the UI remains responsive.
 * </p>
 */
public class ManagementController {

    /** The table view displaying the list of students. */
    @FXML private TableView<StudentDTO> studentTable;
    @FXML private TableColumn<StudentDTO, String> firstnameCol;
    @FXML private TableColumn<StudentDTO, String> surnameCol;
    @FXML private TableColumn<StudentDTO, String> indCol;

    /** The table view displaying grades for the selected student. */
    @FXML private TableView<GradeDTO> gradeTable;
    @FXML private TableColumn<GradeDTO, String> nameCol;
    @FXML private TableColumn<GradeDTO, Double> gradeCol;

    /** Buttons to trigger student or grade deletion. */
    @FXML private Button deleteStudentBtn;
    @FXML private Button deleteGradeBtn;

    /**
     * Initializes the controller class.
     * <p>
     * This method is automatically called after the FXML file has been loaded. It sets up:
     * <ul>
     * <li>Table columns cell value factories.</li>
     * <li>Placeholder text for empty tables.</li>
     * <li>Initial data loading (student list).</li>
     * <li>Bindings for button visibility based on table selection.</li>
     * <li>A listener on the student table to fetch grades when a student is selected.</li>
     * </ul>
     * </p>
     */
    @FXML
    public void initialize() {
        configureColumns();

        studentTable.setPlaceholder(new Label("Ładowanie danych..."));
        gradeTable.setPlaceholder(new Label("Wybierz studenta, aby zobaczyć oceny"));

        refreshStudentList();

        deleteStudentBtn.visibleProperty().bind(studentTable.getSelectionModel().selectedItemProperty().isNotNull());
        deleteGradeBtn.visibleProperty().bind(gradeTable.getSelectionModel().selectedItemProperty().isNotNull());

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

    /**
     * Configures the cell value factories for the TableView columns.
     * <p>
     * Maps properties from {@link StudentDTO} and {@link GradeDTO} to the respective table columns.
     * </p>
     */
    private void configureColumns() {
        firstnameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFirstName()));
        surnameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLastName()));
        indCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getIndexNumber()));

        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCourseName()));
        gradeCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getValue()));
    }

    // ============ SERVER DATA FETCHING ============
    /**
     * Fetches the list of all students from the server asynchronously.
     * <p>
     * Uses a background {@link Task} to prevent blocking the JavaFX Application Thread.
     * On success, updates the {@code studentTable} items.
     * On failure, displays an error alert.
     * </p>
     */
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

    /**
     * Fetches the grades for a specific student from the server asynchronously.
     *
     * @param studentId the unique identifier of the student whose grades are to be fetched.
     */
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

    // ============ USER ACTIONS ============
    /**
     * Handles the action of adding a new student.
     * <p>
     * Displays a dialog to input student details. Upon confirmation, sends a request to the server
     * to create the student. If successful, the student list is refreshed.
     * </p>
     */
    @FXML
    public void addStudentAction() {
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

    /**
     * Handles the action of adding a new grade for the currently selected student.
     * <p>
     * Displays a dialog to input the course name and grade value. Upon confirmation, sends a request
     * to the server to add the grade. If successful, the grades table is refreshed.
     * </p>
     */
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

    /**
     * Handles the action of deleting the selected student.
     * <p>
     * Prompts the user for confirmation before sending a removal request to the server.
     * Upon success, clears the grade table and refreshes the student list.
     * </p>
     */
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

    /**
     * Handles the action of deleting the selected grade.
     * <p>
     * Prompts the user for confirmation before sending a removal request to the server.
     * Upon success, refreshes the grades list for the current student.
     * </p>
     */
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

    /**
     * Navigates the user back to the main menu (Intro screen).
     *
     * @param event the ActionEvent triggered by the button click.
     * @throws IOException if the FXML file for the intro screen cannot be loaded.
     */
    public void goBackToMenuBtnRelease(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/intro.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}