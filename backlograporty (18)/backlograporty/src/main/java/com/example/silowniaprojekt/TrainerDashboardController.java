package com.example.silowniaprojekt;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Kontroler panelu trenera siłowni.
 * Zarządza funkcjami dostępnymi dla trenera, takimi jak zarządzanie klientami,
 * obsługa żądań treningowych, zarządzanie zadaniami, przeglądanie harmonogramu,
 * generowanie raportów i zarządzanie produktami.
 * Implementuje logikę dla wszystkich działań dostępnych w interfejsie panelu trenera.
 */
public class TrainerDashboardController {

    // Sekcje
    @FXML private VBox dashboardSection;
    @FXML private VBox trainingPlansSection; // Ta sekcja będzie służyć jako "Ustal trening"
    @FXML private VBox scheduleSection;
    @FXML private VBox incomeSection;
    @FXML private VBox progressSection;
    @FXML private VBox reportsSection;
    @FXML private VBox productsSection;

    // Panel planu treningowego (Ustal trening)
    @FXML private ComboBox<Client> planClientCombo; // Będzie automatycznie ustawiany na wybranego klienta z tabeli
    @FXML private DatePicker planDatePicker;
    @FXML private TextField exercise1Field;
    @FXML private TextField exercise2Field;
    @FXML private TextField exercise3Field;

    // FXML elements for Training Plans section (client requests to trainer) - teraz "Ustal trening"
    @FXML private TableView<PendingTrainingRequest> pendingRequestsTable;
    @FXML private TableColumn<PendingTrainingRequest, Integer> pendingRequestIdColumn;
    @FXML private TableColumn<PendingTrainingRequest, Integer> pendingClientIdColumn;
    @FXML private TableColumn<PendingTrainingRequest, String> pendingClientNameColumn;
    @FXML private TableColumn<PendingTrainingRequest, String> pendingNotesColumn;

    @FXML private Label selectedRequestLabel;
    @FXML private Button acceptRequestButton; // Zmieniono nazwę przycisku w FXML na "Zapisz plan treningowy" lub podobnie

    // FXML elements for Schedule section (newly added)
    @FXML private TableView<TrainingScheduleEntry> trainingScheduleTable;
    @FXML private TableColumn<TrainingScheduleEntry, Integer> scheduleIdColumn;
    @FXML private TableColumn<TrainingScheduleEntry, String> scheduleClientNameColumn;
    @FXML private TableColumn<TrainingScheduleEntry, LocalDate> scheduleDateColumn;
    @FXML private TableColumn<TrainingScheduleEntry, String> scheduleNotesColumn;


    @FXML private Label totalIncomeLabel;
    @FXML private TableView<TrainerIncomeEntry> incomeTable;
    @FXML private TableColumn<TrainerIncomeEntry, Integer> incomePaymentIdColumn;
    @FXML private TableColumn<TrainerIncomeEntry, String> incomeClientNameColumn;
    @FXML private TableColumn<TrainerIncomeEntry, LocalDateTime> incomeTrainingDateTimeColumn;
    @FXML private TableColumn<TrainerIncomeEntry, Timestamp> incomePaymentDateColumn;
    @FXML private TableColumn<TrainerIncomeEntry, BigDecimal> incomeAmountColumn;
    @FXML private TableColumn<TrainerIncomeEntry, String> incomeTrainingNotesColumn;

    // ObservableList dla dochodów
    private ObservableList<TrainerIncomeEntry> incomeList = FXCollections.observableArrayList();
    // FXML elements for Reports section (general reports table)
    @FXML private TableView<ReportEntry> reportsTable;
    @FXML private TableColumn<ReportEntry, Integer> reportIdColumn;
    @FXML private TableColumn<ReportEntry, Integer> reportClientIdColumn;
    @FXML private TableColumn<ReportEntry, String> reportClientNameColumn;
    @FXML private TableColumn<ReportEntry, String> reportNotesColumn;
    @FXML private TableColumn<ReportEntry, String> reportTrainerNameColumn;

    // Panel produktów
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, BigDecimal> productPriceColumn;
    @FXML private TableColumn<Product, Integer> productStockColumn;

    // FXML elements for Progress section
    @FXML private TableView<TrainerTask> tasksProgressTable;
    @FXML private TableColumn<TrainerTask, Integer> taskClientIdColumn;
    @FXML private TableColumn<TrainerTask, String> taskClientNameColumn;
    @FXML private TableColumn<TrainerTask, LocalDate> taskTrainingDateColumn;
    @FXML private TableColumn<TrainerTask, String> taskDescriptionColumn;
    @FXML private TableColumn<TrainerTask, String> taskPriorityColumn;
    @FXML private TableColumn<TrainerTask, String> taskStatusColumn;

      // Będzie wyświetlać nazwę statusu

    @FXML private Label editTaskClientLabel;
    @FXML private Label editTaskTrainingDateLabel;
    @FXML private TextArea editTaskDescriptionArea; // Użyj TextArea zamiast TextField dla opisu
    @FXML private ComboBox<Priority> editTaskPriorityCombo; // Zakładam klasę Priority
    @FXML private ComboBox<Status> editTaskStatusCombo;     // Zakładam klasę Status

    private ObservableList<TrainerTask> trainerTasksList;
    private ObservableList<Priority> prioritiesList; // Lista priorytetów z bazy
    private ObservableList<Status> statusesList;



    // ObservableList for tasks
    private ObservableList<TrainerTask> tasksList = FXCollections.observableArrayList();


    // Data for the trainer
    private int trainerId;
    private String trainerName;

    // ObservableLists
    private ObservableList<Client> clientsList = FXCollections.observableArrayList();
    private ObservableList<PendingTrainingRequest> pendingRequestsList = FXCollections.observableArrayList();
    private ObservableList<TrainingScheduleEntry> trainingScheduleList = FXCollections.observableArrayList(); // NOWA lista dla harmonogramu
    private ObservableList<ReportEntry> allReportsList = FXCollections.observableArrayList();
    private ObservableList<Product> productList = FXCollections.observableArrayList();


    /**
     * Inicjalizuje kontroler panelu trenera.
     * Metoda wywoływana automatycznie po załadowaniu pliku FXML.
     * Konfiguruje komponenty interfejsu użytkownika, inicjalizuje tabele
     * i przygotowuje sekcje do wyświetlania danych.
     */
    @FXML
    public void initialize() {
        initializeTrainingPlans(); // Nazwa metody odzwierciedla teraz "Ustal trening"
        initializeSchedule(); // Inicjalizacja harmonogramu
        initializeReportsTable();
        initializeProductsTable();
        initializeIncomeTable(); // <-- DODAJ TĘ LINIĘ

        showDashboard(); // Domyślnie pokazujemy dashboard
    }


    // --- INITIALIZATION METHODS ---
    /**
     * Inicjalizuje tabelę przychodów trenera.
     * Konfiguruje powiązania między kolumnami tabeli a właściwościami modelu danych,
     * ustawia formatowanie dat oraz przygotowuje tabelę do wyświetlania danych 
     * o przychodach z treningów.
     * 
     * Formatowanie kolumn obejmuje:
     * - Identyfikator płatności
     * - Imię i nazwisko klienta
     * - Data treningu (sformatowana jako YYYY-MM-DD)
     * - Data płatności (z zachowaniem pełnego formatu Timestamp)
     * - Kwota płatności
     * - Notatki związane z treningiem
     */
    private void initializeIncomeTable() {
        incomePaymentIdColumn.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        incomeClientNameColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));

        // Formatowanie kolumny 'Data/Czas Treningu' (tylko data, bez godziny)
        incomeTrainingDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("trainingDateTime"));
        incomeTrainingDateTimeColumn.setCellFactory(column -> new TableCell<TrainerIncomeEntry, LocalDateTime>() {
            // Formatter dla samej daty (YYYY-MM-DD)
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Formatujemy LocalDateTime na String w formacie samej daty
                    setText(formatter.format(item));
                }
            }
        });

        // Pozostawiamy kolumnę 'Data Płatności' bez dodatkowego formatowania,
        // czyli będzie wyświetlana tak jak jest w obiekcie Timestamp (z godziną, jeśli jest)
        incomePaymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));

        incomeAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        incomeTrainingNotesColumn.setCellValueFactory(new PropertyValueFactory<>("trainingNotes"));

        incomeTable.setItems(incomeList);
    }
    /**
     * Ładuje dane o przychodach trenera z bazy danych.
     * Pobiera informacje o płatnościach za treningi powiązane z danym trenerem,
     * łącząc dane z tabel training_request_payments, trainingrequests, reports i users.
     * Oblicza również sumę wszystkich przychodów i aktualizuje odpowiednią etykietę w UI.
     * Wyniki sortowane są malejąco według daty płatności, co pozwala na łatwe
     * śledzenie najnowszych transakcji.
     * Metoda czyści listę przychodów przed załadowaniem nowych danych,
     * obsługuje połączenie z bazą danych oraz wszelkie wyjątki SQL.
     */
    private void loadIncomeReports() {
        incomeList.clear();
        BigDecimal totalIncome = BigDecimal.ZERO;

        String sql = "SELECT tr_p.id AS payment_id, " + // <--- ZMIANA TUTAJ: tr_p.id zamiast tr_p.payment_id
                "c.name AS client_name, " +
                "tr.training_date, " +
                "tr_p.payment_date, " +
                "tr_p.amount, " +
                "r.notes AS request_notes " +
                "FROM training_request_payments tr_p " +
                "JOIN trainingrequests tr ON tr_p.training_request_id = tr.id " +
                "JOIN reports r ON tr.report = r.id " +
                "JOIN users c ON r.client_id = c.id " +
                "WHERE r.trainer_id = ? " +
                "ORDER BY tr_p.payment_date DESC";

        System.out.println("DEBUG [loadIncomeReports]: SQL: " + sql);
        System.out.println("DEBUG [loadIncomeReports]: Trainer ID: " + trainerId);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, trainerId);
            ResultSet rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                // Konwersja Date na LocalDateTime (jeśli training_date jest typu DATE)
                LocalDate trainingDate = null;
                if (rs.getDate("training_date") != null) {
                    trainingDate = rs.getDate("training_date").toLocalDate();
                }
                LocalDateTime trainingDateTime = (trainingDate != null) ? trainingDate.atStartOfDay() : null;


                TrainerIncomeEntry entry = new TrainerIncomeEntry(
                        rs.getInt("payment_id"), // Tutaj używamy aliasu 'payment_id'
                        rs.getString("client_name"),
                        trainingDateTime,
                        rs.getTimestamp("payment_date"),
                        rs.getBigDecimal("amount"),
                        rs.getString("request_notes")
                );
                incomeList.add(entry);
                totalIncome = totalIncome.add(entry.getAmount());
                count++;
            }
            System.out.println("DEBUG [loadIncomeReports]: Załadowano " + count + " wpisów dochodów.");
        } catch (SQLException e) {
            System.err.println("BŁĄD SQL [loadIncomeReports]: " + e.getMessage());
            e.printStackTrace();
            showError("Błąd ładowania danych o dochodach: " + e.getMessage());
        }

        totalIncomeLabel.setText(String.format("%.2f PLN", totalIncome));
    }


    /**
     * Inicjalizuje sekcję postępów zadań trenera.
     * Konfiguruje tabele, listy i komponenty UI do zarządzania zadaniami trenera.
     * Metoda wykonuje następujące czynności:
     * - Konfiguruje kolumny tabeli zadań i ich powiązania z danymi modelu
     * - Inicjalizuje listy obserwowalne dla zadań, priorytetów i statusów
     * - Konfiguruje pola wyboru (ComboBox) dla priorytetów i statusów
     * - Ustawia wyświetlanie nazw priorytetów i statusów zamiast ich identyfikatorów
     * - Dodaje obsługę zdarzeń dla wyboru zadania w tabeli
     * Wywołana podczas inicjalizacji kontrolera po załadowaniu danych trenera.
     */
    private void initializeProgressSection() {
        // Ustawienie CellValueFactory dla kolumn tabeli zadań
        taskClientIdColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getClientId()));
        taskClientNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClientName()));
        taskTrainingDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTrainingDate()));
        taskDescriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTaskDescription()));

        // Dla priorytetu i statusu wyświetlamy nazwy, a nie ID
        taskPriorityColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        prioritiesList.stream()
                                .filter(p -> p.getId() == cellData.getValue().getPriorityId())
                                .map(Priority::getName)
                                .findFirst()
                                .orElse("Nieznany")
                )
        );
        taskStatusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        statusesList.stream()
                                .filter(s -> s.getId() == cellData.getValue().getStatusId())
                                .map(Status::getName)
                                .findFirst()
                                .orElse("Nieznany")
                )
        );

        // Inicjalizacja ObservableList dla zadań i powiązanie z tabelą
        trainerTasksList = FXCollections.observableArrayList();
        tasksProgressTable.setItems(trainerTasksList);

        // Konfiguracja ComboBoxów dla priorytetu i statusu
        prioritiesList = FXCollections.observableArrayList(); // Inicjalizacja (dane załadujesz w loadPrioritiesAndStatuses)
        statusesList = FXCollections.observableArrayList();   // Inicjalizacja (dane załadujesz w loadPrioritiesAndStatuses)
        editTaskPriorityCombo.setItems(prioritiesList);
        editTaskStatusCombo.setItems(statusesList);

        // Ustawienie CellFactory dla ComboBoxów, aby wyświetlały nazwy obiektów (Priority/Status)
        editTaskPriorityCombo.setCellFactory(new Callback<ListView<Priority>, ListCell<Priority>>() {
            @Override
            public ListCell<Priority> call(ListView<Priority> p) {
                return new ListCell<Priority>() {
                    @Override
                    protected void updateItem(Priority item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getName());
                    }
                };
            }
        });
        editTaskPriorityCombo.setButtonCell(new ListCell<Priority>() {
            @Override
            protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        editTaskStatusCombo.setCellFactory(new Callback<ListView<Status>, ListCell<Status>>() {
            @Override
            public ListCell<Status> call(ListView<Status> p) {
                return new ListCell<Status>() {
                    @Override
                    protected void updateItem(Status item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getName());
                    }
                };
            }
        });
        editTaskStatusCombo.setButtonCell(new ListCell<Status>() {
            @Override
            protected void updateItem(Status item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });


        // Listener zaznaczenia w tabeli zadań
        tasksProgressTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displaySelectedTask(newSelection);
            } else {
                clearTaskEditForm();
            }
        });
    }

    /**
     * Ustawia dane trenera w panelu.
     * Inicjalizuje kontroler danymi trenera, ładuje jego klientów, 
     * prośby o treningi, zadania i inne dane niezbędne do funkcjonowania
     * panelu trenera.
     * 
     * @param trainerId identyfikator trenera w bazie danych
     * @param trainerName imię i nazwisko trenera
     */
    public void setTrainerData(int trainerId, String trainerName) {
        this.trainerId = trainerId;
        this.trainerName = trainerName;
        System.out.println("TrainerDashboardController: setTrainerData called. Trainer ID: " + trainerId + ", Name: " + trainerName);
        // Po ustawieniu danych trenera, załaduj odpowiednie dane dla widoków
        loadAllTrainerReports(); // Dla sekcji "Ustal trening" (prośby od klientów)
        loadClientsForTrainingPlan(); // Dla ComboBoxa w sekcji "Ustal trening"
        loadAllReports(); // Dla sekcji "Raporty" (wszystkie raporty trenera)
        loadProducts(); // Załaduj produkty przy starcie
        loadTrainingSchedule(); // Ładuj harmonogram po ustawieniu ID trenera

        // NOWE: Wywołaj inicjalizację sekcji Postępy tutaj, po ustawieniu danych trenera
        // i po tym, jak FXMLLoader na pewno wstrzyknął wszystkie @FXML elementy
        initializeProgressSection(); // <-- DODAJ TĘ LINIĘ TUTAJ!
        loadPrioritiesAndStatuses(); // Też tutaj, bo są potrzebne dla initializeProgressSection
        loadTrainerTasks();          // I to też tutaj, aby dane były załadowane od razu
    }

    /**
     * Ładuje listy priorytetów i statusów zadań z bazy danych.
     * Pobiera dane z tabel 'priorities' i 'statuses', a następnie wypełnia nimi
     * odpowiednie listy obserwowalne (prioritiesList i statusesList), które są
     * używane w interfejsie użytkownika do wyboru priorytetu i statusu zadania.
     * Metoda obsługuje połączenie z bazą danych oraz wszelkie wyjątki SQL.
     * Rejestruje liczbę załadowanych elementów dla celów diagnostycznych.
     */
    private void loadPrioritiesAndStatuses() {
        prioritiesList.clear();
        statusesList.clear();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            // Zmieniono 'task_priorities' na 'priorities' oraz nazwy kolumn
            String sqlPriorities = "SELECT priority_id AS id, priority_name AS name FROM priorities ORDER BY priority_id";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlPriorities);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Pobieramy dane używając aliasów 'id' i 'name'
                    prioritiesList.add(new Priority(rs.getInt("id"), rs.getString("name")));
                }
            }

            // Zmieniono 'task_statuses' na 'statuses' oraz nazwy kolumn
            String sqlStatuses = "SELECT status_id AS id, status_name AS name FROM statuses ORDER BY status_id";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlStatuses);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Pobieramy dane używając aliasów 'id' i 'name'
                    statusesList.add(new Status(rs.getInt("id"), rs.getString("name")));
                }
            }
            System.out.println("Loaded " + prioritiesList.size() + " priorities and " + statusesList.size() + " statuses.");

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
     * Wyświetla szczegóły wybranego zadania w formularzu edycji.
     * Wypełnia pola formularza danymi z wybranego zadania, w tym:
     * informacje o kliencie, datę treningu, opis zadania,
     * oraz ustawia wybrane wartości w polach wyboru priorytetu i statusu.
     * 
     * @param task Obiekt zadania trenera, które ma być wyświetlone w formularzu
     *             Jeśli null, formularz zostanie wyczyszczony
     */
    private void displaySelectedTask(TrainerTask task) {
        if (task == null) { // Dodatkowe zabezpieczenie
            clearTaskEditForm();
            return;
        }
        editTaskClientLabel.setText("Klient: " + task.getClientName());
        editTaskTrainingDateLabel.setText("Data treningu: " + task.getTrainingDate());
        editTaskDescriptionArea.setText(task.getTaskDescription());

        // Wybierz priorytet w ComboBoxie na podstawie ID
        editTaskPriorityCombo.getSelectionModel().select(
                prioritiesList.stream()
                        .filter(p -> p.getId() == task.getPriorityId())
                        .findFirst()
                        .orElse(null)
        );

        // Wybierz status w ComboBoxie na podstawie ID
        statusesList.forEach(s -> System.out.println("Status: " + s.getName() + " ID: " + s.getId())); // Debugowanie
        editTaskStatusCombo.getSelectionModel().select(
                statusesList.stream()
                        .filter(s -> s.getId() == task.getStatusId())
                        .findFirst()
                        .orElse(null)
        );
    }

    /**
     * Czyści formularz edycji zadania.
     * Resetuje wszystkie pola formularza do wartości domyślnych, 
     * usuwając informacje o kliencie, dacie treningu,
     * opisie zadania oraz wybranym priorytecie i statusie.
     * Wywoływana przy anulowaniu edycji lub po zakończeniu aktualizacji zadania.
     */
    private void clearTaskEditForm() {
        editTaskClientLabel.setText("Klient: ");
        editTaskTrainingDateLabel.setText("Data treningu: ");
        editTaskDescriptionArea.clear();
        editTaskPriorityCombo.getSelectionModel().clearSelection();
        editTaskStatusCombo.getSelectionModel().clearSelection();
    }

    /**
     * Obsługuje aktualizację postępu zadania treningowego.
     * Pobiera wybrane zadanie z tabeli, sprawdza poprawność wprowadzonych danych,
     * a następnie aktualizuje priorytet i status zadania w bazie danych.
     * Metoda wykonuje walidację danych wejściowych, sprawdza czy nastąpiła faktyczna
     * zmiana, obsługuje transakcję bazodanową i wyświetla odpowiednie komunikaty.
     * Po pomyślnej aktualizacji odświeża listę zadań i czyści formularz.
     */
    @FXML
    private void handleUpdateTaskProgress() {
        TrainerTask selectedTask = tasksProgressTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showError("Wybierz zadanie do zaktualizowania.");
            return;
        }

        Priority newPriority = editTaskPriorityCombo.getSelectionModel().getSelectedItem();
        Status newStatus = editTaskStatusCombo.getSelectionModel().getSelectedItem();

        if (newPriority == null || newStatus == null) {
            showError("Wybierz priorytet i status zadania.");
            return;
        }

        // Możesz dodać walidację, czy faktycznie coś się zmieniło, zanim wykonasz UPDATE
        if (selectedTask.getPriorityId() == newPriority.getId() &&
                selectedTask.getStatusId() == newStatus.getId()) {
            showAlert("Informacja", "Nie zmieniono priorytetu ani statusu zadania.");
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "UPDATE trainer_tasks SET priority_id = ?, status_id = ? WHERE task_id = ?"; // Zmieniono WHERE id = ? na WHERE task_id = ?
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, newPriority.getId());
                pstmt.setInt(2, newStatus.getId());
                pstmt.setInt(3, selectedTask.getId()); // Tutaj 'getId()' z TrainerTask zwróci już 'task_id'

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    showAlert("Sukces", "Zadanie zaktualizowane pomyślnie.");
                    loadTrainerTasks(); // Odśwież tabelę po aktualizacji
                    clearTaskEditForm(); // Wyczyść formularz
                } else {
                    showError("Nie udało się zaktualizować zadania.");
                }
            }
        } catch (SQLException e) {
            showError("Błąd aktualizacji zadania: " + e.getMessage());
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
     * Ładuje zadania trenera z bazy danych.
     * Pobiera wszystkie zadania powiązane z zalogowanym trenerem, łącząc informacje
     * z tabel trainer_tasks, trainingrequests, reports i users.
     * Wyniki sortowane są malejąco według daty treningu i identyfikatora zadania,
     * co pozwala na łatwe śledzenie najnowszych zadań.
     * Metoda czyści listę zadań przed załadowaniem nowych danych,
     * obsługuje połączenie z bazą danych oraz wszelkie wyjątki SQL.
     */
    private void loadTrainerTasks() {
        trainerTasksList.clear();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT tt.task_id AS task_id, tt.training_request_id, tt.priority_id, tt.status_id, tt.task_description, " +
                    "tr.training_date, u.id AS client_id, u.name AS client_name " +
                    "FROM trainer_tasks tt " +
                    "JOIN trainingrequests tr ON tt.training_request_id = tr.id " +
                    "JOIN reports r ON tr.report = r.id " +
                    "JOIN users u ON r.client_id = u.id " +
                    "WHERE r.trainer_id = ? ORDER BY tr.training_date DESC, tt.task_id DESC"; // Tutaj też zmień tt.id na tt.task_id

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, trainerId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        trainerTasksList.add(new TrainerTask(
                                rs.getInt("task_id"), // Używasz aliasu 'task_id' z zapytania
                                rs.getInt("training_request_id"),
                                rs.getInt("client_id"),
                                rs.getString("client_name"),
                                rs.getDate("training_date").toLocalDate(),
                                rs.getString("task_description"),
                                rs.getInt("priority_id"),
                                rs.getInt("status_id")
                        ));
                    }
                }
            }
            System.out.println("Loaded " + trainerTasksList.size() + " trainer tasks for trainer ID: " + trainerId);
        } catch (SQLException e) {
            showError("Błąd ładowania zadań trenera: " + e.getMessage());
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
     * Wyświetla sekcję postępów zadań trenera.
     * Ukrywa wszystkie inne sekcje, pokazuje sekcję postępów i odświeża
     * dane o zadaniach trenera, aby zapewnić aktualność wyświetlanych informacji.
     * Metoda wywoływana po kliknięciu odpowiedniego przycisku nawigacyjnego w interfejsie.
     */
    @FXML
    public void showProgress() {
        hideAllSections();
        progressSection.setVisible(true);
        loadTrainerTasks(); // Odśwież dane za każdym razem, gdy przechodzisz do zakładki
    }

    private void initializeTrainingPlans() { // Nazwa odzwierciedla "Ustal trening"
        pendingRequestIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        pendingClientIdColumn.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        pendingClientNameColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        pendingNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        pendingRequestsTable.setItems(pendingRequestsList);

        pendingRequestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displaySelectedRequest(newSelection);
            } else {
                clearSelectedRequest();
            }
        });

        planClientCombo.setItems(clientsList);
        planClientCombo.setConverter(new StringConverter<Client>() {
            @Override
            public String toString(Client client) {
                return client != null ? client.getName() : "";
            }

            @Override
            public Client fromString(String string) {
                return clientsList.stream()
                        .filter(client -> client.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        acceptRequestButton.setDisable(true);
        selectedRequestLabel.setText("Wybierz prośbę z listy powyżej, aby ją zaplanować.");
        clearTrainingPlanForm();
    }

    private void initializeSchedule() {
        // NOWE: Inicjalizacja kolumn dla tabeli harmonogramu
        scheduleIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        scheduleClientNameColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        scheduleDateColumn.setCellValueFactory(new PropertyValueFactory<>("trainingDate"));
        scheduleNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        trainingScheduleTable.setItems(trainingScheduleList);
    }

    private void initializeReportsTable() {
        reportIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        reportClientIdColumn.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        reportClientNameColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        reportNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        reportTrainerNameColumn.setCellValueFactory(new PropertyValueFactory<>("trainerName"));
        reportsTable.setItems(allReportsList);
    }

    private void initializeProductsTable() {
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productStockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        productsTable.setItems(productList);
    }


    // --- DATA LOADING METHODS ---

    private void loadAllTrainerReports() {
        pendingRequestsList.clear();
        // ZAPYTANIE SQL, które wybiera raporty, dla których NIE MA JESZCZE powiązanego wpisu w trainingrequests
        String sql = "SELECT r.id, r.client_id, u.name AS client_name, r.notes " +
                "FROM reports r " +
                "JOIN users u ON r.client_id = u.id " +
                "LEFT JOIN trainingrequests tr ON r.id = tr.report " + // LEFT JOIN, aby znaleźć pasujące rekordy
                "WHERE r.trainer_id = ? AND tr.report IS NULL " +     // Filtrujemy, gdzie nie ma pasującego wpisu (tr.report IS NULL)
                "ORDER BY r.id ASC";

        System.out.println("Loading pending reports for trainer ID: " + trainerId);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, trainerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                pendingRequestsList.add(new PendingTrainingRequest(
                        rs.getInt("id"),
                        rs.getInt("client_id"),
                        rs.getString("client_name"),
                        rs.getString("notes")
                ));
            }
            System.out.println("Loaded " + pendingRequestsList.size() + " pending reports for 'Ustal trening' (trainer ID: " + trainerId + ")");
        } catch (SQLException e) {
            showError("Błąd ładowania próśb o trening (dla sekcji 'Ustal trening'): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAllReports() {
        allReportsList.clear();
        System.out.println("Attempting to load all reports for trainer ID: " + trainerId);
        // To zapytanie pobiera wszystkie raporty, w tym te, dla których utworzono już plan treningowy
        String sql = "SELECT r.id, r.client_id, u.name AS client_name, r.notes, ut.name AS trainer_name " +
                "FROM reports r " +
                "JOIN users u ON r.client_id = u.id " +
                "JOIN users ut ON r.trainer_id = ut.id " +
                "WHERE r.trainer_id = ? " +
                "ORDER BY r.id ASC";
        System.out.println("SQL Query for All Reports: " + sql);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, trainerId);
            System.out.println("Executing query for All Reports with trainerId: " + trainerId);
            ResultSet rs = pstmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                allReportsList.add(new ReportEntry(
                        rs.getInt("id"),
                        rs.getInt("client_id"),
                        rs.getString("client_name"),
                        rs.getString("notes"),
                        rs.getString("trainer_name")
                ));
                count++;
            }
            System.out.println("Loaded " + count + " reports for 'Wszystkie Raporty' (trainer ID: " + trainerId + ")");
        } catch (SQLException e) {
            System.err.println("SQL Exception in loadAllReports: " + e.getMessage());
            e.printStackTrace();
            showError("Błąd ładowania wszystkich raportów (dla sekcji 'Raporty'): " + e.getMessage());
        }
    }

    private void loadTrainingSchedule() {
        trainingScheduleList.clear();
        String sql = "SELECT tr.id, tr.training_date, tr.notes, u.name AS client_name " +
                "FROM trainingrequests tr " +
                "JOIN reports r ON tr.report = r.id " +
                "JOIN users u ON r.client_id = u.id " +
                "WHERE r.trainer_id = ? " +
                "ORDER BY tr.training_date ASC";

        System.out.println("DEBUG [loadTrainingSchedule]: SQL: " + sql);
        System.out.println("DEBUG [loadTrainingSchedule]: Trainer ID: " + trainerId);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, trainerId);
            ResultSet rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                trainingScheduleList.add(new TrainingScheduleEntry(
                        rs.getInt("id"),
                        rs.getString("client_name"),
                        rs.getDate("training_date").toLocalDate(),
                        rs.getString("notes")
                ));
                count++;
            }
            System.out.println("DEBUG [loadTrainingSchedule]: Załadowano " + count + " wpisów");
        } catch (SQLException e) {
            System.err.println("BŁĄD SQL [loadTrainingSchedule]: " + e.getMessage());
            e.printStackTrace();
            showError("Błąd ładowania harmonogramu: " + e.getMessage());
        }
    }


    private void loadClientsForTrainingPlan() {
        clientsList.clear();
        String sql = "SELECT id, name FROM users WHERE role = 'client'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                clientsList.add(new Client(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            showError("Błąd ładowania klientów: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadProducts() {
        productList.clear();
        String sql = "SELECT id, name, price, stock FROM products ORDER BY name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                productList.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price"),
                        rs.getInt("stock")
                ));
            }
            System.out.println("Loaded " + productList.size() + " products.");
        } catch (SQLException e) {
            System.err.println("Błąd ładowania produktów: " + e.getMessage());
            e.printStackTrace();
            showError("Błąd ładowania produktów: " + e.getMessage());
        }
    }

    // --- UI Update & Form Management ---

    private void displaySelectedRequest(PendingTrainingRequest request) {
        selectedRequestLabel.setText("Wybrana prośba: Klient: " + request.getClientName() + ", Uwagi: " + request.getNotes());
        // Automatycznie wybieramy klienta w ComboBoxie, jeśli jest dostępny
        planClientCombo.getSelectionModel().select(
                clientsList.stream()
                        .filter(c -> c.getId() == request.getClientId())
                        .findFirst()
                        .orElse(null)
        );
        planDatePicker.setValue(null); // Czyścimy datę
        exercise1Field.clear();       // Czyścimy pola ćwiczeń
        exercise2Field.clear();
        exercise3Field.clear();
        acceptRequestButton.setDisable(false); // Aktywujemy przycisk zapisu
    }

    private void clearSelectedRequest() {
        selectedRequestLabel.setText("Wybierz prośbę z listy powyżej, aby ją zaplanować.");
        planClientCombo.getSelectionModel().clearSelection();
        planDatePicker.setValue(null);
        exercise1Field.clear();
        exercise2Field.clear();
        exercise3Field.clear();
        acceptRequestButton.setDisable(true);
    }

    private void clearTrainingPlanForm() {
        planClientCombo.getSelectionModel().clearSelection();
        planDatePicker.setValue(null);
        exercise1Field.clear();
        exercise2Field.clear();
        exercise3Field.clear();
    }

    // --- SECTION TOGGLE METHODS ---

    private void hideAllSections() {
        dashboardSection.setVisible(false);
        trainingPlansSection.setVisible(false);
        scheduleSection.setVisible(false);
        incomeSection.setVisible(false);
        progressSection.setVisible(false);
        reportsSection.setVisible(false);
        productsSection.setVisible(false);
    }

    /**
     * Wyświetla główny panel (dashboard) trenera.
     * Ukrywa wszystkie inne sekcje i pokazuje sekcję głównego panelu,
     * która zawiera podsumowanie aktywności trenera i podstawowe statystyki.
     */
    @FXML
    public void showDashboard() {
        hideAllSections();
        dashboardSection.setVisible(true);
    }

    @FXML
    public void showTrainingPlans() { // Nazwa metody odzwierciedla "Ustal trening"
        hideAllSections();
        trainingPlansSection.setVisible(true);
        loadAllTrainerReports(); // Odśwież listę próśb za każdym razem, gdy przechodzisz do zakładki
        loadClientsForTrainingPlan(); // Upewnij się, że combobox klientów jest aktualny
        clearSelectedRequest(); // Czyścimy formularz
    }

    /**
     * Wyświetla sekcję harmonogramu treningów trenera.
     * Ukrywa wszystkie inne sekcje, pokazuje sekcję harmonogramu
     * i odświeża dane o zaplanowanych treningach z klientami.
     * Pozwala na przeglądanie i zarządzanie terminami treningów.
     */
    @FXML
    public void showSchedule() {
        hideAllSections();
        scheduleSection.setVisible(true);
        loadTrainingSchedule();
        trainingScheduleTable.refresh(); // Wymuś odświeżenie
        System.out.println("DEBUG [showSchedule]: Liczba wpisów w harmonogramie: " + trainingScheduleList.size());
    }

    /**
     * Wyświetla sekcję przychodów trenera.
     * Ukrywa wszystkie inne sekcje, pokazuje sekcję przychodów
     * i odświeża dane o przychodach trenera z przeprowadzonych treningów.
     * Prezentuje szczegółowe informacje o wszystkich płatnościach.
     */
    @FXML
    public void showIncome() {
        hideAllSections();
        incomeSection.setVisible(true);
        loadIncomeReports(); // <-- DODAJ TĘ LINIĘ
    }



    /**
     * Wyświetla sekcję raportów trenera.
     * Ukrywa wszystkie inne sekcje, pokazuje sekcję raportów
     * i odświeża dane o raportach klientów przypisanych do trenera.
     * Umożliwia przeglądanie i analizę raportów treningowych.
     */
    @FXML
    public void showReports() {
        hideAllSections();
        reportsSection.setVisible(true);
        loadAllReports(); // Ładuj dane specyficzne dla tabeli "Raporty"
    }

    /**
     * Wyświetla sekcję produktów dostępnych w siłowni.
     * Ukrywa wszystkie inne sekcje, pokazuje sekcję produktów
     * i odświeża listę dostępnych produktów, ich cen i stanów magazynowych.
     * Pozwala trenerowi na przeglądanie asortymentu siłowni.
     */
    @FXML
    public void showProducts() {
        hideAllSections();
        productsSection.setVisible(true);
        loadProducts(); // Odśwież dane produktów za każdym razem, gdy przechodzisz do zakładki
    }

    // --- ACTION METHODS ---

    /**
     * Obsługuje zapisanie planu treningowego dla wybranego klienta.
     * Weryfikuje poprawność wprowadzonych danych, tworzy plan treningowy
     * z wybranymi ćwiczeniami, zapisuje go w bazie danych i tworzy
     * odpowiednie zadanie dla trenera. Po zapisaniu odświeża wszystkie
     * powiązane widoki i wyświetla komunikat o sukcesie.
     */
    @FXML
    public void handleSaveTrainingPlan() {
        PendingTrainingRequest selectedRequest = pendingRequestsTable.getSelectionModel().getSelectedItem();
        LocalDate trainingDate = planDatePicker.getValue();
        String exercise1 = exercise1Field.getText();
        String exercise2 = exercise2Field.getText();
        String exercise3 = exercise3Field.getText();

        if (selectedRequest == null) {
            showError("Wybierz prośbę o trening z listy.");
            return;
        }
        if (trainingDate == null) {
            showError("Wybierz datę treningu.");
            return;
        }
        if (exercise1.isEmpty() && exercise2.isEmpty() && exercise3.isEmpty()) {
            showError("Wprowadź przynajmniej jedno ćwiczenie.");
            return;
        }

        String exercisesNotes = "";
        if (!exercise1.isEmpty()) exercisesNotes += "1. " + exercise1 + "\n";
        if (!exercise2.isEmpty()) exercisesNotes += "2. " + exercise2 + "\n";
        if (!exercise3.isEmpty()) exercisesNotes += "3. " + exercise3 + "\n";
        exercisesNotes = exercisesNotes.trim(); // Usunięcie końcowych pustych linii

        String sqlInsertTraining = "INSERT INTO trainingrequests (report, training_date, notes) VALUES (?, ?, ?)";
        // NOWE: Zapytanie do wstawienia zadania do tabeli trainer_tasks
        String sqlInsertTask = "INSERT INTO trainer_tasks (training_request_id, priority_id, status_id, task_description) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Rozpocznij transakcję

            // 1. Wstawianie do trainingrequests
            int generatedTrainingRequestId = -1; // Zmienna do przechowania wygenerowanego ID
            // Użyj PreparedStatement.RETURN_GENERATED_KEYS, aby pobrać ID nowo wstawionego wiersza
            try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsertTraining, PreparedStatement.RETURN_GENERATED_KEYS)) {
                pstmtInsert.setInt(1, selectedRequest.getId());
                pstmtInsert.setDate(2, java.sql.Date.valueOf(trainingDate));
                pstmtInsert.setString(3, exercisesNotes);

                int affectedRowsInsert = pstmtInsert.executeUpdate();

                if (affectedRowsInsert > 0) {
                    // Pobierz wygenerowane ID z trainingrequests
                    try (ResultSet generatedKeys = pstmtInsert.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            generatedTrainingRequestId = generatedKeys.getInt(1);
                        }
                    }

                    // 2. NOWE: Wstawianie do trainer_tasks
                    if (generatedTrainingRequestId != -1) {
                        // Znajdź domyślny priorytet i status (np. "Pośrednie" i "do zrobienia")
                        // WAŻNE: Upewnij się, że prioritiesList i statusesList są już załadowane
                        // (np. w setTrainerData() lub initialize())
                        int defaultPriorityId = prioritiesList.stream()
                                .filter(p -> p.getName().equals("Pośrednie")) // Użyj "Pośrednie" lub "Ważne"
                                .map(Priority::getId)
                                .findFirst()
                                .orElseThrow(() -> new SQLException("Default Priority 'Pośrednie' not found in loaded data. Ensure loadPrioritiesAndStatuses() is called."));

                        int defaultStatusId = statusesList.stream()
                                .filter(s -> s.getName().equals("do zrobienia"))
                                .map(Status::getId)
                                .findFirst()
                                .orElseThrow(() -> new SQLException("Default Status 'do zrobienia' not found in loaded data. Ensure loadPrioritiesAndStatuses() is called."));

                        try (PreparedStatement pstmtInsertTask = conn.prepareStatement(sqlInsertTask)) {
                            pstmtInsertTask.setInt(1, generatedTrainingRequestId);
                            pstmtInsertTask.setInt(2, defaultPriorityId);
                            pstmtInsertTask.setInt(3, defaultStatusId);
                            pstmtInsertTask.setString(4, exercisesNotes); // Opis zadania jako notatki z treningu

                            pstmtInsertTask.executeUpdate();
                        }
                    } else {
                        throw new SQLException("Failed to retrieve generated training request ID. Task not created.");
                    }

                    conn.commit(); // Zatwierdź transakcję, jeśli wszystko poszło dobrze
                    showAlert("Sukces", "Plan treningowy i zadanie postępu zostały zapisane dla klienta " + selectedRequest.getClientName() + ".");
                    clearTrainingPlanForm();
                    loadAllTrainerReports(); // Odśwież listę próśb
                    clearSelectedRequest();
                    loadAllReports(); // Odśwież ogólne raporty
                    loadTrainingSchedule(); // Odśwież harmonogram
                    loadTrainerTasks();     // NOWE: Odśwież listę zadań w sekcji Postępy
                } else {
                    conn.rollback(); // Wycofaj transakcję, jeśli trainingrequests się nie wstawilo
                    showError("Nie udało się zapisać planu treningowego.");
                }
            }
        } catch (SQLException e) {
            // Obsługa błędu transakcji
            if (conn != null) {
                try {
                    conn.rollback(); // Wycofaj transakcję w przypadku błędu
                } catch (SQLException rollbackEx) {
                    System.err.println("Błąd podczas wycofywania transakcji: " + rollbackEx.getMessage());
                }
            }
            showError("Błąd zapisu planu treningowego i zadania: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Zawsze przywróć auto-commit i zamknij połączenie
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Błąd podczas zamykania połączenia: " + closeEx.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleAddProduct() {
        showAlert("Dodaj Produkt", "Logika dodawania nowego produktu do zaimplementowania.");
        // Otwórz nowe okno/dialog z formularzem do dodawania produktu
        // Wymaga osobnego FXML i kontrolera dla formularza dodawania/edycji produktu
    }

    /**
     * Wyświetla sekcję planów treningowych.
     * Ukrywa wszystkie inne sekcje, pokazuje sekcję planów treningowych
     * i odświeża dane o prośbach klientów o treningi. Umożliwia trenerowi
     * tworzenie i przypisywanie planów treningowych do klientów.
     */
    @FXML
    private void handleEditProduct() {
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            showAlert("Edytuj Produkt", "Logika edycji produktu '" + selectedProduct.getName() + "' do zaimplementowania.");
            // Otwórz okno/dialog z formularzem edycji, pre-wypełnij danymi wybranego produktu
        } else {
            showAlert("Edytuj Produkt", "Wybierz produkt z listy do edycji.");
        }
    }

    @FXML
    private void handleDeleteProduct() {
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Potwierdź usunięcie");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Czy na pewno chcesz usunąć produkt: " + selectedProduct.getName() + "?");
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String sql = "DELETE FROM products WHERE id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, selectedProduct.getId());
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        showAlert("Sukces", "Produkt '" + selectedProduct.getName() + "' został usunięty.");
                        loadProducts(); // Odśwież listę po usunięciu
                    } else {
                        showError("Nie udało się usunąć produktu.");
                    }
                } catch (SQLException e) {
                    showError("Błąd usuwania produktu: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            showAlert("Usuń Produkt", "Wybierz produkt z listy do usunięcia.");
        }
    }


    // --- UTILITY METHODS ---
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Obsługuje wylogowanie trenera z systemu.
     * Zamyka aktualny panel trenera i wyświetla ekran logowania.
     * Resetuje wszystkie dane sesji trenera.
     */
    @FXML
    public void logout() {
        try {
            Stage stage = (Stage) dashboardSection.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Black Iron Gym - Logowanie");
            loginStage.setScene(new Scene(root, 400, 300));
            loginStage.show();
            stage.close();
        } catch (IOException e) {
            showError("Nie udało się wrócić do ekranu logowania: " + e.getMessage());
        }
    }

    // --- INNER MODEL CLASSES ---

    /**
     * Klasa reprezentująca oczekujące żądanie treningu personalnego.
     * Przechowuje informacje o żądaniu, takie jak identyfikator, klient,
     * data żądania, opis, status i proponowane terminy.
     * Implementuje model JavaFX Property dla łatwej integracji z interfejsem użytkownika.
     */
    public static class PendingTrainingRequest {
        private final SimpleIntegerProperty id;
        private final SimpleIntegerProperty clientId;
        private final SimpleStringProperty clientName;
        private final SimpleStringProperty notes;

        /**
         * Tworzy nowe oczekujące żądanie treningu z określonymi parametrami.
         * 
         * @param id Unikalny identyfikator żądania treningu
         * @param clientId Identyfikator klienta, który złożył żądanie
         * @param clientName Imię i nazwisko klienta
         * @param notes Dodatkowe uwagi lub opis żądania treningu
         */
        public PendingTrainingRequest(int id, int clientId, String clientName, String notes) {
            this.id = new SimpleIntegerProperty(id);
            this.clientId = new SimpleIntegerProperty(clientId);
            this.clientName = new SimpleStringProperty(clientName);
            this.notes = new SimpleStringProperty(notes);
        }
    
        /**
         * Zwraca identyfikator żądania treningu.
         * @return Identyfikator żądania
         */
        public int getId() { return id.get(); }
        
        /**
         * Zwraca identyfikator klienta, który złożył żądanie.
         * @return Identyfikator klienta
         */
        public int getClientId() { return clientId.get(); }
        
        /**
         * Zwraca imię i nazwisko klienta.
         * @return Imię i nazwisko klienta
         */
        public String getClientName() { return clientName.get(); }
        
        /**
         * Zwraca uwagi lub opis dołączony do żądania treningu.
         * @return Uwagi do żądania treningu
         */
        public String getNotes() { return notes.get(); }
    
        /**
         * Zwraca właściwość JavaFX przechowującą identyfikator żądania.
         * @return Właściwość identyfikatora żądania
         */
        public SimpleIntegerProperty idProperty() { return id; }
        
        /**
         * Zwraca właściwość JavaFX przechowującą identyfikator klienta.
         * @return Właściwość identyfikatora klienta
         */
        public SimpleIntegerProperty clientIdProperty() { return clientId; }
        
        /**
         * Zwraca właściwość JavaFX przechowującą imię i nazwisko klienta.
         * @return Właściwość imienia i nazwiska klienta
         */
        public SimpleStringProperty clientNameProperty() { return clientName; }
        
        /**
         * Zwraca właściwość JavaFX przechowującą uwagi do żądania.
         * @return Właściwość uwag do żądania
         */
        public SimpleStringProperty notesProperty() { return notes; }
    }

    // NOWA KLASA MODELOWA dla Harmonogramu
    /**
     * Klasa reprezentująca wpis w harmonogramie treningów trenera.
     * Przechowuje informacje o treningu, takie jak identyfikator, klient,
     * data, godzina rozpoczęcia, czas trwania i rodzaj treningu.
     * Wykorzystuje model JavaFX Property dla automatycznej aktualizacji UI.
     */
    public static class TrainingScheduleEntry {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty clientName;
        private final SimpleObjectProperty<LocalDate> trainingDate;
        private final SimpleStringProperty notes;

        /**
         * Tworzy nowy wpis w harmonogramie treningów.
         * 
         * @param id Identyfikator wpisu
         * @param clientName Imię i nazwisko klienta
         * @param trainingDate Data treningu
         * @param notes Notatki dotyczące treningu
         */
        public TrainingScheduleEntry(int id, String clientName, LocalDate trainingDate, String notes) {
            this.id = new SimpleIntegerProperty(id);
            this.clientName = new SimpleStringProperty(clientName);
            this.trainingDate = new SimpleObjectProperty<>(trainingDate);
            this.notes = new SimpleStringProperty(notes);
        }
    
        /**
         * Zwraca identyfikator wpisu w harmonogramie.
         * @return Identyfikator wpisu
         */
        public int getId() { return id.get(); }
        
        /**
         * Zwraca imię i nazwisko klienta.
         * @return Imię i nazwisko klienta
         */
        public String getClientName() { return clientName.get(); }
        
        /**
         * Zwraca datę treningu.
         * @return Data treningu
         */
        public LocalDate getTrainingDate() { return trainingDate.get(); }
        
        /**
         * Zwraca notatki dotyczące treningu.
         * @return Notatki dotyczące treningu
         */
        public String getNotes() { return notes.get(); }
    
        /**
         * Zwraca właściwość identyfikatora wpisu.
         * @return Właściwość identyfikatora wpisu
         */
        public SimpleIntegerProperty idProperty() { return id; }
        
        /**
         * Zwraca właściwość imienia i nazwiska klienta.
         * @return Właściwość imienia i nazwiska klienta
         */
        public SimpleStringProperty clientNameProperty() { return clientName; }
        
        /**
         * Zwraca właściwość daty treningu.
         * @return Właściwość daty treningu
         */
        public SimpleObjectProperty<LocalDate> trainingDateProperty() { return trainingDate; }
        
        /**
         * Zwraca właściwość notatek dotyczących treningu.
         * @return Właściwość notatek dotyczących treningu
         */
        public SimpleStringProperty notesProperty() { return notes; }
    }


    /**
     * Klasa reprezentująca klienta z perspektywy trenera.
     * Przechowuje informacje o kliencie, takie jak identyfikator, imię i nazwisko,
     * email, cele treningowe, historia treningów i postępy.
     * Wykorzystuje model JavaFX Property dla automatycznej aktualizacji UI.
     */
    public static class Client {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;

        public Client(int id, String name) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
        }

        public int getId() { return id.get(); }
        public String getName() { return name.get(); }

        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty nameProperty() { return name; }

        @Override
        public String toString() {
            return name.get();
        }
    }


    /**
     * Klasa reprezentująca wpis raportowy trenera.
     * Przechowuje informacje o raporcie, takie jak identyfikator, klient,
     * data, opis postępów, rekomendacje i następne kroki.
     * Implementuje model JavaFX Property dla łatwej integracji z interfejsem użytkownika.
     */
    public static class ReportEntry {
        private final SimpleIntegerProperty id;
        private final SimpleIntegerProperty clientId;
        private final SimpleStringProperty clientName;
        private final SimpleStringProperty notes;
        private final SimpleStringProperty trainerName;

        public ReportEntry(int id, int clientId, String clientName, String notes, String trainerName) {
            this.id = new SimpleIntegerProperty(id);
            this.clientId = new SimpleIntegerProperty(clientId);
            this.clientName = new SimpleStringProperty(clientName);
            this.notes = new SimpleStringProperty(notes);
            this.trainerName = new SimpleStringProperty(trainerName);
        }

        public int getId() { return id.get(); }
        public int getClientId() { return clientId.get(); }
        public String getClientName() { return clientName.get(); }
        public String getNotes() { return notes.get(); }
        public String getTrainerName() { return trainerName.get(); }

        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleIntegerProperty clientIdProperty() { return clientId; }
        public SimpleStringProperty clientNameProperty() { return clientName; }
        public SimpleStringProperty notesProperty() { return notes; }
        public SimpleStringProperty trainerNameProperty() { return trainerName; }
    }

    /**
     * Klasa reprezentująca produkt zalecany przez trenera.
     * Przechowuje informacje o produkcie, takie jak identyfikator, nazwa,
     * kategoria, opis, korzyści i zalecane dawkowanie.
     * Wykorzystuje model JavaFX Property dla automatycznej aktualizacji UI.
     */
    public static class Product {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleObjectProperty<BigDecimal> price;
        private final SimpleIntegerProperty stock;

        public Product(int id, String name, BigDecimal price, int stock) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleObjectProperty<>(price);
            this.stock = new SimpleIntegerProperty(stock);
        }

        public int getId() { return id.get(); }
        public String getName() { return name.get(); }
        public BigDecimal getPrice() { return price.get(); }
        public int getStock() { return stock.get(); }

        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleObjectProperty<BigDecimal> priceProperty() { return price; }
        public SimpleIntegerProperty stockProperty() { return stock; }
    }
    /**
     * Klasa reprezentująca priorytet zadania trenera.
     * Przechowuje identyfikator oraz nazwę priorytetu.
     * Używana do kategoryzacji zadań według ich ważności.
     * Implementuje model JavaFX Property dla łatwej integracji z interfejsem użytkownika.
     */
    public static class Priority {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
    
        /**
         * Tworzy nowy obiekt priorytetu z podanym identyfikatorem i nazwą.
         *
         * @param id Identyfikator priorytetu
         * @param name Nazwa priorytetu (np. "Niski", "Średni", "Wysoki")
         */
        public Priority(int id, String name) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
        }
    
        /**
         * Zwraca identyfikator priorytetu.
         * @return Identyfikator priorytetu
         */
        public int getId() { return id.get(); }
        
        /**
         * Zwraca nazwę priorytetu.
         * @return Nazwa priorytetu
         */
        public String getName() { return name.get(); }
    
        /**
         * Zwraca właściwość przechowującą identyfikator priorytetu.
         * @return Właściwość identyfikatora
         */
        public SimpleIntegerProperty idProperty() { return id; }
        
        /**
         * Zwraca właściwość przechowującą nazwę priorytetu.
         * @return Właściwość nazwy
         */
        public SimpleStringProperty nameProperty() { return name; }
    
        /**
         * Zwraca tekstową reprezentację priorytetu (jego nazwę).
         * @return Nazwa priorytetu
         */
        @Override
        public String toString() { return name.get(); }
    }
    /**
     * Klasa reprezentująca status zadania trenera.
     * Przechowuje identyfikator oraz nazwę statusu.
     * Używana do śledzenia postępu realizacji zadań treningowych.
     * Implementuje model JavaFX Property dla łatwej integracji z interfejsem użytkownika.
     */
    public static class Status {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
    
        /**
         * Tworzy nowy obiekt statusu z podanym identyfikatorem i nazwą.
         *
         * @param id Identyfikator statusu
         * @param name Nazwa statusu (np. "do zrobienia", "w trakcie", "zakończone")
         */
        public Status(int id, String name) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
        }
    
        /**
         * Zwraca identyfikator statusu.
         * @return Identyfikator statusu
         */
        public int getId() { return id.get(); }
        
        /**
         * Zwraca nazwę statusu.
         * @return Nazwa statusu
         */
        public String getName() { return name.get(); }
    
        /**
         * Zwraca właściwość przechowującą identyfikator statusu.
         * @return Właściwość identyfikatora
         */
        public SimpleIntegerProperty idProperty() { return id; }
        
        /**
         * Zwraca właściwość przechowującą nazwę statusu.
         * @return Właściwość nazwy
         */
        public SimpleStringProperty nameProperty() { return name; }
    
        /**
         * Zwraca tekstową reprezentację statusu (jego nazwę).
         * @return Nazwa statusu
         */
        @Override
        public String toString() { return name.get(); }
    }
    // Klasa dla TrainerTask
    /**
     * Klasa reprezentująca zadanie trenera w systemie siłowni.
     * Przechowuje informacje o zadaniu, takie jak identyfikator, nazwa,
     * opis, data utworzenia, termin wykonania, priorytet i status.
     * Implementuje model JavaFX Property dla łatwej integracji z interfejsem użytkownika.
     */
    public static class TrainerTask {
        private final int id;
        private final int trainingRequestId;
        private final int clientId;
        private final String clientName;
        private final LocalDate trainingDate;
        private final String taskDescription;
        private final int priorityId;
        private final int statusId;

        /**
         * Tworzy nowe zadanie trenera z określonymi parametrami.
         * 
         * @param id Unikalny identyfikator zadania
         * @param trainingRequestId Identyfikator powiązanego żądania treningu
         * @param clientId Identyfikator klienta
         * @param clientName Imię i nazwisko klienta
         * @param trainingDate Data treningu
         * @param taskDescription Opis zadania
         * @param priorityId Identyfikator priorytetu
         * @param statusId Identyfikator statusu
         */
        public TrainerTask(int id, int trainingRequestId, int clientId, String clientName,
                           LocalDate trainingDate, String taskDescription, int priorityId, int statusId) {
            this.id = id;
            this.trainingRequestId = trainingRequestId;
            this.clientId = clientId;
            this.clientName = clientName;
            this.trainingDate = trainingDate;
            this.taskDescription = taskDescription;
            this.priorityId = priorityId;
            this.statusId = statusId;
        }
    
        /**
         * Zwraca identyfikator zadania.
         * @return Identyfikator zadania
         */
        public int getId() { return id; }
        
        /**
         * Zwraca identyfikator powiązanego żądania treningu.
         * @return Identyfikator żądania treningu
         */
        public int getTrainingRequestId() { return trainingRequestId; }
        
        /**
         * Zwraca identyfikator klienta.
         * @return Identyfikator klienta
         */
        public int getClientId() { return clientId; }
        
        /**
         * Zwraca imię i nazwisko klienta.
         * @return Imię i nazwisko klienta
         */
        public String getClientName() { return clientName; }
        
        /**
         * Zwraca datę treningu.
         * @return Data treningu
         */
        public LocalDate getTrainingDate() { return trainingDate; }
        
        /**
         * Zwraca opis zadania.
         * @return Opis zadania
         */
        public String getTaskDescription() { return taskDescription; }
        
        /**
         * Zwraca identyfikator priorytetu zadania.
         * @return Identyfikator priorytetu
         */
        public int getPriorityId() { return priorityId; }
        
        /**
         * Zwraca identyfikator statusu zadania.
         * @return Identyfikator statusu
         */
        public int getStatusId() { return statusId; }
    }

    /**
     * Klasa reprezentująca wpis o przychodach trenera.
     * Przechowuje informacje o przychodach, takie jak identyfikator płatności, 
     * dane klienta, datę treningu, datę płatności, kwotę oraz notatki dotyczące treningu.
     * Wykorzystuje model JavaFX Property dla automatycznej aktualizacji UI.
     */
    public static class TrainerIncomeEntry {
        private final SimpleIntegerProperty paymentId;
        private final SimpleStringProperty clientName;
        private final SimpleObjectProperty<LocalDateTime> trainingDateTime;
        private final SimpleObjectProperty<Timestamp> paymentDate;
        private final SimpleObjectProperty<BigDecimal> amount;
        private final SimpleStringProperty trainingNotes;
    
        /**
         * Tworzy nowy wpis o przychodach trenera z określonymi parametrami.
         * 
         * @param paymentId Identyfikator płatności
         * @param clientName Imię i nazwisko klienta
         * @param trainingDateTime Data i czas treningu
         * @param paymentDate Data płatności
         * @param amount Kwota płatności
         * @param trainingNotes Notatki dotyczące treningu
         */
        public TrainerIncomeEntry(int paymentId, String clientName, LocalDateTime trainingDateTime, Timestamp paymentDate, BigDecimal amount, String trainingNotes) {
            this.paymentId = new SimpleIntegerProperty(paymentId);
            this.clientName = new SimpleStringProperty(clientName);
            this.trainingDateTime = new SimpleObjectProperty<>(trainingDateTime);
            this.paymentDate = new SimpleObjectProperty<>(paymentDate);
            this.amount = new SimpleObjectProperty<>(amount);
            this.trainingNotes = new SimpleStringProperty(trainingNotes);
        }
    
        /**
         * Zwraca identyfikator płatności.
         * @return Identyfikator płatności
         */
        public int getPaymentId() { return paymentId.get(); }
        
        /**
         * Zwraca właściwość identyfikatora płatności.
         * @return Właściwość identyfikatora płatności
         */
        public SimpleIntegerProperty paymentIdProperty() { return paymentId; }
    
        /**
         * Zwraca imię i nazwisko klienta.
         * @return Imię i nazwisko klienta
         */
        public String getClientName() { return clientName.get(); }
        
        /**
         * Zwraca właściwość imienia i nazwiska klienta.
         * @return Właściwość imienia i nazwiska klienta
         */
        public SimpleStringProperty clientNameProperty() { return clientName; }
    
        /**
         * Zwraca datę i czas treningu.
         * @return Data i czas treningu
         */
        public LocalDateTime getTrainingDateTime() { return trainingDateTime.get(); }
        
        /**
         * Zwraca właściwość daty i czasu treningu.
         * @return Właściwość daty i czasu treningu
         */
        public SimpleObjectProperty<LocalDateTime> trainingDateTimeProperty() { return trainingDateTime; }
    
        /**
         * Zwraca datę płatności.
         * @return Data płatności
         */
        public Timestamp getPaymentDate() { return paymentDate.get(); }
        
        /**
         * Zwraca właściwość daty płatności.
         * @return Właściwość daty płatności
         */
        public SimpleObjectProperty<Timestamp> paymentDateProperty() { return paymentDate; }
    
        /**
         * Zwraca kwotę płatności.
         * @return Kwota płatności
         */
        public BigDecimal getAmount() { return amount.get(); }
        
        /**
         * Zwraca właściwość kwoty płatności.
         * @return Właściwość kwoty płatności
         */
        public SimpleObjectProperty<BigDecimal> amountProperty() { return amount; }
    
        /**
         * Zwraca notatki dotyczące treningu.
         * @return Notatki dotyczące treningu
         */
        public String getTrainingNotes() { return trainingNotes.get(); }
        
        /**
         * Zwraca właściwość notatek dotyczących treningu.
         * @return Właściwość notatek dotyczących treningu
         */
        public SimpleStringProperty trainingNotesProperty() { return trainingNotes; }
    }
}