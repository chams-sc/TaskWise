package com.example.taskwiserebirth;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.taskwiserebirth.task.Task;
import com.example.taskwiserebirth.utils.SystemUIHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private Fragment activeFragment;
    private Live2DFragment live2DFragment;
    private AddTaskFragment addTaskFragment;
    private SMSFragment smsFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SystemUIHelper.setSystemUIVisibility(this);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Initialize fragments
        live2DFragment = new Live2DFragment();
        addTaskFragment = new AddTaskFragment();
        smsFragment = new SMSFragment();
        settingsFragment = new SettingsFragment();

        fragmentManager = getSupportFragmentManager();

        // Add fragments to the fragment manager
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frame_layout, live2DFragment, "LIVE2D_FRAGMENT").hide(live2DFragment);
        fragmentTransaction.add(R.id.frame_layout, addTaskFragment, "ADD_TASK_FRAGMENT").hide(addTaskFragment);
        fragmentTransaction.add(R.id.frame_layout, smsFragment, "SMS_FRAGMENT").hide(smsFragment);
        fragmentTransaction.add(R.id.frame_layout, settingsFragment, "SETTINGS_FRAGMENT").hide(settingsFragment);
        fragmentTransaction.show(live2DFragment).commit();

        activeFragment = live2DFragment;

        setupBottomNavigationListener();
    }

    private void setupBottomNavigationListener() {
        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();

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

    public void showTaskDetailFragment(Task task) {
        TaskDetailFragment taskDetailFragment = new TaskDetailFragment(task);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frame_layout, taskDetailFragment, "TASK_DETAIL_FRAGMENT")
                .addToBackStack(null) // Add the transaction to the back stack
                .commit();
    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Check if there's already a fragment attached, and remove it if necessary
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);
        if (currentFragment != null && currentFragment.isAdded()) {
            fragmentTransaction.remove(currentFragment);
        }

        fragmentTransaction.replace(R.id.frame_layout, fragment);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();
    }

    /**
     * Toggles the visibility of the navigation bar.
     *
     * @param visible        true to make the navigation bar visible when scrolling up, false to hide it
     * @param isScrollToggle true if the toggle action involves scrolling, false otherwise
     */
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            SystemUIHelper.setSystemUIVisibility(this);
        }
    }
}
