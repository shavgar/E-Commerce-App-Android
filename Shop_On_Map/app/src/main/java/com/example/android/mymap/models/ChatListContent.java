package com.example.android.mymap.models;

public class ChatListContent {
    private String profileImageUrl;
    private String username;
    private String message;
    private String userid;

    public ChatListContent() {
    }
    public ChatListContent( String userid, String username, String profileImageUrl, String message) {
        this.profileImageUrl = profileImageUrl;
        this.username = username;
        this.message = message;
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
