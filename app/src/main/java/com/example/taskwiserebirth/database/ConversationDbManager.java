package com.example.taskwiserebirth.database;

import android.util.Log;

import org.bson.Document;

import java.util.Date;

import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;

public class ConversationDbManager {

    private static final String TAG_CONVO_DBM = "CONVO_DBM";
    private final User user;
    private final MongoCollection<Document> conversationCollection;

    public ConversationDbManager(User user) {
        this.user = user;
        this.conversationCollection = MongoDbRealmHelper.getMongoCollection("UserAIConversation");
    }

    public void insertDialogue(String dialogue, boolean isAssistant) {
        String role = "user";
        if (isAssistant) {
            role = "assistant";
        }

        if (user != null) {
            Document document = new Document("owner_id", user.getId())
                    .append("dialogue", dialogue)
                    .append("timestamp", new Date())
                    .append("role", role);

            conversationCollection.insertOne(document).getAsync(result -> {
                if (result.isSuccess()) {
                    Log.d(TAG_CONVO_DBM, "Dialogue inserted: " + dialogue);
                } else {
                    Log.e(TAG_CONVO_DBM, "Failed to insert dialogue: " + result.getError().getErrorMessage());
                }
            });
        }
    }
}
