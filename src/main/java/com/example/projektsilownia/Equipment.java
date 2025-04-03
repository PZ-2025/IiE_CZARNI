package com.example.projektsilownia;

public class Equipment {
    private final String name;
    private final int quantity;
    private final String status;

    public Equipment(String name, int quantity, String status) {
        this.name = name;
        this.quantity = quantity;
        this.status = status;
    }

    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public String getStatus() { return status; }
}