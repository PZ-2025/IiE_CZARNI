package com.example.projektsilownia;

public class Membership {
    private final String name;
    private final String description;
    private final double price;
    private final boolean active;

    public Membership(String name, String description, double price, boolean active) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.active = active;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public boolean isActive() { return active; }
}