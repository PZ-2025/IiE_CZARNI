package com.example.silowniaprojekt;

import javafx.beans.property.*;

/**
 * Klasa reprezentująca dane o postępach treningowych klientów.
 * Przechowuje informacje o osiągnięciach, pomiarach ciała,
 * poprawie wyników i realizacji celów treningowych.
 * Wykorzystywana do śledzenia efektywności treningów i generowania raportów postępów.
 */
public class ProgressData {
    private final StringProperty exerciseName;
    private final DoubleProperty progress;
    private final StringProperty target;

    /**
     * Tworzy nowy obiekt danych o postępie treningowym.
     * Inicjalizuje właściwości JavaFX dla nazwy ćwiczenia, 
     * aktualnego postępu oraz celu treningowego.
     *
     * @param exerciseName nazwa ćwiczenia lub kategorii treningu
     * @param progress aktualny postęp wyrażony liczbowo (np. waga, powtórzenia)
     * @param target docelowa wartość do osiągnięcia
     */
    public ProgressData(String exerciseName, double progress, String target) {
        this.exerciseName = new SimpleStringProperty(exerciseName);
        this.progress = new SimpleDoubleProperty(progress);
        this.target = new SimpleStringProperty(target);
    }

    /**
     * Zwraca nazwę ćwiczenia lub kategorii treningu.
     * 
     * @return nazwa ćwiczenia jako ciąg znaków
     */
    // Gettery i property
    public StringProperty exerciseNameProperty() { return exerciseName; }
    public DoubleProperty progressProperty() { return progress; }
    public StringProperty targetProperty() { return target; }
}