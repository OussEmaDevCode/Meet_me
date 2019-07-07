package com.pewds.oussa.Pox.models;

public class conversation {
    private String conversationId;
    public conversation(String conversationId){
        this.conversationId = conversationId;
    }
    public conversation(){}

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
