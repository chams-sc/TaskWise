package com.example.taskwiserebirth;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.taskwiserebirth.database.MongoDbRealmHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppException;
import io.realm.mongodb.Credentials;

public class LoginActivity extends AppCompatActivity {

    // TODO: Rename to AccountActivity
    private final String TAG = "MongoDb";
    private App app;
    Button bottomlogin;
    private Dialog loginDialog;
    private Dialog registerDialog;
    public static final Integer RecordAudioRequestCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SystemUIHelper.setSystemUIVisibility(this);

        // First Initialization of Realm
        Realm.init(this);
        app = MongoDbRealmHelper.initializeRealmApp();

        bottomlogin = findViewById(R.id.before_button);
        bottomlogin.setOnClickListener(v -> showLoginDialog());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == RecordAudioRequestCode) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//            } else {
//                Snackbar.make(findViewById(android.R.id.content), "Permission needed for speech recognition.", Snackbar.LENGTH_LONG)
//                        .setAction("Grant", v -> {
//                            // Request permission again
//                            requestPermission();
//                        })
//                        .show();
//            }
//        }
//    }


    // Show custom dialog
    private Dialog showCustomDialog(final int layoutResId) {
        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layoutResId);

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        return dialog;
    }

    // Show login dialog
    private void showLoginDialog() {
        loginDialog = showCustomDialog(R.layout.bottom_login);

        Button loginBtn = loginDialog.findViewById(R.id.login_button);
        EditText inputEmail = loginDialog.findViewById(R.id.email_edittext);
        EditText inputPassword = loginDialog.findViewById(R.id.password_edittext);

        TextView registerBtn = loginDialog.findViewById(R.id.press_register);

        loginBtn.setOnClickListener(v -> {

            String email = inputEmail.getText().toString();
            String password = inputPassword.getText().toString();

// TODO: for testing, you can create your own account and replace email and pass here; remove later
            logInEmail("mics@gmail.com", "11111111");

            // TODO: comment back for final app
//            if (email.isEmpty() || !isValidEmail(email)) {
//                showError(inputEmail, "Invalid email");
//            }
//            else if (password.isEmpty()) {
//                showError(inputPassword, "Password is empty.");
//            }
//            else {
//                logInEmail(email, password);
//            }
        });

        registerBtn.setOnClickListener(v -> {
            loginDialog.dismiss();
            showRegisterDialog();
        });
    }

    private void logInEmail(String email, String password) {

        Credentials credentials = Credentials.emailPassword(email, password);
        app.loginAsync(credentials, result -> {
            if (result.isSuccess()) {
                Log.d(TAG, "Logged in successfully");

                // TODO: possibly remove toast message
                Toast.makeText(getApplicationContext(), "Logged in successfully", Toast.LENGTH_SHORT).show();

                // Start home activity
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            } else {
                handleMongoError(result.getError());
            }
        });
    }

    // Show error popup
    private void showError(EditText editTextObj, String message) {
        editTextObj.setError(message);
    }

    // Check email pattern
    private boolean isValidEmail(String email) {
        String regex = "^(.+)@(.+)$";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    // Show register dialog
    private void showRegisterDialog() {
        registerDialog = showCustomDialog(R.layout.bottom_register);

        Button registerBtn = registerDialog.findViewById(R.id.register_button);
        EditText inputEmail = registerDialog.findViewById(R.id.emailRegister_edittext);
        EditText inputPassword = registerDialog.findViewById(R.id.passwordRegister_edittext);
        EditText inputCPassword = registerDialog.findViewById(R.id.cpasswordRegister_edittext);

        TextView createAccountText = registerDialog.findViewById(R.id.createAccountText);
        String text = "CREATE ACCOUNT";
        SpannableString spannableString = new SpannableString(text);
        ForegroundColorSpan grayColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.gray));
        ForegroundColorSpan orangeColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.orange));
        spannableString.setSpan(grayColorSpan, 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(orangeColorSpan, 7, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        createAccountText.setText(spannableString);

        registerBtn.setOnClickListener(v -> {
            String email = inputEmail.getText().toString();
            String password = inputPassword.getText().toString();
            String cPassword = inputCPassword.getText().toString();

            if (email.isEmpty() || !isValidEmail(email)) {
                showError(inputEmail, "Please put a valid email");
            } else if (password.isEmpty() || password.length() < 8) {
                showError(inputPassword, "Password must be at least 8 characters");
            } else if (cPassword.isEmpty()) {
                showError(inputCPassword, "Please confirm password");
            } else if (!cPassword.equals(password)) {
                showError(inputCPassword, "Please re-enter your password correctly");
            } else {
                registerUser(email, password);
            }
        });
    }

    // Register user
    private void registerUser(String email, String password) {
        app.getEmailPassword().registerUserAsync(email, password, result -> {
            if (result.isSuccess()) {
                Log.d(TAG, "Registered with email successfully");
                Toast.makeText(getApplicationContext(), "Successful registration!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, result.getError().getErrorCode().toString());
                handleMongoError(result.getError());
            }
        });
    }

    private void handleMongoError(AppException exception) {
        if (exception != null) {
            int errorIntValue = exception.getErrorIntValue();
            String errorMessage = exception.getErrorMessage();

            if (errorMessage == null) {
                errorMessage = "Unknown error occurred";
            }

            // TODO: add other error codes if possible
            switch (errorIntValue) {
                case 4348:
                    Log.e(TAG, errorMessage);
                    Toast.makeText(getApplicationContext(), "Account name already exists.", Toast.LENGTH_SHORT).show();
                    break;
                case 4349:
                    Log.e(TAG, errorMessage);
                    Toast.makeText(getApplicationContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                    break;
                case 1000:
                    Log.e(TAG, errorMessage);
                    Toast.makeText(getApplicationContext(), "Network failed", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.e(TAG, errorMessage);
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    break;
            }

        } else {
            Log.e(TAG, "Unknown error occurred");
            Toast.makeText(getApplicationContext(), "Unknown error occurred", Toast.LENGTH_SHORT).show();
        }
    }
    // Prevent leaked window
    @Override
    protected void onPause() {
        super.onPause();
        dismissDialogs();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissDialogs();
    }

    private void dismissDialogs() {
        if (loginDialog != null && loginDialog.isShowing()) {
            loginDialog.dismiss();
        }
        if (registerDialog != null && registerDialog.isShowing()) {
            registerDialog.dismiss();
        }
    }
}