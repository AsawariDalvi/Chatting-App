package com.mountreach.chattingapp.Model;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Message {

    private String messageId;
    private String senderId;
    private String receiverId;
    private String message;
    private String type;

    // Do not deserialize this directly from Firestore
    private Object timestamp;

    public Message() {}

    public Message(String messageId, String senderId, String receiverId, String message, Object timestamp, String type) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
    }

    // Standard getters/setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        if (timestamp instanceof Timestamp) {
            return (Timestamp) timestamp;
        } else if (timestamp instanceof Long) {
            return new Timestamp(new Date((Long) timestamp));
        }
        return null;
    }

    public long getTimestampMillis() {
        Timestamp ts = getTimestamp();
        return ts != null ? ts.toDate().getTime() : 0L;
    }
}
