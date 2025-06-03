package com.example.silowniaprojekt;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;

/**
 * Klasa reprezentująca dane o przychodach siłowni.
 * Przechowuje informacje o przychodach z różnych źródeł (karnety, treningi personalne,
 * sprzedaż produktów) w poszczególnych okresach rozliczeniowych.
 * Wykorzystywana do generowania raportów finansowych i analizy zyskowności.
 */
public class IncomeData {
    private final StringProperty date;
    private final StringProperty client;
    private final StringProperty training;
    private final DoubleProperty amount;

    public IncomeData(String date, String client, String training, double amount) {
        this.date = new SimpleStringProperty(date);
        this.client = new SimpleStringProperty(client);
        this.training = new SimpleStringProperty(training);
        this.amount = new SimpleDoubleProperty(amount);
    }

    // Gettery i settery
    public String getDate() {
        return date.get();
    }

    public void setDate(String value) {
        date.set(value);
    }

    public StringProperty dateProperty() {
        return date;
    }

    public String getClient() {
        return client.get();
    }

    public void setClient(String value) {
        client.set(value);
    }

    public StringProperty clientProperty() {
        return client;
    }

    public String getTraining() {
        return training.get();
    }

    public void setTraining(String value) {
        training.set(value);
    }

    public StringProperty trainingProperty() {
        return training;
    }

    public double getAmount() {
        return amount.get();
    }

    public void setAmount(double value) {
        amount.set(value);
    }

    public DoubleProperty amountProperty() {
        return amount;
    }
}
