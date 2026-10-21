package com.example.android.mymap.models;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewPostDetails {
    private String postType;
    private String postPrice;
    private String postDescription;
    private String postImageUrl;
    private String postUploadDate;
    private String postUploadTime;
    private String postKey;
    private boolean isPostEditable;
    private boolean phoneAvailable;
    private String userid;

    public NewPostDetails() {
    }

    public NewPostDetails(String postType, String postPrice, String postDescription, String postImageUrl,
                          String postUploadDate, String postUploadTime) {
        this.postType = postType;
        this.postPrice = postPrice;
        this.postDescription = postDescription;
        this.postImageUrl = postImageUrl;
        this.postUploadDate = postUploadDate;
        this.postUploadTime = postUploadTime;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public String getPostPrice() {
        return postPrice;
    }

    public void setPostPrice(String postPrice) {
        this.postPrice = postPrice;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public void setPostImageUrl(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }

    public String getPostUploadDate() {
        return postUploadDate;
    }

    public void setPostUploadDate(String postUploadDate) {
        this.postUploadDate = postUploadDate;
    }

    public String getPostUploadTime() {
        return postUploadTime;
    }

    public void setPostUploadTime(String postUploadTime) {
        this.postUploadTime = postUploadTime;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public boolean isPostEditable() {
        return isPostEditable;
    }

    public void setPostEditable(boolean postEditable) {
        isPostEditable = postEditable;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public boolean isPhoneAvailable() {
        return phoneAvailable;
    }

    public void setPhoneAvailable(boolean phoneAvailable) {
        this.phoneAvailable = phoneAvailable;
    }
}
