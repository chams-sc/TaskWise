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

import com.example.taskwiserebirth.conversational.SpeechSynthesis;
import com.example.taskwiserebirth.database.ConversationDbManager;
import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.database.UserDatabaseManager;
import com.example.taskwiserebirth.database.UserModel;
import com.example.taskwiserebirth.utils.SystemUIHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import io.realm.mongodb.App;
import io.realm.mongodb.User;

public class SettingsFragment extends Fragment {

    private UserDatabaseManager userDatabaseManager;
    private ConversationDbManager conversationDbManager;
    private String aiName;
    private final String TAG_USER_DATABASE = "UserDatabaseManager";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        App app = MongoDbRealmHelper.initializeRealmApp();
        User user = app.currentUser();
        userDatabaseManager =  new UserDatabaseManager(user, requireContext());
        conversationDbManager = new ConversationDbManager(user);


        TextView helpTxt = view.findViewById(R.id.helpTxt);
        helpTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeechSynthesis.synthesizeSpeechAsync("Is there anything else?");
            }
        });

        userDatabaseManager.getUserData(new UserDatabaseManager.GetUserDataCallback() {
            @Override
            public void onUserDataRetrieved(UserModel userModel) {
                aiName = userModel.getAiName();
            }
        });

        TextView changeAiname = view.findViewById(R.id.AIname);
        TextView clearMemory = view.findViewById(R.id.ClearMemory);

        changeAiname.setOnClickListener(v -> showChangeAINameDialog());
        clearMemory.setOnClickListener(v -> showClearMemoryDialog());

        setUpEmailView(view);

        return view;
    }

    private void setUpEmailView (View view) {
        TextView emailTxtView = view.findViewById(R.id.emailTxtView);
        userDatabaseManager.getUserData(userModel -> emailTxtView.setText(userModel.getEmail()));
    }

    private void showChangeAINameDialog() {
        Dialog dialog = createCustomDialog(R.layout.change_ai_name);
        TextInputLayout aiNameLayout = dialog.findViewById(R.id.aiNameLayout);
        TextInputEditText aiNameEditTxt = dialog.findViewById(R.id.aiNameEditTxt);
        Button saveBtn = dialog.findViewById(R.id.saveButtonAiName);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newAiName = aiNameEditTxt.getText().toString();

                if (newAiName.isEmpty()) {
                    aiNameLayout.setError("Please put your new AI name");
                } else {
                    aiNameLayout.setError(null);
                    userDatabaseManager.changeAiName(newAiName);
                    aiName = newAiName;
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }
    private void showClearMemoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Clear Ai Memory");
        builder.setMessage("Are You Sure You want to clear the memory you've made with " + aiName + "?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                conversationDbManager.clearAIMemory(new ConversationDbManager.ClearMemoryCallback() {
                    @Override
                    public void onMemoryCleared(String successMessage) {
                        Toast.makeText(requireContext(), "AI memory has been cleared.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
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
}
