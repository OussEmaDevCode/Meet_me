package com.pewds.oussa.meetme;

public class conversation {
    private String userName;
    private String userId;
    private String conversationId;

    public conversation(String userName, String userId, String conversationId){
        this.userId = userId;
        this.userName = userName;
        this.conversationId = conversationId;
    }
    public conversation(){}

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

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
