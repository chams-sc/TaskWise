package com.example.taskwiserebirth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskwiserebirth.database.ConversationDbManager;
import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.task.TaskModel;
import com.example.taskwiserebirth.utils.CalendarUtils;
import com.example.taskwiserebirth.utils.PermissionUtils;
import com.example.taskwiserebirth.utils.SharedViewModel;
import com.example.taskwiserebirth.utils.SystemUIHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.realm.mongodb.App;
import io.realm.mongodb.User;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private Fragment activeFragment;
    private Live2DFragment live2DFragment;
    private AddTaskFragment addTaskFragment;
    private SMSFragment smsFragment;
    private SettingsFragment settingsFragment;
    private ConversationDbManager conversationDbManager;
    private SharedViewModel sharedViewModel;
    private App app;
    private static final String LAST_OPEN_DATE_KEY = "last_open_date";
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String FIRST_LAUNCH_KEY = "first_launch";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SystemUIHelper.setSystemUIVisibility(this);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        app = MongoDbRealmHelper.initializeRealmApp();
        User user = app.currentUser();
        conversationDbManager = new ConversationDbManager(user);

        // Initialize fragments
        live2DFragment = new Live2DFragment();
        addTaskFragment = new AddTaskFragment();
        smsFragment = new SMSFragment();
        settingsFragment = new SettingsFragment();

        fragmentManager = getSupportFragmentManager();

        // Add fragments to the fragment manager only if not previously added
        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.frame_layout, live2DFragment, "LIVE2D_FRAGMENT").hide(live2DFragment);
            fragmentTransaction.add(R.id.frame_layout, addTaskFragment, "ADD_TASK_FRAGMENT").hide(addTaskFragment);
            fragmentTransaction.add(R.id.frame_layout, smsFragment, "SMS_FRAGMENT").hide(smsFragment);
            fragmentTransaction.add(R.id.frame_layout, settingsFragment, "SETTINGS_FRAGMENT").hide(settingsFragment);
            fragmentTransaction.show(live2DFragment).commit();
            activeFragment = live2DFragment;
        } else {
            activeFragment = fragmentManager.findFragmentByTag("LIVE2D_FRAGMENT");
        }

        setupBottomNavigationListener();
        checkAndTriggerDailyUnfinishedTasks();
        checkRecordAudioPermission();
    }

    public void checkRecordAudioPermission() {
        if (PermissionUtils.checkRecordAudioPermission(this)) {
            startPorcupineService();
        }
    }

    private void startPorcupineService() {
        Intent serviceIntent = new Intent(this, PorcupineService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    public void pausePorcupineService() {
        Intent serviceIntent = new Intent(this, PorcupineService.class);
        serviceIntent.setAction("PAUSE_PORCUPINE");
        startService(serviceIntent);
    }

    public void resumePorcupineService() {
        Intent serviceIntent = new Intent(this, PorcupineService.class);
        serviceIntent.setAction("RESUME_PORCUPINE");
        startService(serviceIntent);
    }

    private void stopPorcupineService() {
        Intent serviceIntent = new Intent(this, PorcupineService.class);
        stopService(serviceIntent);
    }

    private void checkAndTriggerDailyUnfinishedTasks() {
        SharedPreferences preferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String lastOpenDate = preferences.getString(LAST_OPEN_DATE_KEY, null);
        String currentDate = CalendarUtils.getCurrentDate();

        boolean isFirstLaunch = preferences.getBoolean(FIRST_LAUNCH_KEY, true);

        if (isFirstLaunch) {
            // Set the first launch flag to false for subsequent launches
            preferences.edit().putBoolean(FIRST_LAUNCH_KEY, false).apply();
        } else if (lastOpenDate == null || !lastOpenDate.equals(currentDate)) {
            // Ensure Live2DFragment is shown
            showFragment(live2DFragment);

            // Introduce a 10-second delay before triggering speakUnfinishedTasks
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                if (live2DFragment != null) {
                    live2DFragment.speakUnfinishedTasks();
                }
            }, 10000); // 10 seconds delay

            preferences.edit().putString(LAST_OPEN_DATE_KEY, currentDate).apply();
        }
    }

    public ConversationDbManager getConversationDbManager() {
        return conversationDbManager;
    }

    private void setupBottomNavigationListener() {
        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();

            // Clear back stack to ensure main fragment can be shown properly
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            if (itemId == R.id.bottom_home) {
                showFragment(live2DFragment);
            } else if (itemId == R.id.bottom_clip) {
                showFragment(addTaskFragment);
            } else if (itemId == R.id.bottom_sms) {
                showFragment(smsFragment);
            } else if (itemId == R.id.bottom_settings) {
                showFragment(settingsFragment);
            }

            return true;
        });
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(activeFragment).show(fragment).commit();
        activeFragment = fragment;
    }

    public void showTaskDetailFragment(TaskModel task) {
        TaskDetailFragment taskDetailFragment = new TaskDetailFragment(task);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frame_layout, taskDetailFragment, "TASK_DETAIL_FRAGMENT")
                .addToBackStack(null) // Add the transaction to the back stack
                .commit();
    }

    public void showAllTaskFragment() {
        AllTaskFragment allTaskFragment = new AllTaskFragment();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frame_layout, allTaskFragment, "ALL_TASK_FRAGMENT")
                .addToBackStack(null) // Add the transaction to the back stack
                .commit();
    }

    public void showHelpFragment() {
        HelpFragment helpFragment = new HelpFragment();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frame_layout, helpFragment, "HELP_FRAGMENT")
                .addToBackStack(null) // Add the transaction to the back stack
                .commit();
    }

    public void toggleNavBarVisibility(boolean visible, boolean isScrollToggle) {
        long duration = 100;
        if (isScrollToggle) {
            if (visible) {
                bottomNavigationView.animate().translationY(0).setDuration(duration).setInterpolator(new DecelerateInterpolator());
            } else {
                bottomNavigationView.animate().translationY(bottomNavigationView.getHeight()).setDuration(duration).setInterpolator(new AccelerateInterpolator());
            }
        } else {
            if (bottomNavigationView.getVisibility() == View.VISIBLE) {
                bottomNavigationView.animate().translationY(bottomNavigationView.getHeight()).setDuration(duration).setInterpolator(new AccelerateInterpolator()).withEndAction(() -> bottomNavigationView.setVisibility(View.GONE));
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);
                bottomNavigationView.animate().translationY(0).setDuration(duration).setInterpolator(new DecelerateInterpolator());
            }
        }
    }

    public void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Log out the user from the Realm app
        if (app != null && app.currentUser() != null) {
            app.currentUser().logOutAsync(result -> {
                if (result.isSuccess()) {
                    Log.d("MongoDb", "User logged out successfully");
                    Toast.makeText(getApplicationContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Log.e("MongoDb", "Failed to log out: " + result.getError());
                    Toast.makeText(getApplicationContext(), "Logout failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PermissionUtils.checkRecordAudioPermission(this)) {
            startPorcupineService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPorcupineService();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            SystemUIHelper.setSystemUIVisibility(this);
        }
    }
}
