package com.example.taskwiserebirth.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class PermissionUtils {

    public static void requestNotificationPermission(FragmentActivity fragmentActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(fragmentActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(fragmentActivity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    public static boolean isNotificationPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }


    public static boolean checkRecordAudioPermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("This app requires RECORD_AUDIO permission for speech recognition to feature to work as expected.")
                    .setTitle("Permission Required")
                    .setCancelable(false)
                    .setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()))
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                            intent.setData(uri);
                            context.startActivity(intent);

                            dialog.dismiss();
                        }
                    });
            builder.show();
            return false;
        } else {
            return true;
        }
    }

    public static void requestAlarmReminderOn(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Alarms and reminders permission is required to receive notifications on time. Go to App Settings -> Alarms and Reminders")
                .setTitle("Permission Required")
                .setCancelable(false)
                .setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()))
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                        intent.setData(uri);
                        context.startActivity(intent);

                        dialog.dismiss();
                    }
                });
        builder.show();
    }
}
