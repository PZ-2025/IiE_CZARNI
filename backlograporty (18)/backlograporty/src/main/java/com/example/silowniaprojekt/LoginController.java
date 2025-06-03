package com.example.silowniaprojekt;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Kontroler ekranu logowania do systemu siłowni.
 * Zarządza procesem uwierzytelniania użytkowników i przekierowaniem
 * do odpowiedniego panelu na podstawie roli użytkownika (admin, pracownik, trener, klient).
 * Obsługuje zarówno statyczne dane testowe, jak i uwierzytelnianie przez bazę danych.
 */
public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Obsługuje proces logowania użytkownika do systemu.
     * 
     * Weryfikuje wprowadzone dane (email i hasło), sprawdza czy pola nie są puste,
     * a następnie próbuje zalogować użytkownika używając statycznych danych testowych
     * lub danych z bazy danych. Po poprawnym uwierzytelnieniu, przekierowuje
     * użytkownika do odpowiedniego panelu w zależności od jego roli.
     * 
     * W przypadku niepowodzenia wyświetla odpowiedni komunikat błędu.
     */
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Błąd logowania", "Wprowadź zarówno email, jak i hasło");
            return;
        }

        // Statyczne dane testowe dla szybkiego logowania
        try {
            if (email.equalsIgnoreCase("admin@example.com") && password.equals("admin123")) {
                loadAdminDashboard();
                return;
            } else if (email.equalsIgnoreCase("trener@example.com") && password.equals("trener123")) {
                // ZMIANA: Przekazujemy statyczne ID i nazwę dla trenera
                // Zastąp 2 i "Statyczny Trener" faktycznymi danymi, jeśli znasz ID i nazwę trenera
                // który ma być użyty do testów statycznych.
                loadTrainerDashboard(2, "Statyczny Trener");
                return;
            } else if (email.equalsIgnoreCase("pracownik@example.com") && password.equals("pracownik123")) {
                loadEmployeeDashboard();
                return;
            } else if (email.equalsIgnoreCase("klient@example.com") && password.equals("klient123")) {
                // Przykład: Upewnij się, że ID i nazwa są poprawne dla testowego klienta
                loadClientDashboard(1, "Jakub Matlosz", email);
                return;
            }
        } catch (IOException e) {
            handleLoadingError(e);
            return;
        }

        // Logowanie z użyciem bazy danych
        try {
            authenticateUser(email, password);
        } catch (IOException e) {
            handleLoadingError(e);
        }
    }

    private boolean authenticateUser(String email, String password) throws IOException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showAlert("Błąd połączenia", "Nie można połączyć się z bazą danych");
                return false;
            }

            String query = "SELECT id, name, role FROM users WHERE email = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password); // W rzeczywistej aplikacji powinno być porównanie haszowanych haseł

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id");
                String userName = rs.getString("name");
                String userRole = rs.getString("role");

                // Przekierowanie do odpowiedniego panelu na podstawie roli
                switch (userRole) {
                    case "admin":
                        loadAdminDashboard();
                        break;
                    case "trainer":
                        // ZMIANA: Przekazujemy userId i userName pobrane z bazy danych
                        loadTrainerDashboard(userId, userName);
                        break;
                    case "employee":
                        loadEmployeeDashboard();
                        break;
                    case "client":
                        loadClientDashboard(userId, userName, email);
                        break;
                    default:
                        showAlert("Błąd logowania", "Nieznana rola użytkownika");
                        return false;
                }

                // Zapisanie aktywności logowania w bazie
                logUserActivity(userId, "Logowanie do systemu");

                return true;
            } else {
                showAlert("Błąd logowania", "Nieprawidłowy email lub hasło");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas logowania: " + e.getMessage());
            showAlert("Błąd logowania", "Wystąpił problem z bazą danych: " + e.getMessage());
            return false;
        }
    }

    private void logUserActivity(int userId, String action) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;

            String query = "INSERT INTO activity_logs (user_id, action) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setString(2, action);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Błąd podczas zapisywania aktywności: " + e.getMessage());
        }
    }

    private void loadAdminDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("admin_dashboard.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Black Iron Gym - Panel Administratora");
        stage.setScene(new Scene(root));
        stage.setMaximized(true);
        stage.show();

        primaryStage.close();
    }

    // ZMIANA: Metoda loadTrainerDashboard teraz przyjmuje trainerId i trainerName
    private void loadTrainerDashboard(int trainerId, String trainerName) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("trainer_dashboard.fxml"));
        Parent root = loader.load();

        // ZMIANA: Pobieramy kontroler i ustawiamy dane trenera
        TrainerDashboardController trainerController = loader.getController();
        trainerController.setTrainerData(trainerId, trainerName);

        Stage stage = new Stage();
        stage.setTitle("Black Iron Gym - Panel Trenera");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("trainer_styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        primaryStage.close();
    }

    private void loadEmployeeDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("employee_dashboard.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Black Iron Gym - Panel Pracownika");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("employee_styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        primaryStage.close();
    }

    private void loadClientDashboard(int userId, String userName, String userEmail) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client_dashboard.fxml"));
        Parent root = loader.load();

        ClientDashboardController controller = loader.getController();
        controller.setUserData(userId, userName, userEmail);

        Stage stage = new Stage();
        stage.setTitle("Black Iron Gym - Panel Klienta");
        stage.setScene(new Scene(root));
        stage.setMaximized(true);
        stage.show();

        primaryStage.close();
    }

    private void handleLoadingError(Exception e) {
        e.printStackTrace();
        showAlert("Błąd ładowania", "Wystąpił problem podczas ładowania panelu:\n" + e.getMessage());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}