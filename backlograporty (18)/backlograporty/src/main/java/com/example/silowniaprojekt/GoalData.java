package com.example.silowniaprojekt;

import javafx.beans.property.*;

/**
 * Klasa reprezentująca dane o celach treningowych klientów siłowni.
 * Przechowuje informacje o celach, takich jak redukcja masy ciała,
 * budowa masy mięśniowej, poprawa wydolności czy elastyczności.
 * Wykorzystywana do śledzenia postępów i generowania raportów.
 */
public class GoalData {
    private final StringProperty exercise;
    private final StringProperty target;

    public GoalData(String exercise, String target) {
        this.exercise = new SimpleStringProperty(exercise);
        this.target = new SimpleStringProperty(target);
    }

    // Gettery i property
    public StringProperty exerciseProperty() { return exercise; }
    public StringProperty targetProperty() { return target; }
}