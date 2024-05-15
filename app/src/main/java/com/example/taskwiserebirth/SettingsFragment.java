package com.example.taskwiserebirth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import androidx.fragment.app.Fragment;

import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.database.UserDatabaseManager;
import com.example.taskwiserebirth.utils.SystemUIHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import io.realm.mongodb.App;
import io.realm.mongodb.User;

public class SettingsFragment extends Fragment {

    private App app;
    private User user;
    private UserDatabaseManager userDatabaseManager;
    private String TAG_DATABASE = "UserDatabaseManager";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        app = MongoDbRealmHelper.initializeRealmApp();
        user = app.currentUser();
        userDatabaseManager =  new UserDatabaseManager(requireContext(), user);

        TextView changeAiname = view.findViewById(R.id.AIname);
        TextView clearMemory = view.findViewById(R.id.ClearMemory);

        changeAiname.setOnClickListener(v -> showChangeAINameDialog());
        clearMemory.setOnClickListener(v -> showClearMemoryDialog());

        return view;
    }

    private void showChangeAINameDialog() {
        Dialog dialog = createCustomDialog(R.layout.change_ai_name);
        dialog.show();
    }
    private void showClearMemoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Clear Ai Memory");
        builder.setMessage("Are You Sure You want to clear the memory you've made?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getContext(), "Positive Button Clicked", Toast.LENGTH_SHORT).show();
                // Add code to handle positive button click
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getContext(), "Negative Button clicked", Toast.LENGTH_SHORT).show();
                // Add code to handle negative button click
            }
        });

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
    private void showChangePasswordDialog() {
        Dialog dialog = createCustomDialog(R.layout.change_password);

        TextInputLayout oldPwdLayout = dialog.findViewById(R.id.oldPasswordLayout);
        TextInputLayout newPwdLayout = dialog.findViewById(R.id.newPasswordLayout);
        TextInputLayout confirmPwdLayout = dialog.findViewById(R.id.confirmPasswordLayout);

        TextInputEditText oldPwdEditText = dialog.findViewById(R.id.oldPassword);
        TextInputEditText newPwdEditText = dialog.findViewById(R.id.newPassword);
        TextInputEditText confirmPwdEditText = dialog.findViewById(R.id.confirmPassword);

        Button saveButton = dialog.findViewById(R.id.saveButtonCPass);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                String oldPassword = oldPwdEditText.getText().toString();
//                String newPassword = newPwdEditText.getText().toString();
//                String confirmPassword = confirmPwdEditText.getText().toString();
//
//                String[] args = {""};
//
//                if (validateFields(oldPassword, newPassword, confirmPassword, oldPwdLayout, newPwdLayout, confirmPwdLayout)) {
//                    userDatabaseManager.getPassword(new UserDatabaseManager.GetPasswordCallback() {
//                        @Override
//                        public void onPasswordRetrieved(String password) {
//                            if (PasswordUtils.verifyPassword(oldPassword, password)) {
//                                if (newPassword.equals(confirmPassword)) {
//
//                                    if (newPassword.equals(oldPassword)) {
//                                        newPwdLayout.setError("New password cannot be the same to old password");
//                                        return;
//                                    }
//
//                                    UserProfile userProfile = user.getProfile();
//                                    String email = userProfile.getEmail();
//                                    String hashedOldPwd = PasswordUtils.hashPassword(oldPassword);
//                                    String hashedNewPwd = PasswordUtils.hashPassword(newPassword);
//
//                                    Functions functionManager = app.getFunctions(user);
//                                    List<Object> argsList = Arrays.asList(email, hashedNewPwd, password);
//
//                                    functionManager.callFunctionAsync("customChangePassword", argsList, String.class, result -> {
//                                        if (result.isSuccess()) {
//                                            Toast.makeText(requireContext(), "Password changed successfully.", Toast.LENGTH_SHORT).show();
//                                            dialog.dismiss();
//                                        } else {
//                                            Log.e(TAG_DATABASE, "Failed to change password: " + result.getError());
//                                        }
//                                    });
//
//                                } else {
//                                    confirmPwdLayout.setError("Password do not match");
//                                }
//                            } else {
//                                oldPwdLayout.setError("Incorrect password");
//                            }
//                        }
//                    });
//                }
            }
        });
        dialog.show();
    }
}
