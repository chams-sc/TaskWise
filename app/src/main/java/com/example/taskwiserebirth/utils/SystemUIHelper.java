package com.example.taskwiserebirth.utils;

import android.app.Dialog;
import android.os.Build;
import android.view.View;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class SystemUIHelper {

    // Define your flags here
    public static int FLAGS = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LOW_PROFILE;

    public static void setSystemUIVisibility(AppCompatActivity activity) {
        View decorView = activity.getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For devices running on Android R (API 30) and above
            decorView.getWindowInsetsController().hide(
                    android.view.WindowInsets.Type.navigationBars() | android.view.WindowInsets.Type.statusBars());
            decorView.getWindowInsetsController().setSystemBarsBehavior(android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else {
            // For devices running on Android KitKat (API 19) to Q (API 29)
            decorView.setSystemUiVisibility(FLAGS);
        }
    }

    public static void adjustDialog(AppCompatActivity activity, Dialog dialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For devices running on Android R (API 30) and above
            dialog.getWindow().setDecorFitsSystemWindows(false);
        } else {
            // For devices running on Android KitKat (API 19) to Q (API 29)
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        setSystemUIVisibility(activity);
    }

    public static void setFlagsOnThePeekView() {
        try {
            Class<?> wmgClass = Class.forName("android.view.WindowManagerGlobal");
            Object wmgInstance = wmgClass.getMethod("getInstance").invoke(null);
            Field viewsField = wmgClass.getDeclaredField("mViews");
            viewsField.setAccessible(true);

            ArrayList<View> views = (ArrayList<View>) viewsField.get(wmgInstance);
            // When the popup appears, its decorView is the peek of the stack aka last item
            if (!views.isEmpty()) {
                View peekView = views.get(views.size() - 1);
                if (peekView != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        WindowInsetsController insetsController = peekView.getWindowInsetsController();
                        if (insetsController != null) {
                            insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                        }
                    } else {
                        peekView.setSystemUiVisibility(SystemUIHelper.FLAGS);
                        peekView.setOnSystemUiVisibilityChangeListener(visibility -> peekView.setSystemUiVisibility(SystemUIHelper.FLAGS));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
