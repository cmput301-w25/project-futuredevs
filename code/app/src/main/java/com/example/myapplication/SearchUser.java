package com.example.myapplication;

import com.google.firebase.firestore.auth.User;

import java.io.Serializable;

public class SearchUser implements Serializable {
    private String username;
    private String userId;

    public SearchUser(String userId, String username){
        this.userId = userId;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }



}
