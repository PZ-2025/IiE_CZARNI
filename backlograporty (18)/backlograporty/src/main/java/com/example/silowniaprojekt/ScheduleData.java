package com.example.silowniaprojekt;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Klasa reprezentująca dane o harmonogramie zajęć w siłowni.
 * Przechowuje informacje o zaplanowanych zajęciach, takie jak
 * typ zajęć, trener prowadzący, data, godzina, czas trwania
 * i liczba zapisanych uczestników.
 * Wykorzystywana do zarządzania harmonogramem i rezerwacji zajęć.
 */
public class ScheduleData {
    /**
     * Przechowuje informację o godzinie zajęć.
     * Pole reprezentuje przedział czasowy, w którym odbywają się zajęcia.
     */
    private final StringProperty time;
    
    /**
     * Przechowuje informację o zajęciach odbywających się w poniedziałek.
     * Zawiera nazwę zajęć lub informację "brak" jeśli nie ma zaplanowanych zajęć.
     */
    private final StringProperty monday;
    
    /**
     * Przechowuje informację o zajęciach odbywających się we wtorek.
     * Zawiera nazwę zajęć lub informację "brak" jeśli nie ma zaplanowanych zajęć.
     */
    private final StringProperty tuesday;
    
    /**
     * Przechowuje informację o zajęciach odbywających się w środę.
     * Zawiera nazwę zajęć lub informację "brak" jeśli nie ma zaplanowanych zajęć.
     */
    private final StringProperty wednesday;
    
    /**
     * Przechowuje informację o zajęciach odbywających się w czwartek.
     * Zawiera nazwę zajęć lub informację "brak" jeśli nie ma zaplanowanych zajęć.
     */
    private final StringProperty thursday;
    
    /**
     * Przechowuje informację o zajęciach odbywających się w piątek.
     * Zawiera nazwę zajęć lub informację "brak" jeśli nie ma zaplanowanych zajęć.
     */
    private final StringProperty friday;
    
    /**
     * Przechowuje informację o zajęciach odbywających się w sobotę.
     * Zawiera nazwę zajęć lub informację "brak" jeśli nie ma zaplanowanych zajęć.
     */
    private final StringProperty saturday;

    /**
     * Tworzy nowy obiekt ScheduleData z określonymi danymi harmonogramu zajęć.
     * Wszystkie parametry są konwertowane na obiekty StringProperty, które umożliwiają
     * dwukierunkowe wiązanie danych w interfejsie JavaFX.
     *
     * @param time Godzina zajęć
     * @param monday Informacje o zajęciach w poniedziałek
     * @param tuesday Informacje o zajęciach we wtorek
     * @param wednesday Informacje o zajęciach w środę
     * @param thursday Informacje o zajęciach w czwartek
     * @param friday Informacje o zajęciach w piątek
     * @param saturday Informacje o zajęciach w sobotę
     */
    public ScheduleData(String time, String monday, String tuesday, String wednesday,
                        String thursday, String friday, String saturday) {
        this.time = new SimpleStringProperty(time);
        this.monday = new SimpleStringProperty(monday);
        this.tuesday = new SimpleStringProperty(tuesday);
        this.wednesday = new SimpleStringProperty(wednesday);
        this.thursday = new SimpleStringProperty(thursday);
        this.friday = new SimpleStringProperty(friday);
        this.saturday = new SimpleStringProperty(saturday);
    }

    // Gettery i property
    /**
     * Zwraca właściwość czasu zajęć.
     * @return Właściwość reprezentująca czas zajęć
     */
    public StringProperty timeProperty() { return time; }
    
    /**
     * Zwraca właściwość zajęć w poniedziałek.
     * @return Właściwość reprezentująca zajęcia w poniedziałek
     */
    public StringProperty mondayProperty() { return monday; }
    
    /**
     * Zwraca właściwość zajęć we wtorek.
     * @return Właściwość reprezentująca zajęcia we wtorek
     */
    public StringProperty tuesdayProperty() { return tuesday; }
    
    /**
     * Zwraca właściwość zajęć w środę.
     * @return Właściwość reprezentująca zajęcia w środę
     */
    public StringProperty wednesdayProperty() { return wednesday; }
    
    /**
     * Zwraca właściwość zajęć w czwartek.
     * @return Właściwość reprezentująca zajęcia w czwartek
     */
    public StringProperty thursdayProperty() { return thursday; }
    
    /**
     * Zwraca właściwość zajęć w piątek.
     * @return Właściwość reprezentująca zajęcia w piątek
     */
    public StringProperty fridayProperty() { return friday; }
    
    /**
     * Zwraca właściwość zajęć w sobotę.
     * @return Właściwość reprezentująca zajęcia w sobotę
     */
    public StringProperty saturdayProperty() { return saturday; }
}