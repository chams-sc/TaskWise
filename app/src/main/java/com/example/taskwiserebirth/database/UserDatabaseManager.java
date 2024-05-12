package com.example.taskwiserebirth.database;

import android.content.Context;

import org.bson.Document;

import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;

public class UserDatabaseManager {
    private static final String TAG_TASK_DBM = "USER_DB_MANAGER";
    private final User user;
    private final MongoCollection<Document> taskCollection;
    private final Context context;
    public UserDatabaseManager (User user, Context context) {
        this.user = user;
        this.taskCollection = MongoDbRealmHelper.getMongoCollection("UserAccountData");
        this.context = context;
    }
}
