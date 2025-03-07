package com.example.myapplication;

import java.util.Date;

public class MoodEvent {

    // what mood is, could be happy, sad, angry, etc...
    private String mood;

    // when mood is recorded
    private Date timestamp;


    // initializes mood and timestamp
    public MoodEvent(String mood, Date timestamp) {
        this.mood = mood;
        this.timestamp = timestamp;
    }

    // getters to get the mood and timestamp
    public String getMood() {
        return mood;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
