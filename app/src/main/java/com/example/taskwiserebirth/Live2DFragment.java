package com.example.taskwiserebirth;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.bin4rybros.demo.GLRenderer;
import com.bin4rybros.demo.LAppDelegate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class Live2DFragment extends Fragment implements View.OnTouchListener {
    private GLSurfaceView glSurfaceView;
    public static final Integer RecordAudioRequestCode = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live2d, container, false);

        ImageButton collapseBtn = view.findViewById(R.id.fullscreen_button);
        FloatingActionButton speakBtn = view.findViewById(R.id.speakBtn);

        glSurfaceView = view.findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2); // Using OpenGL ES 2.0

        GLRenderer glRenderer = new GLRenderer();
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.setOnTouchListener(this);

        collapseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity) requireActivity()).toggleNavBarVisibility(false, false);
            }
        });

        speakBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        return view;
    }

    private void initializeSpeechRecognizer() {
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // This method is called when the speech recognizer is ready for speech input
                // You can leave this empty if you don't need to perform any specific actions here
            }

            @Override
            public void onBeginningOfSpeech() {
                // Implement onBeginningOfSpeech method
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Implement onRmsChanged method
            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
                // Implement onEndOfSpeech method
            }

            @Override
            public void onError(int error) {
                // Implement onError method
            }

            @Override
            public void onResults(Bundle results) {
                // Implement onResults method
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Implement onPartialResults method
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Implement onEvent method
            }
        });

        // Set the intent for speech recognition
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Live2DFragment", "onStart() called");
        LAppDelegate.getInstance().onStart(getActivity());

        // Debug log to check if LAppDelegate instance is null
        Log.d("Live2DFragment", "LAppDelegate instance: " + (LAppDelegate.getInstance() != null ? "not null" : "null"));

        // Debug log to check if LAppView object is null
        Log.d("Live2DFragment", "LAppView object: " + (LAppDelegate.getInstance().getView() != null ? "not null" : "null"));
    }

    @Override
    public void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        glSurfaceView.onPause();
        LAppDelegate.getInstance().onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
        LAppDelegate.getInstance().onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LAppDelegate.getInstance().onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                LAppDelegate.getInstance().onTouchBegan(pointX, pointY);
                break;
            case MotionEvent.ACTION_UP:
                LAppDelegate.getInstance().onTouchEnd(pointX, pointY);
                break;
            case MotionEvent.ACTION_MOVE:
                LAppDelegate.getInstance().onTouchMoved(pointX, pointY);
                break;
        }
        return true; // Consume the touch event
    }
}

