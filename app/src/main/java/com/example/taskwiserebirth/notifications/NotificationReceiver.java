package com.example.taskwiserebirth.notifications;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.taskwiserebirth.MainActivity;
import com.example.taskwiserebirth.R;

public class NotificationReceiver extends BroadcastReceiver {
    private static final int PERMISSION_REQUEST_CODE = 101;
    private final int notificationID = 123;
    @Override
    public void onReceive(Context context, Intent intent) {

        // check if the app has permission to post notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        String taskName = intent.getStringExtra("TASK_NAME");

        // create and show the notification
        Intent i = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_IMMUTABLE);

        String channelId = "TaskWise";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.baseline_notification)
                .setContentTitle("Taskwise")
                .setContentText(taskName)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        notificationManagerCompat.notify(notificationID, builder.build());
    }

}
