package com.example.taskwiserebirth.database;

import android.content.Context;
import android.util.Log;

import com.example.taskwiserebirth.utils.PasswordUtils;

import org.bson.Document;

import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;

public class UserDatabaseManager {
    private static final String TAG_USER_DBM = "USER_DB_MANAGER";
    private final MongoCollection<Document> userDataCollection;
    private final Context context;
    private User user;
    public UserDatabaseManager (Context context, User user) {
        this.userDataCollection = MongoDbRealmHelper.getMongoCollection("UserAccountData");
        this.context = context;
        this.user = user;
    }

    public void insertUserData(UserModel userModel) {
        if (user != null) {
            String hashedPassword = PasswordUtils.hashPassword(userModel.getPassword());

            Document userData = new Document("owner_id", user.getId())
                    .append("email", userModel.getEmail())
                    .append("password", hashedPassword)
                    .append("ai_name", "Mio");

            userDataCollection.insertOne(userData).getAsync(result -> {
                if (result.isSuccess()) {
                    Log.v(TAG_USER_DBM, "User data inserted");
                } else {
                    Log.e(TAG_USER_DBM, "Failed to insert user data: " + result.getError());
                }
            });
        } else {
            Log.e(TAG_USER_DBM, "User is null");
        }
    }

    public void getUserData () {
        if (user != null) {
            Document userFilter = new Document("owner_id", user.getId());

        } else {
            Log.e(TAG_USER_DBM, "User is null");
        }
    }

    public interface GetUserDataCallback {
        void onUserDataRetrieved(UserModel userModel);
        void onError(Exception e);
    }

    public interface GetPasswordCallback {
        void onPasswordRetrieved(String password);
//        void onPasswordFailed(ErrorCode errorCode);
    }
}
