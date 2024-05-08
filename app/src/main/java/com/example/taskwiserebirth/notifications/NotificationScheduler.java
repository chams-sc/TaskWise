package com.example.taskwiserebirth.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.example.taskwiserebirth.task.Task;
import com.example.taskwiserebirth.utils.PermissionUtils;

import java.util.Calendar;

public class NotificationScheduler {

    private static AlarmManager alarmManager;
    private static PendingIntent pendingIntent;

    public static void scheduleNotification(Context context, Task task) {
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
        pendingIntent = PendingIntent.getBroadcast(context, notificationCode, intent, PendingIntent.FLAG_IMMUTABLE);

        long triggerAtMillis = parseTriggerTime(task);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                if (PermissionUtils.isNotificationPermissionGranted(context)) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, triggerAtMillis, pendingIntent);
                    Toast.makeText(context, "Notification scheduled", Toast.LENGTH_SHORT).show();
                } else {
                    PermissionUtils.requestNotificationPermission((FragmentActivity) context);
                    Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show();
                }
            } else {
                PermissionUtils.requestAlarmReminderOn(context);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, triggerAtMillis, pendingIntent);
            Toast.makeText(context, "Notification scheduled", Toast.LENGTH_SHORT).show();
        }
    }


    private static void scheduleNotificationRepeating(Context context, Task task, boolean isRecurrence) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("TASK_NAME", task.getTaskName());

        int notificationCode = task.getId().toHexString().hashCode();
        pendingIntent = PendingIntent.getBroadcast(context, notificationCode, intent, PendingIntent.FLAG_IMMUTABLE);

        // Set the time for 9 AM every Saturday
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long triggerAtMillis = calendar.getTimeInMillis();

        // Set a repeating alarm
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_DAY * 7, pendingIntent);
        Toast.makeText(context, "Alarm set every Saturday at 9 AM", Toast.LENGTH_SHORT).show();

    }

    public static void cancelNotification(Context context, Task task) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        int notificationCode = task.getId().toHexString().hashCode();
        pendingIntent = PendingIntent.getBroadcast(context, notificationCode, intent, PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }

        alarmManager.cancel(pendingIntent);
        Toast.makeText(context, "Notification canceled", Toast.LENGTH_SHORT).show();
    }
}
