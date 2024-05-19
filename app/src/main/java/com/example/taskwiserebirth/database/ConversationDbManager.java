package com.example.taskwiserebirth.database;

import android.util.Log;

import com.example.taskwiserebirth.chatlogs.ChatMessage;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public void clearAIMemory(ClearMemoryCallback callback) {
        if (user != null) {
            Document userFilter = new Document("owner_id", user.getId());

            conversationCollection.deleteMany(userFilter).getAsync(result -> {
                if (result.isSuccess()) {
                    callback.onMemoryCleared("AI memory has been cleared.");
                } else {
                    Log.e(TAG_CONVO_DBM, "Failed to clear memory: ", result.getError());
                }
            });
        } else {
            Log.e(TAG_CONVO_DBM, "User is null");
        }
    }

    public void getAllUserConversation(GetAllConversationsCallback callback) {
        if (user != null) {
            Document userFilter = new Document("owner_id", user.getId());

            conversationCollection.find(userFilter).iterator().getAsync(result -> {
                if (result.isSuccess()) {
                    List<ChatMessage> chatMessages = new ArrayList<>();
                    while (result.get().hasNext()) {
                        Document document = result.get().next();
                        ChatMessage chatMessage = new ChatMessage(
                                document.getObjectId("_id"),
                                document.getString("owner_id"),
                                document.getString("dialogue"),
                                document.getDate("timestamp").toString(),
                                document.getString("role")
                        );
                        chatMessages.add(chatMessage);
                    }
                    callback.onSuccess(chatMessages);
                } else {
                    callback.onError(result.getError());
                }
            });
        } else {
            Log.e(TAG_CONVO_DBM, "User is null");
        }
    }

    public interface ClearMemoryCallback {
        void onMemoryCleared(String successMessage);
    }

    public interface GetAllConversationsCallback {
        void onSuccess(List<ChatMessage> chatMessages);
        void onError(Exception e);
    }
}
