package com.example.taskwiserebirth.database;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.bson.Document;

import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;

public class UserDatabaseManager {
    private static final String TAG_USER_DBM = "USER_DB_MANAGER";
    private final MongoCollection<Document> userDataCollection;
    private final Context context;
    private User user;
    public UserDatabaseManager (User user, Context context) {
        this.userDataCollection = MongoDbRealmHelper.getMongoCollection("UserAccountData");
        this.context = context;
        this.user = user;
    }

    public void insertUserData(UserModel userModel) {
        if (user != null) {

            Document userData = new Document("owner_id", user.getId())
                    .append("email", userModel.getEmail())
                    .append("ai_name", "Kaia");

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

    public void getUserData (GetUserDataCallback callback) {
        if (user != null) {
            Document userFilter = new Document("owner_id", user.getId());

            // TODO: add other user data needed
            userDataCollection.findOne(userFilter).getAsync(result -> {
                if (result.isSuccess()) {
                    Document doc = result.get();
                    UserModel userModel = new UserModel();
                    userModel.setEmail(doc.getString("email"));
                    userModel.setAiName(doc.getString("ai_name"));

                    callback.onUserDataRetrieved(userModel);
                } else {
                    Log.e(TAG_USER_DBM, "Error retrieving user data " + result.getError());
                }
            });
        } else {
            Log.e(TAG_USER_DBM, "User is null");
        }
    }

    public void changeAiName(String aiName) {
        if (user != null) {
            Document userFilter = new Document("owner_id", user.getId());
            Document updateAiName = new Document("$set", new Document("ai_name", aiName));

            userDataCollection.updateOne(userFilter, updateAiName).getAsync(result -> {
                if (result.isSuccess()) {
                    Toast.makeText(context, "AI name is now changed to " + aiName, Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG_USER_DBM, "Error updating ai_name: " + result.getError());
                }
            });
        } else {
            Log.e(TAG_USER_DBM, "User is null");
        }
    }

    public interface GetUserDataCallback {
        void onUserDataRetrieved(UserModel userModel);
    }

}
