package com.example.taskwiserebirth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BeforeLogin extends AppCompatActivity {

    Button bottomlogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_before_login);

        bottomlogin = findViewById(R.id.before_button);
        bottomlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        });
    }

    // Define the method to show the login dialog outside the onCreate method
    private void showLoginDialog() {
        final Dialog loginDialog = new Dialog(this);
        loginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loginDialog.setContentView(R.layout.bottom_login);

        loginDialog.show();
        loginDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loginDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loginDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        loginDialog.getWindow().setGravity(Gravity.BOTTOM);

        Button loginButton = loginDialog.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform your login logic here
                // For example, navigate to HomeFragment
                Intent intent = new Intent(BeforeLogin.this, HomeFragment.class);
                startActivity(intent);

                // Dismiss the login dialog
                loginDialog.dismiss();
            }
        });

        TextView registerBtn = loginDialog.findViewById(R.id.press_register);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the login dialog
                loginDialog.dismiss();

                // Create and show the register dialog using the same layout
                final Dialog registerDialog = new Dialog(BeforeLogin.this);
                registerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                registerDialog.setContentView(R.layout.bottom_register); // Use the same layout as the login dialog

                registerDialog.show();
                registerDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                registerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                registerDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                registerDialog.getWindow().setGravity(Gravity.BOTTOM);
            }
        });
    }
}


