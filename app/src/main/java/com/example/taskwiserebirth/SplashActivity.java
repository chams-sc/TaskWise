package com.example.taskwiserebirth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taskwiserebirth.utils.SystemUIHelper;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SystemUIHelper.setSystemUIVisibility(this);


        new Handler().postDelayed(() -> {
            // Start the main activity after the splash time has elapsed
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);

            // Close the splash activity to prevent going back to it
            finish();
        }, 1000); // 1 second
    }
}