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
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The controller class for the main management view of the client application.
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

    /** Buttons to trigger student or grade operations. */
    @FXML private Button deleteStudentBtn;
    @FXML private Button deleteGradeBtn;
    
    // === NOWE: Przycisk edycji ===
    @FXML private Button editGradeBtn;

    @FXML private Button avgGradeBtn;



    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        configureColumns();

        studentTable.setPlaceholder(new Label("Ładowanie danych..."));
        gradeTable.setPlaceholder(new Label("Wybierz studenta, aby zobaczyć oceny"));

        refreshStudentList();

        // Wiązanie widoczności przycisków
        deleteStudentBtn.visibleProperty().bind(studentTable.getSelectionModel().selectedItemProperty().isNotNull());
        
        // Zarówno usuwanie jak i edycja wymagają zaznaczenia konkretnej oceny
        deleteGradeBtn.visibleProperty().bind(gradeTable.getSelectionModel().selectedItemProperty().isNotNull());

        if (editGradeBtn != null) {
            editGradeBtn.visibleProperty().bind(gradeTable.getSelectionModel().selectedItemProperty().isNotNull());
        }

        if (avgGradeBtn != null) {
            avgGradeBtn.visibleProperty().bind(gradeTable.getSelectionModel().selectedItemProperty().isNotNull());
        }

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

    private void configureColumns() {
        firstnameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFirstName()));
        surnameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLastName()));
        indCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getIndexNumber()));

        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCourseName()));
        gradeCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getValue()));
    }

    // ============ SERVER DATA FETCHING ============

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
            styleDialog(alert);
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

    // ============ USER ACTIONS ============

    @FXML
    public void addStudentAction() {
        Dialog<StudentDTO> dialog = new Dialog<>();
        dialog.setTitle("Nowy Student");
        dialog.setHeaderText("Wprowadź dane studenta");

        ButtonType saveBtnType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField firstnameField = new TextField(); firstnameField.setPromptText("Imię");
        TextField surnameField = new TextField(); surnameField.setPromptText("Nazwisko");
        TextField indField = new TextField(); indField.setPromptText("123456");

        grid.add(new Label("Imię:"), 0, 0); grid.add(firstnameField, 1, 0);
        grid.add(new Label("Nazwisko:"), 0, 1); grid.add(surnameField, 1, 1);
        grid.add(new Label("Indeks:"), 0, 2); grid.add(indField, 1, 2);
        dialog.getDialogPane().setContent(grid);

        styleDialog(dialog);

        Node saveBtn = dialog.getDialogPane().lookupButton(saveBtnType);
        saveBtn.setDisable(true);
        saveBtn.disableProperty().bind(firstnameField.textProperty().isEmpty()
                .or(surnameField.textProperty().isEmpty())
                .or(indField.textProperty().isEmpty()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
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
            task.setOnFailed(e -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Błąd: " + e.getSource().getException().getMessage());
                styleDialog(errorAlert);
                errorAlert.show();
            });
            new Thread(task).start();
        });
    }

    @FXML
    public void addGradeAction() {
        StudentDTO selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Wybierz studenta!");
            styleDialog(alert);
            alert.show();
            return;
        }

        Dialog<GradeDTO> dialog = new Dialog<>();
        dialog.setTitle("Nowa Ocena");
        dialog.setHeaderText("Dodaj ocenę dla: " + selectedStudent.getFirstName());

        ButtonType saveBtnType = new ButtonType("Dodaj", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField nameField = new TextField(); nameField.setPromptText("Przedmiot");
        ComboBox<Integer> ocenaBox = new ComboBox<>();
        ocenaBox.getItems().addAll(2, 3, 4, 5);
        ocenaBox.setValue(3);

        grid.add(new Label("Przedmiot:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Ocena:"), 0, 1); grid.add(ocenaBox, 1, 1);
        dialog.getDialogPane().setContent(grid);

        styleDialog(dialog);

        Node saveBtn = dialog.getDialogPane().lookupButton(saveBtnType);
        saveBtn.setDisable(true);
        nameField.textProperty().addListener((o, oldVal, newVal) -> saveBtn.setDisable(newVal.trim().isEmpty()));

        dialog.setResultConverter(button -> {
            if (button == saveBtnType) {
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
            task.setOnSucceeded(e -> fetchGradesForStudent(selectedStudent.getId()));
            task.setOnFailed(e -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Błąd: " + e.getSource().getException().getMessage());
                styleDialog(errorAlert);
                errorAlert.show();
            });
            new Thread(task).start();
        });
    }

    // === NOWE: Akcja edycji oceny ===
    @FXML
    public void editGradeAction() {
        StudentDTO selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        GradeDTO selectedGrade = gradeTable.getSelectionModel().getSelectedItem();

        if (selectedStudent == null || selectedGrade == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Wybierz studenta i ocenę do edycji!");
            styleDialog(alert);
            alert.show();
            return;
        }

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Edycja Oceny");
        dialog.setHeaderText("Zmień ocenę z przedmiotu: " + selectedGrade.getCourseName());

        ButtonType saveBtnType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Integer> ocenaBox = new ComboBox<>();
        ocenaBox.getItems().addAll(2, 3, 4, 5);
        // Ustawienie aktualnej wartości
        ocenaBox.setValue(selectedGrade.getValue().intValue());

        grid.add(new Label("Nowa ocena:"), 0, 0);
        grid.add(ocenaBox, 1, 0);

        dialog.getDialogPane().setContent(grid);
        styleDialog(dialog);

        dialog.setResultConverter(button -> {
            if (button == saveBtnType) {
                return ocenaBox.getValue();
            }
            return null;
        });

        Optional<Integer> result = dialog.showAndWait();
        result.ifPresent(newGradeValue -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    ClientConnection.getService().updateGrade(
                            selectedStudent.getId(),
                            selectedGrade.getCourseName(),
                            newGradeValue
                    );
                    return null;
                }
            };

            task.setOnSucceeded(e -> {
                System.out.println("Zaktualizowano ocenę.");
                fetchGradesForStudent(selectedStudent.getId());
            });

            task.setOnFailed(e -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Błąd aktualizacji: " + e.getSource().getException().getMessage());
                styleDialog(errorAlert);
                errorAlert.show();
            });

            new Thread(task).start();
        });
    }

    @FXML
    public void deleteStudentAction() {
        StudentDTO selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Usunąć studenta " + selected.getLastName() + "?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Potwierdzenie usunięcia");
        alert.setTitle("Potwierdzenie");

        styleDialog(alert);

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
        alert.setHeaderText("Potwierdzenie usunięcia");

        styleDialog(alert);

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

                task.setOnFailed(e -> {
                    Throwable error = e.getSource().getException();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Nie udało się usunąć oceny:\n" + error.getMessage());
                    styleDialog(errorAlert);
                    errorAlert.show();
                });

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

    private void styleDialog(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();

        try {
            if (getClass().getResource("/style.css") != null) {
                dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
                dialogPane.getStyleClass().add("dialog-pane");
            }
        } catch (Exception e) {
            System.err.println("Błąd ładowania CSS: " + e.getMessage());
        }

        try {
            Stage stage = (Stage) dialogPane.getScene().getWindow();
            if (getClass().getResourceAsStream("/logo_square.jpg") != null) {
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo_square.jpg"))));
            }
        } catch (Exception e) {
            System.err.println("Błąd ładowania ikonki: " + e.getMessage());
        }

        for (ButtonType btnType : dialogPane.getButtonTypes()) {
            Node node = dialogPane.lookupButton(btnType);
            if (node instanceof Button) {
                Button btn = (Button) node;
                if (btnType.getButtonData() == ButtonBar.ButtonData.OK_DONE || btnType == ButtonType.YES) {
                    btn.getStyleClass().add("btn-success");
                    btn.setText("Potwierdź");
                } else if (btnType == ButtonType.CANCEL || btnType == ButtonType.NO || btnType == ButtonType.CLOSE) {
                    btn.getStyleClass().add("btn-danger");
                    btn.setText("Anuluj");
                }
            }
        }
    }

    @FXML
    public void calculateAverageAction() {

        GradeDTO selectedGrade = gradeTable.getSelectionModel().getSelectedItem();

        if (selectedGrade == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Wybierz ocenę/przedmiot z tabeli!");
            styleDialog(alert);
            alert.show();
            return;
        }

        String courseName = selectedGrade.getCourseName();

        Task<Double> task = new Task<>() {
            @Override
            protected Double call() throws Exception {
                return ClientConnection.getService().getAverageGradeForCourse(courseName);
            }
        };

        task.setOnSucceeded(e -> {
            double avg = task.getValue();
            String resultMsg = (avg == 0)
                    ? "Brak danych dla przedmiotu: " + courseName
                    : String.format("Średnia ocen wszystkich studentów z przedmiotu %s wynosi: %.2f", courseName, avg);

            Alert alert = new Alert(Alert.AlertType.INFORMATION, resultMsg);
            alert.setTitle("Średnia ocen");
            styleDialog(alert);
            alert.show();
        });

        task.setOnFailed(e -> {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Błąd: " + e.getSource().getException().getMessage());
            styleDialog(errorAlert);
            errorAlert.show();
        });

        new Thread(task).start();
    }
}