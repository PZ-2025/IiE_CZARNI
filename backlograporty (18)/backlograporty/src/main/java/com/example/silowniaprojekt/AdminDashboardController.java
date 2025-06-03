package com.example.silowniaprojekt;

import java.awt.Desktop;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Kontroler panelu administracyjnego dla systemu zarządzania siłownią.
 * Zarządza wszystkimi aspektami administracyjnymi aplikacji, w tym:
 * użytkownikami, treningami, produktami, sprzętem, karnetami i transakcjami.
 *
 * @author System zarządzania siłownią
 * @version 1.0
 */
public class AdminDashboardController {

    /** Identyfikator zalogowanego użytkownika */
    private int userId;

    /** Nazwa zalogowanego użytkownika */
    private String userName;

    /** Email administratora */
    private String adminEmail;

    // Sekcje interfejsu użytkownika
    /** Sekcja dashboard z podsumowaniem */
    @FXML private VBox dashboardSection;

    /** Sekcja zarządzania użytkownikami */
    @FXML private VBox usersSection;

    /** Sekcja zarządzania treningami */
    @FXML private VBox trainingsSection;

    /** Sekcja zarządzania sklepem */
    @FXML private VBox storeSection;

    /** Sekcja ustawień */
    @FXML private VBox settingsSection;

    /** Sekcja zarządzania sprzętem */
    @FXML private VBox equipmentSection;

    /** Sekcja raportów */
    @FXML private VBox reportsSection;

    /** Sekcja zarządzania karnetami */
    @FXML private VBox membershipsSection;

    /** Sekcja zarządzania transakcjami */
    @FXML private VBox transactionsSection;

    // Przyciski nawigacji
    @FXML private Button dashboardBtn;
    @FXML private Button usersBtn;
    @FXML private Button trainingsBtn;
    @FXML private Button storeBtn;
    @FXML private Button reportsBtn;
    @FXML private Button membershipsBtn;
    @FXML private Button transactionsBtn;

    // Komponenty dashboard
    /** Wykres aktywności klientów */
    @FXML private LineChart<String, Number> activityChart;

    // Tabele danych
    /** Tabela użytkowników */
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> userRoleColumn;

    /** Tabela treningów */
    @FXML private TableView<Training> trainingsTable;
    @FXML private TableColumn<Training, String> trainingDateColumn;
    @FXML private TableColumn<Training, String> trainingNotesColumn;
    @FXML private TableColumn<Training, String> trainingClientColumn;
    @FXML private TableColumn<Training, String> trainingTrainerColumn;

    /** Tabela produktów */
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, Double> productPriceColumn;
    @FXML private TableColumn<Product, Integer> productStockColumn;

    /** Tabela sprzętu */
    @FXML private TableView<Equipment> equipmentTable;
    @FXML private TableColumn<Equipment, String> equipmentNameColumn;
    @FXML private TableColumn<Equipment, Integer> equipmentQuantityColumn;
    @FXML private TableColumn<Equipment, String> equipmentStatusColumn;

    /** Tabela karnetów */
    @FXML private TableView<Membership> membershipsTable;
    @FXML private TableColumn<Membership, String> membershipClientColumn;
    @FXML private TableColumn<Membership, Double> membershipAmountColumn;
    @FXML private TableColumn<Membership, String> membershipDateColumn;

    /** Tabela transakcji */
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Integer> transactionIdColumn;
    @FXML private TableColumn<Transaction, String> transactionClientColumn;
    @FXML private TableColumn<Transaction, String> transactionProductColumn;
    @FXML private TableColumn<Transaction, String> transactionDateColumn;
    @FXML private TableColumn<Transaction, Double> transactionAmountColumn;

    // Kontrolki filtrowania raportów
    /** ComboBox do wyboru predefiniowanych okresów */
    @FXML private ComboBox<String> periodComboBox;

    /** DatePicker dla daty początkowej */
    @FXML private DatePicker startDatePicker;

    /** DatePicker dla daty końcowej */
    @FXML private DatePicker endDatePicker;

    /** RadioButton dla predefiniowanych okresów */
    @FXML private RadioButton predefinedPeriodsRadio;

    /** RadioButton dla niestandardowych dat */
    @FXML private RadioButton customDatesRadio;

    /** ComboBox do filtrowania produktów */
    @FXML private ComboBox<String> productFilterCombo;

    // Kolekcje danych
    /** Lista obserwowalna użytkowników */
    private ObservableList<User> users = FXCollections.observableArrayList();

    /** Lista obserwowalna treningów */
    private ObservableList<Training> trainings = FXCollections.observableArrayList();

    /** Lista obserwowalna produktów */
    private ObservableList<Product> products = FXCollections.observableArrayList();

    /** Lista obserwowalna sprzętu */
    private ObservableList<Equipment> equipment = FXCollections.observableArrayList();

    /** Lista obserwowalna karnetów */
    private ObservableList<Membership> memberships = FXCollections.observableArrayList();

    /** Lista obserwowalna transakcji */
    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    /**
     * Inicjalizuje kontroler po załadowaniu FXML.
     * Konfiguruje wykresy, tabele, ładuje dane z bazy danych i ustawia domyślny widok.
     */
    @FXML
    public void initialize() {
        initChart();
        initTables();
        loadDataFromDatabase();
        showDashboard();
        initializeDateFilters();
        initializeProductFilter();
    }

    /**
     * Ustawia dane zalogowanego użytkownika.
     *
     * @param userId identyfikator użytkownika
     * @param userName nazwa użytkownika
     * @param userEmail email użytkownika
     */
    public void setUserData(int userId, String userName, String userEmail) {
        this.userId = userId;
        this.userName = userName;
        this.adminEmail = userEmail;
    }

    /**
     * Inicjalizuje filtry dat dla sekcji raportów.
     * Konfiguruje ComboBox z predefiniowanymi okresami i DatePicker dla niestandardowych dat.
     */
    private void initializeDateFilters() {
        // Inicjalizacja periodComboBox
        if (periodComboBox != null) {
            periodComboBox.getItems().clear();
            periodComboBox.getItems().addAll(
                    "Ostatni tydzień", "Ostatni miesiąc", "Ostatni kwartał",
                    "Bieżący rok", "Wszystkie"
            );
            periodComboBox.setValue("Ostatni miesiąc");
        }

        if (predefinedPeriodsRadio != null && customDatesRadio != null) {
            // Grupa dla radio buttonów
            ToggleGroup dateFilterGroup = new ToggleGroup();
            predefinedPeriodsRadio.setToggleGroup(dateFilterGroup);
            customDatesRadio.setToggleGroup(dateFilterGroup);

            // Domyślnie wybrane predefiniowane okresy
            predefinedPeriodsRadio.setSelected(true);

            // Listener dla zmiany typu filtrowania
            dateFilterGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
                boolean isCustom = newToggle == customDatesRadio;
                if (periodComboBox != null) periodComboBox.setDisable(isCustom);
                if (startDatePicker != null) startDatePicker.setDisable(!isCustom);
                if (endDatePicker != null) endDatePicker.setDisable(!isCustom);
            });

            // Początkowo wyłącz date pickery
            if (startDatePicker != null) startDatePicker.setDisable(true);
            if (endDatePicker != null) endDatePicker.setDisable(true);
        }
    }

    /**
     * Inicjalizuje filtr produktów w sekcji raportów.
     * Ładuje nazwy produktów z bazy danych do ComboBox.
     */
    private void initializeProductFilter() {
        if (productFilterCombo != null) {
            try {
                List<String> productNames = ReportGenerator.getProductNames();
                productFilterCombo.getItems().addAll(productNames);
                productFilterCombo.getSelectionModel().selectFirst();
            } catch (SQLException e) {
                showError("Błąd ładowania produktów: " + e.getMessage());
            }
        }
    }

    /**
     * Inicjalizuje wszystkie tabele i ich kolumny.
     * Łączy kolumny z odpowiednimi właściwościami obiektów modelu.
     */
    private void initTables() {
        // Użytkownicy
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        usersTable.setItems(users);

        // Treningi
        trainingDateColumn.setCellValueFactory(new PropertyValueFactory<>("trainingDate"));
        trainingNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        trainingClientColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        trainingTrainerColumn.setCellValueFactory(new PropertyValueFactory<>("trainerName"));
        trainingsTable.setItems(trainings);

        // Produkty
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productStockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        productsTable.setItems(products);

        // Sprzęt
        equipmentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        equipmentQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        equipmentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        equipmentTable.setItems(equipment);

        // Karnety
        membershipClientColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        membershipAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        membershipDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        membershipsTable.setItems(memberships);

        // Transakcje
        transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        transactionClientColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        transactionProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        transactionsTable.setItems(transactions);
    }

    /**
     * Inicjalizuje wykres aktywności klientów na dashboard.
     * Dodaje przykładowe dane przedstawiające aktywność w ciągu tygodnia.
     */
    private void initChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Aktywność klientów");
        series.getData().addAll(
                new XYChart.Data<>("Pon", 65),
                new XYChart.Data<>("Wt", 59),
                new XYChart.Data<>("Śr", 80),
                new XYChart.Data<>("Czw", 81),
                new XYChart.Data<>("Pt", 56),
                new XYChart.Data<>("Sob", 55),
                new XYChart.Data<>("Nd", 40)
        );
        activityChart.getData().add(series);
    }

    /**
     * Ładuje wszystkie dane z bazy danych.
     * Wywołuje metody ładujące dane dla każdej sekcji aplikacji.
     */
    private void loadDataFromDatabase() {
        loadUsers();
        loadTrainings();
        loadProducts();
        loadEquipment();
        loadMemberships();
        loadTransactions();
    }

    /**
     * Ładuje użytkowników z bazy danych.
     * Pobiera wszystkich użytkowników i dodaje ich do listy obserwowalnej.
     */
    private void loadUsers() {
        users.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania użytkowników: " + e.getMessage());
        }
    }

    /**
     * Ładuje treningi z bazy danych.
     * Pobiera wszystkie żądania treningów i dodaje je do listy obserwowalnej.
     */
    private void loadTrainings() {
        trainings.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM trainingrequests");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                trainings.add(new Training(
                        rs.getInt("id"),
                        rs.getString("training_date"),
                        rs.getString("notes"),
                        "Jan Kowalski",
                        "Anna Nowak"
                ));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania treningów: " + e.getMessage());
        }
    }

    /**
     * Ładuje produkty z bazy danych.
     * Pobiera wszystkie produkty sklepu i dodaje je do listy obserwowalnej.
     */
    private void loadProducts() {
        products.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM products");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                ));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania produktów: " + e.getMessage());
        }
    }

    /**
     * Ładuje dane sprzętu.
     * Obecnie używa przykładowych danych - w przyszłości powinno pobierać z bazy danych.
     */
    private void loadEquipment() {
        equipment.clear();
        equipment.addAll(
                new Equipment(1, "Bieżnia", 5, "Dostępna"),
                new Equipment(2, "Hantle", 10, "W użyciu"),
                new Equipment(3, "Ławka", 3, "Dostępna")
        );
    }

    /**
     * Ładuje karnety z bazy danych.
     * Pobiera wszystkie płatności za karnety wraz z danymi klientów.
     */
    private void loadMemberships() {
        memberships.clear();
        String query = """
        SELECT mp.*, u.name AS client_name 
        FROM membership_payments mp
        JOIN users u ON mp.client_id = u.id
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                memberships.add(new Membership(
                        rs.getInt("id"),
                        rs.getString("client_name"),
                        rs.getDouble("amount"),
                        rs.getDate("payment_date").toString()
                ));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania płatności: " + e.getMessage());
        }
    }

    /**
     * Ładuje transakcje z bazy danych.
     * Pobiera wszystkie transakcje wraz z danymi klientów i produktów.
     */
    private void loadTransactions() {
        transactions.clear();
        String query = """
            SELECT t.id, u.name AS client_name, p.name AS product_name, 
                   t.transaction_date, t.amount 
            FROM transactions t
            JOIN users u ON t.client_id = u.id
            JOIN products p ON t.product_id = p.id
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                transactions.add(new Transaction(
                        rs.getInt("id"),
                        rs.getString("client_name"),
                        rs.getString("product_name"),
                        rs.getTimestamp("transaction_date").toLocalDateTime().toLocalDate().toString(),
                        rs.getDouble("amount")
                ));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania transakcji: " + e.getMessage());
        }
    }

    // Metody nawigacji

    /** Pokazuje sekcję dashboard */
    @FXML private void showDashboard() { toggleSection(dashboardSection, dashboardBtn); }

    /** Pokazuje sekcję użytkowników */
    @FXML private void showUsers() { toggleSection(usersSection, usersBtn); }

    /** Pokazuje sekcję treningów */
    @FXML private void showTrainings() { toggleSection(trainingsSection, trainingsBtn); }

    /** Pokazuje sekcję sklepu */
    @FXML private void showStore() { toggleSection(storeSection, storeBtn); }

    /** Pokazuje sekcję raportów */
    @FXML private void showReports() { toggleSection(reportsSection, reportsBtn); }

    /** Pokazuje sekcję karnetów */
    @FXML private void showMemberships() { toggleSection(membershipsSection, membershipsBtn); }

    /** Pokazuje sekcję transakcji */
    @FXML private void showTransactions() {toggleSection(transactionsSection, transactionsBtn);}

    /**
     * Przełącza widok na określoną sekcję i aktywuje odpowiedni przycisk.
     *
     * @param section sekcja do pokazania
     * @param button przycisk do oznaczenia jako aktywny
     */
    private void toggleSection(VBox section, Button button) {
        hideAllSections();
        section.setVisible(true);
        setActiveButton(button);
    }

    /**
     * Ukrywa wszystkie sekcje interfejsu użytkownika.
     */
    private void hideAllSections() {
        dashboardSection.setVisible(false);
        usersSection.setVisible(false);
        trainingsSection.setVisible(false);
        storeSection.setVisible(false);
        settingsSection.setVisible(false);
        equipmentSection.setVisible(false);
        reportsSection.setVisible(false);
        membershipsSection.setVisible(false);
        transactionsSection.setVisible(false);
    }

    /**
     * Ustawia przycisk jako aktywny i usuwa stan aktywny z innych przycisków.
     *
     * @param button przycisk do oznaczenia jako aktywny
     */
    private void setActiveButton(Button button) {
        dashboardBtn.getStyleClass().remove("active");
        usersBtn.getStyleClass().remove("active");
        trainingsBtn.getStyleClass().remove("active");
        storeBtn.getStyleClass().remove("active");
        reportsBtn.getStyleClass().remove("active");
        membershipsBtn.getStyleClass().remove("active");
        transactionsBtn.getStyleClass().remove("active");
        button.getStyleClass().add("active");
    }

    /**
     * Generuje raport finansowy w formacie PDF.
     * Pozwala użytkownikowi wybrać lokalizację zapisu pliku.
     */
    @FXML
    private void generateFinancialReport() {
        try {
            String period = getPeriodString();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Zapisz Raport Finansowy");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
            fileChooser.setInitialFileName("raport_finansowy_" + LocalDate.now() + ".pdf");

            Stage stage = (Stage) reportsSection.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                String userEmail = getCurrentUserEmail();
                ReportGenerator.generateFinancialReport(period, file, userEmail);
                showAlert("Sukces", "Raport finansowy został wygenerowany!");
            }
        } catch (Exception e) {
            showError("Błąd generowania raportu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generuje raport produktów w formacie PDF.
     * Uwzględnia filtr produktu jeśli został wybrany.
     */
    @FXML
    private void generateProductsReport() {
        try {
            String period = getPeriodString();
            String selectedProduct = productFilterCombo != null ?
                    productFilterCombo.getSelectionModel().getSelectedItem() : "Wszystkie";

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Zapisz Raport Produktów");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
            String fileName = selectedProduct != null && !selectedProduct.equals("Wszystkie") ?
                    "raport_produktow_" + selectedProduct.replaceAll("\\s+", "_") + "_" + LocalDate.now() + ".pdf" :
                    "raport_produktow_" + LocalDate.now() + ".pdf";
            fileChooser.setInitialFileName(fileName);

            Stage stage = (Stage) reportsSection.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                String userEmail = getCurrentUserEmail();
                ReportGenerator.generateProductsReport(period, file, userEmail, selectedProduct);
                showAlert("Sukces", "Raport produktów został wygenerowany!");
            }
        } catch (Exception e) {
            showError("Błąd generowania raportu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generuje raport karnetów w formacie PDF.
     */
    @FXML
    private void generateMembershipReport() {
        try {
            String period = getPeriodString();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Zapisz Raport Karnetów");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
            fileChooser.setInitialFileName("raport_karnetow_" + LocalDate.now() + ".pdf");

            Stage stage = (Stage) reportsSection.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                String userEmail = getCurrentUserEmail();
                ReportGenerator.generateMembershipReport(period, file, userEmail);
                showAlert("Sukces", "Raport karnetów został wygenerowany!");
            }
        } catch (Exception e) {
            showError("Błąd generowania raportu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generuje raport transakcji w formacie PDF.
     */
    @FXML
    private void generateTransactionsReport() {
        try {
            String period = getPeriodString();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Zapisz Raport Transakcji");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
            fileChooser.setInitialFileName("raport_transakcji_" + LocalDate.now() + ".pdf");

            Stage stage = (Stage) reportsSection.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                String userEmail = getCurrentUserEmail();
                ReportGenerator.generateTransactionsReport(period, file, userEmail);
                showAlert("Sukces", "Raport transakcji został wygenerowany!");
            }
        } catch (Exception e) {
            showError("Błąd generowania raportu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Pobiera string reprezentujący wybrany okres czasowy dla raportów.
     *
     * @return string okresu w formacie odpowiednim dla generatora raportów
     * @throws IllegalArgumentException gdy daty są nieprawidłowe
     */
    private String getPeriodString() {
        if (customDatesRadio != null && customDatesRadio.isSelected()) {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Wybierz daty początkową i końcową");
            }

            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Data początkowa nie może być późniejsza niż końcowa");
            }

            return startDate.toString() + ":" + endDate.toString();
        } else {
            String selectedPeriod = periodComboBox != null ?
                    periodComboBox.getSelectionModel().getSelectedItem() : "Ostatni miesiąc";
            if (selectedPeriod == null) {
                selectedPeriod = "Ostatni miesiąc";
            }
            return selectedPeriod;
        }
    }

    /**
     * Pobiera email bieżącego użytkownika.
     *
     * @return email użytkownika lub domyślny email administratora
     */
    private String getCurrentUserEmail() {
        return this.adminEmail != null ? this.adminEmail : "admin@blackirongym.com";
    }

    /**
     * Wyświetla alert błędu.
     *
     * @param message treść komunikatu błędu
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Wyświetla alert informacyjny.
     *
     * @param title tytuł alertu
     * @param content treść alertu
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Wylogowuje użytkownika i powraca do ekranu logowania.
     */
    @FXML
    private void logout() {
        try {
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            stage.setScene(new Scene(root, 400, 300));
            stage.setTitle("Logowanie");
        } catch (IOException e) {
            showAlert("Błąd", "Błąd podczas wylogowywania: " + e.getMessage());
        }
    }

    /**
     * Otwiera dialog dodawania nowego użytkownika.
     * Pozwala wprowadzić dane i zapisać użytkownika w bazie danych.
     */
    @FXML
    private void addUser() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Dodaj nowego użytkownika");
        dialog.setHeaderText("Wprowadź dane nowego użytkownika");

        ButtonType addButton = new ButtonType("Dodaj", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Imię i nazwisko");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Hasło");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("client", "trainer", "employee", "admin");
        roleBox.setValue("client");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Imię i nazwisko:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Hasło:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Rola:"), 0, 3);
        grid.add(roleBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                return new User(0, nameField.getText(), emailField.getText(), roleBox.getValue());
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();

        result.ifPresent(user -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn != null) {
                    String query = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                    stmt.setString(1, user.getName());
                    stmt.setString(2, user.getEmail());
                    stmt.setString(3, passwordField.getText());
                    stmt.setString(4, user.getRole());

                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows > 0) {
                        ResultSet rs = stmt.getGeneratedKeys();
                        if (rs.next()) {
                            user.setId(rs.getInt(1));
                        }
                        users.add(user);
                        showAlert("Sukces", "Użytkownik został dodany.");
                    }
                }
            } catch (SQLException e) {
                System.err.println("Błąd podczas dodawania użytkownika: " + e.getMessage());
                user.setId(users.size() + 1);
                users.add(user);
                showAlert("Uwaga", "Użytkownik został dodany lokalnie, ale wystąpił problem z bazą danych.");
            }
        });
    }

    /**
     * Otwiera dialog edycji wybranego użytkownika.
     * Pozwala na modyfikację danych użytkownika w bazie danych.
     */
    @FXML
    private void editUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Błąd", "Wybierz użytkownika do edycji");
            return;
        }

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edytuj użytkownika");
        dialog.setHeaderText("Edytuj dane użytkownika");

        ButtonType saveButton = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField nameField = new TextField(selectedUser.getName());
        TextField emailField = new TextField(selectedUser.getEmail());
        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList(
                "client", "trainer", "employee", "admin"
        ));
        roleBox.setValue(selectedUser.getRole());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Imię i nazwisko:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Rola:"), 0, 2);
        grid.add(roleBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                return new User(
                        selectedUser.getId(),
                        nameField.getText(),
                        emailField.getText(),
                        roleBox.getValue()
                );
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();

        result.ifPresent(editedUser -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE users SET name = ?, email = ?, role = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, editedUser.getName());
                stmt.setString(2, editedUser.getEmail());
                stmt.setString(3, editedUser.getRole());
                stmt.setInt(4, editedUser.getId());

                if (stmt.executeUpdate() > 0) {
                    selectedUser.setName(editedUser.getName());
                    selectedUser.setEmail(editedUser.getEmail());
                    selectedUser.setRole(editedUser.getRole());
                    usersTable.refresh();
                    showAlert("Sukces", "Dane użytkownika zostały zaktualizowane");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Nie udało się zaktualizować użytkownika: " + e.getMessage());
            }
        });
    }

    /**
     * Usuwa wybranego użytkownika po potwierdzeniu.
     * Wyświetla dialog potwierdzenia przed usunięciem.
     */
    @FXML
    private void deleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Błąd", "Wybierz użytkownika do usunięcia");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Potwierdzenie usunięcia");
        confirmation.setHeaderText("Czy na pewno chcesz usunąć użytkownika " + selectedUser.getName() + "?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM users WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, selectedUser.getId());

                if (stmt.executeUpdate() > 0) {
                    users.remove(selectedUser);
                    showAlert("Sukces", "Użytkownik został usunięty");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Nie udało się usunąć użytkownika: " + e.getMessage());
            }
        }
    }

    /**
     * Otwiera dialog dodawania nowego produktu.
     * Pozwala wprowadzić dane produktu i zapisać go w bazie danych.
     */
    @FXML
    private void addProduct() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Dodaj nowy produkt");
        dialog.setHeaderText("Wprowadź dane nowego produktu");

        ButtonType addButton = new ButtonType("Dodaj", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Nazwa produktu");

        TextField priceField = new TextField();
        priceField.setPromptText("Cena");

        TextField stockField = new TextField();
        stockField.setPromptText("Ilość w magazynie");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nazwa:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Cena:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Ilość:"), 0, 2);
        grid.add(stockField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                try {
                    double price = Double.parseDouble(priceField.getText().replace(",", "."));
                    int stock = Integer.parseInt(stockField.getText());
                    return new Product(0, nameField.getText(), price, stock);
                } catch (NumberFormatException e) {
                    showAlert("Błąd", "Nieprawidłowy format liczby");
                    return null;
                }
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();

        result.ifPresent(product -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn != null) {
                    String query = "INSERT INTO products (name, price, stock) VALUES (?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                    stmt.setString(1, product.getName());
                    stmt.setDouble(2, product.getPrice());
                    stmt.setInt(3, product.getStock());

                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows > 0) {
                        ResultSet rs = stmt.getGeneratedKeys();
                        if (rs.next()) {
                            product.setId(rs.getInt(1));
                        }
                        products.add(product);
                        showAlert("Sukces", "Produkt został dodany.");
                    }
                }
            } catch (SQLException e) {
                System.err.println("Błąd podczas dodawania produktu: " + e.getMessage());
                product.setId(products.size() + 1);
                products.add(product);
                showAlert("Uwaga", "Produkt został dodany lokalnie, ale wystąpił problem z bazą danych.");
            }
        });
    }

    /**
     * Otwiera dialog zmiany hasła dla wybranego użytkownika.
     * Waliduje nowe hasło i aktualizuje je w bazie danych z hashowaniem.
     */
    @FXML
    private void changePassword() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Błąd", "Wybierz użytkownika do zmiany hasła");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Zmiana hasła");
        dialog.setHeaderText("Zmiana hasła dla: " + selectedUser.getName());

        ButtonType changeButton = new ButtonType("Zmień", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButton, ButtonType.CANCEL);

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nowe hasło (min. 8 znaków)");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Potwierdź hasło");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nowe hasło:"), 0, 0);
        grid.add(newPasswordField, 1, 0);
        grid.add(new Label("Potwierdź hasło:"), 0, 1);
        grid.add(confirmPasswordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Node changeButtonNode = dialog.getDialogPane().lookupButton(changeButton);
        changeButtonNode.setDisable(true);

        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            boolean isValid = newPassword.length() >= 8 &&
                    newPassword.equals(confirmPassword);

            changeButtonNode.setDisable(!isValid);
        });

        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            boolean isValid = newPassword.length() >= 8 &&
                    newPassword.equals(confirmPassword);

            changeButtonNode.setDisable(!isValid);
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButton) {
                return newPasswordField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newPassword -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE users SET password = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);

                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                stmt.setString(1, hashedPassword);
                stmt.setInt(2, selectedUser.getId());

                if (stmt.executeUpdate() > 0) {
                    showAlert("Sukces", "Hasło zostało zmienione");
                } else {
                    showAlert("Błąd", "Nie udało się zmienić hasła");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Błąd bazy danych: " + e.getMessage());
            }
        });
    }

    /**
     * Otwiera dialog zmiany daty treningu.
     * Pozwala administratorowi zmienić datę wybranego treningu.
     */
    @FXML
    private void changeTrainingDate() {
        Training selectedTraining = trainingsTable.getSelectionModel().getSelectedItem();
        if (selectedTraining == null) {
            showAlert("Błąd", "Wybierz trening do zmiany daty");
            return;
        }

        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Zmiana daty treningu");
        dialog.setHeaderText("Zmiana daty treningu dla: " + selectedTraining.getClientName());

        ButtonType saveButton = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.parse(selectedTraining.getTrainingDate()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nowa data:"), 0, 0);
        grid.add(datePicker, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                return datePicker.getValue();
            }
            return null;
        });

        Optional<LocalDate> result = dialog.showAndWait();

        result.ifPresent(newDate -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE trainingrequests SET training_date = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, newDate.toString());
                stmt.setInt(2, selectedTraining.getId());

                if (stmt.executeUpdate() > 0) {
                    selectedTraining.setTrainingDate(newDate.toString());
                    trainingsTable.refresh();
                    showAlert("Sukces", "Data treningu została zmieniona");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Nie udało się zaktualizować daty: " + e.getMessage());
            }
        });
    }

    /**
     * Otwiera dialog edycji notatek treningu.
     * Pozwala administratorowi modyfikować notatki do wybranego treningu.
     */
    @FXML
    private void editTrainingNotes() {
        Training selectedTraining = trainingsTable.getSelectionModel().getSelectedItem();
        if (selectedTraining == null) {
            showAlert("Błąd", "Wybierz trening do edycji notatek");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edycja notatek");
        dialog.setHeaderText("Edycja notatek dla treningu: " + selectedTraining.getClientName());

        ButtonType saveButton = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextArea notesArea = new TextArea(selectedTraining.getNotes());
        notesArea.setPrefRowCount(5);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Notatki:"), 0, 0);
        grid.add(notesArea, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                return notesArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newNotes -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE trainingrequests SET notes = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, newNotes);
                stmt.setInt(2, selectedTraining.getId());

                if (stmt.executeUpdate() > 0) {
                    selectedTraining.setNotes(newNotes);
                    trainingsTable.refresh();
                    showAlert("Sukces", "Notatki zostały zaktualizowane");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Nie udało się zaktualizować notatek: " + e.getMessage());
            }
        });
    }

    /**
     * Otwiera dialog zmiany trenera dla wybranego treningu.
     * Ładuje listę dostępnych trenerów z bazy danych.
     */
    @FXML
    private void changeTrainingTrainer() {
        Training selectedTraining = trainingsTable.getSelectionModel().getSelectedItem();
        if (selectedTraining == null) {
            showAlert("Błąd", "Wybierz trening do zmiany trenera");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Zmiana trenera");
        dialog.setHeaderText("Zmiana trenera dla: " + selectedTraining.getClientName());

        ButtonType saveButton = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        ComboBox<String> trainerComboBox = new ComboBox<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM users WHERE role = 'trainer'");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                trainerComboBox.getItems().add(rs.getString("name"));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania trenerów: " + e.getMessage());
        }

        trainerComboBox.setValue(selectedTraining.getTrainerName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nowy trener:"), 0, 0);
        grid.add(trainerComboBox, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                return trainerComboBox.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newTrainer -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "SELECT id FROM users WHERE name = ? AND role = 'trainer'";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, newTrainer);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int trainerId = rs.getInt("id");

                    String updateQuery = "UPDATE trainingrequests SET trainer_id = ? WHERE id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setInt(1, trainerId);
                    updateStmt.setInt(2, selectedTraining.getId());

                    if (updateStmt.executeUpdate() > 0) {
                        selectedTraining.setTrainerName(newTrainer);
                        trainingsTable.refresh();
                        showAlert("Sukces", "Trener został zmieniony");
                    }
                } else {
                    showAlert("Błąd", "Nie znaleziono trenera o podanej nazwie");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Nie udało się zmienić trenera: " + e.getMessage());
            }
        });
    }

    /**
     * Otwiera dialog zmiany kwoty karnetu.
     * Pozwala administratorowi zmodyfikować kwotę płatności za karnet.
     */
    @FXML
    private void changeMembershipAmount() {
        Membership selectedMembership = membershipsTable.getSelectionModel().getSelectedItem();
        if (selectedMembership == null) {
            showAlert("Błąd", "Wybierz karnet do zmiany kwoty");
            return;
        }

        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Zmiana kwoty karnetu");
        dialog.setHeaderText("Zmiana kwoty karnetu dla: " + selectedMembership.getClientName());

        ButtonType saveButton = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField amountField = new TextField(String.valueOf(selectedMembership.getAmount()));
        amountField.setPromptText("Nowa kwota");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nowa kwota:"), 0, 0);
        grid.add(amountField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                try {
                    return Double.parseDouble(amountField.getText());
                } catch (NumberFormatException e) {
                    showAlert("Błąd", "Nieprawidłowy format kwoty");
                    return null;
                }
            }
            return null;
        });

        Optional<Double> result = dialog.showAndWait();

        result.ifPresent(newAmount -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE membership_payments SET amount = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setDouble(1, newAmount);
                stmt.setInt(2, selectedMembership.getId());

                if (stmt.executeUpdate() > 0) {
                    selectedMembership.setAmount(newAmount);
                    membershipsTable.refresh();
                    showAlert("Sukces", "Kwota karnetu została zmieniona");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Nie udało się zmienić kwoty: " + e.getMessage());
            }
        });
    }

    /**
     * Otwiera dialog zmiany daty karnetu.
     * Pozwala administratorowi zmodyfikować datę płatności za karnet.
     */
    @FXML
    private void changeMembershipDate() {
        Membership selectedMembership = membershipsTable.getSelectionModel().getSelectedItem();
        if (selectedMembership == null) {
            showAlert("Błąd", "Wybierz karnet do zmiany daty");
            return;
        }

        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Zmiana daty karnetu");
        dialog.setHeaderText("Zmiana daty karnetu dla: " + selectedMembership.getClientName());

        ButtonType saveButton = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.parse(selectedMembership.getPaymentDate()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nowa data:"), 0, 0);
        grid.add(datePicker, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                return datePicker.getValue();
            }
            return null;
        });

        Optional<LocalDate> result = dialog.showAndWait();

        result.ifPresent(newDate -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE membership_payments SET payment_date = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setDate(1, java.sql.Date.valueOf(newDate));
                stmt.setInt(2, selectedMembership.getId());

                if (stmt.executeUpdate() > 0) {
                    selectedMembership.setPaymentDate(newDate.toString());
                    membershipsTable.refresh();
                    showAlert("Sukces", "Data karnetu została zmieniona");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Nie udało się zmienić daty: " + e.getMessage());
            }
        });
    }

    /**
     * Klasa modelu reprezentująca użytkownika systemu.
     * Zawiera podstawowe informacje o użytkowniku z obsługą JavaFX Properties.
     */
    public static class User {
        /** Identyfikator użytkownika */
        private final SimpleIntegerProperty id;

        /** Nazwa użytkownika */
        private final SimpleStringProperty name;

        /** Email użytkownika */
        private final SimpleStringProperty email;

        /** Rola użytkownika (client, trainer, employee, admin) */
        private final SimpleStringProperty role;

        /**
         * Konstruktor użytkownika.
         *
         * @param id identyfikator użytkownika
         * @param name nazwa użytkownika
         * @param email email użytkownika
         * @param role rola użytkownika
         */
        public User(int id, String name, String email, String role) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
            this.role = new SimpleStringProperty(role);
        }

        // Gettery
        public int getId() { return id.get(); }
        public String getName() { return name.get(); }
        public String getEmail() { return email.get(); }
        public String getRole() { return role.get(); }

        // Settery
        public void setId(int value) { id.set(value); }
        public void setName(String value) { name.set(value); }
        public void setEmail(String value) { email.set(value); }
        public void setRole(String value) { role.set(value); }

        // Properties dla JavaFX
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty roleProperty() { return role; }
    }

    /**
     * Klasa modelu reprezentująca trening.
     * Zawiera informacje o treningu z obsługą JavaFX Properties.
     */
    public static class Training {
        /** Identyfikator treningu */
        private final SimpleIntegerProperty id;

        /** Data treningu */
        private final SimpleStringProperty trainingDate;

        /** Notatki do treningu */
        private final SimpleStringProperty notes;

        /** Nazwa klienta */
        private final SimpleStringProperty clientName;

        /** Nazwa trenera */
        private final SimpleStringProperty trainerName;

        /**
         * Konstruktor treningu.
         *
         * @param id identyfikator treningu
         * @param trainingDate data treningu
         * @param notes notatki do treningu
         * @param clientName nazwa klienta
         * @param trainerName nazwa trenera
         */
        public Training(int id, String trainingDate, String notes, String clientName, String trainerName) {
            this.id = new SimpleIntegerProperty(id);
            this.trainingDate = new SimpleStringProperty(trainingDate);
            this.notes = new SimpleStringProperty(notes);
            this.clientName = new SimpleStringProperty(clientName);
            this.trainerName = new SimpleStringProperty(trainerName);
        }

        // Gettery
        public int getId() { return id.get(); }
        public String getTrainingDate() { return trainingDate.get(); }
        public String getNotes() { return notes.get(); }
        public String getClientName() { return clientName.get(); }
        public String getTrainerName() { return trainerName.get(); }

        // Settery
        public void setTrainingDate(String trainingDate) { this.trainingDate.set(trainingDate); }
        public void setNotes(String notes) { this.notes.set(notes); }
        public void setTrainerName(String trainerName) { this.trainerName.set(trainerName); }
    }

    /**
     * Klasa modelu reprezentująca produkt sklepu.
     * Zawiera informacje o produkcie z obsługą JavaFX Properties.
     */
    public static class Product {
        /** Identyfikator produktu */
        private final SimpleIntegerProperty id;

        /** Nazwa produktu */
        private final SimpleStringProperty name;

        /** Cena produktu */
        private final SimpleDoubleProperty price;

        /** Stan magazynowy */
        private final SimpleIntegerProperty stock;

        /**
         * Konstruktor produktu.
         *
         * @param id identyfikator produktu
         * @param name nazwa produktu
         * @param price cena produktu
         * @param stock stan magazynowy
         */
        public Product(int id, String name, double price, int stock) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
            this.stock = new SimpleIntegerProperty(stock);
        }

        // Gettery
        public int getId() { return id.get(); }
        public String getName() { return name.get(); }
        public double getPrice() { return price.get(); }
        public int getStock() { return stock.get(); }

        // Settery
        public void setId(int value) { id.set(value); }
        public void setName(String value) { name.set(value); }
        public void setPrice(double value) { price.set(value); }
        public void setStock(int value) { stock.set(value); }

        // Properties dla JavaFX
        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleDoubleProperty priceProperty() { return price; }
        public SimpleIntegerProperty stockProperty() { return stock; }
    }

    /**
     * Klasa modelu reprezentująca sprzęt siłowni.
     * Zawiera informacje o sprzęcie z obsługą JavaFX Properties.
     */
    public static class Equipment {
        /** Identyfikator sprzętu */
        private final SimpleIntegerProperty id;

        /** Nazwa sprzętu */
        private final SimpleStringProperty name;

        /** Ilość sprzętu */
        private final SimpleIntegerProperty quantity;

        /** Status sprzętu */
        private final SimpleStringProperty status;

        /**
         * Konstruktor sprzętu.
         *
         * @param id identyfikator sprzętu
         * @param name nazwa sprzętu
         * @param quantity ilość sprzętu
         * @param status status sprzętu
         */
        public Equipment(int id, String name, int quantity, String status) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.status = new SimpleStringProperty(status);
        }

        // Gettery
        public int getId() { return id.get(); }
        public String getName() { return name.get(); }
        public int getQuantity() { return quantity.get(); }
        public String getStatus() { return status.get(); }
    }

    /**
     * Klasa modelu reprezentująca karnet.
     * Zawiera informacje o płatności za karnet z obsługą JavaFX Properties.
     */
    public static class Membership {
        /** Identyfikator karnetu */
        private final SimpleIntegerProperty id;

        /** Nazwa klienta */
        private final SimpleStringProperty clientName;

        /** Kwota płatności */
        private final SimpleDoubleProperty amount;

        /** Data płatności */
        private final SimpleStringProperty paymentDate;

        /**
         * Konstruktor karnetu.
         *
         * @param id identyfikator karnetu
         * @param clientName nazwa klienta
         * @param amount kwota płatności
         * @param paymentDate data płatności
         */
        public Membership(int id, String clientName, double amount, String paymentDate) {
            this.id = new SimpleIntegerProperty(id);
            this.clientName = new SimpleStringProperty(clientName);
            this.amount = new SimpleDoubleProperty(amount);
            this.paymentDate = new SimpleStringProperty(paymentDate);
        }

        // Gettery
        public int getId() { return id.get(); }
        public String getClientName() { return clientName.get(); }
        public double getAmount() { return amount.get(); }
        public String getPaymentDate() { return paymentDate.get(); }

        // Settery
        public void setAmount(double amount) { this.amount.set(amount); }
        public void setPaymentDate(String paymentDate) { this.paymentDate.set(paymentDate); }
    }

    /**
     * Klasa modelu reprezentująca transakcję.
     * Zawiera informacje o transakcji sklepowej z obsługą JavaFX Properties.
     */
    public static class Transaction {
        /** Identyfikator transakcji */
        private final SimpleIntegerProperty id;

        /** Nazwa klienta */
        private final SimpleStringProperty clientName;

        /** Nazwa produktu */
        private final SimpleStringProperty productName;

        /** Data transakcji */
        private final SimpleStringProperty transactionDate;

        /** Kwota transakcji */
        private final SimpleDoubleProperty amount;

        /**
         * Konstruktor transakcji.
         *
         * @param id identyfikator transakcji
         * @param clientName nazwa klienta
         * @param productName nazwa produktu
         * @param transactionDate data transakcji
         * @param amount kwota transakcji
         */
        public Transaction(int id, String clientName, String productName, String transactionDate, double amount) {
            this.id = new SimpleIntegerProperty(id);
            this.clientName = new SimpleStringProperty(clientName);
            this.productName = new SimpleStringProperty(productName);
            this.transactionDate = new SimpleStringProperty(transactionDate);
            this.amount = new SimpleDoubleProperty(amount);
        }

        // Gettery
        public int getId() { return id.get(); }
        public String getClientName() { return clientName.get(); }
        public String getProductName() { return productName.get(); }
        public String getTransactionDate() { return transactionDate.get(); }
        public double getAmount() { return amount.get(); }
    }
}