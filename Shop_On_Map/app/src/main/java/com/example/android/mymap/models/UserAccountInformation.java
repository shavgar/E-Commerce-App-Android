package com.example.android.mymap.models;

public class UserAccountInformation {
    private String FirstName, LastName, Country, ProfileImageUrl, PhoneNumber;

    /*This constructor is used to get data from the database */
    public UserAccountInformation() {

    }
    /*This constructor is used to insert data into the database*/

    public UserAccountInformation(String firstName, String lastName, String country, String profileImageUrl, String phoneNumber) {
        FirstName = firstName;
        LastName = lastName;
        Country = country;
        ProfileImageUrl = profileImageUrl;
        PhoneNumber = phoneNumber;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getProfileImageUrl() {
        return ProfileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        ProfileImageUrl = profileImageUrl;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }
}
