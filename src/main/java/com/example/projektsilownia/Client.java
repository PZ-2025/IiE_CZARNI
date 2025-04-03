package com.example.projektsilownia;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Klasa reprezentująca klienta siłowni.
 * Przechowuje podstawowe informacje o kliencie oraz typ jego karnetu.
 */
public class Client {
    private final StringProperty name;
    private final StringProperty birthDate;
    private final StringProperty membershipType;

    /**
     * Konstruktor tworzący nowego klienta.
     *
     * @param name Imię i nazwisko klienta
     * @param birthDate Data urodzenia klienta w formacie YYYY-MM-DD
     * @param membershipType Typ karnetu klienta (np. "Premium", "Standard", "VIP")
     */
    public Client(String name, String birthDate, String membershipType) {
        this.name = new SimpleStringProperty(name);
        this.birthDate = new SimpleStringProperty(birthDate);
        this.membershipType = new SimpleStringProperty(membershipType);
    }

    /**
     * Zwraca właściwość (property) imienia i nazwiska klienta.
     * Wykorzystywane do wiązania z interfejsem użytkownika JavaFX.
     *
     * @return Właściwość zawierająca imię i nazwisko klienta
     */
    public StringProperty nameProperty() { return name; }

    /**
     * Zwraca właściwość (property) daty urodzenia klienta.
     * Wykorzystywane do wiązania z interfejsem użytkownika JavaFX.
     *
     * @return Właściwość zawierająca datę urodzenia klienta
     */
    public StringProperty birthDateProperty() { return birthDate; }

    /**
     * Zwraca właściwość (property) typu karnetu klienta.
     * Wykorzystywane do wiązania z interfejsem użytkownika JavaFX.
     *
     * @return Właściwość zawierająca typ karnetu klienta
     */
    public StringProperty membershipTypeProperty() { return membershipType; }

    /**
     * Zwraca imię i nazwisko klienta.
     *
     * @return Imię i nazwisko klienta jako String
     */
    public String getName() { return name.get(); }

    /**
     * Zwraca datę urodzenia klienta.
     *
     * @return Data urodzenia w formacie YYYY-MM-DD
     */
    public String getBirthDate() { return birthDate.get(); }

    /**
     * Zwraca typ karnetu klienta.
     *
     * @return Typ karnetu (np. "Premium", "Standard", "VIP")
     */
    public String getMembershipType() { return membershipType.get(); }
}