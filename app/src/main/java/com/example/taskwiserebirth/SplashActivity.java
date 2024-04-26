package com.example.taskwiserebirth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // TODO: Rename to SplashActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SystemUIHelper.setSystemUIVisibility(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the main activity after the splash time has elapsed
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);

                // Close the splash activity to prevent going back to it
                finish();
            }
        }, 2000); // 2 seconds
    }
}