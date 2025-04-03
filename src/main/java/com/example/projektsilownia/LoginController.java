package com.example.projektsilownia;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;

public class LoginController {
    private final LoginView view;
    private final MainApp mainApp;

    // Sztywne dane logowania
    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "password123";

    public LoginController(LoginView view, MainApp mainApp) {
        this.view = view;
        this.mainApp = mainApp;
        setupEventHandlers();
        setupHoverEffects();
    }

    private void setupEventHandlers() {
        view.getLoginButton().setOnAction(event -> {
            String username = view.getUsernameField().getText();
            String password = view.getPasswordField().getText();

            if (authenticate(username, password)) {
                mainApp.showEmployeeView(); // Przejście do panelu pracownika
            } else {
                showAlert(Alert.AlertType.ERROR, "Błąd logowania", "Nieprawidłowy login lub hasło");
            }
        });

        // Pozostałe metody bez zmian
        view.getForgotPassword().setOnAction(event -> {
            showAlert(Alert.AlertType.INFORMATION, "Przypomnienie hasła",
                    "Domyślne dane logowania:\nLogin: " + VALID_USERNAME + "\nHasło: " + VALID_PASSWORD);
        });

        view.getRegisterLink().setOnAction(event -> {
            showAlert(Alert.AlertType.INFORMATION, "Rejestracja",
                    "Rejestracja nowego użytkownika jest obecnie niedostępna");
        });

        view.getInstagramLink().setOnAction(event -> {
            showAlert(Alert.AlertType.INFORMATION, "Social Media", "Przekierowanie do Instagram");
        });

        view.getFacebookLink().setOnAction(event -> {
            showAlert(Alert.AlertType.INFORMATION, "Social Media", "Przekierowanie do Facebook");
        });

        view.getAppleLink().setOnAction(event -> {
            showAlert(Alert.AlertType.INFORMATION, "Social Media", "Przekierowanie do Apple");
        });
    }

    private boolean authenticate(String username, String password) {
        return VALID_USERNAME.equals(username) && VALID_PASSWORD.equals(password);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupHoverEffects() {
        Button loginButton = view.getLoginButton();
        loginButton.setOnMouseEntered(e -> {
            loginButton.setStyle("-fx-background-color: #ff0033; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 16; " +
                    "-fx-background-radius: 25; " +
                    "-fx-pref-height: 45; " +
                    "-fx-pref-width: 340; " +
                    "-fx-effect: dropshadow(gaussian, rgba(220, 20, 60, 0.5), 15, 0, 0, 0);");
        });
        loginButton.setOnMouseExited(e -> {
            loginButton.setStyle("-fx-background-color: crimson; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 16; " +
                    "-fx-background-radius: 25; " +
                    "-fx-pref-height: 45; " +
                    "-fx-pref-width: 340;");
        });

        // Social icons hover effects
        setupSocialHover(view.getInstagramLink());
        setupSocialHover(view.getFacebookLink());
        setupSocialHover(view.getAppleLink());
    }

    private void setupSocialHover(Hyperlink link) {
        link.setOnMouseEntered(e -> {
            link.setTextFill(javafx.scene.paint.Color.CRIMSON);
            link.setTranslateY(-3);
        });
        link.setOnMouseExited(e -> {
            link.setTextFill(javafx.scene.paint.Color.WHITE);
            link.setTranslateY(0);
        });
    }
}