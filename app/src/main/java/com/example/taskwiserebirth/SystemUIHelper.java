package com.example.taskwiserebirth;

import android.os.Build;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SystemUIHelper {

    public static void setSystemUIVisibility(AppCompatActivity activity) {
        View decorView = activity.getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For devices running on Android R (API 30) and above
            decorView.getWindowInsetsController().hide(
                    android.view.WindowInsets.Type.navigationBars() | android.view.WindowInsets.Type.statusBars());
            decorView.getWindowInsetsController().setSystemBarsBehavior(android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else {
            // For devices running on Android KitKat (API 19) to Q (API 29)
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LOW_PROFILE
            );
        }
    }
}
