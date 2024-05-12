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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.utils.SystemUIHelper;

import io.realm.mongodb.App;
import io.realm.mongodb.User;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        App app = MongoDbRealmHelper.initializeRealmApp();
        User user = app.currentUser();

        TextView changeEmail = view.findViewById(R.id.ChangeEmail);
        TextView changePassword = view.findViewById(R.id.ChangePassword);
        TextView changeAiname = view.findViewById(R.id.AIname);
        TextView clearMemory = view.findViewById(R.id.ClearMemory);

        changeEmail.setOnClickListener(v -> showChangeEmailDialog());
        changePassword.setOnClickListener(v -> showChangePasswordDialog());
        changeAiname.setOnClickListener(v -> showChangeAINameDialog());
        clearMemory.setOnClickListener(v -> showClearMemoryDialog());

        return view;
    }

    private void showChangeEmailDialog() {
        Dialog dialog = createCustomDialog(R.layout.change_email);
        dialog.show();
    }

    private void showChangePasswordDialog() {
        Dialog dialog = createCustomDialog(R.layout.change_password);
        dialog.show();
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


}
