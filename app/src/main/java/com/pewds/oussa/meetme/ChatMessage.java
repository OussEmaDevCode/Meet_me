package com.pewds.oussa.meetme;

import java.util.Date;
import java.util.List;

public class ChatMessage {
    private String messageText;
    private String messageUser;
    private long messageTime;
    private List<Double> latlong;
    private String uId;
    public ChatMessage(String messageText, String messageUser,List<Double> latlong,String id) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.latlong = latlong;
        this.uId = id;
        // Initialize to current time
        messageTime = new Date().getTime();
    }

    public ChatMessage(){

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public List<Double> getMessagelocation() {
        return latlong;
    }

    public void setMessagelocation(List<Double> latlong) {
        this.latlong = latlong;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getMessageUserId() {
        return uId;
    }

    public void setMessageUserId(String userId) {
        this.uId = userId;
    }
}
