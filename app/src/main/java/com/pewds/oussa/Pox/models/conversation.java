package com.pewds.oussa.Pox.models;

public class conversation {
    private String userName;
    private String conversationId;
    private String PhotoUri;
    public conversation(String userName, String conversationId, String photoUri){
        this.userName = userName;
        this.conversationId = conversationId;
        this.PhotoUri = photoUri;
    }
    public conversation(){}

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

    public String getPhotoUri() {
        return PhotoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.PhotoUri = photoUri;
    }
}
