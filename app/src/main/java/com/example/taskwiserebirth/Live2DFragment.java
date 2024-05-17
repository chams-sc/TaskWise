package com.example.taskwiserebirth;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bin4rybros.demo.GLRenderer;
import com.bin4rybros.demo.LAppDelegate;
import com.example.taskwiserebirth.conversational.AIRandomSpeech;
import com.example.taskwiserebirth.conversational.HttpRequest;
import com.example.taskwiserebirth.conversational.SpeechRecognition;
import com.example.taskwiserebirth.conversational.SpeechSynthesis;
import com.example.taskwiserebirth.database.ConversationDbManager;
import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.database.TaskDatabaseManager;
import com.example.taskwiserebirth.database.UserDatabaseManager;
import com.example.taskwiserebirth.task.Task;
import com.example.taskwiserebirth.utils.CalendarUtils;
import com.example.taskwiserebirth.utils.DialogUtils;
import com.example.taskwiserebirth.utils.ValidValues;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.mongodb.App;
import io.realm.mongodb.User;

public class Live2DFragment extends Fragment implements View.OnTouchListener, SpeechRecognition.SpeechRecognitionListener {
    private GLSurfaceView glSurfaceView;
    private SpeechRecognition speechRecognition;
    private TaskDatabaseManager taskDatabaseManager;
    private ConversationDbManager conversationDbManager;
    private UserDatabaseManager userDatabaseManager;
    private User user;
    private Task finalTask;
    private String aiName;
    private TextView realTimeSpeechTextView;
    private String tempTaskName;
    private String tempDeadline;
    private Task tempTask;

    private boolean inEditTaskInteraction = false;
    private boolean confirmAddTaskWithUser = false;
    private boolean inTaskDetailInteraction = false;
    private boolean isUserDone = false;
    private final String TAG_SERVER_RESPONSE = "SERVER_RESPONSE";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live2d, container, false);

        glSurfaceView = view.findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2); // Using OpenGL ES 2.0

        GLRenderer glRenderer = new GLRenderer();
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.setOnTouchListener(this);

        App app = MongoDbRealmHelper.initializeRealmApp();
        user = app.currentUser();

        taskDatabaseManager = new TaskDatabaseManager(user, requireContext());
        conversationDbManager = new ConversationDbManager(user);
        userDatabaseManager = new UserDatabaseManager(user, requireContext());
        userDatabaseManager.getUserData(userModel -> aiName = userModel.getAiName());

        ImageButton collapseBtn = view.findViewById(R.id.fullscreen_button);
        FloatingActionButton speakBtn = view.findViewById(R.id.speakBtn);

        collapseBtn.setOnClickListener(v -> ((MainActivity) requireActivity()).toggleNavBarVisibility(false, false));

        speechRecognition = new SpeechRecognition(requireContext(), speakBtn, this);
        speakBtn.setOnClickListener(v -> {
            if (speechRecognition.isListening()) {
                speechRecognition.stopSpeechRecognition();
            } else {
                speechRecognition.startSpeechRecognition();
            }
        });

        realTimeSpeechTextView = view.findViewById(R.id.realTimeSpeechTextView);
        realTimeSpeechTextView.setOnClickListener(new View.OnClickListener() {
            private boolean isExpanded = false;

            @Override
            public void onClick(View v) {
                if (isExpanded) {
                    realTimeSpeechTextView.setMaxLines(7);
                    realTimeSpeechTextView.setEllipsize(TextUtils.TruncateAt.END);
                } else {
                    realTimeSpeechTextView.setMaxLines(Integer.MAX_VALUE);
                    realTimeSpeechTextView.setEllipsize(null);
                }
                isExpanded = !isExpanded;
            }
        });

        return view;
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
            } else {
                // check if deadline is set to the past, if true set to default "No Deadline"
                if (!CalendarUtils.isDateAccepted(deadline)) {
                    Toast.makeText(requireContext(), "Deadline cannot be in the past so it is set to default", Toast.LENGTH_SHORT).show();
                    deadline = "No deadline";
                }
            }
        }

        if (taskName.isEmpty()) {
            SpeechSynthesis.synthesizeSpeechAsync("I am unable to determine the task name");
            return;
        }

        switch(intent) {
            case "Add Task":
                addNewTask(taskName, "No deadline");
                return;
            case "Add Task With Deadline":
                addNewTask(taskName, deadline);
                return;
            case "Edit Task":
                editTaskThroughSpeech(taskName);
                return;
            case "Delete Task":
                deleteTaskThroughSpeech(taskName);
                return;
            case "Mark as Finished":
                markTaskFinished(taskName);
                return;
            case "Details of the Task":
            case "Information of the Task":
            case "I need the details of the task":
            case "I need the information of the task":
                getTaskDetail(taskName);
                return;
            case "most important tasks":
                getMostImportantTasks();
                return;
            default:
                SpeechSynthesis.synthesizeSpeechAsync("I'm sorry, I didn't quite catch that. Could you please be a bit more specific? It would really help me assist you better.");
        }
    }

    private void getMostImportantTasks() {

    }

    private void getTaskDetail(String taskName) {
        taskDatabaseManager.fetchTaskByName(tasks -> {
            if (tasks.isEmpty()) {
                SpeechSynthesis.synthesizeSpeechAsync("I'm sorry but I couldn't find your task " + taskName);
            } else {
               tempTask = tasks.get(0);
               inTaskDetailInteraction = true;
               SpeechSynthesis.synthesizeSpeechAsync("Sure, what do you need to know about your task " + taskName + "?");
            }
        }, taskName);
    }

    private void addNewTask(String taskName, String deadline) {
        taskDatabaseManager.fetchAllTasks(tasks -> {
            if (tasks.size() >= 10) {
                SpeechSynthesis.synthesizeSpeechAsync(String.format("You already have %d unfinished tasks, are you sure you want to add more?", tasks.size()));

                tempTaskName = taskName;
                tempDeadline = deadline;
                confirmAddTaskWithUser = true;
            } else {
                taskDatabaseManager.fetchUnfinishedTaskByName(tasks1 -> {
                    if (tasks1.isEmpty()) {
                        insertTaskAllowed(taskName, deadline);
                    } else {
                        SpeechSynthesis.synthesizeSpeechAsync(String.format("%s is already in your list of tasks", taskName));
                    }
                }, taskName);
            }
        }, false);
    }

    private void insertTaskAllowed(String taskName, String deadline) {
        Task task = setTaskFromSpeech(taskName, deadline);
        taskDatabaseManager.insertTask(task);
        openTaskDetailFragment(task);
        SpeechSynthesis.synthesizeSpeechAsync(AIRandomSpeech.generateTaskAdded(taskName));
    }

    private void markTaskFinished(String taskName) {
        taskDatabaseManager.fetchUnfinishedTaskByName(tasks -> {
            Task task = tasks.get(0);
            Log.d(TAG_SERVER_RESPONSE, "Task found: " + task.getTaskName());

            taskDatabaseManager.markTaskAsFinished(task);
            SpeechSynthesis.synthesizeSpeechAsync(AIRandomSpeech.generateTaskFinished(taskName));
        }, taskName);
    }

    private void deleteTaskThroughSpeech(String taskName) {
        taskDatabaseManager.fetchUnfinishedTaskByName(tasks -> {
            Task task = tasks.get(0);
            Log.d(TAG_SERVER_RESPONSE, "Task found: " + task.getTaskName());

            taskDatabaseManager.deleteTask(task);
            SpeechSynthesis.synthesizeSpeechAsync("I have successfully deleted your task " + taskName);
        }, taskName);
    }

    private void editTaskThroughSpeech(String taskName) {
        taskDatabaseManager.fetchUnfinishedTaskByName(tasks -> {
            Task task = tasks.get(0);
            Log.d(TAG_SERVER_RESPONSE, "Task found: " + task.getTaskName());

            finalTask = task;
            isUserDone = false;

            turnBasedInteraction();
            inEditTaskInteraction = true;
        }, taskName);
    }

    private void askQuestion( String question) {
        Toast.makeText(requireContext(), String.format("%s: %s", aiName, question), Toast.LENGTH_SHORT).show();
        SpeechSynthesis.synthesizeSpeechAsync(question);
    }

    private void turnBasedInteraction() {
        if (isUserDone) {
            return;
        }

        final String initialQuestion = "Sure, what do you want to edit?";
        final String followUpQuestion = "Got it. Is there anything else?";

        if (!inEditTaskInteraction) {
            askQuestion(initialQuestion);
        } else {
            askQuestion(followUpQuestion);
        }
    }

    private void processResponse(String detail, String responseText) {
        Map<String, Pattern> patternMap = new HashMap<>();
        patternMap.put("Task Name", Pattern.compile("TASK_NAME: (.+)"));
        patternMap.put("Urgency", Pattern.compile("URGENCY: (.+)"));
        patternMap.put("Importance", Pattern.compile("IMPORTANCE: (.+)"));
        patternMap.put("Deadline", Pattern.compile("DEADLINE: (.+)"));
        patternMap.put("Set Recurrence", Pattern.compile("RECURRENCE: (.+)"));
        patternMap.put("Edit Recurrence", Pattern.compile("RECURRENCE: (.+)"));
        patternMap.put("Repeat Task", Pattern.compile("RECURRENCE: (.+)")); // Assuming it's the same as "Set or Edit Recurrence"
        patternMap.put("Schedule", Pattern.compile("SCHEDULE: (.+)"));
        patternMap.put("Reminder", Pattern.compile("REMINDER: (.+)"));
        patternMap.put("Notes", Pattern.compile("NOTES: (.+)"));

        Pattern pattern = patternMap.get(detail);
        if (pattern == null) {
            if (responseText.equals("DONE")) {
                inEditTaskInteraction = false;
                isUserDone = true;
                prefilterFinalTask(finalTask);
                taskDatabaseManager.updateTask(finalTask);
                SpeechSynthesis.synthesizeSpeechAsync("I have updated your task " + finalTask.getTaskName());
                return;

                // TODO: Can cause to run turnBasedInteraction also when no intent is caught.
            } else if (responseText.equals("NOT DONE")) {
                SpeechSynthesis.synthesizeSpeechAsync("Ok! I'm listening");
                return;
            } else {        // the responseText is UNRECOGNIZED
                inEditTaskInteraction = false;
                isUserDone = true;
                prefilterFinalTask(finalTask);
                taskDatabaseManager.updateTask(finalTask);
                Log.e("TEST", finalTask.getRecurrence());
                Log.e("TEST", finalTask.getId().toString());
                SpeechSynthesis.synthesizeSpeechAsync("Sorry, I didn't get that. If you need anything else, just tell me.");
                return;
            }
        }

        Matcher matcher = pattern.matcher(responseText);
        if (matcher.find()) {
            String value = matcher.group(1);
            applyTaskDetail(detail, value);
        } else {
            Toast.makeText(requireContext(), "Couldn't determine the correct value for " + detail, Toast.LENGTH_SHORT).show();
        }
    }

    //TODO: if user edited the recurrence, tell user deadline and sched were set to default
    private void prefilterFinalTask(Task finalTask) {
        if(!finalTask.getRecurrence().equals("None")) {
            finalTask.setDeadline("No deadline");   // Recurrent tasks have no deadlines

            if(finalTask.getSchedule().equals("No schedule")) {
                finalTask.setSchedule("09:00 AM");      // default sched time
            } else {
                String schedule = finalTask.getSchedule();
                // Extracting the time part from the schedule string
                String filteredSched = schedule.substring(schedule.lastIndexOf("|") + 1).trim();
                finalTask.setSchedule(filteredSched);
            }
        }

        taskDatabaseManager.updateTask(finalTask);
    }

    private void applyTaskDetail(String detail, String value) {
        switch (detail) {
            case "Task Name":
                finalTask.setTaskName(value);
                break;
            case "Urgency":
                if (ValidValues.VALID_URGENCY_LEVELS.contains(value)) {
                    finalTask.setUrgencyLevel(value);
                } else {
                    finalTask.setUrgencyLevel("None");
                }
                break;
            case "Importance":
                if (ValidValues.VALID_IMPORTANCE_LEVELS.contains(value)) {
                    finalTask.setImportanceLevel(value);
                } else {
                    finalTask.setImportanceLevel("None");
                }
                break;
            case "Deadline":
                if (CalendarUtils.isDateAccepted(value)) {
                    finalTask.setDeadline(value);
                } else {
                    finalTask.setDeadline("No deadline");
                }
                break;
            case "Set Recurrence":
            case "Edit Recurrence":
            case "Repeat Task":
                if (CalendarUtils.isRecurrenceAccepted(value)) {
                    finalTask.setRecurrence(CalendarUtils.formatRecurrence(value));
                } else {
                    finalTask.setRecurrence("None");
                }
                break;
            case "Schedule":
                if (CalendarUtils.isDateAccepted(value)) {
                    finalTask.setSchedule(value);
                } else {
                    finalTask.setSchedule("No schedule");
                }
                break;
            case "Reminder":
                finalTask.setReminder(value.equals("True"));
                break;
            case "Notes":
                finalTask.setNotes(value);
                break;
        }
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
        newTask.setReminder(true);
        newTask.setNotes("");
        newTask.setStatus("Unfinished");

        return newTask;
    }

    @Override
    public void onSpeechRecognized(String recognizedSpeech) {
        realTimeSpeechTextView.setText(recognizedSpeech);

        if (confirmAddTaskWithUser) {
            confirmWithUser(recognizedSpeech);
        } else if (inTaskDetailInteraction) {
            final List<String> doneIntents = Arrays.asList("done", "finished", "all set", "i'm good", "thank you");
            HttpRequest.taskDetailResponse(tempTask, recognizedSpeech, aiName, new HttpRequest.HttpRequestCallback() {
                @Override
                public void onSuccess(String intent, String responseText) {
                    mainHandler.post(() -> {
                        Log.v("ResponseText", responseText);
                        Log.v("Intent", intent);

                        if(intent.equalsIgnoreCase("null")) {
                            SpeechSynthesis.synthesizeSpeechAsync("I'm sorry I didn't understand, if you need details of your task just tell me what you want to know. If you are done, you can say \"I'm done\".");
                        } else if (isDoneIntent(intent)) {
                            inTaskDetailInteraction = false;
                            SpeechSynthesis.synthesizeSpeechAsync("If you need anything else, don't hesitate to tell me!");
                        } else {
                            SpeechSynthesis.synthesizeSpeechAsync(responseText);
                        }
                    });
                }

                private boolean isDoneIntent(String intent) {
                    for (String doneIntent : doneIntents) {
                        if (doneIntent.equalsIgnoreCase(intent)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void onFailure(String errorMessage) {
                    mainHandler.post(() -> {
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                        Log.d(TAG_SERVER_RESPONSE, errorMessage);
                    });
                }
            });
        } else {
            HttpRequest.sendRequest(recognizedSpeech, aiName, user.getId(), inEditTaskInteraction, new HttpRequest.HttpRequestCallback() {
                @Override
                public void onSuccess(String intent, String responseText) {
                    Log.v("ResponseText", responseText);
                    Log.v("Intent", intent);
                    mainHandler.post(() -> {
                        if (inEditTaskInteraction) {
                            processResponse(intent, responseText);
                            turnBasedInteraction();
                        } else {
                            if (!intent.equals("null")) {
                                performIntent(intent, responseText);
                            } else {
                                Toast.makeText(requireContext(), String.format("%s: %s", aiName, responseText), Toast.LENGTH_LONG).show();
                                insertDialogue(recognizedSpeech, false);

                                SpeechSynthesis.synthesizeSpeechAsync(responseText);
                                insertDialogue(responseText, true);
                            }
                        }
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    mainHandler.post(() -> {
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                        Log.d(TAG_SERVER_RESPONSE, errorMessage);
                    });
                }
            });
        }
    }

    private void insertDialogue(String dialogue, boolean isAssistant) {
        conversationDbManager.insertDialogue(dialogue, isAssistant);
    }

    private void confirmWithUser(String recognizedSpeech) {
        if (recognizedSpeech.equalsIgnoreCase("yes")) {
            confirmAddTaskWithUser = false;
            insertTaskAllowed(tempTaskName, tempDeadline);
        } else if (recognizedSpeech.equalsIgnoreCase("no")) {
            SpeechSynthesis.synthesizeSpeechAsync("Oki");
            confirmAddTaskWithUser = false;
        } else {
            Log.d("confirmWithUser", "not recognized");
            SpeechSynthesis.synthesizeSpeechAsync("I'm sorry, I didn't understand that. Are you sure you want to add task?");
        }
    }

    private void openTaskDetailFragment(Task task) {
        TaskDetailFragment taskDetailFragment = new TaskDetailFragment(task);
        ((MainActivity) requireActivity()).replaceFragment(taskDetailFragment, true);
    }

    @Override
    public void onPartialSpeechRecognized(String partialSpeech) {
        realTimeSpeechTextView.setVisibility(View.VISIBLE);
        realTimeSpeechTextView.setText(partialSpeech);
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
    public void onDestroyView() {
        super.onDestroyView();
        if (realTimeSpeechTextView != null) {
            realTimeSpeechTextView.setOnClickListener(null);
            realTimeSpeechTextView = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LAppDelegate.getInstance().onDestroy();
        if (speechRecognition != null) {
            speechRecognition.stopSpeechRecognition();
            speechRecognition = null;
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

