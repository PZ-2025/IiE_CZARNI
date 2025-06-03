package com.example.silowniaprojekt;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Abstrakcyjny kontroler bazowy dla wszystkich paneli w systemie siłowni.
 * Zawiera wspólne funkcjonalności dla wszystkich typów paneli (administrator, 
 * pracownik, trener, klient) takie jak wylogowanie, nawigacja między sekcjami,
 * wyświetlanie alertów oraz inicjalizacja podstawowych komponentów.
 */
public class DashboardController {
    @FXML private Label welcomeLabel;
    @FXML private VBox adminPanel;
    @FXML private VBox trainerPanel;
    @FXML private VBox employeePanel;
    @FXML private VBox clientPanel;

    public void setUserData(String role, String greetingName) {
        welcomeLabel.setText("Witaj " + greetingName + "!");

        // Ukryj wszystkie panele
        adminPanel.setVisible(false);
        trainerPanel.setVisible(false);
        employeePanel.setVisible(false);
        clientPanel.setVisible(false);

        // Pokaż odpowiedni panel
        switch (role) {
            case "admin":
                adminPanel.setVisible(true);
                break;
            case "trainer":
                trainerPanel.setVisible(true);
                break;
            case "employee":
                employeePanel.setVisible(true);
                break;
            case "client":
                clientPanel.setVisible(true);
                break;
        }
    }
}