package com.example.taskwiserebirth;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.bottom_home) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.bottom_clip) {
                replaceFragment(new ClipFragment());
            } else if (itemId == R.id.bottom_sms) {
                replaceFragment(new SMSFragment());
            } else if (itemId == R.id.bottom_settings) {
                replaceFragment(new SettingsFragment());
            }
            return true;
        });

        View rootView = findViewById(android.R.id.content);

        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                toggleBottomNavigationView();
                return true; // Indicates that the touch event has been handled
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick(); // Call performClick when the touch is released
            }
            return false; // Return false to indicate that the touch event is not consumed
        });

        rootView.setOnClickListener(v -> {
            // Your click handling logic here
        });

    }

    private void toggleBottomNavigationView() {
        if (bottomNavigationView.getVisibility() == View.VISIBLE) {
            bottomNavigationView.setVisibility(View.GONE);
        } else {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}
