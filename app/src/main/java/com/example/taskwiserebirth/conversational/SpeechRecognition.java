package com.example.taskwiserebirth.conversational;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.taskwiserebirth.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechRecognition {

    private SpeechRecognizer speechRecognizer;
    private final Context context;
    private final FloatingActionButton speakBtn;
    private final SpeechRecognitionListener listener;
    private boolean isListening = false;

    public interface SpeechRecognitionListener {
        void onSpeechRecognized(String recognizedSpeech);
    }

    public SpeechRecognition(Context context, FloatingActionButton speakBtn, SpeechRecognitionListener listener) {
        this.context = context.getApplicationContext();
        this.speakBtn = speakBtn;
        this.listener = listener;
    }

    public boolean isListening() {
        return isListening;
    }

    public void startSpeechRecognition() {
        if (checkPermission()) {
            initializeSpeechRecognizer();
        } else {
            Toast.makeText(context, "Permission is needed for speech recognition to work.", Toast.LENGTH_SHORT).show();
        }
        isListening = true;
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                speakBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.mic_listening));
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                speakBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.mic_standby));
                isListening = false;
            }

            @Override
            public void onError(int error) {
                switch (error) {
                    case SpeechRecognizer.ERROR_NETWORK:
                        Toast.makeText(context, "Network error occurred. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        Toast.makeText(context, "I'm sorry, I didn't catch that can you try that again?", Toast.LENGTH_SHORT).show();
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        Toast.makeText(context, "Insufficient permissions.", Toast.LENGTH_SHORT).show();
                        break;
                }
                speakBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.mic_standby));
                isListening = false;
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data != null && !data.isEmpty()) {
                    String recognizedSpeech = data.get(0);
                    listener.onSpeechRecognized(recognizedSpeech);
                } else {
                    Toast.makeText(context, "No speech recognized", Toast.LENGTH_SHORT).show();
                }
                speakBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.mic_standby));
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.startListening(speechRecognizerIntent);
    }


    public void stopSpeechRecognition() {
        isListening = false;
        speakBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.mic_standby));
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
            speechRecognizer.destroy();
            speechRecognizer = null;
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
