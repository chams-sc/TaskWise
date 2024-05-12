package com.example.taskwiserebirth;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.taskwiserebirth.utils.SystemUIHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        SystemUIHelper.setSystemUIVisibility(this);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        replaceFragment(new Live2DFragment(), false);

        setupBottomNavigationListener();
    }

    private void setupBottomNavigationListener() {
        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);

            if (itemId == R.id.bottom_home && !(currentFragment instanceof Live2DFragment)) {
                replaceFragment(new Live2DFragment(), false);
            } else if (itemId == R.id.bottom_clip && !(currentFragment instanceof AddTaskFragment)) {
                replaceFragment(new AddTaskFragment(), false);
            } else if (itemId == R.id.bottom_sms && !(currentFragment instanceof SMSFragment)) {
                replaceFragment(new SMSFragment(), false);
            } else if (itemId == R.id.bottom_settings && !(currentFragment instanceof SettingsFragment)) {
                replaceFragment(new SettingsFragment(), false);
            }

            return true;
        });
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

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            SystemUIHelper.setSystemUIVisibility(this);
//        } else {
//            SystemUIHelper.setFlagsOnThePeekView();
//        }
//    }

}
