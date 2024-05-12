package com.example.taskwiserebirth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.taskwiserebirth.utils.SystemUIHelper;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        TextView changeEmail = view.findViewById(R.id.ChangeEmail);
        TextView changePassword = view.findViewById(R.id.ChangePassword);
        TextView changeAiname = view.findViewById(R.id.AIname);
        TextView clearMemory = view.findViewById(R.id.ClearMemory);

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog(getContext(), R.layout.change_email, new DialogCallback() {
                    @Override
                    public void handleDialog(Dialog dialog) {
                        handleChangeEmailDialog(dialog);
                    }
                });
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create and show the dialog
                showCustomDialog(getContext(), R.layout.change_password, new DialogCallback() {
                    @Override
                    public void handleDialog(Dialog dialog) {
                        handleChangePasswordDialog(dialog);
                    }
                });
            }
        });

        changeAiname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create and show the dialog
                showCustomDialog(getContext(), R.layout.change_ai_name, new DialogCallback() {
                    @Override
                    public void handleDialog(Dialog dialog) {
                        handleChangeAINameDialog(dialog);
                    }
                });
            }
        });

        clearMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear memory
                ClearMemory();
            }
        });

        return view;
    }

    private void ClearMemory() {
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

    private void handleChangeEmailDialog(Dialog dialog) {
        // Get references to EditText, buttons, etc. from the dialog layout
        // Set click listeners or handle user input as needed for the Change Email dialog
    }

    private void handleChangePasswordDialog(Dialog dialog) {
        // Get references to EditText, buttons, etc. from the dialog layout
        // Set click listeners or handle user input as needed for the Change Password dialog
    }

    private void handleChangeAINameDialog(Dialog dialog) {
        // Get references to EditText, buttons, etc. from the dialog layout
        // Set click listeners or handle user input as needed for the Change AI Name dialog
    }

    private void showCustomDialog(final Context context, final int layoutResId, DialogCallback callback) {
        final Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layoutResId);

        SystemUIHelper.setFlagsOnThePeekView();

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        dialog.setOnDismissListener(dialogInterface -> {
            SystemUIHelper.setSystemUIVisibility((AppCompatActivity) requireActivity());
        });

        callback.handleDialog(dialog);
        dialog.show();
    }


    interface DialogCallback {
        void handleDialog(Dialog dialog);
    }
}
