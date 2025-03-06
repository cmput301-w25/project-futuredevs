package com.example.myapplication;

import java.util.Date;

public class MoodEvent {
    private String mood;
    private Date timestamp;

    public MoodEvent(String mood, Date timestamp) {
        this.mood = mood;
        this.timestamp = timestamp;
    }

    public String getMood() {
        return mood;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
