package com.example.taskwiserebirth.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.example.taskwiserebirth.task.Task;

public class NotificationScheduler {

    private static AlarmManager alarmManager;
    private static PendingIntent pendingIntent;

    public static void scheduleNotification(Context context, Task task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {

            } else {

            }
        }

        if (task.getRecurrence().equals("None")) {
            if (task.getDeadline().equals("No deadline") && task.getSchedule().equals("No schedule")) {
                scheduleNotificationRepeating(context, task, false);
            } else {
                scheduleNotificationOnce(context, task);
            }
        } else {
            scheduleNotificationRepeating(context, task, true);
        }
    }

    private static long parseTriggerTime(Task task) {
        if (task.getSchedule().equals("No schedule") && task.getDeadline().equals("No deadline")) {

        }
        return 60000;
    }

    private static void scheduleNotificationOnce(Context context, Task task) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("TASK_NAME", task.getTaskName());

        int notificationCode = task.getId().toHexString().hashCode();
        pendingIntent = PendingIntent.getBroadcast(context, notificationCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long triggerAtMillis = parseTriggerTime(task);
    }


    private static void scheduleNotificationRepeating(Context context, Task task, boolean isRecurrence) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int notificationCode = task.getId().toHexString().hashCode();
        // Set the interval to 15 seconds
        long intervalMillis = 10 * 1000; // 15 seconds
        long triggerAtMillis = System.currentTimeMillis() + intervalMillis;

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("task_name", task.getTaskName());
        intent.putExtra("is_repeating", true);
        intent.putExtra("trigger_time", triggerAtMillis);
        intent.putExtra("notification_code", notificationCode);

        pendingIntent = PendingIntent.getBroadcast(context, notificationCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }

        Toast.makeText(context, "Repeating notification scheduled", Toast.LENGTH_SHORT).show();
    }


    public static void cancelNotification(Context context, Task task) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        int notificationCode = task.getId().toHexString().hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationCode, intent, PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }

        alarmManager.cancel(pendingIntent);
        Toast.makeText(context, "Notification canceled", Toast.LENGTH_SHORT).show();
    }
}
