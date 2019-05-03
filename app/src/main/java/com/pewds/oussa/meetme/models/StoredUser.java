package com.pewds.oussa.meetme.models;

public class StoredUser {
    private String userName;
    private String userId;
    private String photoUri;
    public StoredUser(String userName, String userId, String photoUri){
        this.userId = userId;
        this.userName = userName;
        this.photoUri = photoUri;
    }
public StoredUser(){}
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }
}
