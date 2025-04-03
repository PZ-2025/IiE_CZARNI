package com.example.projektsilownia;

import javafx.collections.*;

/**
 * Klasa modelu pracownika reprezentująca dane systemu siłowni.
 * Przechowuje listy obserwowalne klientów, sprzętu, karnetów i harmonogramów zajęć.
 *
 */
public class EmployeeModel {
    private final ObservableList<Client> clients = FXCollections.observableArrayList();
    private final ObservableList<Equipment> equipment = FXCollections.observableArrayList();
    private final ObservableList<Membership> memberships = FXCollections.observableArrayList();
    private final ObservableList<ScheduleItem> schedule = FXCollections.observableArrayList();

    /**
     * Konstruktor inicjalizujący model pracownika.
     * Automatycznie wypełnia listy przykładowymi danymi.
     */
    public EmployeeModel() {
        initializeSampleData();
    }

    /**
     * Inicjalizuje przykładowe dane dla wszystkich list w modelu.
     * Dodaje przykładowych klientów, sprzęt, karnety i harmonogramy zajęć.
     */
    private void initializeSampleData() {
        // Przykładowi klienci
        clients.addAll(
                new Client("Jan Kowalski", "1990-05-15", "Premium"),
                new Client("Anna Nowak", "1985-11-22", "Standard"),
                new Client("Marek Wiśniewski", "1995-03-08", "VIP"),
                new Client("Katarzyna Zielińska", "1988-07-12", "Premium")
        );

        // Przykładowy sprzęt
        equipment.addAll(
                new Equipment("Hantle 5kg", 18, "Dostępne"),
                new Equipment("Bieżnia elektryczna", 5, "W naprawie"),
                new Equipment("Ławka do wyciskania", 8, "Dostępne"),
                new Equipment("Rower stacjonarny", 10, "Dostępne")
        );

        // Przykładowe karnety
        memberships.addAll(
                new Membership("Standard", "Dostęp podstawowy", 149, true),
                new Membership("Premium", "Dostęp + 2 treningi", 299, true),
                new Membership("VIP", "Dostęp VIP + trener", 599, false)
        );

        // Przykładowy harmonogram
        schedule.addAll(
                new ScheduleItem("07:00-08:00", "Poniedziałek", "CrossFit", "Trener Marek"),
                new ScheduleItem("17:00-18:30", "Wtorek", "Joga", "Trener Anna"),
                new ScheduleItem("19:00-20:30", "Środa", "TRX", "Trener Krzysztof")
        );
    }

    /**
     *
     * @return ObservableList zawierająca obiekty Client
     */
    public ObservableList<Client> getClients() { return clients; }

    /**
     * @return ObservableList zawierająca obiekty Equipment
     */
    public ObservableList<Equipment> getEquipment() { return equipment; }

    /**
     * @return ObservableList zawierająca obiekty Membership
     */
    public ObservableList<Membership> getMemberships() { return memberships; }

    /**
     * @return ObservableList zawierająca obiekty ScheduleItem
     */
    public ObservableList<ScheduleItem> getSchedule() { return schedule; }
}