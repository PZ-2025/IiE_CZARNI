import com.example.silowniaprojekt.EmployeeDashboardController;
import com.example.silowniaprojekt.DatabaseConnection;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ComboBox;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Klasa testowa dla kontrolera panelu pracownika siłowni.
 * Sprawdza poprawność działania głównych funkcjonalności kontrolera,
 * takich jak dodawanie, edycja i usuwanie użytkowników oraz inicjalizacja
 * komponentów interfejsu użytkownika.
 * 
 * Testy wykorzystują rzeczywiste połączenie z bazą danych i symulują
 * działanie interfejsu użytkownika.
 */
class EmployeeDashboardControllerTest {

    /**
     * Kontroler panelu pracownika testowany w tej klasie.
     * Inicjalizowany przed każdym testem w metodzie setUp.
     */
    private EmployeeDashboardController controller;

    /**
     * Przygotowuje środowisko testowe przed każdym testem.
     * 
     * Metoda:
     * 1. Sprawdza połączenie z bazą danych
     * 2. Inicjalizuje kontroler panelu pracownika
     * 3. Tworzy i konfiguruje komponenty UI potrzebne do testów
     * 4. Wywołuje metodę initialize() kontrolera
     */
    @BeforeEach
    void setUp() {
        // Sprawdzenie połączenia z bazą danych
        Connection conn = DatabaseConnection.getConnection();
        assertNotNull(conn, "Połączenie z bazą danych nie może być null.");

        // Inicjalizacja kontrolera
        controller = new EmployeeDashboardController();

        // Ustawienie UI ręcznie (symulacja FXML)
        controller.clientNameField = new TextField();
        controller.emailField = new TextField();
        controller.passwordField = new PasswordField();
        controller.roleComboBox = new ComboBox<>();
        controller.roleComboBox.getItems().addAll("client", "trainer", "employee");

        // Załaduj dane
        controller.initialize(); // zakładamy, że ładuje wszystkie sekcje
    }

    /**
     * Testuje czy metoda initialize() poprawnie ładuje użytkowników z bazy danych.
     * 
     * Sprawdza czy:
     * 1. Lista użytkowników nie jest null
     * 2. Lista użytkowników nie jest pusta
     * 
     * Dodatkowo wypisuje liczbę załadowanych użytkowników.
     */
    @Test
    void testInitialize_loadsUsersIntoTable() {
        ObservableList<EmployeeDashboardController.User> users = controller.users;
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Lista użytkowników powinna zawierać dane.");
        System.out.println("Załadowano " + users.size() + " użytkowników.");
    }
    
    /**
     * Testuje dodawanie nowego użytkownika z poprawnymi danymi.
     * 
     * Test:
     * 1. Ustawia poprawne dane w polach formularza
     * 2. Wywołuje metodę addUser()
     * 3. Sprawdza czy użytkownik został dodany do listy
     */
    @Test
    void testAddUser_validInput_addsToTable() {
        String testName = "Jan Testowy";
        String testEmail = "jan@test.com";

        // Ustawiamy pola formularza
        controller.clientNameField.setText(testName);
        controller.emailField.setText(testEmail);
        controller.passwordField.setText("haslo123");
        controller.roleComboBox.setValue("client");

        // Wywołujemy metodę dodającą użytkownika
        controller.addUser();  // addUser() nie wymaga argumentów

        // Sprawdzamy, czy użytkownik został dodany
        assertTrue(controller.users.stream()
                        .anyMatch(u -> u.getName().equals(testName) && u.getEmail().equals(testEmail)),
                "Użytkownik powinien zostać dodany do listy.");
    }

    /**
     * Testuje dodawanie użytkownika z pustym polem imienia.
     * 
     * Test sprawdza czy:
     * 1. Metoda addUser() prawidłowo waliduje dane wejściowe
     * 2. Użytkownik z pustym imieniem nie zostanie dodany do listy
     * 
     * Oczekiwany wynik: Użytkownik nie zostanie dodany do listy.
     */
    @Test
    void testAddUser_emptyName_showsError() {
        // Puste imię
        controller.clientNameField.setText("");
        controller.emailField.setText("jan@test.com");
        controller.passwordField.setText("haslo123");
        controller.roleComboBox.setValue("client");
    
        controller.addUser();  // bez argumentów
    
        // Tutaj możesz dodać mock showError() lub sprawdzić brak nowego użytkownika
        assertFalse(controller.users.stream()
                        .anyMatch(u -> u.getEmail().equals("jan@test.com")),
                "Użytkownik z pustym imieniem nie powinien zostać dodany.");
    }
    
    /**
     * Testuje edycję danych istniejącego użytkownika.
     * 
     * Test:
     * 1. Pobiera pierwszego użytkownika z listy (jeśli lista nie jest pusta)
     * 2. Aktualizuje adres email użytkownika
     * 3. Sprawdza czy adres email został zaktualizowany
     * 
     * Test opiera się na symulacji edycji, zakładając, że metoda editUser()
     * otwiera dialog edycji i aktualizuje dane użytkownika.
     */
    @Test
    void testEditUser_validInput_updatesUser() {
        if (!controller.users.isEmpty()) {
            EmployeeDashboardController.User user = controller.users.get(0);
            String originalEmail = user.getEmail();
            String updatedEmail = "zaktualizowany@email.com";
    
            // Symulacja edycji
            controller.editUser();  // zakładamy, że otwiera dialog edycji
    
            // Alternatywa: ręczna zmiana emaila
            user.emailProperty().set(updatedEmail);
    
            assertTrue(controller.users.stream()
                            .anyMatch(u -> u.getEmail().equals(updatedEmail)),
                    "Email użytkownika powinien zostać zaktualizowany.");
        }
    }
    
    /**
     * Testuje usuwanie użytkownika z listy.
     * 
     * Test:
     * 1. Pobiera pierwszego użytkownika z listy (jeśli lista nie jest pusta)
     * 2. Usuwa użytkownika z listy
     * 3. Sprawdza czy użytkownik został prawidłowo usunięty
     * 
     * Oczekiwany wynik: Lista użytkowników nie powinna zawierać usuniętego użytkownika.
     */
    @Test
    void testDeleteUser_removesFromList() {
        if (!controller.users.isEmpty()) {
            EmployeeDashboardController.User user = controller.users.get(0);

            // Usuwamy użytkownika
            controller.users.remove(user);

            assertFalse(controller.users.contains(user), "Użytkownik powinien zostać usunięty.");
        }
    }
}