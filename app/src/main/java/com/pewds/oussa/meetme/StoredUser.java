package com.pewds.oussa.meetme;

import java.util.List;

public class StoredUser {
    private String userName;
    private String userId;
    public StoredUser(String userName, String userId){
        this.userId = userId;
        this.userName = userName;
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

}
