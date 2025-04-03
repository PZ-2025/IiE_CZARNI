package com.example.projektsilownia;

/**
 * Kontroler zarządzający interakcją między widokiem (EmployeeView) a modelem (EmployeeModel)
 *
 */
public class EmployeeController {
    private final EmployeeModel model;
    private final EmployeeView view;

    /**
     * Konstruktor inicjalizujący kontroler z określonym modelem i widokiem
     *
     * @param model Model danych pracownika
     * @param view Widok interfejsu użytkownika pracownika
     */
    public EmployeeController(EmployeeModel model, EmployeeView view) {
        this.model = model;
        this.view = view;

        initializeDataBinding();
        setupEventHandlers();
    }

    /**
     * Inicjalizuje wiązanie danych między modelem a widokiem.
     * Powiązuje ObservableLists z modelu z odpowiednimi TableView w widoku.
     */
    private void initializeDataBinding() {
        view.getClientsTable().setItems(model.getClients());
        view.getEquipmentTable().setItems(model.getEquipment());
        view.getMembershipsTable().setItems(model.getMemberships());
        view.getScheduleTable().setItems(model.getSchedule());
    }

    /**
     * Konfiguruje obsługę zdarzeń dla elementów interfejsu użytkownika.
     *
     */
    private void setupEventHandlers() {
        // Obsługa przycisku dodawania klienta
        view.getAddClientButton().setOnAction(e -> {
            model.getClients().add(new Client("Nowy Klient", "2000-01-01", "Standard"));
        });

        // Obsługa przycisku dodawania sprzętu
        view.getAddEquipmentButton().setOnAction(e -> {
            model.getEquipment().add(new Equipment("Nowy sprzęt", 1, "Dostępne"));
        });

        // Obsługa przycisku dodawania karnetu
        view.getAddMembershipButton().setOnAction(e -> {
            model.getMemberships().add(new Membership("Nowy karnet", "Opis", 100, true));
        });

        // Obsługa przycisku dodawania harmonogramu
        view.getAddScheduleButton().setOnAction(e -> {
            model.getSchedule().add(new ScheduleItem("00:00-00:00", "Dzień", "Zajęcia", "Trener"));
        });
    }
}