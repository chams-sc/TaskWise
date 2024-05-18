package com.example.taskwiserebirth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.taskwiserebirth.database.ConversationDbManager;
import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.database.UserDatabaseManager;
import com.example.taskwiserebirth.utils.SystemUIHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import io.realm.mongodb.App;
import io.realm.mongodb.User;

public class SettingsFragment extends Fragment {

    private UserDatabaseManager userDatabaseManager;
    private ConversationDbManager conversationDbManager;
    private String aiName;
    private SharedPreferences sharedPreferences;
    public static final String SHARED_PREFS = "sharedPrefs";
    private static final String STATUS_KEY = "focus_mode";
    private final String TAG_USER_DATABASE = "UserDatabaseManager";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initializeVariables();
        setUpUIComponents(view);
        setUpListeners(view);

        return view;
    }

    private void initializeVariables() {
        App app = MongoDbRealmHelper.initializeRealmApp();
        User user = app.currentUser();
        userDatabaseManager = new UserDatabaseManager(user, requireContext());
        conversationDbManager = new ConversationDbManager(user);
        sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
    }

    private void setUpUIComponents(View view) {
        SwitchCompat focusModeSwitch = view.findViewById(R.id.switchBtn);
        TextView emailTxtView = view.findViewById(R.id.emailTxtView);

        userDatabaseManager.getUserData(userModel -> {
            aiName = userModel.getAiName();
            emailTxtView.setText(userModel.getEmail());
        });

        boolean currentFocusModeValue = sharedPreferences.getBoolean(STATUS_KEY, false);
        focusModeSwitch.setChecked(currentFocusModeValue);
    }

    private void setUpListeners(View view) {
        TextView changeAiname = view.findViewById(R.id.AIname);
        TextView clearMemory = view.findViewById(R.id.ClearMemory);
        SwitchCompat focusModeSwitch = view.findViewById(R.id.switchBtn);

        changeAiname.setOnClickListener(v -> showChangeAINameDialog());
        clearMemory.setOnClickListener(v -> showClearMemoryDialog());

        focusModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateFocusModePreference(isChecked));
    }

    private void updateFocusModePreference(boolean isChecked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(STATUS_KEY, isChecked);
        editor.apply();
    }

    private void showChangeAINameDialog() {
        Dialog dialog = createCustomDialog(R.layout.change_ai_name);
        TextInputLayout aiNameLayout = dialog.findViewById(R.id.aiNameLayout);
        TextInputEditText aiNameEditTxt = dialog.findViewById(R.id.aiNameEditTxt);
        Button saveBtn = dialog.findViewById(R.id.saveButtonAiName);

        saveBtn.setOnClickListener(v -> {
            String newAiName = aiNameEditTxt.getText().toString();

            if (newAiName.isEmpty()) {
                aiNameLayout.setError("Please put your new AI name");
            } else {
                aiNameLayout.setError(null);
                userDatabaseManager.changeAiName(newAiName);
                aiName = newAiName;
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showClearMemoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Clear Ai Memory");
        builder.setMessage("Are you sure you want to clear your memories with " + aiName + "?");

        builder.setPositiveButton("Yes", (dialogInterface, i) -> conversationDbManager.clearAIMemory(successMessage -> Toast.makeText(requireContext(), "AI memory has been cleared.", Toast.LENGTH_SHORT).show()));

        builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private Dialog createCustomDialog(int layoutResId) {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layoutResId);

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        SystemUIHelper.setFlagsOnThePeekView();
        dialog.setOnDismissListener(dialogInterface -> SystemUIHelper.setSystemUIVisibility((AppCompatActivity) requireActivity()));

        return dialog;
    }


    private boolean validateFields(String oldPassword, String newPassword, String confirmPassword,
                                   TextInputLayout oldPwdLayout, TextInputLayout newPwdLayout, TextInputLayout confirmPwdLayout) {
        if (oldPassword.isEmpty()) {
            oldPwdLayout.setError("Old password is required");
            return false;
        } else {
            oldPwdLayout.setError(null);
        }

        if (newPassword.isEmpty()) {
            newPwdLayout.setError("New password is required");
            return false;
        } else {
            newPwdLayout.setError(null);
        }

        if (confirmPassword.isEmpty()) {
            confirmPwdLayout.setError("Confirm password is required");
            return false;
        } else {
            confirmPwdLayout.setError(null);
        }

        return true;
    }
}
