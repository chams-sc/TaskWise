package com.example.taskwiserebirth;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import ai.picovoice.porcupine.Porcupine;
import ai.picovoice.porcupine.PorcupineException;
import ai.picovoice.porcupine.PorcupineManager;
import ai.picovoice.porcupine.PorcupineManagerCallback;

public class PorcupineService extends Service {

    private static final String CHANNEL_ID = "PorcupineServiceChannel";
    private static final String WAKEWORD_DETECTED_ACTION = "com.example.taskwiserebirth.WAKEWORD_DETECTED";
    private PorcupineManager porcupineManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        initializePorcupine();
        startForeground(1, getNotification());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Porcupine Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification getNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Porcupine Wake Word Service")
                .setContentText("Listening for wake words")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }

    private void initializePorcupine() {
        try {
            porcupineManager = new PorcupineManager.Builder()
                    .setAccessKey("NGQpZinYtUHQqcSina8WhS92hZo3TznujGxCFxK017ySw3BACzHMdQ==")
                    .setKeyword(Porcupine.BuiltInKeyword.PICOVOICE)
                    .setSensitivity(0.5f)
                    .build(getApplicationContext(), new PorcupineManagerCallback() {
                        @Override
                        public void invoke(int keywordIndex) {
                            onWakeWordDetected();
                        }
                    });
            porcupineManager.start();
        } catch (PorcupineException e) {
            e.printStackTrace();
        }
    }

    private void onWakeWordDetected() {
        Log.v("PorcupineService", "Wake word detected!");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (porcupineManager != null) {
            try {
                porcupineManager.stop();
                porcupineManager.delete();
            } catch (PorcupineException e) {
                e.printStackTrace();
            }
        }
    }
}
