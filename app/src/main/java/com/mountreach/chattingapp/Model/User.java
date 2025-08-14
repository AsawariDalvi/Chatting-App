package com.mountreach.chattingapp.Model;

import com.google.firebase.Timestamp;
import com.google.firebase.database.Exclude; // This Exclude is for Realtime Database. For Firestore, use @DocumentId, @PropertyName if needed, or simply ensure fields are public/have getters/setters.

import java.io.Serializable;

public class User implements Serializable {
    private String uid; // Contact ID - this will often be the document ID in Firestore for user profiles

    // @Exclude from Realtime Database is not directly applicable for Firestore's default mapping
    // If 'userId' is not a field in your Firestore document, ensure it's handled separately
    // or marked with @Exclude if you're also mapping this class to Realtime Database.
    // For Firestore, simply omit fields you don't want mapped, or use custom mapping.
    // Assuming 'userId' is a temporary field for local use or derived data,
    // if it's not stored in Firestore documents, Firebase will ignore it.
    private String userId; // Who saved the contact (owner UID) - Consider if this truly belongs in 'User' or 'Contact' model.

    private String firstName;
    private String lastName;
    private String fullName; // Can be derived from firstName and lastName for display
    private String phoneNumber;
    private String countryCode;
    private String fullPhone;
    private String profileImageUrl;
    private boolean isOnline;

    // ✅ This is correct for mapping a Firestore Timestamp field
    private Timestamp timestamp; // Timestamp of when the user object was created/last updated in Firestore

    // 🔥 Added for chat preview - these fields are usually NOT directly stored in the 'User' document itself
    // but are derived or populated dynamically from the 'Chats' or 'Messages' collection.
    // If you *do* store them in the User document (e.g., as a summary field for quick fetching),
    // then their type must match what's in Firestore.
    //
    // If 'lastMessageTime' is stored as a Firestore Timestamp, change this to Timestamp.
    // If it's stored as a simple number (milliseconds or seconds), then 'long' is correct.
    // Let's assume for now it's a 'long' (e.g., System.currentTimeMillis()).
    @Exclude // Exclude this from being saved to Firestore if it's dynamic
    private String lastMessage; // The content of the last message
    @Exclude // Exclude this from being saved to Firestore if it's dynamic
    private long lastMessageTime; // Timestamp of the last message (e.g., epoch milliseconds)


    // ✅ Firestore requires empty constructor
    public User() {
    }

    // ✅ Full constructor for initial data
    // Consider if you need all fields in one constructor, or separate ones for clarity
    public User(String uid, String userId, String firstName, String lastName, String fullName,
                String phoneNumber, String countryCode, String fullPhone,
                String profileImageUrl, boolean isOnline, Timestamp timestamp) {
        this.uid = uid;
        this.userId = userId; // Consider if this is part of the User's own profile or a "Contact"
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.countryCode = countryCode;
        this.fullPhone = fullPhone;
        this.profileImageUrl = profileImageUrl;
        this.isOnline = isOnline;
        this.timestamp = timestamp;
    }

    // --- Getters & Setters ---

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    // If userId is truly a field you store in Firestore for the 'User' document, keep this.
    // If it's more about who *saved* this user as a contact, it might belong in a separate 'Contact' model.
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Recommended: Make fullName a computed getter if it's always derived.
    // If fullName is stored separately in Firestore, then keep getter/setter for the field.
    public String getFullName() {
        // Only compute if fullName isn't explicitly set (or if you prefer dynamic)
        if (fullName != null && !fullName.isEmpty()) {
            return fullName;
        } else if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        }
        return ""; // Or null, depending on your default
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getFullPhone() {
        return fullPhone;
    }

    public void setFullPhone(String fullPhone) {
        this.fullPhone = fullPhone;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean getIsOnline() { // Use getIsOnline() for boolean fields
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }


    @Exclude
    public String getLastMessage() { // Removed 'Text' for brevity, still clear
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Exclude
    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    // ✅ For Adapter Compatibility (Fallback) - this is good practice
    // Firestore will ignore methods without corresponding fields/getters/setters
    // unless explicitly told to map them.
    @Exclude
    public String getUserName() {
        if (fullName != null && !fullName.isEmpty()) return fullName;
        if (firstName != null && !firstName.isEmpty()) return firstName;
        if (uid != null) return uid;
        return "Unnamed";
    }


}