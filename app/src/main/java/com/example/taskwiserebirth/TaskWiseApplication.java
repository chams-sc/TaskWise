package com.example.taskwiserebirth;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.example.taskwiserebirth.database.MongoDbRealmHelper;

import io.realm.Realm;

public class TaskWiseApplication extends Application {
    // prevents the leak of realm instance across activities
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        MongoDbRealmHelper.initializeRealmApp();

        createNotificationChannel();
    }

    @Override
    public void onTerminate() {
        Realm.getDefaultInstance().close();
        super.onTerminate();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "TaskWiseChannel";
            String description = "Channel for TaskWise notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel notificationChannel = new NotificationChannel("TaskWise", name, importance);
            notificationChannel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

}
