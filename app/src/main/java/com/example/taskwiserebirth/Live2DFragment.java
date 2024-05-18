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
import com.example.taskwiserebirth.task.TaskPriorityCalculator;
import com.example.taskwiserebirth.utils.CalendarUtils;
import com.example.taskwiserebirth.utils.DialogUtils;
import com.example.taskwiserebirth.utils.ValidValues;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpeechSynthesis.initialize(); // Reinitialize the SpeechSynthesis executor
    }

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

        // Define a set of intents that do not require a task name check
        Set<String> noTaskNameRequiredIntents = new HashSet<>(Arrays.asList(
                "most important tasks",
                "unfinished tasks",
                "finished tasks",
                "nearest deadline",
                "how many task have I created today"
        ));

        // Check if taskName is required for the given intent
        if (!noTaskNameRequiredIntents.contains(intent) && taskName.isEmpty()) {
            SpeechSynthesis.synthesizeSpeechAsync("Hmmm, I am unable to determine the task name");
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
            case "unfinished tasks":
                getUnfinishedTasks();
                return;
            case "finished tasks":
                getFinishedTasks();
                return;
            case "nearest deadline":
                getNearestDeadline();
                return;
            case "how many task have I created today":
                countCreatedTasksToday();
                return;
            default:
                SpeechSynthesis.synthesizeSpeechAsync("I'm sorry, I didn't quite catch that. Could you please be a bit more specific? It would really help me assist you better.");
        }
    }

    private void countCreatedTasksToday() {
        taskDatabaseManager.fetchAllTasks(new TaskDatabaseManager.TaskFetchListener() {
            @Override
            public void onTasksFetched(List<Task> tasks) {
                if (!tasks.isEmpty()) {
                    int tasksCreatedToday = 0;
                    String latestTaskName = "";
                    Date latestCreationDate = null;
                    Date today = new Date();

                    for (Task task : tasks) {
                        if (CalendarUtils.isSameDay(task.getCreationDate(), today)) {
                            tasksCreatedToday++;

                            // Update latest task if its creation date is more recent
                            if (latestCreationDate == null || task.getCreationDate().after(latestCreationDate)) {
                                latestCreationDate = task.getCreationDate();
                                latestTaskName = task.getTaskName();
                            }
                        }
                    }

                    String speechResponse = "You created " + tasksCreatedToday + " tasks today";
                    if (tasksCreatedToday > 0) {
                        speechResponse += " with " + latestTaskName + " as the latest.";
                    }
                    SpeechSynthesis.synthesizeSpeechAsync(speechResponse);
                } else {
                    SpeechSynthesis.synthesizeSpeechAsync("You currently have no tasks");
                }
            }
        });
    }

    private void getNearestDeadline() {
        taskDatabaseManager.fetchTasksWithStatus(tasks -> {
            if (tasks.isEmpty()) {
                SpeechSynthesis.synthesizeSpeechAsync("You currently have no tasks");
            } else {
                List<Task> tasksWithDeadlines = new ArrayList<>();
                Map<Task, Date> taskDeadlineMap = new HashMap<>();

                for (Task task : tasks) {
                    Date deadlineDate = CalendarUtils.parseDeadline(task.getDeadline());
                    if (deadlineDate != null) {
                        tasksWithDeadlines.add(task);
                        taskDeadlineMap.put(task, deadlineDate);
                    }
                }

                if (tasksWithDeadlines.isEmpty()) {
                    SpeechSynthesis.synthesizeSpeechAsync("Currently, all your tasks have no deadlines.");
                    return;
                }

                // Sort tasks by their parsed deadline date using Collections.sort
                Collections.sort(tasksWithDeadlines, new Comparator<Task>() {
                    @Override
                    public int compare(Task t1, Task t2) {
                        return taskDeadlineMap.get(t1).compareTo(taskDeadlineMap.get(t2));
                    }
                });

                // Get the task with the nearest deadline
                Task nearestDeadlineTask = tasksWithDeadlines.get(0);
                Date nearestDeadlineDate = taskDeadlineMap.get(nearestDeadlineTask);
                String formattedDeadline = CalendarUtils.formatCustomDeadline(nearestDeadlineDate);

                String response = "Your task with the nearest deadline is " + nearestDeadlineTask.getTaskName() +
                        " due on " + formattedDeadline + ".";
                SpeechSynthesis.synthesizeSpeechAsync(response);
            }
        }, false);
    }

    private void getFinishedTasks() {
        taskDatabaseManager.fetchTasksWithStatus(tasks -> {
            if (tasks.isEmpty()) {
                SpeechSynthesis.synthesizeSpeechAsync("Hmmm, you were not able to finish any task today. But keep going!");
            } else {
                int finishedTaskCount = tasks.size();

                String response = "You were able to finish a total of " + finishedTaskCount + " today. Great job!";

                SpeechSynthesis.synthesizeSpeechAsync(response);
            }
        }, true);
    }

    private void getUnfinishedTasks() {
        taskDatabaseManager.fetchTasksWithStatus(tasks -> {
            if (tasks.isEmpty()) {
                SpeechSynthesis.synthesizeSpeechAsync("You currently have no tasks at the moment. Good for you.");
            } else {
                int unfinishedTaskCount = tasks.size();

                List<Task> sortedTasks = TaskPriorityCalculator.sortTasksByPriority(tasks, new Date());
                Task mostImportantTask = sortedTasks.get(0);

                String response = "You have " + unfinishedTaskCount + " unfinished tasks. "
                        + "With " + mostImportantTask.getTaskName() + "as your most important task.";

                SpeechSynthesis.synthesizeSpeechAsync(response);
            }
        }, false);
    }

    private void getMostImportantTasks() {
        taskDatabaseManager.fetchTasksWithStatus(tasks -> {
            List<Task> sortedTasks = TaskPriorityCalculator.sortTasksByPriority(tasks, new Date());

            // Get the top 3 tasks
            int topTaskCount = Math.min(3, sortedTasks.size());
            StringBuilder topTasksStringBuilder = new StringBuilder();

            for (int i = 0; i < topTaskCount; i++) {
                Task task = sortedTasks.get(i);
                if (i > 0) {
                    if (i == topTaskCount - 1) {
                        topTasksStringBuilder.append(" and ");
                    } else {
                        topTasksStringBuilder.append(", ");
                    }
                }
                topTasksStringBuilder.append(task.getTaskName());
            }

            String topTasksString = topTasksStringBuilder.toString();

            String response;
            if (topTaskCount == 1) {
                response = "Your current most important task is: " + topTasksString;
            } else {
                response = "Your current top " + topTaskCount + " most important tasks are: " + topTasksString;
            }

            SpeechSynthesis.synthesizeSpeechAsync(response);
        }, false);
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
        taskDatabaseManager.fetchTasksWithStatus(tasks -> {
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
            if (tasks.isEmpty()) {
                SpeechSynthesis.synthesizeSpeechAsync("I'm sorry but there's no such task as " + taskName);
                return;
            }

            Task task = tasks.get(0);
            Log.d(TAG_SERVER_RESPONSE, "Task found: " + task.getTaskName());

            taskDatabaseManager.markTaskAsFinished(task);
            SpeechSynthesis.synthesizeSpeechAsync(AIRandomSpeech.generateTaskFinished(taskName));
        }, taskName);
    }

    private void deleteTaskThroughSpeech(String taskName) {
        taskDatabaseManager.fetchUnfinishedTaskByName(tasks -> {
            if (tasks.isEmpty()) {
                SpeechSynthesis.synthesizeSpeechAsync("I'm sorry but there's no such task as " + taskName);
                return;
            }

            Task task = tasks.get(0);
            Log.d(TAG_SERVER_RESPONSE, "Task found: " + task.getTaskName());

            taskDatabaseManager.deleteTask(task);
            SpeechSynthesis.synthesizeSpeechAsync("I have successfully deleted your task " + taskName);
        }, taskName);
    }

    private void editTaskThroughSpeech(String taskName) {
        taskDatabaseManager.fetchTaskByName(tasks -> {
            if (tasks.isEmpty()) {
                SpeechSynthesis.synthesizeSpeechAsync("I'm sorry but I couldn't find your task " + taskName);
                return;
            }

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

                updateTaskThroughSpeech(false);
                return;
                // TODO: Can cause to run turnBasedInteraction also when no intent is caught.
            } else if (responseText.equals("NOT DONE")) {
                SpeechSynthesis.synthesizeSpeechAsync("Ok! I'm listening");
                return;
            } else {        // the responseText is UNRECOGNIZED
                updateTaskThroughSpeech(true);
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

    private void updateTaskThroughSpeech (boolean withError) {
        inEditTaskInteraction = false;
        isUserDone = true;
        taskDatabaseManager.updateTask(finalTask);

        if (withError) {
            SpeechSynthesis.synthesizeSpeechAsync("Sorry, I didn't get that but I have recorded your task. If you need anything else, just tell me.");
        } else {
            SpeechSynthesis.synthesizeSpeechAsync(AIRandomSpeech.generateTaskUpdated(finalTask.getTaskName()));
        }

        openTaskDetailFragment(finalTask);
    }

    private void prefilterFinalTask() {
        if(!finalTask.getRecurrence().equals("None")) {
            finalTask.setDeadline("No deadline");   // Recurrent tasks have no deadlines

            if(!finalTask.getSchedule().equals("No schedule")) {
                String schedule = finalTask.getSchedule();
                // Extracting the time part from the schedule string
                String filteredSched = schedule.substring(schedule.lastIndexOf("|") + 1).trim();
                finalTask.setSchedule(filteredSched);
            } else {
                finalTask.setSchedule("09:00 AM");
            }
        }
    }

    private void prefilterWhenDeadline()  {
        if (!finalTask.getRecurrence().equals("None")){
            finalTask.setRecurrence("None");
            finalTask.setSchedule("No schedule");
        }
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
                prefilterWhenDeadline();
                break;
            case "Set Recurrence":
            case "Edit Recurrence":
            case "Repeat Task":
                if ("Daily".equals(value)) {
                    finalTask.setRecurrence(value);
                } else if (CalendarUtils.isRecurrenceAccepted(value)) {
                    finalTask.setRecurrence(CalendarUtils.formatRecurrence(value));
                } else {
                    finalTask.setRecurrence("None");
                }
                prefilterFinalTask();
                Log.v("TEST", "Recurrence: " + finalTask.getRecurrence());
                break;
            case "Schedule":
                if (CalendarUtils.isDateAccepted(value)) {
                    finalTask.setSchedule(value);
                } else {
                    finalTask.setSchedule("No schedule");
                }
                prefilterFinalTask();
                Log.v("TEST", "Schedule: " + finalTask.getSchedule());
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
            handleTaskDetailInteraction(recognizedSpeech);
        } else {
            handleRegularInteraction(recognizedSpeech);
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
            SpeechSynthesis.synthesizeSpeechAsync("I'm sorry, I didn't understand that. Are you sure you want to add task?");
        }
    }

    private void handleTaskDetailInteraction(String recognizedSpeech) {
        final List<String> doneIntents = Arrays.asList("done", "finished", "all set", "i'm good", "thank you");
        HttpRequest.taskDetailResponse(tempTask, recognizedSpeech, aiName, new HttpRequest.HttpRequestCallback() {
            @Override
            public void onSuccess(String intent, String responseText) {
                mainHandler.post(() -> {
                    if (intent.equalsIgnoreCase("null")) {
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
    }

    private void handleRegularInteraction(String recognizedSpeech) {
        HttpRequest.sendRequest(recognizedSpeech, aiName, user.getId(), inEditTaskInteraction, new HttpRequest.HttpRequestCallback() {
            @Override
            public void onSuccess(String intent, String responseText) {
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
            speechRecognition.release(); // Release resources
            speechRecognition = null; // Nullify the SpeechRecognition reference
        }
        SpeechSynthesis.shutdown();
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

