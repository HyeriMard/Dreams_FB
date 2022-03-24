package com.example.dreams.Model;

public class Chats {
    private String sms, status, userID, type;

    public Chats(String sms, String status, String userID, String type) {
        this.sms = sms;
        this.status = status;
        this.userID = userID;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Chats() {
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
