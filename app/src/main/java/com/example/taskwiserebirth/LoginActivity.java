package com.example.taskwiserebirth;

import android.app.AlarmManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
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
import androidx.core.content.ContextCompat;

import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.database.UserDatabaseManager;
import com.example.taskwiserebirth.database.UserModel;
import com.example.taskwiserebirth.utils.PermissionUtils;
import com.example.taskwiserebirth.utils.SystemUIHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.mongodb.App;
import io.realm.mongodb.AppException;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;

public class LoginActivity extends AppCompatActivity {

    private final String TAG_MONGO = "MongoDb";
    private App app;
    private Dialog loginDialog;
    private Dialog registerDialog;
    public static final String SHARED_PREFS = "sharedPrefs";
    private static final String STATUS_KEY = "status";
    private UserDatabaseManager userDatabaseManager;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SystemUIHelper.setSystemUIVisibility(this);

        app = MongoDbRealmHelper.initializeRealmApp();

        checkStatus();

        Button bottomLogin = findViewById(R.id.before_button);
        bottomLogin.setOnClickListener(v -> showLoginDialog());

        PermissionUtils.requestNotificationPermission(this);

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                PermissionUtils.requestAlarmReminderOn(this);
            }
        }
    }

    private void checkStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(STATUS_KEY, false);
        if(isLoggedIn){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

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
        logout();

        loginDialog = showCustomDialog(R.layout.bottom_login);

        Button loginBtn = loginDialog.findViewById(R.id.login_button);
        EditText inputEmail = loginDialog.findViewById(R.id.email_edittext);
        EditText inputPassword = loginDialog.findViewById(R.id.password_edittext);

        TextView registerBtn = loginDialog.findViewById(R.id.press_register);

        loginBtn.setOnClickListener(v -> {

            String email = inputEmail.getText().toString();
            String password = inputPassword.getText().toString();

            if (email.isEmpty() || !isValidEmail(email)) {
                showError(inputEmail, "Invalid email");
            }
            else if (password.isEmpty()) {
                showError(inputPassword, "Password is empty.");
            }
            else {
                logInEmail(email, password);
            }
        });

        registerBtn.setOnClickListener(v -> {
            loginDialog.dismiss();
            showRegisterDialog();
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
        ForegroundColorSpan grayColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.gray));
        ForegroundColorSpan orangeColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.orange));
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

    private void logInEmail(String email, String password) {
        Credentials credentials = Credentials.emailPassword(email, password);

        app.loginAsync(credentials, result -> {
            if (result.isSuccess()) {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putBoolean(STATUS_KEY, true);
                editor.apply();

                Toast.makeText(getApplicationContext(), "Logged in successfully", Toast.LENGTH_SHORT).show();

                // Start home activity
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            } else {
                handleMongoError(result.getError());
            }
        });
    }

    // Register user
    private void registerUser(String email, String password) {
        app.getEmailPassword().registerUserAsync(email, password, result -> {
            if (result.isSuccess()) {
                authenticateUser(email, password);
                Toast.makeText(getApplicationContext(), "Successful registration!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG_MONGO, result.getError().getErrorCode().toString());
                handleMongoError(result.getError());
            }
        });
    }

    private void authenticateUser(String email, String password) {
        Credentials credentials = Credentials.emailPassword(email, password);
        app.loginAsync(credentials, result -> {
            if (result.isSuccess()) {
                user = app.currentUser();
                if (user != null) {
                    userDatabaseManager = new UserDatabaseManager(user, getApplicationContext());
                    UserModel newUserModel = new UserModel(email);
                    userDatabaseManager.insertUserData(newUserModel);
                } else {
                    Log.e(TAG_MONGO, "User object is null after authentication.");
                }
            } else {
                Log.e(TAG_MONGO, "Failed to authenticate:" + result.getError());
            }
        });
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(STATUS_KEY, false);
        editor.apply();

        // Log out the user from the Realm app
        if (app != null && app.currentUser() != null) {
            app.currentUser().logOutAsync(result -> {
                if (result.isSuccess()) {
                    // Successfully logged out
                    Log.d(TAG_MONGO, "User logged out successfully");
                } else {
                    // Handle logout failure
                    Log.e(TAG_MONGO, "Failed to log out: " + result.getError());
                }
            });
        }
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
                    Log.e(TAG_MONGO, errorMessage);
                    Toast.makeText(getApplicationContext(), "Email already exists.", Toast.LENGTH_SHORT).show();
                    break;
                case 4349:
                    Log.e(TAG_MONGO, errorMessage);
                    Toast.makeText(getApplicationContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                    break;
                case 1000:
                    Log.e(TAG_MONGO, errorMessage);
                    Toast.makeText(getApplicationContext(), "Network failed", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.e(TAG_MONGO, errorMessage);
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    break;
            }

        } else {
            Log.e(TAG_MONGO, "Unknown error occurred");
            Toast.makeText(getApplicationContext(), "Unknown error occurred", Toast.LENGTH_SHORT).show();
        }
    }

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
            loginDialog = null;
        }
        if (registerDialog != null && registerDialog.isShowing()) {
            registerDialog.dismiss();
            registerDialog = null;
        }
    }
}
