package com.example.projektsilownia;

public class ScheduleItem {
    private final String time;
    private final String day;
    private final String type;
    private final String trainer;

    public ScheduleItem(String time, String day, String type, String trainer) {
        this.time = time;
        this.day = day;
        this.type = type;
        this.trainer = trainer;
    }

    public String getTime() { return time; }
    public String getDay() { return day; }
    public String getType() { return type; }
    public String getTrainer() { return trainer; }
}