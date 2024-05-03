package com.example.taskwiserebirth;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bin4rybros.demo.GLRenderer;
import com.bin4rybros.demo.LAppDelegate;
import com.example.taskwiserebirth.conversational.HttpRequest;
import com.example.taskwiserebirth.conversational.SpeechRecognition;
import com.example.taskwiserebirth.conversational.TTSManager;
import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.database.TaskDatabaseManager;
import com.example.taskwiserebirth.task.Task;
import com.example.taskwiserebirth.utils.DialogUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.mongodb.App;
import io.realm.mongodb.User;

public class Live2DFragment extends Fragment implements View.OnTouchListener, SpeechRecognition.SpeechRecognitionListener {
    private GLSurfaceView glSurfaceView;
    private SpeechRecognition speechRecognition;
    private TTSManager ttsManager;
    private TaskDatabaseManager taskDatabaseManager;
    private final String TAG_SERVER_RESPONSE = "SERVER_RESPONSE";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live2d, container, false);

        App app = MongoDbRealmHelper.initializeRealmApp();
        User user = app.currentUser();

        taskDatabaseManager = new TaskDatabaseManager(user, requireContext());

        ImageButton collapseBtn = view.findViewById(R.id.fullscreen_button);
        FloatingActionButton speakBtn = view.findViewById(R.id.speakBtn);

        glSurfaceView = view.findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2); // Using OpenGL ES 2.0

        GLRenderer glRenderer = new GLRenderer();
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.setOnTouchListener(this);

        collapseBtn.setOnClickListener(v -> ((MainActivity) requireActivity()).toggleNavBarVisibility(false, false));

        speechRecognition = new SpeechRecognition(requireContext(), speakBtn, this);
        speakBtn.setOnClickListener(v -> speechRecognition.startSpeechRecognition());

//        setUpTTS();

        return view;
    }

    private void setUpTTS() {
        ttsManager = new TTSManager(requireContext(), new TTSManager.TTSInitListener() {
            @Override
            public void onInitSuccess() {
                ttsManager.setLanguageAndVoice(); // Example: US English with the first available voice
            }

            @Override
            public void onInitFailure() {
                Log.e("ANDROID_TTS", "Failed to initialize TTS engine");
            }
        });
    }

    private void performIntent(String intent, String responseText){
        Pattern taskPattern = Pattern.compile("TASK_NAME: (.+?)(?:\\nDEADLINE:|$)");
        Pattern deadlinePattern = Pattern.compile("DEADLINE: (.+)");

        String taskName = "", deadline = "";

        Matcher taskMatcher = taskPattern.matcher(responseText);
        if (taskMatcher.find()) {
            taskName = taskMatcher.group(1);
        }

        Matcher deadlineMatcher = deadlinePattern.matcher(responseText);
        if (deadlineMatcher.find()) {
            deadline = deadlineMatcher.group(1);
            // check if the deadline matches the required pattern "MM-dd-yyyy | hh:mm a"
            if (!deadline.matches("\\d{2}-\\d{2}-\\d{4} \\| \\d{2}:\\d{2} [AP]M")) {
                deadline = "No deadline";
            }
        }

        Log.w(TAG_SERVER_RESPONSE, intent + " " + responseText);
        Log.w(TAG_SERVER_RESPONSE, taskName);

        if (taskName.isEmpty()) {
            Toast.makeText(requireContext(), "Task name not specified", Toast.LENGTH_LONG).show();
            return;
        }

        switch(intent) {
            case "Add Task":
                taskDatabaseManager.insertTask(setTaskFromSpeech(taskName, "No deadline"));
                return;
            case "Add Task With Deadline":
                taskDatabaseManager.insertTask(setTaskFromSpeech(taskName, deadline));
                return;
            case "Edit Task":
                editTaskThroughSpeech(taskName);
                return;
            default:
                Toast.makeText(requireContext(), "Failed to perform intent", Toast.LENGTH_LONG).show();
        }
    }

    private void editTaskThroughSpeech(String taskName) {
        taskDatabaseManager.fetchTaskByName(new TaskDatabaseManager.TaskFetchListener() {
            @Override
            public void onTasksFetched(List<Task> tasks) {
                Task task = tasks.get(0);
                Log.d(TAG_SERVER_RESPONSE, "Task found: " + task.getTaskName());
            }
        }, taskName);
    }

    private Task setTaskFromSpeech(String taskName, String deadline) {
        String urgency = DialogUtils.setAutomaticUrgency(deadline);

        Task newTask = new Task();
        newTask.setTaskName(taskName);
        newTask.setImportanceLevel("None");
        newTask.setUrgencyLevel(urgency);
        newTask.setDeadline(deadline);
        newTask.setSchedule("No schedule");
        newTask.setRecurrence("None");
        newTask.setReminder(false);
        newTask.setNotes("");
        newTask.setStatus("Unfinished");
        newTask.setDateFinished(null);

        return newTask;
    }

    @Override
    public void onSpeechRecognized(String recognizedSpeech) {
        String aiName = "Asa";
        Toast.makeText(requireContext(), "User: " + recognizedSpeech, Toast.LENGTH_SHORT).show();
        HttpRequest.sendRequest(recognizedSpeech, aiName, new HttpRequest.HttpRequestCallback() {
            @Override
            public void onSuccess(String intent, String responseText) {
                new Handler(Looper.getMainLooper()).post(() -> {

                    if (!intent.equals("null")) {
                        performIntent(intent, responseText);
                    } else {
                        Toast.makeText(requireContext(), String.format("%s: %s", aiName, responseText), Toast.LENGTH_LONG).show();
                    }
//                    ttsManager.convertTextToSpeech(responseBody);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                    Log.d(TAG_SERVER_RESPONSE, errorMessage);
                });
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        LAppDelegate.getInstance().onStart(getActivity());
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
        speechRecognition.stopSpeechRecognition();

        LAppDelegate.getInstance().onDestroy();

        if (ttsManager != null) {
            ttsManager.shutdown();
        }
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
        return true;
    }
}

