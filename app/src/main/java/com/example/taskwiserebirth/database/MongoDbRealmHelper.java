package com.example.taskwiserebirth.database;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDbRealmHelper {

    private static final String SERVICE_NAME = "mongodb-atlas";
    private static final String DATABASE_NAME = "TaskWise";
    private static final String APP_ID = "taskwise-bxyah";
    private static App app;
    private static List<DatabaseChangeListener> listeners = new ArrayList<>();

    public static App initializeRealmApp() {
        if (app == null) {
            app = new App(new AppConfiguration.Builder(APP_ID).build());
        }
        return app;
    }

    public static App getRealmApp() {
        if (app == null) {
            throw new IllegalStateException("Realm app is not initialized. Call initializeRealmApp() first.");
        }
        return app;
    }

    public static MongoClient getMongoClient() {
        App app = getRealmApp();
        return app.currentUser().getMongoClient(SERVICE_NAME);
    }

    public static MongoDatabase getMongoDatabase() {
        MongoClient mongoClient = getMongoClient();
        return mongoClient.getDatabase(DATABASE_NAME);
    }

    public static MongoCollection<Document> getMongoCollection(String collectionName) {
        MongoDatabase mongoDatabase = getMongoDatabase();
        return mongoDatabase.getCollection(collectionName);
    }


    public static void addDatabaseChangeListener(DatabaseChangeListener listener) {
        listeners.add(listener);
    }

    public static void removeDatabaseChangeListener(DatabaseChangeListener listener) {
        listeners.remove(listener);
    }

    public static void notifyDatabaseChangeListeners() {
        for (DatabaseChangeListener listener : listeners) {
            listener.onDatabaseChange();
        }
    }

}
