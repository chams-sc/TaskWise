package com.example.taskwiserebirth.chatlogs;

import org.bson.types.ObjectId;

public class ChatMessage {
    private ObjectId id;
    private String ownerId;
    private String dialogue;
    private String timestamp;
    private String role;

    // Constructor, getters and setters
    public ChatMessage(ObjectId id, String ownerId, String dialogue, String timestamp, String role) {
        this.id = id;
        this.ownerId = ownerId;
        this.dialogue = dialogue;
        this.timestamp = timestamp;
        this.role = role;
    }

    public ObjectId getId() { return id; }
    public String getDialogue() { return dialogue; }
    public String getTimestamp() { return timestamp; }
    public String getRole() { return role; }
}
