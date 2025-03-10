package com.example.myapplication;

import java.io.Serializable;
import java.util.Date;

public class Mood implements Serializable {
    private String moodId;
    private String userId;
    private Date timestamp;
    private String emotionalState;
    private String trigger;
    private String reasonText;
    private String reasonPhotoUrl;
    private String socialSituation;



    public Mood(String moodId, String userId, Date timestamp, String emotionalState,
                String trigger, String reasonText, String reasonPhotoUrl,
                String socialSituation) {
        this.moodId = moodId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.emotionalState = emotionalState;
        this.trigger = trigger;
        this.reasonText = reasonText;
        this.reasonPhotoUrl = reasonPhotoUrl;
        this.socialSituation = socialSituation;
    }

    public String getMoodId() {
        return moodId;
    }

    public void setMoodId(String moodId) {
        this.moodId = moodId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getEmotionalState() {
        return emotionalState;
    }

    public void setEmotionalState(String emotionalState) {
        this.emotionalState = emotionalState;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public String getReasonPhotoUrl() {
        return reasonPhotoUrl;
    }

    public void setReasonPhotoUrl(String reasonPhotoUrl) {
        this.reasonPhotoUrl = reasonPhotoUrl;
    }

    public String getSocialSituation() {
        return socialSituation;
    }

    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }





}
