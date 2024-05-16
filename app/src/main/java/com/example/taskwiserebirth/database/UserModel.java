package com.example.taskwiserebirth.database;

import org.bson.types.ObjectId;

public class UserModel {

    public UserModel() {
    }

    public UserModel(String email) {
        this.email = email;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAiName() {
        return aiName;
    }

    public void setAiName(String aiName) {
        this.aiName = aiName;
    }

    private ObjectId id;
    private String email;
    private String aiName;
}
