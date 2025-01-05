package com.example.taskwiserebirth.notifications;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.taskwiserebirth.MainActivity;
import com.example.taskwiserebirth.R;
import com.example.taskwiserebirth.conversational.AIRandomSpeech;
import com.example.taskwiserebirth.task.TaskModel;
import com.example.taskwiserebirth.utils.CalendarUtils;
import com.google.gson.Gson;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String taskJson = intent.getStringExtra("task_json");
        Gson gson = new Gson();
        TaskModel task = gson.fromJson(taskJson, TaskModel.class);

        String taskName = task.getTaskName();
        String deadline = task.getDeadline();
        String contentText;

        if (deadline.equalsIgnoreCase("no deadline")) {
            contentText = AIRandomSpeech.generateUnfinishedTaskReminder(taskName);
        } else {
            String formattedDate = CalendarUtils.getFormattedDate(deadline);
            String formattedTime = CalendarUtils.getFormattedTime(deadline);
            if (CalendarUtils.isDateAccepted(deadline)) {
                contentText = AIRandomSpeech.generateTaskDueReminder(taskName, formattedDate, formattedTime);
            } else {
                contentText = AIRandomSpeech.generatePastDueTaskReminder(taskName, formattedDate, formattedTime);
            }
        }

        boolean isRepeating = intent.getBooleanExtra("is_repeating", false);

        if (isRepeating) {
            NotificationScheduler.scheduleNotificationOnce(context, task, true);
        }

        // check needed for notificationManagerCompat.notify
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS);
        }

        Intent i = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = "TaskWise";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.baseline_notification)
                .setContentTitle(taskName)
                .setContentText(contentText)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        int notificationID = task.getId().toHexString().hashCode();
        notificationManagerCompat.notify(notificationID, builder.build());
    }

}
