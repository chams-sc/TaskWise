package com.example.taskwiserebirth.conversational;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.speech.SpeechRecognizer;
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;

public class SpeechRecognition {

    private SpeechRecognizer speechRecognizer;
    private Context context;
    private ImageButton speakBtn;

    public SpeechRecognition(Context context, ImageButton speakBtn) {
        this.context = context;
        this.speakBtn = speakBtn;
    }

    public void startSpeechRecognition() {
        if (checkPermission()) {
//            initializeSpeechRecognizer();
        } else {
            // Handle permission not granted
        }
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("This app requires RECORD_AUDIO permission for speech recognition to feature to work as expected.")
                    .setTitle("Permission Required")
                    .setCancelable(false)
                    .setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()))
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                            intent.setData(uri);
                            context.startActivity(intent);

                            dialog.dismiss();
                        }
                    });
            builder.show();
            return false;
        } else {
            return true;
        }
    }

}
