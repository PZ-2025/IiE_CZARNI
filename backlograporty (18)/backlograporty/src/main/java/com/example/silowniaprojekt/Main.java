package com.example.silowniaprojekt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Główna klasa aplikacji siłowni Black Iron Gym.
 * Inicjalizuje aplikację JavaFX, ładuje ekran logowania
 * i konfiguruje podstawowe ustawienia interfejsu użytkownika.
 * Klasa stanowi punkt wejścia do aplikacji i zarządza cyklem życia
 * głównego okna aplikacji.
 */
public class Main extends Application {
    /**
     * Inicjalizuje i uruchamia aplikację siłowni.
     * Metoda ładuje ekran logowania, konfiguruje jego wygląd,
     * ustawia style CSS, dodaje ikonę aplikacji i wyświetla
     * główne okno aplikacji.
     * 
     * @param primaryStage główne okno aplikacji
     * @throws Exception w przypadku błędu ładowania zasobów FXML lub CSS
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();

        LoginController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);

        // Ustawienie stylu i ikon
        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("login_styles.css").toExternalForm());

        // Ustawienie ikony aplikacji
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.avif")));
        } catch (Exception e) {
            System.err.println("..." + e.getMessage());
        }

        primaryStage.setTitle("Black Iron Gym - System logowania");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Punkt wejścia do aplikacji.
     * Uruchamia aplikację JavaFX przekazując argumenty wiersza poleceń.
     * 
     * @param args argumenty wiersza poleceń przekazane do aplikacji
     */
    public static void main(String[] args) {
        launch(args);
    }
}