package com.example.taskwiserebirth;

import android.app.Application;

import com.example.taskwiserebirth.database.MongoDbRealmHelper;

import io.realm.Realm;

public class TaskWiseApplication extends Application {
    // prevents the leak of realm instance across activities
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        MongoDbRealmHelper.initializeRealmApp();
    }

    @Override
    public void onTerminate() {
        Realm.getDefaultInstance().close();
        super.onTerminate();
    }

}
