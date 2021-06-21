package com.example.schedulemyreminder.models;

public class ReminderModel {
    private int id;
    private String message;
    private String date;

    public ReminderModel(int id, String message, String date) {
        this.id = id;
        this.message = message;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }
}
