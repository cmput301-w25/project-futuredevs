package com.example.myapplication;

import java.time.LocalDateTime;

public class MoodHistory implements Comparable<MoodHistory> {
    private String username;

    private String Mood;
    private long timestamp;


    /**
     * Constructs a new {@code MoodHistory} instance.
     *
     * @param username  the username associated with the mood entry
     * @param Mood      the recorded mood
     * @param timestamp the timestamp of when the mood was recorded
     */
    public MoodHistory(String username, String Mood,long timestamp){
        this.username = username;
        this.Mood = Mood;
        this.timestamp = timestamp;
    }
    /**
     * Gets the recorded mood.
     *
     * @return the mood as a string
     */
    public String getMood() {
        return Mood;
    }
    /**
     * Gets the username associated with the mood entry.
     *
     * @return the username
     */


    public String getUsername() {
        return username;
    }
    /**
     * Gets the timestamp of when the mood was recorded.
     *
     * @return the timestamp as a long value
     */
    public long getTimestamp() {
        return timestamp;



    }
    /**
     * Compares this {@code MoodHistory} object with another based on timestamp.
     *
     * @param moodHistory the other MoodHistory object to compare with
     * @return a negative integer, zero, or a positive integer if this object's timestamp
     *         is less than, equal to, or greater than the specified object's timestamp
     */
    @Override
    public int compareTo(MoodHistory moodHistory) {
        return 0;
    }
}
// getters
