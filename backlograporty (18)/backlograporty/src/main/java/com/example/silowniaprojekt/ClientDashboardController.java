package com.example.silowniaprojekt;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.time.format.DateTimeFormatter; // Dodane dla formatowania daty/czasu

/**
 * Kontroler panelu klienta siłowni.
 * Zarządza funkcjami dostępnymi dla klienta, takimi jak przeglądanie aktywności,
 * zaplanowanych treningów, zarządzanie zadaniami, składanie zapytań o treningi personalne.
 * Implementuje logikę dla wszystkich działań dostępnych w interfejsie panelu klienta.
 */
public class ClientDashboardController {

    // Sekcje
    @FXML private VBox homeSection;
    @FXML private VBox profileSection; // Zostawiamy, ale będzie wyświetlać tylko zadania trenera
    @FXML private VBox membershipSection;
    @FXML private VBox trainingsSection; // This was the old one for "Nasi Trenerzy", will be repurposed for the new form
    @FXML private VBox shopSection;
    @FXML private VBox scheduleSection; // TUTAJ ZMIANA: będzie wyświetlać ZAPLANOWANE TRENINGI
    @FXML private VBox clientTransactionsSection; // For "Twoje Transakcje"
    @FXML private VBox clientTrainingsRequestSection; // For "Zapisz się na Trening"
    @FXML private VBox changePasswordSection;


    // Panel sklepu
    @FXML private Label cartCounterLabel;

    // Panel harmonogramu (TERAZ ZAPLANOWANE TRENINGI KLIENTA)
    @FXML private TableView<ClientScheduledTraining> scheduleTable; // Zmieniono typ
    @FXML private TableColumn<ClientScheduledTraining, String> scheduledTrainingTrainerColumn; // Trener
    @FXML private TableColumn<ClientScheduledTraining, LocalDateTime> scheduledTrainingDateColumn; // Data i czas treningu
    @FXML private TableColumn<ClientScheduledTraining, String> scheduledTrainingNotesColumn; // Notatki (z requestu)
    // USUNIĘTO: @FXML private TableColumn<ClientScheduledTraining, String> scheduledTrainingStatusColumn;


    // Panel historii transakcji i płatności
    @FXML private TableView<ClientActivity> clientActivitiesTable;
    @FXML private TableColumn<ClientActivity, String> activityTypeColumn;
    @FXML private TableColumn<ClientActivity, String> activityDescriptionColumn;
    @FXML private TableColumn<ClientActivity, BigDecimal> activityAmountColumn;
    @FXML private TableColumn<ClientActivity, Timestamp> activityDateColumn;
    @FXML private TableColumn<ClientActivity, String> activityStatusColumn;

    @FXML private TableView<ClientTask> clientTasksTable;
    @FXML private TableColumn<ClientTask, Integer> taskIdColumn;
    @FXML private TableColumn<ClientTask, java.time.LocalDate> taskDateColumn;
    @FXML private TableColumn<ClientTask, String> taskDescriptionColumn;
    @FXML private TableColumn<ClientTask, String> taskPriorityColumn;
    @FXML private TableColumn<ClientTask, String> taskStatusColumn;
    @FXML private TableColumn<ClientTask, String> taskTrainerNameColumn;

    // FXML dla szczegółów wybranego zadania w profilu
    @FXML private Label selectedTaskDateLabel;
    @FXML private Label selectedTaskPriorityLabel;
    @FXML private Label selectedTaskStatusLabel;
    @FXML private TextArea selectedTaskDescriptionArea;
    @FXML private Label selectedTaskTrainerLabel;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label passwordChangeStatus;
    
    // ObservableLists for TableViews/ComboBoxes
    private ObservableList<ClientTrainingRequest> clientTrainingRequestsData = FXCollections.observableArrayList();
    private ObservableList<ClientActivity> clientActivitiesData = FXCollections.observableArrayList();
    private ObservableList<Trainer> trainerList = FXCollections.observableArrayList();
    private ObservableList<ClientTask> clientTasksObservableList = FXCollections.observableArrayList();
    private ObservableList<Priority> prioritiesList = FXCollections.observableArrayList();
    private ObservableList<Status> statusesList = FXCollections.observableArrayList();
    private ObservableList<ClientScheduledTraining> clientScheduledTrainingsData = FXCollections.observableArrayList(); // NOWA LISTA

    // Panel zapisu na trening
    @FXML private ComboBox<Trainer> trainerComboBox;
    @FXML private TextArea trainingNotesTextArea;

    // Table for client's training requests (NEW)
    @FXML private TableView<ClientTrainingRequest> clientTrainingRequestsTable;
    @FXML private TableColumn<ClientTrainingRequest, String> trainerNameColumn;
    @FXML private TableColumn<ClientTrainingRequest, String> requestNotesColumn;
    @FXML private TableColumn<ClientTrainingRequest, java.sql.Date> trainingDateColumn;
    @FXML private TableColumn<ClientTrainingRequest, String> trainingStatusColumn;

    // Dane użytkownika
    private int userId;
    private String userName;
    private String userEmail;

    // Koszyk
    private int cartItemCount = 0;


    /**
     * Inicjalizuje kontroler panelu klienta.
     * Metoda wywoływana automatycznie przez JavaFX po załadowaniu widoku FXML.
     * Inicjalizuje wszystkie główne komponenty UI, w tym tabele z zadaniami, 
     * zaplanowanymi treningami, aktywnościami klienta oraz formularz zapytania o trening.
     * Ustawia sekcję startową (Home) jako domyślnie widoczną.
     */
    @FXML
    public void initialize() {
        initializeProfile(); // To inicjalizuje tylko tabelę zadań trenera
        initializeSchedule(); // TUTAJ ZMIANA: inicjalizacja dla zaplanowanych treningów
        initializeClientActivities();
        initializeClientTrainingsRequest();

        // Ustawienie sekcji startowej jako aktywnej
        showHome();
    }
    
    /**
     * Ustawia dane użytkownika w kontrolerze i ładuje dane powiązane z użytkownikiem.
     * Ta metoda powinna być wywołana po inicjalizacji kontrolera, aby ustawić
     * identyfikator, nazwę i email zalogowanego klienta.
     * 
     * @param userId identyfikator użytkownika w bazie danych
     * @param userName nazwa użytkownika (imię i nazwisko)
     * @param userEmail adres email użytkownika
     */
    public void setUserData(int userId, String userName, String userEmail) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;

        loadPrioritiesAndStatuses();
        loadClientActivities(); // Załaduj aktywności klienta przy ustawieniu danych użytkownika
        loadTrainers();
        loadClientTasks();
        // loadClientScheduledTrainings(); // Możesz załadować tutaj, ale lepiej w showSchedule()
    }

    // --- INITIALIZATION METHODS ---

    /**
     * Ładuje listę priorytetów i statusów z bazy danych.
     * Te listy są używane do kategoryzacji i filtrowania zadań klienta
     * oraz do wyświetlania w interfejsie użytkownika.
     * Metoda pobiera dane z tabel 'priorities' i 'statuses' w bazie danych.
     */
    private void loadPrioritiesAndStatuses() {
        prioritiesList.clear();
        statusesList.clear();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sqlPriorities = "SELECT priority_id AS id, priority_name AS name FROM priorities ORDER BY priority_id";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlPriorities);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    prioritiesList.add(new Priority(rs.getInt("id"), rs.getString("name")));
                }
            }

            String sqlStatuses = "SELECT status_id AS id, status_name AS name FROM statuses ORDER BY status_id";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlStatuses);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    statusesList.add(new Status(rs.getInt("id"), rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            showError("Błąd ładowania priorytetów/statusów: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Ładuje zadania przypisane klientowi z bazy danych.
     * Pobiera wszystkie zadania, które trenerzy przypisali do tego klienta,
     * wraz z ich szczegółami, takimi jak priorytet, status, data treningu i opis.
     * Zadania są sortowane od najnowszych do najstarszych według daty treningu.
     */
    private void loadClientTasks() {
        if (userId == 0) {
            System.err.println("Błąd: userId nie ustawione w ClientDashboardController dla loadClientTasks.");
            return;
        }
    
        clientTasksObservableList.clear();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT tt.task_id, tr.id AS training_request_id, tr.training_date, tt.task_description, " +
                    "p.priority_id, p.priority_name, s.status_id, s.status_name, " +
                    "t.id AS trainer_id, t.name AS trainer_name " +
                    "FROM trainer_tasks tt " +
                    "JOIN trainingrequests tr ON tt.training_request_id = tr.id " +
                    "JOIN reports r ON tr.report = r.id " +
                    "JOIN users c ON r.client_id = c.id " +
                    "JOIN users t ON r.trainer_id = t.id " +
                    "JOIN priorities p ON tt.priority_id = p.priority_id " +
                    "JOIN statuses s ON tt.status_id = s.status_id " +
                    "WHERE c.id = ? ORDER BY tr.training_date DESC, tt.assigned_date DESC";
    
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        clientTasksObservableList.add(new ClientTask(
                                rs.getInt("task_id"),
                                rs.getInt("training_request_id"),
                                rs.getDate("training_date").toLocalDate(),
                                rs.getString("task_description"),
                                rs.getInt("priority_id"),
                                rs.getString("priority_name"),
                                rs.getInt("status_id"),
                                rs.getString("status_name"),
                                rs.getInt("trainer_id"),
                                rs.getString("trainer_name")
                        ));
                    }
                }
            }
            System.out.println("Loaded " + clientTasksObservableList.size() + " tasks for client ID: " + userId);
        } catch (SQLException e) {
            showError("Błąd ładowania zadań klienta: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Wyświetla szczegóły wybranego zadania klienta w panelu szczegółów.
     * Aktualizuje etykiety i pola tekstowe w interfejsie, aby pokazać 
     * informacje o zaznaczonym zadaniu, takie jak data, priorytet, status,
     * opis i przypisany trener.
     * 
     * @param task zadanie klienta do wyświetlenia, może być null
     */
    private void displaySelectedClientTask(ClientTask task) {
        if (task == null) {
            clearClientTaskDetails();
            return;
        }
        selectedTaskDateLabel.setText("Data Treningu: " + task.getTrainingDate());
        selectedTaskPriorityLabel.setText("Priorytet: " + task.getPriorityName());
        selectedTaskStatusLabel.setText("Status: " + task.getStatusName());
        selectedTaskDescriptionArea.setText(task.getTaskDescription());
        selectedTaskTrainerLabel.setText("Trener: " + task.getTrainerName());
    }
    
    /**
     * Czyści panel szczegółów zadania klienta.
     * Resetuje wszystkie etykiety i pola tekstowe w panelu szczegółów zadania,
     * przygotowując go do wyświetlenia nowych danych lub pozostawienia pustym,
     * gdy nie wybrano żadnego zadania.
     */
    private void clearClientTaskDetails() {
        selectedTaskDateLabel.setText("Data Treningu: ");
        selectedTaskPriorityLabel.setText("Priorytet: ");
        selectedTaskStatusLabel.setText("Status: ");
        selectedTaskDescriptionArea.clear();
        selectedTaskTrainerLabel.setText("Trener: ");
    }

    /**
     * Inicjalizuje sekcję profilu klienta, w tym tabelę zadań przypisanych przez trenerów.
     * Konfiguruje kolumny tabeli, ustawia fabryki komórek i dodaje nasłuchiwacz 
     * do wyświetlania szczegółów wybranego zadania w panelu bocznym.
     */
    private void initializeProfile() {
        // === INICJALIZACJA TABELI ZADAŃ TRENERA ===
        clientTasksTable.setItems(clientTasksObservableList);
    
        taskIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTaskId()).asObject());
        taskDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTrainingDate()));
        taskDescriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTaskDescription()));
        taskTrainerNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTrainerName()));
    
        taskPriorityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPriorityName()));
        taskStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatusName()));
    
        clientTasksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displaySelectedClientTask(newSelection);
            } else {
                clearClientTaskDetails();
            }
        });
        // ===========================================
    }
    
    /**
     * Inicjalizuje sekcję harmonogramu treningów klienta.
     * Konfiguruje kolumny tabeli zaplanowanych treningów, ustawia formatowanie daty i czasu
     * oraz przypisuje źródło danych do tabeli.
     */
    private void initializeSchedule() {
        // Ustawienie ValueFactories dla nowej tabeli zaplanowanych treningów
        scheduledTrainingTrainerColumn.setCellValueFactory(new PropertyValueFactory<>("trainerName"));
        scheduledTrainingDateColumn.setCellValueFactory(new PropertyValueFactory<>("trainingDateTime")); // Zmieniono na LocalDateTime
        scheduledTrainingNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        // USUNIĘTO: scheduledTrainingStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    
        // Formatowanie daty i czasu w kolumnie
        scheduledTrainingDateColumn.setCellFactory(column -> new TableCell<ClientScheduledTraining, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); // Zmieniono formatowanie na pełną datę i czas
    
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
    
        scheduleTable.setItems(clientScheduledTrainingsData);
    }
    
    /**
     * Inicjalizuje sekcję aktywności klienta, takich jak transakcje i płatności.
     * Konfiguruje kolumny tabeli aktywności i przypisuje źródło danych do tabeli.
     */
    private void initializeClientActivities() {
        activityTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        activityDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        activityAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        activityDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        activityStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    
        clientActivitiesTable.setItems(clientActivitiesData);
    }
    
    /**
     * Inicjalizuje sekcję zapytań o treningi z trenerem.
     * Konfiguruje kolumny tabeli zapytań o treningi, ładuje listę dostępnych trenerów
     * i przypisuje źródło danych do tabeli.
     */
    private void initializeClientTrainingsRequest() {
        trainerNameColumn.setCellValueFactory(new PropertyValueFactory<>("trainerName"));
        requestNotesColumn.setCellValueFactory(new PropertyValueFactory<>("requestNotes"));
        trainingDateColumn.setCellValueFactory(new PropertyValueFactory<>("trainingDate"));
        trainingStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        clientTrainingRequestsTable.setItems(clientTrainingRequestsData);

        loadTrainers();
        loadClientTrainingRequests();
    }

    /**
     * Ładuje zapytania klienta o treningi z bazy danych.
     * Pobiera wszystkie zapytania o treningi złożone przez klienta, wraz z ich statusem
     * (oczekujący lub zaplanowany) oraz danymi trenera, do którego skierowano zapytanie.
     * Wyniki są sortowane od najnowszych do najstarszych.
     */
    private void loadClientTrainingRequests() {
        clientTrainingRequestsData.clear();
        String sql = "SELECT " +
                "r.notes AS request_notes, " +
                "tr.training_date, " +
                "u.name AS trainer_name " +
                "FROM reports r " +
                "LEFT JOIN trainingrequests tr ON r.id = tr.report " +
                "JOIN users u ON r.trainer_id = u.id " +
                "WHERE r.client_id = ? " +
                "ORDER BY tr.training_date DESC, r.id DESC";
    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String status = (rs.getDate("training_date") != null) ? "Zaplanowany" : "Oczekujący";
    
                clientTrainingRequestsData.add(new ClientTrainingRequest(
                        rs.getString("trainer_name"),
                        rs.getString("request_notes"),
                        rs.getDate("training_date"),
                        status
                ));
            }
        }
        catch (SQLException e) {
            System.err.println("Błąd ładowania prośb o trening klienta: " + e.getMessage());
            e.printStackTrace();
            showError("Błąd ładowania Twoich treningów: " + e.getMessage());
        }
    }
    
    /**
     * Ładuje zaplanowane treningi klienta z bazy danych.
     * Pobiera tylko te zapytania o treningi, które zostały już zaplanowane
     * (posiadają datę i czas treningu). Wyniki są sortowane od najnowszych do najstarszych.
     * Używa się ich do wyświetlania w sekcji harmonogramu treningów klienta.
     */
    private void loadClientScheduledTrainings() {
        if (userId == 0) {
            System.err.println("Błąd: userId nie ustawione w ClientDashboardController dla loadClientScheduledTrainings.");
            return;
        }

        clientScheduledTrainingsData.clear();
        String sql = "SELECT " +
                "tr.training_date, " +
                "u.name AS trainer_name, " +
                "r.notes AS request_notes " + // Usunięto tr.status
                "FROM trainingrequests tr " +
                "JOIN reports r ON tr.report = r.id " +
                "JOIN users u ON r.trainer_id = u.id " +
                "WHERE r.client_id = ? AND tr.training_date IS NOT NULL " + // Tylko zaplanowane treningi
                "ORDER BY tr.training_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Timestamp trainingTimestamp = rs.getTimestamp("training_date");
                LocalDateTime trainingDateTime = (trainingTimestamp != null) ? trainingTimestamp.toLocalDateTime() : null;

                clientScheduledTrainingsData.add(new ClientScheduledTraining(
                        rs.getString("trainer_name"),
                        trainingDateTime,
                        rs.getString("request_notes")// Używamy domyślnego statusu
                ));
            }
            System.out.println("Loaded " + clientScheduledTrainingsData.size() + " scheduled trainings for client ID: " + userId);
        } catch (SQLException e) {
            System.err.println("Błąd ładowania zaplanowanych treningów klienta: " + e.getMessage());
            e.printStackTrace();
            showError("Błąd ładowania zaplanowanych treningów: " + e.getMessage());
        }
    }


    // --- DATA LOADING METHODS ---

    /**
     * Ładuje wszystkie aktywności finansowe klienta z bazy danych.
     * Obejmuje to:
     * 1. Transakcje zakupowe produktów ze sklepu
     * 2. Płatności za karnety członkowskie
     * 3. Płatności za treningi indywidualne
     * 
     * Wyniki są sortowane chronologicznie od najnowszych do najstarszych
     * i wyświetlane w tabeli aktywności klienta.
     */
    private void loadClientActivities() {
        clientActivitiesData.clear();
    
        // Load Transactions for this user
        String transactionSql = "SELECT t.transaction_date, t.amount, p.name AS product_name " +
                "FROM transactions t " +
                "JOIN products p ON t.product_id = p.id " +
                "WHERE t.client_id = ? " +
                "ORDER BY t.transaction_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(transactionSql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                clientActivitiesData.add(new ClientActivity(
                        "Transakcja",
                        "Zakup: " + rs.getString("product_name"),
                        rs.getBigDecimal("amount"),
                        rs.getTimestamp("transaction_date"),
                        "Zakończona"
                ));
            }
        } catch (SQLException e) {
            System.err.println("Błąd ładowania transakcji klienta: " + e.getMessage());
            e.printStackTrace();
        }
    
        // Load Membership Payments for this user
        String membershipSql = "SELECT amount, payment_date FROM membership_payments WHERE client_id = ? ORDER BY payment_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(membershipSql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                clientActivitiesData.add(new ClientActivity(
                        "Płatność za karnet",
                        "Opłata za karnet",
                        rs.getBigDecimal("amount"),
                        rs.getTimestamp("payment_date"),
                        "Zakończona"
                ));
            }
        } catch (SQLException e) {
            System.err.println("Błąd ładowania płatności za karnety klienta: " + e.getMessage());
            e.printStackTrace();
        }
    
        // --- DODANY FRAGMENT DLA PŁATNOŚCI ZA TRENINGI INDYWIDUALNE ---
        String trainingPaymentSql = "SELECT trp.amount, trp.payment_date, r.notes " +
                "FROM training_request_payments trp " +
                "JOIN trainingrequests tr ON trp.training_request_id = tr.id " +
                "JOIN reports r ON tr.report = r.id " + // Dołączamy do reports, aby uzyskać client_id
                "WHERE r.client_id = ? " + // Używamy client_id z tabeli reports
                "ORDER BY trp.payment_date DESC";
    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(trainingPaymentSql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String description = "Płatność za trening indywidualny";
                String notes = rs.getString("notes");
                if (notes != null && !notes.isEmpty()) {
                    description += " (" + notes + ")";
                }
    
                clientActivitiesData.add(new ClientActivity(
                        "Płatność za Trening",
                        description,
                        rs.getBigDecimal("amount"),
                        rs.getTimestamp("payment_date"),
                        "Zakończona"
                ));
            }
        } catch (SQLException e) {
            System.err.println("Błąd ładowania płatności za treningi klienta: " + e.getMessage());
            e.printStackTrace();
        }
        // --- KONIEC DODANEGO FRAGMENTU ---
    }
    
    /**
     * Ładuje listę dostępnych trenerów z bazy danych.
     * Pobiera wszystkich użytkowników z rolą 'trainer' i dodaje ich do listy
     * rozwijalnej (ComboBox) używanej do wyboru trenera przy składaniu 
     * zapytania o trening personalny.
     * Konfiguruje również sposób wyświetlania trenerów w liście i konwersję
     * między obiektem Trainer a tekstem.
     */
    private void loadTrainers() {
        trainerList.clear();
        // Zmieniono zapytanie SQL - usunięto 'surname'
        String sql = "SELECT id, name FROM users WHERE role = 'trainer'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                trainerList.add(new Trainer(
                        rs.getInt("id"),
                        rs.getString("name") // Teraz przekazujemy tylko 'name'
                ));
            }
            trainerComboBox.setItems(trainerList);
            trainerComboBox.setCellFactory(lv -> new ListCell<Trainer>() {
                @Override
                protected void updateItem(Trainer trainer, boolean empty) {
                    super.updateItem(trainer, empty);
                    if (empty || trainer == null) {
                        setText(null);
                    } else {
                        // Wyświetlaj tylko imię
                        setText(trainer.getName());
                    }
                }
            });
            trainerComboBox.setConverter(new StringConverter<Trainer>() {
                @Override
                public String toString(Trainer trainer) {
                    if (trainer == null) return null;
                    // Zwróć tylko imię
                    return trainer.getName();
                }

                @Override
                public Trainer fromString(String string) {
                    return null; // Nie jest używane do konwersji String na Trainer
                }
            });
        } catch (SQLException e) {
            showError("Błąd ładowania trenerów: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- SECTION TOGGLE METHODS ---

    /**
     * Wyświetla sekcję startową (Home) panelu klienta.
     * Ukrywa wszystkie inne sekcje i pokazuje sekcję startową.
     */
    @FXML
    public void showHome() {
        hideAllSections();
        homeSection.setVisible(true);
        homeSection.setManaged(true);
    }
    
    /**
     * Wyświetla sekcję profilu klienta, zawierającą zadania przydzielone przez trenera.
     * Ukrywa wszystkie inne sekcje i pokazuje sekcję profilu.
     */
    @FXML
    public void showProfile() {
        hideAllSections();
        profileSection.setVisible(true);
        profileSection.setManaged(true);
    }
    
    /**
     * Wyświetla sekcję zarządzania karnetami.
     * Ukrywa wszystkie inne sekcje i pokazuje sekcję karnetów.
     */
    @FXML
    public void showMembership() {
        hideAllSections();
        membershipSection.setVisible(true);
        membershipSection.setManaged(true);
    }
    
    /**
     * Wyświetla sekcję "Zapisz się na Trening", gdzie klient może złożyć zapytanie
     * o trening personalny do wybranego trenera.
     * Odświeża listę istniejących zapytań o treningi przy każdym otwarciu.
     */
    @FXML
    public void showTrainings() { // To jest sekcja "Zapisz się na Trening"
        hideAllSections();
        clientTrainingsRequestSection.setVisible(true);
        clientTrainingsRequestSection.setManaged(true);
        loadClientTrainingRequests();
    }
    
    /**
     * Wyświetla sekcję sklepu, gdzie klient może przeglądać i dodawać do koszyka
     * produkty dostępne w siłowni.
     */
    @FXML
    public void showShop() {
        hideAllSections();
        shopSection.setVisible(true);
        shopSection.setManaged(true);
    }
    
    /**
     * Wyświetla sekcję harmonogramu treningów klienta.
     * Odświeża listę zaplanowanych treningów przy każdym otwarciu sekcji,
     * aby zapewnić aktualne dane.
     */
    @FXML
    public void showSchedule() { // TUTAJ ZMIANA: Wywołanie ładowania zaplanowanych treningów
        hideAllSections();
        scheduleSection.setVisible(true);
        scheduleSection.setManaged(true);
        loadClientScheduledTrainings(); // Ładuj dane przy otwarciu sekcji
    }

    @FXML
    public void showChangePassword() {
        hideAllSections();
        changePasswordSection.setVisible(true);

        // Wyczyść pola
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        passwordChangeStatus.setText("");
    }
    /**
     * Wyświetla sekcję aktywności i transakcji klienta.
     * Odświeża listę aktywności przy każdym otwarciu sekcji,
     * aby zapewnić aktualne dane o transakcjach i płatnościach.
     */
    @FXML
    public void showClientActivities() {
        hideAllSections();
        clientTransactionsSection.setVisible(true);
        clientTransactionsSection.setManaged(true);
        loadClientActivities(); // Załaduj aktywności klienta przy otwarciu sekcji
    }
    
    /**
     * Ukrywa wszystkie sekcje panelu klienta.
     * Używana przed pokazaniem konkretnej sekcji, aby zapewnić, że tylko jedna
     * sekcja jest widoczna w danym momencie.
     */
    private void hideAllSections() {
        homeSection.setVisible(false);
        profileSection.setVisible(false);
        membershipSection.setVisible(false);
        trainingsSection.setVisible(false);
        shopSection.setVisible(false);
        scheduleSection.setVisible(false);
        clientTransactionsSection.setVisible(false);
        clientTrainingsRequestSection.setVisible(false);
        changePasswordSection.setVisible(false);

        homeSection.setManaged(false);
        profileSection.setManaged(false);
        membershipSection.setManaged(false);
        trainingsSection.setManaged(false);
        shopSection.setManaged(false);
        scheduleSection.setManaged(false);
        clientTransactionsSection.setManaged(false);
        clientTrainingsRequestSection.setManaged(false);
    }

    // --- CART/SHOP/MEMBERSHIP METHODS ---

    /**
     * Dodaje karnet BASIC do koszyka klienta.
     * Wyświetla okno dialogowe potwierdzenia z ceną karnetu.
     */
    @FXML
    public void addBasicToCart() {
        showConfirmDialog("Karnet BASIC", "Czy chcesz dodać do koszyka karnet BASIC za 150 zł?");
    }
    
    /**
     * Dodaje karnet PRO do koszyka klienta.
     * Wyświetla okno dialogowe potwierdzenia z ceną karnetu.
     */
    @FXML
    public void addProToCart() {
        showConfirmDialog("Karnet PRO", "Czy chcesz dodać do koszyka karnet PRO za 400 zł?");
    }
    
    /**
     * Dodaje karnet DZIENNY do koszyka klienta.
     * Wyświetla okno dialogowe potwierdzenia z ceną karnetu.
     */
    @FXML
    public void addDailyToCart() {
        showConfirmDialog("Karnet DZIENNY", "Czy chcesz dodać do koszyka karnet DZIENNY za 25 zł?");
    }
    
    /**
     * Dodaje karnet MIESIĘCZNY do koszyka klienta.
     * Wyświetla okno dialogowe potwierdzenia z ceną karnetu.
     */
    @FXML
    public void addMonthlyToCart() {
        showConfirmDialog("Karnet MIESIĘCZNY", "Czy chcesz dodać do koszyka karnet MIESIĘCZNY za 180 zł?");
    }
    
    /**
     * Dodaje karnet ROCZNY do koszyka klienta.
     * Wyświetla okno dialogowe potwierdzenia z ceną karnetu.
     */
    @FXML
    public void addYearlyToCart() {
        showConfirmDialog("Karnet ROCZNY", "Czy chcesz dodać do koszyka karnet ROCZNY za 1200 zł?");
    }
    
    /**
     * Wyświetla zawartość koszyka klienta.
     * Pokazuje liczbę produktów w koszyku lub informację, że koszyk jest pusty.
     */
    @FXML
    public void showCart() {
        if (cartItemCount == 0) {
            showAlert("Koszyk", "Twój koszyk jest pusty.");
        } else {
            showAlert("Koszyk", "Masz " + cartItemCount + " produkt(ów) w koszyku.");
        }
    }
    
    /**
     * Dodaje produkt WHEY PROTEIN COMPLEX do koszyka klienta.
     */
    @FXML
    public void buyWheyProtein() { addToCart("WHEY PROTEIN COMPLEX", 129.0); }
    
    /**
     * Dodaje produkt Kreatyna monohydrat do koszyka klienta.
     */
    @FXML
    public void buyCreatine() { addToCart("Kreatyna monohydrat", 35.0); }
    
    /**
     * Dodaje produkt Shaker do koszyka klienta.
     */
    @FXML
    public void buyShaker() { addToCart("Shaker Wielkiego Chłopa", 29.0); }
    
    /**
     * Dodaje produkt Strzykawka do koszyka klienta.
     */
    @FXML
    public void buySyringe() { addToCart("Strzykawka", 6.0); }
    
    /**
     * Dodaje produkt Trembolon do koszyka klienta.
     */
    @FXML
    public void buyTrembolone() { addToCart("Trembolon", 165.0); }
    
    /**
     * Dodaje produkt Nandrolon do koszyka klienta.
     */
    @FXML
    public void buyNandrolone() { addToCart("Nandrolon", 200.0); }
    
    /**
     * Dodaje produkt Drostanolon do koszyka klienta.
     */
    @FXML
    public void buyWeight1() { addToCart("Drostanolon", 125.0); }
    
    /**
     * Dodaje produkt Dihydroboldenone (DHB) do koszyka klienta.
     */
    @FXML
    public void buyWeight2() { addToCart("Dihydroboldenone (DHB)", 150.0); }
    
    /**
     * Dodaje produkt Testosterone do koszyka klienta.
     */
    @FXML
    public void buyWeight3() { addToCart("Testosterone", 135.0); }
    
    /**
     * Dodaje produkt do koszyka klienta.
     * Zwiększa licznik produktów w koszyku, aktualizuje etykietę licznika
     * i wyświetla komunikat potwierdzający dodanie produktu.
     * 
     * @param productName nazwa produktu dodawanego do koszyka
     * @param price cena produktu
     */
    private void addToCart(String productName, double price) {
        cartItemCount++;
        cartCounterLabel.setText("(" + cartItemCount + ")");
        showAlert("Koszyk", "Dodano do koszyka: " + productName + " - " + price + " zł");
    }

    // --- TRAINING REQUEST METHODS ---

    /**
     * Obsługuje wysłanie zapytania o trening personalny do wybranego trenera.
     * Pobiera wybranego trenera oraz notatki wprowadzone przez klienta,
     * a następnie zapisuje zapytanie w bazie danych.
     * Zapytanie będzie widoczne dla trenera, który może zaplanować trening
     * lub odrzucić zapytanie.
     */
    @FXML
    private void requestTraining() {
        Trainer selectedTrainer = trainerComboBox.getSelectionModel().getSelectedItem();
        String notes = trainingNotesTextArea.getText();
    
        if (selectedTrainer == null) {
            showError("Wybierz trenera.");
            return;
        }
    
        // Zmienione zapytanie SQL, aby wstawić tylko client_id i trainer_id do reports
        // W trainingrequests entry będzie tworzone przez trenera
        String sql = "INSERT INTO reports (client_id, trainer_id, notes) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, selectedTrainer.getId());
            pstmt.setString(3, notes.isEmpty() ? null : notes);
            int affectedRows = pstmt.executeUpdate();
    
            if (affectedRows > 0) {
                showAlert("Sukces", "Twoja prośba o trening została wysłana. Trener skontaktuje się wkrótce.");
                clearTrainingRequestForm();
                loadClientTrainingRequests(); // Odśwież tabelę z prośbami klienta
            } else {
                showError("Nie udało się wysłać prośby o trening.");
            }
        } catch (SQLException e) {
            showError("Błąd wysyłania prośby o trening: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Czyści formularz zapytania o trening personalny.
     * Resetuje wybór trenera i usuwa tekst z pola notatek.
     */
    private void clearTrainingRequestForm() {
        trainerComboBox.getSelectionModel().clearSelection();
        trainingNotesTextArea.clear();
    }
    
    // --- GENERAL UTILITY METHODS ---
    
    /**
     * Wyświetla okno dialogowe potwierdzenia z określonym tytułem i treścią.
     * Jeśli użytkownik potwierdzi operację, produkt zostanie dodany do koszyka.
     * 
     * @param title tytuł okna dialogowego i nazwa produktu
     * @param message treść komunikatu wyświetlana użytkownikowi
     */
    private void showConfirmDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
    
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Tutaj logika dodawania do koszyka/płatności w zależności od tego, co jest kupowane
            cartItemCount++;
            cartCounterLabel.setText("(" + cartItemCount + ")");
            showAlert("Koszyk", "Dodano do koszyka: " + title);
        }
    }
    
    /**
     * Wyświetla okno dialogowe informacyjne z określonym tytułem i treścią.
     * 
     * @param title tytuł okna dialogowego
     * @param content treść komunikatu wyświetlana użytkownikowi
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Wyświetla okno dialogowe błędu z określoną treścią.
     * 
     * @param message treść komunikatu błędu wyświetlana użytkownikowi
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void changePassword(ActionEvent event) {
        // Tutaj dodaj logikę walidacji i zmiany hasła
        // Przykładowa walidacja:
        if (newPasswordField.getText().isEmpty() ||
                !newPasswordField.getText().equals(confirmPasswordField.getText())) {
            passwordChangeStatus.setText("Hasła nie są identyczne!");
            passwordChangeStatus.setStyle("-fx-text-fill: #ff5555;");
            return;
        }

        // Logika zmiany hasła (połączenie z bazą danych, itp.)
        // ...

        // Komunikat o sukcesie
        passwordChangeStatus.setText("Hasło zostało zmienione pomyślnie!");
        passwordChangeStatus.setStyle("-fx-text-fill: #55ff55;");
    }

    public void showChangePassword(ActionEvent event) {
        // Ukryj wszystkie sekcje
        homeSection.setVisible(false);
        profileSection.setVisible(false);
        membershipSection.setVisible(false);
        trainingsSection.setVisible(false);
        shopSection.setVisible(false);
        scheduleSection.setVisible(false);
        clientTransactionsSection.setVisible(false);
        clientTrainingsRequestSection.setVisible(false);

        // Pokaż sekcję zmiany hasła
        changePasswordSection.setVisible(true);

        // Wyczyść pola
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        passwordChangeStatus.setText("");
    }

    // === INNER CLASSES ===

    // Zaktualizowana KLASA DANYCH DLA ZAPLANOWANYCH TRENINGÓW (BEZ STATUSU)
    /**
     * Klasa reprezentująca zaplanowany trening klienta.
     * Przechowuje informacje o treningu, takie jak identyfikator, nazwa,
     * data, godzina, trener prowadzący i lokalizacja.
     * Wykorzystuje model JavaFX Property dla łatwej integracji z interfejsem użytkownika.
     */
    public static class ClientScheduledTraining {
        private final SimpleStringProperty trainerName;
        private final SimpleObjectProperty<LocalDateTime> trainingDateTime;
        private final SimpleStringProperty notes;
        // USUNIĘTO: private final SimpleStringProperty status;

        public ClientScheduledTraining(String trainerName, LocalDateTime trainingDateTime, String notes) { // Zmieniono konstruktor
            this.trainerName = new SimpleStringProperty(trainerName);
            this.trainingDateTime = new SimpleObjectProperty<>(trainingDateTime);
            this.notes = new SimpleStringProperty(notes);
            // USUNIĘTO: this.status = new SimpleStringProperty(status);
        }

        public String getTrainerName() { return trainerName.get(); }
        public SimpleStringProperty trainerNameProperty() { return trainerName; }

        public LocalDateTime getTrainingDateTime() { return trainingDateTime.get(); }
        public SimpleObjectProperty<LocalDateTime> trainingDateTimeProperty() { return trainingDateTime; }

        public String getNotes() { return notes.get(); }
        public SimpleStringProperty notesProperty() { return notes; }

        // USUNIĘTO: public String getStatus() { return status.get(); }
        // USUNIĘTO: public SimpleStringProperty statusProperty() { return status; }
    }


    /**
     * Klasa reprezentująca zapytanie klienta o trening personalny.
     * Przechowuje informacje o zapytaniu, takie jak opis, preferowana data,
     * preferowany trener i status zapytania (oczekujący lub zaplanowany).
     * Wykorzystuje model JavaFX Property dla łatwej integracji z interfejsem użytkownika.
     */
    public static class ClientTrainingRequest {
        /**
         * Imię i nazwisko trenera, do którego skierowano zapytanie.
         */
        private final SimpleStringProperty trainerName;
        
        /**
         * Notatki klienta dotyczące zapytania o trening.
         */
        private final SimpleStringProperty requestNotes;
        
        /**
         * Data zaplanowanego treningu (jeśli trening został już zaplanowany).
         */
        private final SimpleObjectProperty<java.sql.Date> trainingDate;
        
        /**
         * Status zapytania (Oczekujący lub Zaplanowany).
         */
        private final SimpleStringProperty status;
    
        /**
         * Tworzy nowe zapytanie o trening personalny.
         *
         * @param trainerName imię i nazwisko trenera
         * @param requestNotes notatki dotyczące zapytania
         * @param trainingDate data zaplanowanego treningu (może być null dla oczekujących)
         * @param status status zapytania (Oczekujący lub Zaplanowany)
         */
        public ClientTrainingRequest(String trainerName, String requestNotes, java.sql.Date trainingDate, String status) {
            this.trainerName = new SimpleStringProperty(trainerName);
            this.requestNotes = new SimpleStringProperty(requestNotes);
            this.trainingDate = new SimpleObjectProperty<>(trainingDate);
            this.status = new SimpleStringProperty(status);
        }
    
        /**
         * Zwraca imię i nazwisko trenera.
         * @return imię i nazwisko trenera
         */
        public String getTrainerName() { return trainerName.get(); }
        
        /**
         * Zwraca właściwość przechowującą imię i nazwisko trenera.
         * @return właściwość imienia i nazwiska trenera
         */
        public SimpleStringProperty trainerNameProperty() { return trainerName; }
    
        /**
         * Zwraca notatki dotyczące zapytania.
         * @return notatki zapytania
         */
        public String getRequestNotes() { return requestNotes.get(); }
        
        /**
         * Zwraca właściwość przechowującą notatki zapytania.
         * @return właściwość notatek zapytania
         */
        public SimpleStringProperty requestNotesProperty() { return requestNotes; }
    
        /**
         * Zwraca datę zaplanowanego treningu.
         * @return data treningu lub null, jeśli nie zaplanowano
         */
        public java.sql.Date getTrainingDate() { return trainingDate.get(); }
        
        /**
         * Zwraca właściwość przechowującą datę treningu.
         * @return właściwość daty treningu
         */
        public SimpleObjectProperty<java.sql.Date> trainingDateProperty() { return trainingDate; }
    
        /**
         * Zwraca status zapytania.
         * @return status zapytania (Oczekujący lub Zaplanowany)
         */
        public String getStatus() { return status.get(); }
        
        /**
         * Zwraca właściwość przechowującą status zapytania.
         * @return właściwość statusu zapytania
         */
        public SimpleStringProperty statusProperty() { return status; }
    }
    
    /**
     * Klasa reprezentująca aktywność finansową klienta w siłowni.
     * Przechowuje informacje o aktywnościach, takie jak typ aktywności (transakcja, płatność),
     * kwota, data, opis i status płatności.
     * Wykorzystuje model JavaFX Property dla automatycznej aktualizacji UI.
     */
    public static class ClientActivity {
        /**
         * Typ aktywności (np. Transakcja, Płatność za karnet, Płatność za Trening).
         */
        private final SimpleStringProperty type;
        
        /**
         * Opis aktywności z dodatkowymi szczegółami.
         */
        private final SimpleStringProperty description;
        
        /**
         * Kwota transakcji lub płatności.
         */
        private final SimpleObjectProperty<BigDecimal> amount;
        
        /**
         * Data i czas aktywności.
         */
        private final SimpleObjectProperty<Timestamp> date;
        
        /**
         * Status aktywności (np. Zakończona, W trakcie).
         */
        private final SimpleStringProperty status;
    
        /**
         * Tworzy nową aktywność klienta.
         *
         * @param type typ aktywności
         * @param description opis aktywności
         * @param amount kwota aktywności
         * @param date data i czas aktywności
         * @param status status aktywności
         */
        public ClientActivity(String type, String description, BigDecimal amount, Timestamp date, String status) {
            this.type = new SimpleStringProperty(type);
            this.description = new SimpleStringProperty(description);
            this.amount = new SimpleObjectProperty<>(amount);
            this.date = new SimpleObjectProperty<>(date);
            this.status = new SimpleStringProperty(status);
        }
    
        /**
         * Zwraca typ aktywności.
         * @return typ aktywności
         */
        public String getType() { return type.get(); }
        
        /**
         * Zwraca właściwość przechowującą typ aktywności.
         * @return właściwość typu aktywności
         */
        public SimpleStringProperty typeProperty() { return type; }
    
        /**
         * Zwraca opis aktywności.
         * @return opis aktywności
         */
        public String getDescription() { return description.get(); }
        
        /**
         * Zwraca właściwość przechowującą opis aktywności.
         * @return właściwość opisu aktywności
         */
        public SimpleStringProperty descriptionProperty() { return description; }
    
        /**
         * Zwraca kwotę aktywności.
         * @return kwota aktywności
         */
        public BigDecimal getAmount() { return amount.get(); }
        
        /**
         * Zwraca właściwość przechowującą kwotę aktywności.
         * @return właściwość kwoty aktywności
         */
        public SimpleObjectProperty<BigDecimal> amountProperty() { return amount; }
    
        /**
         * Zwraca datę i czas aktywności.
         * @return data i czas aktywności
         */
        public Timestamp getDate() { return date.get(); }
        
        /**
         * Zwraca właściwość przechowującą datę i czas aktywności.
         * @return właściwość daty i czasu aktywności
         */
        public SimpleObjectProperty<Timestamp> dateProperty() { return date; }
    
        /**
         * Zwraca status aktywności.
         * @return status aktywności
         */
        public String getStatus() { return status.get(); }
        
        /**
         * Zwraca właściwość przechowującą status aktywności.
         * @return właściwość statusu aktywności
         */
        public SimpleStringProperty statusProperty() { return status; }
    }
    
    /**
     * Klasa reprezentująca trenera w systemie siłowni.
     * Przechowuje podstawowe informacje o trenerze, takie jak identyfikator i imię.
     * Wykorzystywana głównie do wyboru trenera w formularzu zapytania o trening.
     */
    public static class Trainer {
        /**
         * Unikalny identyfikator trenera w bazie danych.
         */
        private final int id;
        
        /**
         * Imię i nazwisko trenera.
         */
        private final String name;
        // private final String surname; // USUNIĘTE
    
        /**
         * Tworzy nowego trenera z podanym identyfikatorem i imieniem.
         *
         * @param id identyfikator trenera
         * @param name imię i nazwisko trenera
         */
        public Trainer(int id, String name) { // Zmieniony konstruktor
            this.id = id;
            this.name = name;
            // this.surname = surname; // USUNIĘTE
        }
    
        /**
         * Zwraca identyfikator trenera.
         * @return identyfikator trenera
         */
        public int getId() { return id; }
        
        /**
         * Zwraca imię i nazwisko trenera.
         * @return imię i nazwisko trenera
         */
        public String getName() { return name; }
        // public String getSurname() { return surname; } // USUNIĘTE
    
        /**
         * Zwraca tekstową reprezentację trenera (imię i nazwisko).
         * Używane do wyświetlania w ComboBox i innych komponentach UI.
         * 
         * @return tekstowa reprezentacja trenera
         */
        @Override
        public String toString() {
            return name; // Zwracamy tylko imię
        }
    }

    /**
     * Klasa reprezentująca zadanie klienta przydzielone przez trenera.
     * Przechowuje informacje o zadaniu, takie jak identyfikator, opis zadania,
     * powiązany trening, data treningu, priorytet, status oraz informacje o trenerze.
     * Implementuje model JavaFX Property dla automatycznej aktualizacji UI.
     */
    public static class ClientTask {
        /**
         * Unikalny identyfikator zadania w bazie danych.
         */
        private final SimpleIntegerProperty taskId;
        
        /**
         * Identyfikator powiązanego zapytania o trening.
         */
        private final SimpleIntegerProperty trainingRequestId;
        
        /**
         * Data treningu, do którego odnosi się zadanie.
         */
        private final SimpleObjectProperty<java.time.LocalDate> trainingDate;
        
        /**
         * Opis zadania - instrukcje od trenera dla klienta.
         */
        private final SimpleStringProperty taskDescription;
        
        /**
         * Identyfikator priorytetu zadania.
         */
        private final SimpleIntegerProperty priorityId;
        
        /**
         * Nazwa priorytetu zadania (np. Niski, Średni, Wysoki).
         */
        private final SimpleStringProperty priorityName;
        
        /**
         * Identyfikator statusu zadania.
         */
        private final SimpleIntegerProperty statusId;
        
        /**
         * Nazwa statusu zadania (np. Nowe, W trakcie, Zakończone).
         */
        private final SimpleStringProperty statusName;
        
        /**
         * Identyfikator trenera, który przydzielił zadanie.
         */
        private final SimpleIntegerProperty trainerId;
        
        /**
         * Imię i nazwisko trenera, który przydzielił zadanie.
         */
        private final SimpleStringProperty trainerName;
    
        /**
         * Tworzy nowe zadanie klienta z podanymi parametrami.
         *
         * @param taskId identyfikator zadania
         * @param trainingRequestId identyfikator powiązanego zapytania o trening
         * @param trainingDate data treningu
         * @param taskDescription opis zadania
         * @param priorityId identyfikator priorytetu
         * @param priorityName nazwa priorytetu
         * @param statusId identyfikator statusu
         * @param statusName nazwa statusu
         * @param trainerId identyfikator trenera
         * @param trainerName imię i nazwisko trenera
         */
        public ClientTask(int taskId, int trainingRequestId, java.time.LocalDate trainingDate, String taskDescription,
                          int priorityId, String priorityName, int statusId, String statusName,
                          int trainerId, String trainerName) {
            this.taskId = new SimpleIntegerProperty(taskId);
            this.trainingRequestId = new SimpleIntegerProperty(trainingRequestId);
            this.trainingDate = new SimpleObjectProperty<>(trainingDate);
            this.taskDescription = new SimpleStringProperty(taskDescription);
            this.priorityId = new SimpleIntegerProperty(priorityId);
            this.priorityName = new SimpleStringProperty(priorityName);
            this.statusId = new SimpleIntegerProperty(statusId);
            this.statusName = new SimpleStringProperty(statusName);
            this.trainerId = new SimpleIntegerProperty(trainerId);
            this.trainerName = new SimpleStringProperty(trainerName);
        }
    
        /**
         * Zwraca identyfikator zadania.
         * @return identyfikator zadania
         */
        public int getTaskId() { return taskId.get(); }
        
        /**
         * Zwraca właściwość przechowującą identyfikator zadania.
         * @return właściwość identyfikatora zadania
         */
        public SimpleIntegerProperty taskIdProperty() { return taskId; }
    
        /**
         * Zwraca identyfikator powiązanego zapytania o trening.
         * @return identyfikator zapytania o trening
         */
        public int getTrainingRequestId() { return trainingRequestId.get(); }
        
        /**
         * Zwraca właściwość przechowującą identyfikator zapytania o trening.
         * @return właściwość identyfikatora zapytania o trening
         */
        public SimpleIntegerProperty trainingRequestIdProperty() { return trainingRequestId; }
    
        /**
         * Zwraca datę treningu.
         * @return data treningu
         */
        public java.time.LocalDate getTrainingDate() { return trainingDate.get(); }
        
        /**
         * Zwraca właściwość przechowującą datę treningu.
         * @return właściwość daty treningu
         */
        public SimpleObjectProperty<java.time.LocalDate> trainingDateProperty() { return trainingDate; }
    
        /**
         * Zwraca opis zadania.
         * @return opis zadania
         */
        public String getTaskDescription() { return taskDescription.get(); }
        
        /**
         * Zwraca właściwość przechowującą opis zadania.
         * @return właściwość opisu zadania
         */
        public SimpleStringProperty taskDescriptionProperty() { return taskDescription; }
    
        /**
         * Zwraca identyfikator priorytetu zadania.
         * @return identyfikator priorytetu
         */
        public int getPriorityId() { return priorityId.get(); }
        
        /**
         * Zwraca właściwość przechowującą identyfikator priorytetu.
         * @return właściwość identyfikatora priorytetu
         */
        public SimpleIntegerProperty priorityIdProperty() { return priorityId; }
    
        /**
         * Zwraca nazwę priorytetu zadania.
         * @return nazwa priorytetu
         */
        public String getPriorityName() { return priorityName.get(); }
        
        /**
         * Zwraca właściwość przechowującą nazwę priorytetu.
         * @return właściwość nazwy priorytetu
         */
        public SimpleStringProperty priorityNameProperty() { return priorityName; }
    
        /**
         * Zwraca identyfikator statusu zadania.
         * @return identyfikator statusu
         */
        public int getStatusId() { return statusId.get(); }
        
        /**
         * Zwraca właściwość przechowującą identyfikator statusu.
         * @return właściwość identyfikatora statusu
         */
        public SimpleIntegerProperty statusIdProperty() { return statusId; }
    
        /**
         * Zwraca nazwę statusu zadania.
         * @return nazwa statusu
         */
        public String getStatusName() { return statusName.get(); }
        
        /**
         * Zwraca właściwość przechowującą nazwę statusu.
         * @return właściwość nazwy statusu
         */
        public SimpleStringProperty statusNameProperty() { return statusName; }
    
        /**
         * Zwraca identyfikator trenera.
         * @return identyfikator trenera
         */
        public int getTrainerId() { return trainerId.get(); }
        
        /**
         * Zwraca właściwość przechowującą identyfikator trenera.
         * @return właściwość identyfikatora trenera
         */
        public SimpleIntegerProperty trainerIdProperty() { return trainerId; }
    
        /**
         * Zwraca imię i nazwisko trenera.
         * @return imię i nazwisko trenera
         */
        public String getTrainerName() { return trainerName.get(); }
        
        /**
         * Zwraca właściwość przechowującą imię i nazwisko trenera.
         * @return właściwość imienia i nazwiska trenera
         */
        public SimpleStringProperty trainerNameProperty() { return trainerName; }
    }


    /**
     * Klasa reprezentująca priorytet zadania w systemie siłowni.
     * Przechowuje identyfikator i nazwę priorytetu.
     * Używana do kategoryzowania zadań według ich ważności.
     */
    public static class Priority {
        /**
         * Unikalny identyfikator priorytetu w bazie danych.
         */
        private final SimpleIntegerProperty id;
        
        /**
         * Nazwa priorytetu (np. Niski, Średni, Wysoki).
         */
        private final SimpleStringProperty name;
    
        /**
         * Tworzy nowy priorytet z podanym identyfikatorem i nazwą.
         *
         * @param id identyfikator priorytetu
         * @param name nazwa priorytetu
         */
        public Priority(int id, String name) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
        }
    
        /**
         * Zwraca identyfikator priorytetu.
         * @return identyfikator priorytetu
         */
        public int getId() { return id.get(); }
        
        /**
         * Zwraca właściwość przechowującą identyfikator priorytetu.
         * @return właściwość identyfikatora priorytetu
         */
        public SimpleIntegerProperty idProperty() { return id; }
    
        /**
         * Zwraca nazwę priorytetu.
         * @return nazwa priorytetu
         */
        public String getName() { return name.get(); }
        
        /**
         * Zwraca właściwość przechowującą nazwę priorytetu.
         * @return właściwość nazwy priorytetu
         */
        public SimpleStringProperty nameProperty() { return name; }
    
        /**
         * Zwraca tekstową reprezentację priorytetu (jego nazwę).
         * Używane do wyświetlania w ComboBox i innych komponentach UI.
         * 
         * @return tekstowa reprezentacja priorytetu
         */
        @Override
        public String toString() {
            return name.get();
        }
    }
    
    /**
     * Klasa reprezentująca status zadania w systemie siłowni.
     * Przechowuje identyfikator i nazwę statusu.
     * Używana do śledzenia postępu w realizacji zadań (np. Nowe, W trakcie, Zakończone).
     */
    public static class Status {
        /**
         * Unikalny identyfikator statusu w bazie danych.
         */
        private final SimpleIntegerProperty id;
        
        /**
         * Nazwa statusu (np. Nowe, W trakcie, Zakończone).
         */
        private final SimpleStringProperty name;
    
        /**
         * Tworzy nowy status z podanym identyfikatorem i nazwą.
         *
         * @param id identyfikator statusu
         * @param name nazwa statusu
         */
        public Status(int id, String name) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
        }
    
        /**
         * Zwraca identyfikator statusu.
         * @return identyfikator statusu
         */
        public int getId() { return id.get(); }
        
        /**
         * Zwraca właściwość przechowującą identyfikator statusu.
         * @return właściwość identyfikatora statusu
         */
        public SimpleIntegerProperty idProperty() { return id; }
    
        /**
         * Zwraca nazwę statusu.
         * @return nazwa statusu
         */
        public String getName() { return name.get(); }
        
        /**
         * Zwraca właściwość przechowującą nazwę statusu.
         * @return właściwość nazwy statusu
         */
        public SimpleStringProperty nameProperty() { return name; }
    
        /**
         * Zwraca tekstową reprezentację statusu (jego nazwę).
         * Używane do wyświetlania w ComboBox i innych komponentach UI.
         * 
         * @return tekstowa reprezentacja statusu
         */
        @Override
        public String toString() {
            return name.get();
        }
    }
}