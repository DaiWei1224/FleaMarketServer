package com.example.fleamarket.net;

import java.io.Serializable;

public class Chat implements Serializable {
    private String senderID;
    private String senderName;
    private String receiverID;
    private String sendTime;
    private String content;

    public Chat() {}

    public Chat(String senderID, String senderName, String receiverID, String sendTime, String content) {
        this.senderID = senderID;
        this.senderName = senderName;
        this.receiverID = receiverID;
        this.sendTime = sendTime;
        this.content = content;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
