package com.example.myapplication;

import java.time.LocalDateTime;

public class MoodHistory implements Comparable<MoodHistory> {
    private String username;

    private String Mood;
    private long timestamp;


    public MoodHistory(String username, String Mood,long timestamp){
        this.username = username;
        this.Mood = Mood;
        this.timestamp = timestamp;
    }

    public String getMood() {
        return Mood;
    }



    public String getUsername() {
        return username;
    }

    public long getTimestamp() {
        return timestamp;



    }

    @Override
    public int compareTo(MoodHistory moodHistory) {
        return 0;
    }
}
// getters
