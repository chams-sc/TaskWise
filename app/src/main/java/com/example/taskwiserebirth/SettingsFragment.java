package com.example.taskwiserebirth;

import android.app.AlertDialog;
import android.app.Dialog;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.taskwiserebirth.database.ConversationDbManager;
import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.database.UserDatabaseManager;
import com.example.taskwiserebirth.utils.AssistiveModeHelper;
import com.example.taskwiserebirth.utils.SharedViewModel;
import com.example.taskwiserebirth.utils.SystemUIHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import io.realm.mongodb.App;
import io.realm.mongodb.User;

public class SettingsFragment extends Fragment {

    private UserDatabaseManager userDatabaseManager;
    private ConversationDbManager conversationDbManager;
    private String aiName;
    private SharedViewModel sharedViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Observe focus mode changes
        sharedViewModel.getFocusModeLiveData().observe(getViewLifecycleOwner(), isEnabled -> {
            SwitchCompat focusModeSwitch = view.findViewById(R.id.switchBtn);
            focusModeSwitch.setChecked(isEnabled);
        });

        initializeVariables();
        setUpUIComponents(view);
        setUpListeners(view);

        return view;
    }

    private void initializeVariables() {
        App app = MongoDbRealmHelper.initializeRealmApp();
        User user = app.currentUser();
        userDatabaseManager = new UserDatabaseManager(user, requireContext());
        conversationDbManager = ((MainActivity) requireActivity()).getConversationDbManager();
    }

    private void setUpUIComponents(View view) {
        TextView emailTxtView = view.findViewById(R.id.emailTxtView);

        userDatabaseManager.getUserData(userModel -> {
            if (isAdded()) {
                aiName = userModel.getAiName();
                emailTxtView.setText(userModel.getEmail());
                sharedViewModel.setAiName(aiName); // Set initial aiName
            }
        });

        updateFocusModeUI(view);
    }

    private void setUpListeners(View view) {
        TextView changeAiname = view.findViewById(R.id.AIname);
        TextView clearMemory = view.findViewById(R.id.ClearMemory);
        TextView logout = view.findViewById(R.id.logoutTxtView);
        SwitchCompat focusModeSwitch = view.findViewById(R.id.switchBtn);

        changeAiname.setOnClickListener(v -> showChangeAINameDialog());
        clearMemory.setOnClickListener(v -> showClearMemoryDialog());
        logout.setOnClickListener(v -> logout());

        focusModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateFocusModePreference(isChecked));
    }

    private void logout() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).logout();
        }
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
                if (isAdded()) {
                    userDatabaseManager.changeAiName(newAiName);
                    aiName = newAiName;
                    sharedViewModel.setAiName(aiName);
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private void showClearMemoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Clear Ai Memory");
        builder.setMessage("Are you sure you want to clear your memories with " + aiName + "?");

        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            if (isAdded()) {
                conversationDbManager.clearAIMemory(successMessage -> {
                    Toast.makeText(requireContext(), "AI memory has been cleared.", Toast.LENGTH_SHORT).show();
                    sharedViewModel.setClearMemory(true);
                    // Notify SMSFragment about the memory clearing
                    if (getActivity() instanceof MainActivity) {
                        SMSFragment smsFragment = (SMSFragment) getActivity().getSupportFragmentManager().findFragmentByTag("SMS_FRAGMENT");
                        if (smsFragment != null) {
                            smsFragment.onMemoryCleared();
                        }
                    }
                });
            }
        });

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

    private void updateFocusModePreference(boolean isChecked) {
        AssistiveModeHelper.setAssistiveMode(requireContext(), isChecked);
    }

    private void updateFocusModeUI(View view) {
        SwitchCompat focusModeSwitch = view.findViewById(R.id.switchBtn);
        focusModeSwitch.setChecked(AssistiveModeHelper.isAssistiveModeEnabled(requireContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            updateFocusModeUI(getView());
        }
    }
}
