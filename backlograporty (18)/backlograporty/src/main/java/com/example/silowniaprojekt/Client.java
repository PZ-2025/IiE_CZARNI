package com.example.silowniaprojekt;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Klasa reprezentująca klienta siłowni w systemie.
 * Przechowuje podstawowe dane użytkownika, takie jak imię i nazwisko,
 * adres email, hasło oraz przypisaną rolę w systemie.
 * Implementuje model oparty na JavaFX Property dla łatwej integracji z interfejsem użytkownika.
 */
public class Client {
    /**
     * Imię i nazwisko klienta.
     * Przechowywane jako właściwość JavaFX dla automatycznej aktualizacji UI.
     */
    private final StringProperty fullName = new SimpleStringProperty();
    
    /**
     * Adres email klienta.
     * Służy jako unikalny identyfikator klienta w systemie.
     */
    private final StringProperty email = new SimpleStringProperty();
    
    /**
     * Hasło klienta.
     * Przechowywane jako właściwość, powinno być zaszyfrowane przed zapisaniem.
     */
    private final StringProperty password = new SimpleStringProperty();
    
    /**
     * Rola klienta w systemie (np. "client", "trainer", "employee").
     * Określa poziom uprawnień i dostępne funkcje.
     */
    private final StringProperty role = new SimpleStringProperty();

    /**
     * Tworzy nowego klienta z podanymi danymi.
     *
     * @param fullName Imię i nazwisko klienta
     * @param email Adres email klienta
     * @param password Hasło klienta
     * @param role Rola klienta w systemie
     */
    public Client(String fullName, String email, String password, String role) {
        this.fullName.set(fullName);
        this.email.set(email);
        this.password.set(password);
        this.role.set(role);
    }
    
    // Gettery i properties
    /**
     * Zwraca imię i nazwisko klienta.
     * @return Imię i nazwisko
     */
    public String getFullName() { return fullName.get(); }
    
    /**
     * Zwraca właściwość przechowującą imię i nazwisko klienta.
     * @return Właściwość imienia i nazwiska
     */
    public StringProperty fullNameProperty() { return fullName; }
    
    /**
     * Zwraca adres email klienta.
     * @return Adres email
     */
    public String getEmail() { return email.get(); }
    
    /**
     * Zwraca właściwość przechowującą adres email klienta.
     * @return Właściwość adresu email
     */
    public StringProperty emailProperty() { return email; }
    
    /**
     * Zwraca hasło klienta.
     * @return Hasło klienta
     */
    public String getPassword() { return password.get(); }
    
    /**
     * Zwraca właściwość przechowującą hasło klienta.
     * @return Właściwość hasła
     */
    public StringProperty passwordProperty() { return password; }
    
    /**
     * Zwraca rolę klienta w systemie.
     * @return Rola klienta
     */
    public String getRole() { return role.get(); }
    
    /**
     * Zwraca właściwość przechowującą rolę klienta.
     * @return Właściwość roli
     */
    public StringProperty roleProperty() { return role; }
}