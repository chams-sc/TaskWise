package com.example.taskwiserebirth.conversational;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.taskwiserebirth.MainActivity;
import com.example.taskwiserebirth.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechRecognition {

    private SpeechRecognizer speechRecognizer;
    private Context context;
    private final CardView cardViewSpeech;
    private final FloatingActionButton speakBtn;
    private SpeechRecognitionListener listener;
    private boolean isListening = false;
    private MainActivity mainActivity; // Reference to MainActivity

    public interface SpeechRecognitionListener {
        void onSpeechRecognized(String recognizedSpeech);
        void onPartialSpeechRecognized(String partialSpeech);
    }

    public SpeechRecognition(Context context, FloatingActionButton speakBtn, CardView cardViewSpeech, SpeechRecognitionListener listener, MainActivity mainActivity) {
        this.context = context;
        this.speakBtn = speakBtn;
        this.cardViewSpeech = cardViewSpeech;
        this.listener = listener;
        this.mainActivity = mainActivity;
    }

    public boolean isListening() {
        return isListening;
    }

    public void startSpeechRecognition() {
        mainActivity.pausePorcupineService(); // Pause Porcupine service
        initializeSpeechRecognizer();
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
                        SpeechSynthesis.synthesizeSpeechAsync("I'm sorry, I didn't catch that can you try that again?");
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        Toast.makeText(context, "Insufficient permissions.", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(context, "An error occurred: " + error, Toast.LENGTH_SHORT).show();
                        break;
                }
                speakBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.mic_standby));
                isListening = false;
                cardViewSpeech.setVisibility(View.INVISIBLE);
                mainActivity.resumePorcupineService();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data != null && !data.isEmpty()) {
                    String recognizedSpeech = data.get(0);
                    if (listener != null) { // Null check
                        listener.onSpeechRecognized(recognizedSpeech);
                    }
                } else {
                    Toast.makeText(context, "No speech recognized", Toast.LENGTH_SHORT).show();
                }
                speakBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.mic_standby));
                mainActivity.resumePorcupineService();
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data != null && !data.isEmpty()) {
                    String partialSpeech = data.get(0);
                    if (listener != null) { // Null check
                        listener.onPartialSpeechRecognized(partialSpeech);
                    }
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizer.startListening(speechRecognizerIntent);
    }


    public void stopSpeechRecognition() {
        if (isListening) {
            isListening = false;
            speakBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.mic_standby));
            if (speechRecognizer != null) {
                speechRecognizer.stopListening();
                speechRecognizer.cancel();
                speechRecognizer.destroy();
                speechRecognizer = null;
            }
        }
        mainActivity.resumePorcupineService();
    }

    public void release() {
        if (speechRecognizer != null) {
            if (isListening) {
                speechRecognizer.stopListening();
            }
            speechRecognizer.setRecognitionListener(null);
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        listener = null;
        context = null;
    }
}
