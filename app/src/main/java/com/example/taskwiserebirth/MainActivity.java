package com.example.taskwiserebirth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the main activity after the splash time has elapsed
                Intent intent = new Intent(MainActivity.this, Login_Activity.class);
                startActivity(intent);

                // Close the splash activity to prevent going back to it
                finish();
            }
        }, 2000); // 2 seconds
    }
}