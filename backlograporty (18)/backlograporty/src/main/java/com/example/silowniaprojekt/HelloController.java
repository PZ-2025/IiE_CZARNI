package com.example.silowniaprojekt;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Kontroler widoku powitalnego aplikacji siłowni.
 * Zarządza interakcjami użytkownika na ekranie powitalnym,
 * takimi jak logowanie, wybór roli czy przejście do rejestracji.
 * Jest pierwszym kontrolerem, z którym interaguje użytkownik.
 */
public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}