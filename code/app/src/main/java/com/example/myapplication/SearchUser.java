package com.example.myapplication;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class SearchUser implements Serializable {
    private String userID;
    private String username;
    private List<String> followers;

    private List<String> following;

    private List<String> followRequests;

    public SearchUser(String userID, String username) {
        this.userID = userID;
        this.username = username;
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
        this.followRequests = new ArrayList<>();
    }
    public SearchUser() {
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
        this.followRequests = new ArrayList<>();
    }
    // Getters and setters
    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public List<String> getFollowers() {
        return followers;
    }
    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }
    public List<String> getFollowing() {
        return following;
    }
    public void setFollowing(List<String> following) {
        this.following = following;
    }
    public List<String> getFollowRequests() {
        return followRequests;
    }
    public void setFollowRequests(List<String> followRequests) {
        this.followRequests = followRequests;
    }
}
