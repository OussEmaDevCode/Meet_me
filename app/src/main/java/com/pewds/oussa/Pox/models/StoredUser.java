package com.pewds.oussa.Pox.models;

public class StoredUser {
    private String userName;
    private String userId;
    private String photoUri;
    private String pox;
    public StoredUser(String userName, String userId, String photoUri, String pox){
        this.userId = userId;
        this.userName = userName;
        this.photoUri = photoUri;
        this.pox = pox;
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

    public String getPox() {
        return pox;
    }

    public void setPox(String pox) {
        this.pox = pox;
    }
}
