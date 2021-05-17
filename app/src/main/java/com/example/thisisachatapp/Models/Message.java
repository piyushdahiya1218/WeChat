package com.example.thisisachatapp.Models;

public class Message {
    private String messageId, message, senderId;
    private long timestamp;
    private String urilink;
    private String date;
/*
    private int feeling = -1;
*/

    public Message() {
    }

    public Message(String message, String senderId, long timestamp, String date) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.date = date;
    }
    public Message(String senderId, long timestamp, String urilink) {
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.urilink = urilink;
    }


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUrilink() {
        return urilink;
    }

    public void setUrilink(String uri) {
        this.urilink = uri;
    }

/*
    public int getFeeling() {
        return feeling;
    }

    public void setFeeling(int feeling) {
        this.feeling = feeling;
    }
*/

}
