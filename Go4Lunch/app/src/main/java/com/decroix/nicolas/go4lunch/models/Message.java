package com.decroix.nicolas.go4lunch.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Represents a message sent to the firestore
 * All functions (getters/setters, empty constructor) are required by firestore.
 */
@SuppressWarnings("unused")
public class Message {

    private String message;
    private Date dateCreated;
    private User userSender;
    private String userSenderID;
    private String urlImage;

    public Message() { }

    public Message(String message, User userSender) {
        this.message = message;
        this.userSender = userSender;
        this.userSenderID = userSender.getUid();
    }

    public Message(String message, String urlImage, User userSender) {
        this.message = message;
        this.urlImage = urlImage;
        this.userSender = userSender;
        this.userSenderID = userSender.getUid();
    }

    // --- GETTERS ---
    public String getMessage() { return message; }

    @ServerTimestamp
    public Date getDateCreated() { return dateCreated; }

    public User getUserSender() { return userSender; }

    public String getUrlImage() { return urlImage; }

    public String getUserSenderID() {
        return userSenderID;
    }

    // --- SETTERS ---
    public void setMessage(String message) { this.message = message; }

    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }

    public void setUserSender(User userSender) { this.userSender = userSender; }

    public void setUrlImage(String urlImage) { this.urlImage = urlImage; }

    public void setUserSenderID(String userSenderID) {
        this.userSenderID = userSenderID;
    }
}
