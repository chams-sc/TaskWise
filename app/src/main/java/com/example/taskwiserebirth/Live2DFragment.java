package com.example.taskwiserebirth;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bin4rybros.demo.GLRenderer;
import com.bin4rybros.demo.LAppDefine;
import com.bin4rybros.demo.LAppDelegate;
import com.bin4rybros.demo.LAppLive2DManager;
import com.bin4rybros.demo.LAppModel;
import com.example.taskwiserebirth.conversational.AIRandomSpeech;
import com.example.taskwiserebirth.conversational.HttpRequest;
import com.example.taskwiserebirth.conversational.SpeechRecognition;
import com.example.taskwiserebirth.conversational.SpeechSynthesis;
import com.example.taskwiserebirth.database.ConversationDbManager;
import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.database.TaskDatabaseManager;
import com.example.taskwiserebirth.task.TaskModel;
import com.example.taskwiserebirth.task.TaskPriorityCalculator;
import com.example.taskwiserebirth.utils.AssistiveModeHelper;
import com.example.taskwiserebirth.utils.CalendarUtils;
import com.example.taskwiserebirth.utils.ExplicitContentFilter;
import com.example.taskwiserebirth.utils.PermissionUtils;
import com.example.taskwiserebirth.utils.SharedViewModel;
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
import java.util.Random;
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
    private User user;
    private TaskModel tempTaskForAddEdit = new TaskModel();
    private TaskModel taskToEdit;
    private String aiName;
    private TextView realTimeSpeechTextView;
    private CardView cardViewSpeech;
    private SharedViewModel sharedViewModel;
    private ImageView collapseBtn;
    private String tempEditTaskName = "";
    private String tempNotes = "";
    private String partialSpeech = "";
    private final String defaultExpression = "default1";


    private TaskModel tempTask;
    private TaskModel taskToConfirmDeletion;
    private String chosenExpression;
    private Map<String, String[]> expressionMap;

    private boolean isFullscreen = false;
    private boolean inEditTaskInteraction = false;
    private boolean confirmingAddTask = false;
    private boolean inTaskDetailInteraction = false;
    private boolean isExpanded = false;
    private boolean addTaskAskingForTaskName = false;
    private boolean editTaskAskingForTaskName = false;
    private boolean hasRecurrenceAddTask = false;
    private boolean isRequestNameFromGrok = false;
    private boolean hasRecurrenceOnRecognizedSpeech = false;
    private boolean previousHasRecurrenceOnRecognizedSpeech = false;
    private boolean addNotesAskingForTaskName = false;
    private boolean confirmingDeleteTask = false;

    private final String TAG_SERVER_RESPONSE = "SERVER_RESPONSE";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final long FADE_OUT_DELAY = 3000; // 3 seconds delay before starting fade-out
    private static final long FADE_OUT_DURATION = 500;

    private BroadcastReceiver wakeWordReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpeechSynthesis.initialize(); // Reinitialize the SpeechSynthesis executor
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        wakeWordReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (PorcupineService.ACTION_WAKE_WORD_DETECTED.equals(intent.getAction())) {
                    handleSpeakButtonClick();
                }
            }
        };
        IntentFilter filter = new IntentFilter(PorcupineService.ACTION_WAKE_WORD_DETECTED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(wakeWordReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live2d, container, false);

        initializeGLSurfaceView(view);
        initializeManagersAndUser();
        initializeUIComponents(view);
        initializeSpeechRecognition(view);
        setupExpressions();

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        // Observe aiName changes
        sharedViewModel.getAiNameLiveData().observe(getViewLifecycleOwner(), name -> aiName = name);

        return view;
    }

    private void onAssistiveModeChanged(boolean isEnabled) {
        AssistiveModeHelper.setAssistiveMode(requireContext(), isEnabled);
        sharedViewModel.setAssistiveMode(isEnabled);
    }

    private void initializeGLSurfaceView(View view) {
        glSurfaceView = view.findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2); // Using OpenGL ES 2.0

        GLRenderer glRenderer = new GLRenderer();
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.setOnTouchListener(this);
    }

    private void initializeManagersAndUser() {
        App app = MongoDbRealmHelper.initializeRealmApp();
        user = app.currentUser();

        taskDatabaseManager = new TaskDatabaseManager(user, requireContext());
        conversationDbManager = ((MainActivity) requireActivity()).getConversationDbManager();
    }

    private void initializeUIComponents(View view) {
        collapseBtn = view.findViewById(R.id.fullscreen_button);
        collapseBtn.setOnClickListener(v -> toggleFullscreen());
        FloatingActionButton speakBtn = view.findViewById(R.id.speakBtn);
        speakBtn.setOnClickListener(v -> handleSpeakButtonClick());

        cardViewSpeech = view.findViewById(R.id.cardviewSpeech);
        realTimeSpeechTextView = view.findViewById(R.id.realTimeSpeechTextView);
        realTimeSpeechTextView.setOnClickListener(v -> toggleRealTimeSpeechTextViewExpansion());
    }

    private void toggleFullscreen() {
        isFullscreen = !isFullscreen;
        if (isFullscreen) {
            collapseBtn.setBackgroundResource(R.drawable.ic_exit_fullscreen);
        } else {
            collapseBtn.setBackgroundResource(R.drawable.fullscreen);
        }
        ((MainActivity) requireActivity()).toggleNavBarVisibility(false, false);
    }

    private void handleSpeakButtonClick() {
        if (PermissionUtils.checkRecordAudioPermissionDialog(requireContext())) {
            if (speechRecognition != null && speechRecognition.isListening()) {
                speechRecognition.stopSpeechRecognition();
                stopCurrentMotion();
                mainHandler.postDelayed(() -> setModelExpression(defaultExpression), 500);

                cardViewSpeech.setVisibility(View.INVISIBLE);

                if (!partialSpeech.isEmpty()) {
                    processRecognizedSpeech(partialSpeech);
                    partialSpeech = "";
                }
            } else if (speechRecognition != null) {
                speechRecognition.startSpeechRecognition();
                changeExpression("listening");
                startSpecificModelMotion(LAppDefine.MotionGroup.IDLE.getId(), 3);

                cardViewSpeech.setVisibility(View.VISIBLE);
                cardViewSpeech.setAlpha(1f);
                realTimeSpeechTextView.setText("Listening...");

                Runnable checkListeningAndAnimate = new Runnable() {
                    @Override
                    public void run() {
                        if (speechRecognition.isListening()) {
                            startSpecificModelMotion(LAppDefine.MotionGroup.IDLE.getId(), 3);
                            mainHandler.postDelayed(this, 2000);
                        }
                    }
                };
                mainHandler.postDelayed(checkListeningAndAnimate, 2000);
            }
        }
    }

    private void toggleRealTimeSpeechTextViewExpansion() {
        if (isExpanded) {
            realTimeSpeechTextView.setMaxLines(3);
            realTimeSpeechTextView.setEllipsize(TextUtils.TruncateAt.END);
        } else {
            realTimeSpeechTextView.setMaxLines(Integer.MAX_VALUE);
            realTimeSpeechTextView.setEllipsize(null);
        }
        isExpanded = !isExpanded;
    }

    private void initializeSpeechRecognition(View view) {
        FloatingActionButton speakBtn = view.findViewById(R.id.speakBtn);
        speechRecognition = new SpeechRecognition(requireContext(), speakBtn, cardViewSpeech,this, (MainActivity) getActivity());
    }

    public void speakUnfinishedTasks() {
        String greeting = AIRandomSpeech.generateGreeting();

        startSpecificModelMotion(LAppDefine.MotionGroup.AFFIRMATION.getId(), 0);

        mainHandler.postDelayed(() -> {
            synthesizeAssistantSpeech(greeting);

            mainHandler.postDelayed(() -> {
                startSpecificModelMotion(LAppDefine.MotionGroup.THINKING.getId(), 1);

                taskDatabaseManager.fetchTasksWithStatus(tasks -> {
                    if (!tasks.isEmpty()) {
                        List<TaskModel> sortedTasks = TaskPriorityCalculator.sortTasksByPriority(tasks, new Date());

                        double highestPriorityScore = sortedTasks.get(0).getPriorityScore();

                        List<TaskModel> highestPriorityTasks = new ArrayList<>();
                        for (TaskModel task : sortedTasks) {
                            if (task.getPriorityScore() == highestPriorityScore) {
                                highestPriorityTasks.add(task);
                            } else {
                                break;
                            }
                        }

                        String response;
                        if (highestPriorityTasks.size() > 1) {
                            StringBuilder taskNames = new StringBuilder();
                            for (int i = 0; i < highestPriorityTasks.size(); i++) {
                                if (i > 0) {
                                    if (i == highestPriorityTasks.size() - 1) {
                                        taskNames.append(" and ");
                                    } else {
                                        taskNames.append(", ");
                                    }
                                }
                                taskNames.append(highestPriorityTasks.get(i).getTaskName());
                            }
                            response = "Currently, you have " + tasks.size() + " unfinished tasks, with " + taskNames.toString() + " as your most important tasks.";
                        } else {
                            TaskModel mostImportantTask = sortedTasks.get(0);
                            response = "Currently, you have " + tasks.size() + " unfinished tasks, with " + mostImportantTask.getTaskName() + " as your most important task.";
                        }

                        mainHandler.postDelayed(() -> synthesizeAssistantSpeech(response), 3000);
                    } else {
                        synthesizeAssistantSpeech(AIRandomSpeech.generateNoTasksMessages());
                    }
                }, false);

            }, 7000);
        }, 3000);
    }

    private void stopCurrentMotion() {
        LAppLive2DManager manager = LAppLive2DManager.getInstance();
        LAppModel model = manager.getModel(0); // Assuming you want the first model, change index if needed
        if (model != null) {
            model.motionManager.stopAllMotions();
        }
    }

    private void startSpecificModelMotion(String motionGroup, int motionNumber) {
        LAppLive2DManager manager = LAppLive2DManager.getInstance();
        LAppModel model = manager.getModel(0); // Assuming you want the first model, change index if needed
        if (model != null) {
            model.startSpecificMotion(motionGroup, motionNumber);
        }
    }

    private void startRandomMotionFromGroup(String motionGroup, int priority) {
        LAppLive2DManager manager = LAppLive2DManager.getInstance();
        LAppModel model = manager.getModel(0);
        if (model != null) {
            model.startRandomMotionFromGroup(motionGroup, priority);
        }

        Log.v("STARTRANDOMMOTION", "starting random montion: " + motionGroup);
    }

    private void setupExpressions() {
        // Initialize the expression map
        expressionMap = new HashMap<>();

        // Add expressions for different states
        expressionMap.put("listening", new String[]{"listening1.exp3.json", "listening2.exp3.json"});
        expressionMap.put("smile", new String[]{"smile1", "smile2"});
        expressionMap.put("happy", new String[]{"happy1", "happy2"});
        expressionMap.put("shy", new String[]{"shy1", "shy2"});
        expressionMap.put("angry", new String[]{"angry1.exp3.json", "angry2.exp3.json"});
    }

    private void chooseRandomExpression(String state) {
        // Get the array of expressions for the given state
        String[] expressions = expressionMap.get(state);
        if (expressions != null) {
            // Randomly choose an expression
            Random random = new Random();
            int index = random.nextInt(expressions.length);
            chosenExpression = expressions[index];
        } else {
            // Handle the case where the state does not exist
            chosenExpression = "default.exp3.json"; // Default expression
        }
    }

    public void changeExpression(String state) {
        chooseRandomExpression(state);
        setModelExpression(chosenExpression);
    }

    private void setModelExpression(String expressionID) {
        LAppLive2DManager manager = LAppLive2DManager.getInstance();
        LAppModel model = manager.getModel(0); // Assuming you want the first model, change index if needed
        if (model != null) {
            model.setSpecificExpression(expressionID);
        }
    }

    // if may recurrence dapat walang deadline and yung schedule automatically maseset to 9:00 am or extract time part from sched
    private void prefilterWhenRecurrence() {
        if(!tempTaskForAddEdit.getRecurrence().equals("None") && !tempTaskForAddEdit.getRecurrence().equalsIgnoreCase("unspecified")) {
            tempTaskForAddEdit.setDeadline("No deadline");   // Recurrent tasks have no deadlines

            hasRecurrenceAddTask = true;

            if(!tempTaskForAddEdit.getSchedule().equals("No schedule")) {
                String schedule = tempTaskForAddEdit.getSchedule();
                // Extracting the time part from the schedule string
                String filteredSched = schedule.substring(schedule.lastIndexOf("|") + 1).trim();
                tempTaskForAddEdit.setSchedule(filteredSched);
            } else {
                tempTaskForAddEdit.setSchedule("09:00 AM");
            }
        } else {
            hasRecurrenceAddTask = false;
        }
    }

    // if deadline ang ineedit dapat recurrence ay none and schedule maseset to no schedule -> 05-24-24 | 09:00 AM
    private void prefilterWhenDeadline()  {
        if (!tempTaskForAddEdit.getRecurrence().equals("None") && !tempTaskForAddEdit.getRecurrence().equalsIgnoreCase("unspecified")) {
            tempTaskForAddEdit.setRecurrence("None");
            tempTaskForAddEdit.setSchedule("No schedule");
        }
    }

    private void applyTaskDetail(String detail, String value) {
        switch (detail) {
            case "Task Name":
                tempTaskForAddEdit.setTaskName(value);
                break;
            case "Urgency":
                if (ValidValues.VALID_URGENCY_LEVELS.contains(value)) {
                    tempTaskForAddEdit.setUrgencyLevel(value);
                } else {
                    tempTaskForAddEdit.setUrgencyLevel("None");
                }
                break;
            case "Importance":
                if (ValidValues.VALID_IMPORTANCE_LEVELS.contains(value)) {
                    tempTaskForAddEdit.setImportanceLevel(value);
                } else {
                    tempTaskForAddEdit.setImportanceLevel("None");
                }
                break;
            case "Deadline":
                if (CalendarUtils.isDateAccepted(value)) {
                    tempTaskForAddEdit.setDeadline(value);
                } else {
                    tempTaskForAddEdit.setDeadline("No deadline");
                }
                prefilterWhenDeadline();
                break;
            case "Set Recurrence":
            case "Edit Recurrence":
            case "Repeat Task":
                if ("Daily".equalsIgnoreCase(value)) {
                    tempTaskForAddEdit.setRecurrence(value);
                } else if (CalendarUtils.isRecurrenceAccepted(value)) {
                    tempTaskForAddEdit.setRecurrence(CalendarUtils.formatRecurrence(value));
                } else {
                    tempTaskForAddEdit.setRecurrence("None");
                }
                prefilterWhenRecurrence();
                break;
            case "Schedule":
                if (CalendarUtils.isDateAccepted(value)) {
                    tempTaskForAddEdit.setSchedule(value);
                } else {
                    tempTaskForAddEdit.setSchedule("No schedule");
                }
                prefilterWhenRecurrence();
                break;
            case "Reminder":
                tempTaskForAddEdit.setReminder(value.equals("True"));
                break;
            case "Notes":
                tempTaskForAddEdit.setNotes(value);
                break;
        }
    }

    private void addCompleteTask(TaskModel task) {
        // checks for no of unfinished tasks
        taskDatabaseManager.fetchTasksWithStatus(tasks -> {
            if (tasks.size() >= 10) {
                synthesizeAssistantSpeech(AIRandomSpeech.generateUnfinishedTasksMessage(tasks.size()));
                confirmingAddTask = true;
            } else {
                // checks if task name already exists
                taskDatabaseManager.fetchTaskByName(tasks1 -> {
                    if (tasks1.isEmpty()) {
                        insertCompleteTask(task);
                    } else {
                        TaskModel fetchedTask = tasks1.get(0);
                        if (fetchedTask.getStatus().equalsIgnoreCase("unfinished")) {
                            synthesizeAssistantSpeech(String.format("%s is already in your list of unfinished tasks", task.getTaskName()));
                        } else {
                            synthesizeAssistantSpeech(String.format("Since %s is already in your list of completed tasks, I will add it again as per your instructions.", task.getTaskName()));
                            taskDatabaseManager.deleteTask(fetchedTask);
                            mainHandler.postDelayed(() -> insertCompleteTask(task), 5000);
                        }
                    }
                }, task.getTaskName());
            }
        }, false);
    }

    private void insertCompleteTask(TaskModel completeTask) {
        taskDatabaseManager.insertTask(completeTask);
        if (hasRecurrenceAddTask) {
            startSpecificModelMotion(LAppDefine.MotionGroup.THINKING.getId(), 1);
            mainHandler.postDelayed(() -> synthesizeAssistantSpeech("Recurring or repeating tasks cant have deadlines, so deadline is not applicable"), 3000);
        }
        hasRecurrenceAddTask = false;

        taskDatabaseManager.fetchTasksWithStatus(tasks -> {
            if (tasks.isEmpty()) {
                synthesizeAssistantSpeech(AIRandomSpeech.generateNoTasksMessages());
                return;
            }

            List<TaskModel> sortedTasks = TaskPriorityCalculator.sortTasksByPriority(tasks, new Date());

            // Debugging: Log sorted tasks with their priority scores
            for (TaskModel task : sortedTasks) {
                Log.v("TaskPriorityInsert", "Task: " + task.getTaskName() + ", Priority Score: " + task.getPriorityScore());
            }

            // Find the priority score of the newly added task by name
            double newTaskPriorityScore = 0;
            boolean foundNewTask = false;
            for (TaskModel task : sortedTasks) {
                if (task.getTaskName().equals(completeTask.getTaskName())) {
                    newTaskPriorityScore = task.getPriorityScore();
                    completeTask.setPriorityCategory(task.getPriorityCategory());
                    foundNewTask = true;
                    break;
                }
            }

            if (!foundNewTask) {
                Log.e("TaskPriority", "Newly added task not found in sorted tasks");
            }

            // Collect tasks with the same priority score as the new task
            List<TaskModel> samePriorityTasks = new ArrayList<>();
            for (TaskModel task : sortedTasks) {
                if (task.getPriorityScore() == newTaskPriorityScore && !task.getTaskName().equals(completeTask.getTaskName())) {
                    samePriorityTasks.add(task);
                }
            }


            String prompt;
            String formattedPrompt;
            if (!samePriorityTasks.isEmpty()) {
                StringBuilder taskNames = new StringBuilder();
                for (int i = 0; i < samePriorityTasks.size(); i++) {
                    if (i > 0) {
                        if (i == samePriorityTasks.size() - 1) {
                            taskNames.append(" and ");
                        } else {
                            taskNames.append(", ");
                        }
                    }
                    taskNames.append(samePriorityTasks.get(i).getTaskName());
                }

                if (completeTask.getPriorityCategory().equals(TaskPriorityCalculator.PRIORITY_NO_SET)) {
                    prompt = "The user just asked you to add a task called %s. Tell user that you successfully added his task %s to the list. However, make sure to inform the user that he has not set a priority level for this along with his other tasks: %s.";
                    formattedPrompt = String.format(prompt, completeTask.getTaskName(), completeTask.getTaskName(), taskNames.toString());
                } else {
                    prompt = "The user just asked you to add a task called %s. Tell user that you successfully added his task %s to the list. However, make sure to inform the user that it has the same priority of %s with his other tasks: %s.";
                    formattedPrompt = String.format(prompt, completeTask.getTaskName(), completeTask.getTaskName(), completeTask.getPriorityCategory(), taskNames.toString());
                }
            } else {
                prompt = "The user just asked you to add a task called %s. Tell the user that you successfully added his task %s to the list. Be creative with your response, using encouraging words, motivational things, or fun messages to inspire the user. Although creative, make sure to keep your responses short, must be 2 sentences only.";
                formattedPrompt = String.format(prompt, completeTask.getTaskName(), completeTask.getTaskName());
            }
            getVicunaResponse(completeTask, "add_task", formattedPrompt);
        }, false);
    }

    private void performIntent(String intent, String responseText){

        Pattern taskPattern = Pattern.compile("TASK_NAME: (.+?)(?:\\nDEADLINE:|$)");

        String taskName = "";

        Set<String> validAddTaskIntents = new HashSet<>(Arrays.asList(
                "add task",
                "add task with deadline",
                "add task with recurrence",
                "add task and set the importance",
                "add task and set the urgency",
                "add task and set the reminder",
                "add task and set the schedule"
        ));

        if (validAddTaskIntents.contains(intent.toLowerCase())) {
            prefilterAddEditTask(responseText, true);
            return;
        } else {
            Matcher taskMatcher = taskPattern.matcher(responseText);
            if (taskMatcher.find()) {
                taskName = taskMatcher.group(1);
            }

            // Define a set of intents that do not require a task name check
            Set<String> noTaskNameRequiredIntents = new HashSet<>(Arrays.asList(
                    "most important tasks",
                    "how many unfinished tasks do i have",
                    "how many tasks did i finish",
                    "which of my task has the nearest deadline",
                    "how many task did i create",
                    "Details of the Task",
                    "Information of the Task",
                    "I need the details of the task",
                    "I need the information of the task",
                    "add notes to my task",
                    "add notes"
            ));

            // Check if taskName is required for the given intent
            if (!noTaskNameRequiredIntents.contains(intent) && taskName.equalsIgnoreCase("unspecified")) {
                synthesizeAssistantSpeech("I'm sorry but I am unable to determine the task name.");
                return;
            }
        }

        switch(intent) {
            case "Edit Task":
            case "Edit Task With Deadline":
            case "Edit Task With Recurrence":
            case "edit task and set the recurrence":
            case "edit task and set the importance":
            case "edit task and set the urgency":
            case "edit task and set the reminder":
            case "edit task and set the schedule":
                prefilterAddEditTask(responseText, false);
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
            case "how many unfinished tasks do i have":
                getUnfinishedTasks();
                return;
            case "how many tasks did i finish":
                getFinishedTasks();
                return;
            case "which of my task has the nearest deadline":
                getNearestDeadline();
                return;
            case "how many task did i create":
                countCreatedTasksToday();
                return;
            case "what is the deadline of my task":
                getTaskDeadline(taskName);
                return;
            case "what is the schedule of my task":
                getTaskSchedule(taskName);
                return;
            case "what is the urgency of my task":
            case "how urgent is my task":
                getTaskUrgency(taskName);
                return;
            case "how important is my task":
            case "what is the importance of my task":
                getTaskImportance(taskName);
                return;
            case "what is the recurrence of my task":
                getTaskReccurence(taskName);
                return;
            case "did i set a reminder in my task":
                getTaskReminder(taskName);
                return;
            case "what are the notes of my task":
            case "what notes did i put in my task":
            case "what did i note in my task":
                getTaskNotes(taskName);
                return;
            case "add notes to my task":
            case "add notes":
                addNotesToTask(responseText);
                return;
            default:
                synthesizeAssistantSpeech("I'm sorry, I didn't quite catch that. Could you please be a bit more specific? It would help me assist you better.");
        }
    }

    private void addNotesToTask(String responseText) {
        Pattern taskNamePattern = Pattern.compile("TASK_NAME: (.+?)(?=\\n|$)");
        Pattern notesPattern = Pattern.compile("NOTES: (.+?)(?=\\n|$)");

        String taskName;
        String notes;

        Matcher taskNameMatcher = taskNamePattern.matcher(responseText);
        if (taskNameMatcher.find()) {
            taskName = taskNameMatcher.group(1);
        } else {
            taskName = "";
        }

        Matcher notesMatcher = notesPattern.matcher(responseText);
        if (notesMatcher.find()) {
            notes = notesMatcher.group(1);
        } else {
            notes = "";
        }

        Log.v("DEBUG_addNotesToTask", "task name: " + taskName + "response: " + responseText);
        taskDatabaseManager.fetchTaskByName(tasks -> {
            if (tasks.isEmpty()) {
                synthesizeAssistantSpeech("I'm sorry but I couldn't find your task " + taskName + ". Which task were you referring to?");
                addNotesAskingForTaskName = true;
                tempNotes = notes;
            } else {
                TaskModel task = tasks.get(0);
                String taskNotes = task.getNotes();
                task.setNotes((taskNotes + "\n" + notes).trim());
                startRandomMotionFromGroup(LAppDefine.MotionGroup.AFFIRMATION.getId(), LAppDefine.Priority.FORCE.getPriority());

                taskDatabaseManager.updateTask(task, updatedTask -> {
                    synthesizeAssistantSpeech(AIRandomSpeech.generateTaskUpdated(task.getTaskName()));
                    mainHandler.postDelayed(() -> mainHandler.postDelayed(() -> openTaskDetailFragment(task), 4000), 3000);
                });
            }
        }, taskName);
    }

    private void getTaskNotes(String taskName) {
        taskDatabaseManager.fetchTaskByName(tasks -> {
            if (tasks.isEmpty()) {
                synthesizeAssistantSpeech("I'm sorry but I couldn't find your task " + taskName);
            } else {
                TaskModel task = tasks.get(0);
                if (task.getNotes().isEmpty()) {
                    synthesizeAssistantSpeech("You did not note anything for your task " + taskName);
                } else {
                    synthesizeAssistantSpeech("In your task " + taskName + "you noted: " + task.getNotes());
                }
            }
        }, taskName);
    }

    private void getTaskReminder(String taskName) {
        taskDatabaseManager.fetchTaskByName(tasks -> {
            if (tasks.isEmpty()) {
                synthesizeAssistantSpeech("I'm sorry but I couldn't find your task " + taskName);
            } else {
                TaskModel task = tasks.get(0);
                if (task.isReminder()) {
                    synthesizeAssistantSpeech("Your reminder for task " + taskName + " is currently on." );
                } else {
                    synthesizeAssistantSpeech("Your reminder for task " + taskName + " is currently off." );
                }
            }
        }, taskName);
    }

    private void getTaskReccurence(String taskName) {
        taskDatabaseManager.fetchTaskByName(tasks -> {
            if (tasks.isEmpty()) {
                synthesizeAssistantSpeech("I'm sorry but I couldn't find your task " + taskName);
            } else {
                TaskModel task = tasks.get(0);
                if (task.getRecurrence().equalsIgnoreCase("none")) {
                    synthesizeAssistantSpeech("The recurrence of your task  " + taskName + " is set to none." );
                } else if (task.getRecurrence().equalsIgnoreCase("daily") ){
                    synthesizeAssistantSpeech("The recurrence of your task" + taskName + " is set to daily.");
                } else {
                    String fullDayNames = CalendarUtils.convertRecurrenceToDayNames(task.getRecurrence());
                    synthesizeAssistantSpeech("The recurrence of your task" + taskName + " is set every " + fullDayNames);
                }
            }
        }, taskName);
    }

    private void getTaskImportance(String taskName) {
        taskDatabaseManager.fetchTaskByName(tasks -> {
            if (tasks.isEmpty()) {
                synthesizeAssistantSpeech("I'm sorry but I couldn't find your task " + taskName);
            } else {
                TaskModel task = tasks.get(0);
                if (task.getImportanceLevel().equalsIgnoreCase("none")) {
                    synthesizeAssistantSpeech("The importance of your task  " + taskName + " is set to none." );
                } else {
                    synthesizeAssistantSpeech("The importance of your task" + taskName + " is set to " + task.getImportanceLevel());
                }
            }
        }, taskName);
    }

    private void getTaskUrgency(String taskName) {
        taskDatabaseManager.fetchTaskByName(tasks -> {
            if (tasks.isEmpty()) {
                synthesizeAssistantSpeech("I'm sorry but I couldn't find your task " + taskName);
            } else {
                TaskModel task = tasks.get(0);
                if (task.getUrgencyLevel().equalsIgnoreCase("none")) {
                    synthesizeAssistantSpeech("The urgency of your task  " + taskName + " is set to none." );
                } else {
                    synthesizeAssistantSpeech("The urgency of your task" + taskName + " is set to " + task.getUrgencyLevel());
                }
            }
        }, taskName);
    }

    private void getTaskSchedule(String taskName) {
        taskDatabaseManager.fetchTaskByName(tasks -> {
            if (tasks.isEmpty()) {
                synthesizeAssistantSpeech("I'm sorry but I couldn't find your task " + taskName);
            } else {
                TaskModel task = tasks.get(0);
                if (task.getSchedule().equalsIgnoreCase("no schedule")) {
                    synthesizeAssistantSpeech("Your task " + taskName + " has no schedule." );
                } else {
                    String formattedDate = CalendarUtils.getFormattedDate(task.getSchedule());
                    String formattedTime = CalendarUtils.getFormattedTime(task.getSchedule());

                    synthesizeAssistantSpeech(String.format("You scheduled your task %s on %s at %s", taskName, formattedDate, formattedTime));
                }
            }
        }, taskName);
    }

    private void getTaskDeadline(String taskName) {
        taskDatabaseManager.fetchTaskByName(tasks -> {
            if (tasks.isEmpty()) {
                synthesizeAssistantSpeech("I'm sorry but I couldn't find your task " + taskName);
            } else {
                TaskModel task = tasks.get(0);
                if (task.getDeadline().equalsIgnoreCase("no deadline")) {
                    synthesizeAssistantSpeech("Your task " + taskName + " has no deadline." );
                } else {
                    String formattedDate = CalendarUtils.getFormattedDate(task.getDeadline());
                    String formattedTime = CalendarUtils.getFormattedTime(task.getDeadline());

                    synthesizeAssistantSpeech("Your task " + taskName + " is due on " + formattedDate + " at " + formattedTime);
                }
            }
        }, taskName);
    }


    private void countCreatedTasksToday() {
        taskDatabaseManager.fetchAllTasks(tasks -> {
            if (!tasks.isEmpty()) {
                int tasksCreatedToday = 0;
                String latestTaskName = "";
                Date latestCreationDate = null;
                Date today = new Date();

                for (TaskModel task : tasks) {
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
                synthesizeAssistantSpeech(speechResponse);
            } else {
                synthesizeAssistantSpeech(AIRandomSpeech.generateNoTasksMessages());
            }
        });
    }

    private void getNearestDeadline() {
        taskDatabaseManager.fetchTasksWithStatus(tasks -> {
            if (tasks.isEmpty()) {
                synthesizeAssistantSpeech(AIRandomSpeech.generateNoTasksMessages());
            } else {
                List<TaskModel> tasksWithDeadlines = new ArrayList<>();
                Map<TaskModel, Date> taskDeadlineMap = new HashMap<>();

                for (TaskModel task : tasks) {
                    Date deadlineDate = CalendarUtils.parseDeadline(task.getDeadline());
                    if (deadlineDate != null) {
                        tasksWithDeadlines.add(task);
                        taskDeadlineMap.put(task, deadlineDate);
                    }
                }

                if (tasksWithDeadlines.isEmpty()) {
                    synthesizeAssistantSpeech("Currently, all your tasks have no deadlines.");
                    return;
                }

                // Sort tasks by their parsed deadline date
                Collections.sort(tasksWithDeadlines, new Comparator<TaskModel>() {
                    @Override
                    public int compare(TaskModel t1, TaskModel t2) {
                        return taskDeadlineMap.get(t1).compareTo(taskDeadlineMap.get(t2));
                    }
                });
//                Collections.sort(tasksWithDeadlines, Comparator.comparing(taskDeadlineMap::get));

                // Get the task(s) with the nearest deadline
                Date nearestDeadlineDate = taskDeadlineMap.get(tasksWithDeadlines.get(0));
                List<TaskModel> nearestDeadlineTasks = new ArrayList<>();
                for (TaskModel task : tasksWithDeadlines) {
                    if (taskDeadlineMap.get(task).equals(nearestDeadlineDate)) {
                        nearestDeadlineTasks.add(task);
                    } else {
                        break;
                    }
                }

                String formattedDeadline = CalendarUtils.formatCustomDeadline(nearestDeadlineDate);
                String response;
                boolean isPastDeadline = nearestDeadlineDate.before(new Date());

                if (nearestDeadlineTasks.size() > 1) {
                    StringBuilder taskNames = new StringBuilder();
                    for (int i = 0; i < nearestDeadlineTasks.size(); i++) {
                        if (i > 0) {
                            if (i == nearestDeadlineTasks.size() - 1) {
                                taskNames.append(" and ");
                            } else {
                                taskNames.append(", ");
                            }
                        }
                        taskNames.append(nearestDeadlineTasks.get(i).getTaskName());
                    }
                    if (isPastDeadline) {
                        response = "You have multiple tasks that were due on " + formattedDeadline + ", namely " + taskNames.toString() + ". However, these tasks are already past their deadline.";
                    } else {
                        response = "You have multiple tasks due on " + formattedDeadline + ", namely " + taskNames.toString() + ".";
                    }
                } else {
                    TaskModel nearestDeadlineTask = nearestDeadlineTasks.get(0);
                    if (isPastDeadline) {
                        response = "Your task " + nearestDeadlineTask.getTaskName() + " was due on " + formattedDeadline + ". However, this task is already past its deadline.";
                    } else {
                        response = "Your task with the nearest deadline is " + nearestDeadlineTask.getTaskName() + " due on " + formattedDeadline + ".";
                    }
                }

                synthesizeAssistantSpeech(response);
            }
        }, false);
    }

    private void getFinishedTasks() {
        taskDatabaseManager.fetchTasksWithStatus(tasks -> {
            if (tasks.isEmpty()) {
                synthesizeAssistantSpeech(AIRandomSpeech.generateNoTasksCompleted());
            } else {
                int finishedTaskCount = 0;
                Date today = new Date();

                for (TaskModel task : tasks) {
                    Date dateFinished = task.getDateFinished();
                    if (dateFinished != null && CalendarUtils.isSameDay(dateFinished, today)) {
                        finishedTaskCount++;
                    }
                }

                String response;
                if (finishedTaskCount == 0) {
                    response = AIRandomSpeech.generateNoTasksCompleted();
                } else if (finishedTaskCount == 1) {
                    response = "You were able to finish a total of " + finishedTaskCount + " task today. Great job!";
                } else {
                    response = AIRandomSpeech.generateFinishedTaskCountMessage(finishedTaskCount);
                }
                synthesizeAssistantSpeech(response);
            }
        }, true);
    }

    private void getUnfinishedTasks() {
        taskDatabaseManager.fetchTasksWithStatus(tasks -> {
            if (tasks.isEmpty()) {
                synthesizeAssistantSpeech(AIRandomSpeech.generateNoTasksMessages());
            } else {
                int unfinishedTaskCount = tasks.size();

                List<TaskModel> sortedTasks = TaskPriorityCalculator.sortTasksByPriority(tasks, new Date());
                TaskModel mostImportantTask = sortedTasks.get(0);

                String task = "task";
                if (unfinishedTaskCount > 1) {
                    task = "tasks";
                }

                String response = "You have " + unfinishedTaskCount + " unfinished " + task
                        + ". With " + mostImportantTask.getTaskName() + " as your most important task.";

                synthesizeAssistantSpeech(response);
            }
        }, false);
    }

    private void getMostImportantTasks() {
        taskDatabaseManager.fetchTasksWithStatus(tasks -> {
            if (tasks.isEmpty()) {
                synthesizeAssistantSpeech(AIRandomSpeech.generateNoTasksMessages());
                return;
            }

            List<TaskModel> sortedTasks = TaskPriorityCalculator.sortTasksByPriority(tasks, new Date());

            double highestPriorityScore = sortedTasks.get(0).getPriorityScore();

            // Collect tasks with the highest priority score
            List<TaskModel> highestPriorityTasks = new ArrayList<>();
            for (TaskModel task : sortedTasks) {
                if (task.getPriorityScore() == highestPriorityScore) {
                    highestPriorityTasks.add(task);
                } else {
                    break; // As tasks are sorted, break once a lower priority task is found
                }
            }

            String response;
            if (highestPriorityTasks.size() > 1) {
                StringBuilder taskNames = new StringBuilder();
                for (int i = 0; i < highestPriorityTasks.size(); i++) {
                    if (i > 0) {
                        if (i == highestPriorityTasks.size() - 1) {
                            taskNames.append(" and ");
                        } else {
                            taskNames.append(", ");
                        }
                    }
                    taskNames.append(highestPriorityTasks.get(i).getTaskName());
                }
                response = "Your current most important tasks are: " + taskNames.toString() + " with the same priority and deadline.";
            } else {
                // If there is no tie and we want to mention the top 3 tasks
                int topTaskCount = Math.min(3, sortedTasks.size());
                StringBuilder topTasksStringBuilder = new StringBuilder();

                for (int i = 0; i < topTaskCount; i++) {
                    TaskModel task = sortedTasks.get(i);
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

                if (topTaskCount == 1) {
                    response = "Your current most important task is: " + topTasksString;
                } else {
                    response = "Your current top " + topTaskCount + " most important tasks are: " + topTasksString + " respectively.";
                }
            }

            synthesizeAssistantSpeech(response);
        }, false);
    }

    private void getTaskDetail(String taskName) {
        taskDatabaseManager.fetchTaskByName(tasks -> {
            if (taskName.equalsIgnoreCase("unspecified")) {
                synthesizeAssistantSpeech("Which task do you need information of?");
                isRequestNameFromGrok = true;
                return;
            }
            if (tasks.isEmpty()) {
                synthesizeAssistantSpeech("I'm sorry but I couldn't find your task " + taskName);
            } else {
               tempTask = tasks.get(0);
               inTaskDetailInteraction = true;
               synthesizeAssistantSpeech("Sure, what do you need to know about your task " + taskName + "?");
            }
        }, taskName);
    }


    private void markTaskFinished(String taskName) {
        taskDatabaseManager.fetchUnfinishedTaskByName(tasks -> {
            if (tasks.isEmpty()) {
                startSpecificModelMotion(LAppDefine.MotionGroup.THINKING.getId(), 0);
                mainHandler.postDelayed(() -> synthesizeAssistantSpeech(AIRandomSpeech.generateTaskNotFound(taskName)), 3000);
                return;
            }

            TaskModel task = tasks.get(0);
            Log.d(TAG_SERVER_RESPONSE, "Task found: " + task.getTaskName());

            taskDatabaseManager.markTaskAsFinished(task);
            synthesizeAssistantSpeech(AIRandomSpeech.generateTaskFinished(taskName));
        }, taskName);
    }

    private void deleteTaskThroughSpeech(String taskName) {
        taskDatabaseManager.fetchUnfinishedTaskByName(tasks -> {
            if (tasks.isEmpty()) {
                startSpecificModelMotion(LAppDefine.MotionGroup.THINKING.getId(), 0);
                mainHandler.postDelayed(() -> synthesizeAssistantSpeech(AIRandomSpeech.generateTaskNotFound(taskName)), 3000);
                return;
            }

            TaskModel task = tasks.get(0);
            taskToConfirmDeletion = task;

            startRandomMotionFromGroup(LAppDefine.MotionGroup.ASKING.getId(), LAppDefine.Priority.FORCE.getPriority());
            synthesizeAssistantSpeech("Are you sure you want to delete the task " + taskName + "? Please say yes or no.");
            confirmingDeleteTask = true;
        }, taskName);
    }

    private void confirmDeleteTaskWithUser(String recognizedSpeech) {
        if (recognizedSpeech.equalsIgnoreCase("yes")) {
            taskDatabaseManager.deleteTask(taskToConfirmDeletion);

            startRandomMotionFromGroup(LAppDefine.MotionGroup.NEGATIVE.getId(), LAppDefine.Priority.FORCE.getPriority());
            mainHandler.postDelayed(() -> synthesizeAssistantSpeech("I have successfully deleted your task " + taskToConfirmDeletion.getTaskName()), 3000);

            confirmingDeleteTask = false;
        } else if (recognizedSpeech.equalsIgnoreCase("no")) {
            synthesizeAssistantSpeech("Okay, I didn't delete the task called " + taskToConfirmDeletion.getTaskName() + ".");
            confirmingDeleteTask = false;
        } else {
            synthesizeAssistantSpeech("I'm sorry, I didn't understand that. Are you sure you want to delete the task? Please say yes or no.");
        }
    }

    private void askQuestion( String question) {
        synthesizeAssistantSpeech(question);
    }

    private void prefilterAddEditTask(String responseText, boolean isAddTask) {
        // Define patterns to extract each detail
        Pattern taskNamePattern = Pattern.compile("TASK_NAME: (.+?)(?=\\n|$)");
        Pattern newTaskNamePattern = Pattern.compile("NEW_TASK_NAME: (.+?)(?=\\n|$)");
        Pattern importancePattern = Pattern.compile("IMPORTANCE: (.+?)(?=\\n|$)");
        Pattern urgencyPattern = Pattern.compile("URGENCY: (.+?)(?=\\n|$)");
        Pattern deadlinePattern = Pattern.compile("DEADLINE: (.+?)(?=\\n|$)");
        Pattern recurrencePattern = Pattern.compile("RECURRENCE: (.+?)(?=\\n|$)");
        Pattern schedulePattern = Pattern.compile("SCHEDULE: (.+?)(?=\\n|$)");
        Pattern reminderPattern = Pattern.compile("REMINDER: (.+?)(?=\\n|$)");
        Pattern notesPattern = Pattern.compile("NOTES: (.+?)(?=\\n|$)");

        String taskName = "Unspecified";
        String newTaskName = "Unspecified";
        String importance = "Unspecified";
        String urgency = "Unspecified";
        String deadline = "Unspecified";
        String recurrence = "Unspecified";
        String schedule = "Unspecified";
        String reminder = "Unspecified";
        String notes = "Unspecified";

        Matcher taskNameMatcher = taskNamePattern.matcher(responseText);
        if (taskNameMatcher.find()) {
            taskName = taskNameMatcher.group(1);
        }

        Matcher newTaskNameMatcher = newTaskNamePattern.matcher(responseText);
        if (newTaskNameMatcher.find()) {
            newTaskName = newTaskNameMatcher.group(1);
        }

        Matcher importanceMatcher = importancePattern.matcher(responseText);
        if (importanceMatcher.find()) {
            importance = importanceMatcher.group(1);
        }

        Matcher urgencyMatcher = urgencyPattern.matcher(responseText);
        if (urgencyMatcher.find()) {
            urgency = urgencyMatcher.group(1);
        }

        Matcher deadlineMatcher = deadlinePattern.matcher(responseText);
        if (deadlineMatcher.find()) {
            deadline = deadlineMatcher.group(1);
        }

        Matcher recurrenceMatcher = recurrencePattern.matcher(responseText);
        if (recurrenceMatcher.find()) {
            recurrence = recurrenceMatcher.group(1);
        }

        Matcher scheduleMatcher = schedulePattern.matcher(responseText);
        if (scheduleMatcher.find()) {
            schedule = scheduleMatcher.group(1);
        }

        Matcher reminderMatcher = reminderPattern.matcher(responseText);
        if (reminderMatcher.find()) {
            reminder = reminderMatcher.group(1);
        }

        Matcher notesMatcher = notesPattern.matcher(responseText);
        if (notesMatcher.find()) {
            notes = notesMatcher.group(1);
        }

        if (isAddTask) {
            Log.v("isAddTask", "deadline:" + deadline);
            tempTaskForAddEdit = new TaskModel(taskName, importance, urgency, deadline, schedule, recurrence, true, notes);
            applyTaskDetail("Importance", importance);
            applyTaskDetail("Urgency", urgency);
            applyTaskDetail("Deadline", deadline);
            applyTaskDetail("Set Recurrence", recurrence);
            applyTaskDetail("Schedule", schedule);
            if ("unspecified".equalsIgnoreCase(notes)) {
                notes = "";
                applyTaskDetail("Notes", notes);
            }

            if (!"Unspecified".equalsIgnoreCase(taskName)) {
                addCompleteTask(tempTaskForAddEdit);
                tempTaskForAddEdit = new TaskModel();
            } else {
                synthesizeAssistantSpeech("You forgot to mention the name of the task, what should we call it?");
                addTaskAskingForTaskName = true;
            }
        } else {
            tempTaskForAddEdit = new TaskModel(taskName, importance, urgency, deadline, schedule, recurrence, true, notes);
            inEditTaskInteraction = true;

            if (tempEditTaskName.isEmpty()) {
                tempEditTaskName = tempTaskForAddEdit.getTaskName();
            }

            if (reminder.equalsIgnoreCase("false")) {
                tempTaskForAddEdit.setReminder(false);
            } else {
                tempTaskForAddEdit.setReminder(true);
            }

            if ("Unspecified".equalsIgnoreCase(tempEditTaskName)) {
                synthesizeAssistantSpeech("You forgot to mention the name of the task, which task was it?");
                editTaskAskingForTaskName = true;
            } else {
                editTaskThroughSpeech(tempEditTaskName, newTaskName);
            }
        }
    }

    private void getEditTaskName(String recognizedSpeech) {
        Log.v("getEditTaskName", "getEditTaskName run");
        taskDatabaseManager.fetchTaskByName(tasks -> {
            if (tasks.isEmpty()) {
                startSpecificModelMotion(LAppDefine.MotionGroup.THINKING.getId(), 0);
                mainHandler.postDelayed(() -> synthesizeAssistantSpeech(AIRandomSpeech.generateTaskNotFoundAndVerify(recognizedSpeech)), 3000);
            } else {
                editTaskAskingForTaskName = false;
                editTaskThroughSpeech(recognizedSpeech,"unspecified");
            }
        }, recognizedSpeech);
    }

    private void getNotesTaskName(String recognizedSpeech) {
        Log.v("getNotesTaskName", "getNotesTaskName run");
        taskDatabaseManager.fetchTaskByName(tasks -> {
            if (tasks.isEmpty()) {
                startSpecificModelMotion(LAppDefine.MotionGroup.THINKING.getId(), 0);
                mainHandler.postDelayed(() -> synthesizeAssistantSpeech(AIRandomSpeech.generateTaskNotFoundAndVerify(recognizedSpeech)), 3000);
            } else {
                addNotesAskingForTaskName = false;
                TaskModel task = tasks.get(0);
                String taskNotes = task.getNotes();
                task.setNotes((taskNotes + "\n" + tempNotes).trim());

                startRandomMotionFromGroup(LAppDefine.MotionGroup.AFFIRMATION.getId(), LAppDefine.Priority.FORCE.getPriority());

                taskDatabaseManager.updateTask(task, updatedTask -> {
                    synthesizeAssistantSpeech(AIRandomSpeech.generateTaskUpdated(task.getTaskName()));
                    mainHandler.postDelayed(() -> mainHandler.postDelayed(() -> openTaskDetailFragment(task), 4000), 3000);
                });
            }
        }, recognizedSpeech);
    }

    private void getAddTaskName(String recognizedSpeech) {
        Log.v("getAddTaskName", "running");
        tempTaskForAddEdit.setTaskName(recognizedSpeech);
        addCompleteTask(tempTaskForAddEdit);
        addTaskAskingForTaskName = false;
    }

    private void editTaskThroughSpeech(String taskName, String newTaskName) {
        taskDatabaseManager.fetchTaskByName(tasks -> {
            if (tasks.isEmpty()) {
                startSpecificModelMotion(LAppDefine.MotionGroup.THINKING.getId(), 0);
                mainHandler.postDelayed(() -> synthesizeAssistantSpeech(AIRandomSpeech.generateTaskNotFoundAndVerify(taskName)), 3000);
                editTaskAskingForTaskName = true;
                return;
            }

            TaskModel task = tasks.get(0);
            Log.d("editTaskThroughSpeech", "Task found: " + task.getTaskName());

            tempEditTaskName = task.getTaskName();

            taskToEdit = task;
            if (!newTaskName.equalsIgnoreCase("unspecified")) {
                taskToEdit.setTaskName(newTaskName);
                tempEditTaskName = newTaskName;
            }

            // Apply importance level if specified
            if (!tempTaskForAddEdit.getImportanceLevel().equalsIgnoreCase("unspecified")
                    && ValidValues.VALID_IMPORTANCE_LEVELS.contains(tempTaskForAddEdit.getImportanceLevel())) {
                taskToEdit.setImportanceLevel(tempTaskForAddEdit.getImportanceLevel());
            }

            // Apply urgency level if specified
            if (!tempTaskForAddEdit.getUrgencyLevel().equalsIgnoreCase("unspecified")
                    && ValidValues.VALID_URGENCY_LEVELS.contains(tempTaskForAddEdit.getUrgencyLevel())) {
                taskToEdit.setUrgencyLevel(tempTaskForAddEdit.getUrgencyLevel());
            }

            if (!tempTaskForAddEdit.getNotes().equalsIgnoreCase("unspecified")) {
                taskToEdit.setNotes(tempTaskForAddEdit.getNotes());
            }

            taskToEdit.setReminder(tempTaskForAddEdit.isReminder());

            // Handle recurrence, deadline, and schedule changes
            boolean hasRecurrenceChange = !tempTaskForAddEdit.getRecurrence().equalsIgnoreCase("unspecified");
            boolean hasDeadlineChange = !tempTaskForAddEdit.getDeadline().equalsIgnoreCase("unspecified");
            boolean hasScheduleChange = !tempTaskForAddEdit.getSchedule().equalsIgnoreCase("unspecified");

            Log.v("DEBUG_DEADLINE_CHANGE", "hasDeadlineChange: " + hasDeadlineChange);
            Log.v("DEBUG_SCHEDULE_CHANGE", "hasScheduleChange: " + hasScheduleChange);
            Log.v("DEBUG_RECURRENCE_CHANGE", "hasRecurrenceChange: " + hasRecurrenceChange);
            Log.v("DEBUG_HAS_RECURRENCE_ON_RECOGNIZED_SPEECH", "hasRecurrenceOnRecognizedSpeech: " + hasRecurrenceOnRecognizedSpeech);

            // may recurrence change at hindi equal to none
            if (hasRecurrenceChange && hasRecurrenceOnRecognizedSpeech) {
                if (!tempTaskForAddEdit.getRecurrence().equalsIgnoreCase("none")) {
                    if (hasDeadlineChange && hasScheduleChange) {
                        // set deadline to none
                        taskToEdit.setDeadline("No deadline");
                        // check schedule if in right format
                        if (CalendarUtils.isDateInCorrectFormat(tempTaskForAddEdit.getSchedule())) {
                            String schedule = tempTaskForAddEdit.getSchedule();
                            // Extracting the time part from the schedule string
                            String filteredSched = schedule.substring(schedule.lastIndexOf("|") + 1).trim();
                            Log.v("EditTaskThroughSpeech", "sched: " + filteredSched);
                            taskToEdit.setSchedule(filteredSched);
                        } else {
                            taskToEdit.setSchedule("09:00 AM");
                        }

                        if (tempTaskForAddEdit.getRecurrence().equalsIgnoreCase("daily")) {
                            taskToEdit.setRecurrence("Daily");
                        } else {
                            taskToEdit.setRecurrence(CalendarUtils.formatRecurrence(tempTaskForAddEdit.getRecurrence()));
                        }

                        startSpecificModelMotion(LAppDefine.MotionGroup.THINKING.getId(), 1);
                        mainHandler.postDelayed(() -> synthesizeAssistantSpeech("Recurring or repeating tasks cant have deadlines, so deadline is not applicable"), 3000);
                    } else if (hasDeadlineChange || hasScheduleChange) {

                        if (hasDeadlineChange || !taskToEdit.getDeadline().equalsIgnoreCase("no deadline")) {
                            startSpecificModelMotion(LAppDefine.MotionGroup.THINKING.getId(), 1);
                            mainHandler.postDelayed(() -> synthesizeAssistantSpeech("Recurring or repeating tasks cant have deadlines, so deadline is not applicable"), 3000);
                        }

                        taskToEdit.setDeadline("No deadline");
                        // if iseset to days of week or daily, check kung valid ang format ng sched, if not format it
                        if (!taskToEdit.getSchedule().equalsIgnoreCase("no schedule")) {
                            String schedule = taskToEdit.getSchedule();
                            // Extracting the time part from the schedule string
                            String filteredSched = schedule.substring(schedule.lastIndexOf("|") + 1).trim();
                            taskToEdit.setSchedule(filteredSched);
                        } else {
                            taskToEdit.setSchedule("09:00 AM");
                        }

                        if (tempTaskForAddEdit.getRecurrence().equalsIgnoreCase("daily")) {
                            Log.v("EditTaskThroughSpeech", "tempTaskForAddEdit.getRecurrence().equalsIgnoreCase(\"daily\")");
                            taskToEdit.setRecurrence("Daily");
                        } else {
                            taskToEdit.setRecurrence(CalendarUtils.formatRecurrence(tempTaskForAddEdit.getRecurrence()));
                        }

                    } else {
                        if (taskToEdit.getRecurrence().equalsIgnoreCase("none") || !tempTaskForAddEdit.getRecurrence().equalsIgnoreCase("none")) {
                            // may sched, no deadline
                            if (!taskToEdit.getSchedule().equalsIgnoreCase("No schedule") && taskToEdit.getDeadline().equalsIgnoreCase("no deadline")) {
                                Log.v("DEBUG", "!taskToEdit.getSchedule().equalsIgnoreCase(\"No schedule\")");
                                String schedule = taskToEdit.getSchedule();
                                // Extracting the time part from the schedule string
                                String filteredSched = schedule.substring(schedule.lastIndexOf("|") + 1).trim();
                                taskToEdit.setSchedule(filteredSched);
                            // may deadline, no sched
                            } else if (!taskToEdit.getDeadline().equalsIgnoreCase("no deadline") && taskToEdit.getSchedule().equalsIgnoreCase("no schedule")) {
                                Log.v("DEBUG", "!taskToEdit.getDeadline().equalsIgnoreCase(\"no deadline\") && taskToEdit.getSchedule().equalsIgnoreCase(\"no schedule\")");
                                taskToEdit.setDeadline("No deadline");
                                taskToEdit.setSchedule("09:00 AM");

                                startSpecificModelMotion(LAppDefine.MotionGroup.THINKING.getId(), 1);
                                mainHandler.postDelayed(() -> synthesizeAssistantSpeech("Recurring or repeating tasks cant have deadlines, so deadline is not applicable"), 3000);

                            } else if (!taskToEdit.getSchedule().equalsIgnoreCase("no schedule") && !taskToEdit.getDeadline().equalsIgnoreCase("no deadline")) {
                                Log.v("DEBUG", "!taskToEdit.getSchedule().equalsIgnoreCase(\"no schedule\") && !taskToEdit.getDeadline().equalsIgnoreCase(\"no deadline\")");
                                taskToEdit.setDeadline("No deadline");

                                String schedule = taskToEdit.getSchedule();
                                // Extracting the time part from the schedule string
                                String filteredSched = schedule.substring(schedule.lastIndexOf("|") + 1).trim();
                                taskToEdit.setSchedule(filteredSched);

                                startSpecificModelMotion(LAppDefine.MotionGroup.THINKING.getId(), 1);
                                mainHandler.postDelayed(() -> synthesizeAssistantSpeech("Recurring or repeating tasks cant have deadlines, so deadline is not applicable"), 3000);
                            }

                            if (tempTaskForAddEdit.getRecurrence().equalsIgnoreCase("daily")) {
                                taskToEdit.setRecurrence("Daily");
                            } else {
                                taskToEdit.setRecurrence(CalendarUtils.formatRecurrence(tempTaskForAddEdit.getRecurrence()));
                            }
                        }
                    }

                } else {
                    if (!taskToEdit.getRecurrence().equalsIgnoreCase("none")) {
                        taskToEdit.setSchedule("No schedule");
                        taskToEdit.setRecurrence("None");
                    }
                }

            } else {
                if (hasScheduleChange) {
                    if (!taskToEdit.getRecurrence().equalsIgnoreCase("none")) {
                        Log.v("DEBUG", "hasScheduleChange !taskToEdit.getRecurrence().equalsIgnoreCase(\"none\")");
                        String schedule = tempTaskForAddEdit.getSchedule();
                        // Extracting the time part from the schedule string
                        String filteredSched = schedule.substring(schedule.lastIndexOf("|") + 1).trim();
                        taskToEdit.setSchedule(filteredSched);
                    } else {
                        Log.v("DEBUG", "hasScheduleChange else running");
                        Log.v("Schedule", tempTaskForAddEdit.getSchedule());
                        // check if sched is earlier than current time
                        if (CalendarUtils.isDateAccepted(tempTaskForAddEdit.getSchedule())) {
                            if (!taskToEdit.getDeadline().equalsIgnoreCase("no deadline")) {
                                boolean newSchedIsValid = CalendarUtils.isDateAccepted(tempTaskForAddEdit.getSchedule(), taskToEdit.getDeadline());
                                if (newSchedIsValid) {
                                    taskToEdit.setSchedule(tempTaskForAddEdit.getSchedule());
                                } else {
                                    startSpecificModelMotion(LAppDefine.MotionGroup.THINKING.getId(), 1);
                                    mainHandler.postDelayed(() -> synthesizeAssistantSpeech("Your new schedule cannot be later than your deadline."), 3000);
                                }
                            } else {
                                taskToEdit.setSchedule(tempTaskForAddEdit.getSchedule());
                            }
                        } else {
                            startSpecificModelMotion(LAppDefine.MotionGroup.THINKING.getId(), 1);
                            mainHandler.postDelayed(() -> synthesizeAssistantSpeech("Your schedule cannot be earlier than the current date and time."), 3000);
                        }
                    }
                }
                if (hasDeadlineChange) {
                    if (CalendarUtils.isDateAccepted(tempTaskForAddEdit.getDeadline())) {
                        if (!taskToEdit.getRecurrence().equalsIgnoreCase("none")) {
                            Log.v ("hasDeadlineChange", "!taskToEdit.getRecurrence().equalsIgnoreCase(\"none\")");
                            taskToEdit.setRecurrence("None");
                            taskToEdit.setSchedule("No schedule");
                            taskToEdit.setDeadline(tempTaskForAddEdit.getDeadline());
                            synthesizeAssistantSpeech("I have removed your existing recurrence to apply your new deadline.");
                        } else {
                            if (!taskToEdit.getSchedule().equalsIgnoreCase("no schedule")) {
                                Log.v ("hasDeadlineChange", "!taskToEdit.getSchedule().equalsIgnoreCase(\"no schedule\")");
                                boolean schedIsValid = CalendarUtils.isDateAccepted(taskToEdit.getSchedule(), tempTaskForAddEdit.getDeadline());
                                if (schedIsValid) {
                                    taskToEdit.setDeadline(tempTaskForAddEdit.getDeadline());
                                } else {
                                    taskToEdit.setSchedule("No schedule");
                                    taskToEdit.setDeadline(tempTaskForAddEdit.getDeadline());
                                    synthesizeAssistantSpeech("Your schedule cannot be later than your new deadline. I have set your schedule to none.");
                                }
                            } else {
                                taskToEdit.setDeadline(tempTaskForAddEdit.getDeadline());
                            }
                        }
                    } else {
                        startSpecificModelMotion(LAppDefine.MotionGroup.THINKING.getId(), 1);
                        mainHandler.postDelayed(() -> synthesizeAssistantSpeech("Your deadline cannot be earlier than the current date and time."), 3000);
                    }
                }
            }

            taskDatabaseManager.updateTask(taskToEdit, updatedTask -> {
                mainHandler.postDelayed(this::turnBasedInteraction, 3000);
            });
        }, taskName);
    }


    private void turnBasedInteraction() {
        if (!inEditTaskInteraction) {
            return;
        }
        startSpecificModelMotion(LAppDefine.MotionGroup.ASKING.getId(), 0);
        String followUpQuestion = AIRandomSpeech.generateFollowUpChangeMessage();
        askQuestion(followUpQuestion);
    }

    private void performIntentEdit(String responseText) {
        prefilterAddEditTask(responseText, false);
    }

    @Override
    public void onSpeechRecognized(String recognizedSpeech) {
        realTimeSpeechTextView.setText(recognizedSpeech);
        setModelExpression(defaultExpression);
        partialSpeech = "";

        mainHandler.postDelayed(this::fadeOutCardView, FADE_OUT_DELAY);
        processRecognizedSpeech(recognizedSpeech);
    }

    private void processRecognizedSpeech(String recognizedSpeech) {
        if (ExplicitContentFilter.containsExplicitContent(recognizedSpeech)) {
            synthesizeAssistantSpeech("I'm sorry, but I cannot process that request because it contains inappropriate or sensitive content. Please try again with different words.");
            return;
        }

        insertDialogue(recognizedSpeech, false);
        if (recognizedSpeech.equalsIgnoreCase("reset conversation")) {
            inEditTaskInteraction = false;
            inTaskDetailInteraction = false;
            addTaskAskingForTaskName = false;
            editTaskAskingForTaskName = false;
            isRequestNameFromGrok = false;
            addNotesAskingForTaskName = false;
            confirmingDeleteTask = false;

            tempEditTaskName = "";
            tempNotes = "";

            startSpecificModelMotion(LAppDefine.MotionGroup.SWITCH.getId(), 0);

            mainHandler.postDelayed(() -> synthesizeAssistantSpeech(AIRandomSpeech.generateResetCompletedMessage()), 2000);
            return;
        }

        if (recognizedSpeech.equalsIgnoreCase("assistive mode on")) {
            if (AssistiveModeHelper.isAssistiveModeEnabled(requireContext())) {
                synthesizeAssistantSpeech("Assistive mode is already activated");
                return;
            }
            onAssistiveModeChanged(true);
            startSpecificModelMotion(LAppDefine.MotionGroup.SWITCH.getId(), 0);

            mainHandler.postDelayed(() -> synthesizeAssistantSpeech(AIRandomSpeech.generateAssistiveModeOn()), 2000);
            return;
        } else if (recognizedSpeech.equalsIgnoreCase("assistive mode off")) {
            if (!AssistiveModeHelper.isAssistiveModeEnabled(requireContext())) {
                synthesizeAssistantSpeech("Assistive mode is already off");
                return;
            }
            onAssistiveModeChanged(false);
            startSpecificModelMotion(LAppDefine.MotionGroup.SWITCH.getId(), 0);
            mainHandler.postDelayed(() -> synthesizeAssistantSpeech(AIRandomSpeech.generateAssistiveModeOff()), 2000);
            return;
        }

        if (editTaskAskingForTaskName) {
            hasRecurrenceOnRecognizedSpeech = previousHasRecurrenceOnRecognizedSpeech;
        } else {
            // Check if the recognized speech contains the word "recurrence"
            hasRecurrenceOnRecognizedSpeech = recognizedSpeech.toLowerCase().contains("recurrence");
            previousHasRecurrenceOnRecognizedSpeech = hasRecurrenceOnRecognizedSpeech;
        }

        if (confirmingAddTask) {
            confirmAddTaskWithUser(recognizedSpeech);
        } else if (confirmingDeleteTask) {
            confirmDeleteTaskWithUser(recognizedSpeech);
        } else if (inTaskDetailInteraction) {
            handleTaskDetailInteraction(recognizedSpeech);
        } else if (addTaskAskingForTaskName) {
            getAddTaskName(recognizedSpeech);
        } else if (editTaskAskingForTaskName) {
            getEditTaskName(recognizedSpeech);
        } else if (addNotesAskingForTaskName) {
            getNotesTaskName(recognizedSpeech);
        }else if (isRequestNameFromGrok) {
            handleRequestTaskName(recognizedSpeech);
        } else if (inEditTaskInteraction) {
            handleEditTaskInteraction(recognizedSpeech);
        } else {
            handleAllIntent(recognizedSpeech);
//            if (containsKeyword(recognizedSpeech, new String[]{"add", "edit", "delete", "mark"})) {
//                String firstKeyword = getFirstKeyword(recognizedSpeech, new String[]{"add", "edit", "delete", "mark"});
//                if (firstKeyword != null) {
//                    switch (firstKeyword) {
//                        case "add":
//                            handleAddInteraction(recognizedSpeech);
//                            break;
//                        case "edit":
//                            handleEditInteraction(recognizedSpeech);
//                            break;
//                        case "delete":
//                            handleDeleteInteraction(recognizedSpeech);
//                            break;
//                        case "mark":
//                            handleMarkInteraction(recognizedSpeech);
//                            break;
//                    }
//                }
//            } else {
//                handleSecondaryIntent(recognizedSpeech);
//            }
        }
    }

    private void getVicunaResponse(TaskModel task, String systemAction, String prompt) {
        Log.v("getVicunaResponse", "getVicunaResponse running");
        HttpRequest.handleVicunaResponse(aiName, task, prompt, new HttpRequest.HttpRequestCallback() {
            @Override
            public void onSuccess(String intent, String responseText) {
                if (systemAction.equalsIgnoreCase("add_task")) {
                    startRandomMotionFromGroup(LAppDefine.MotionGroup.AFFIRMATION.getId(), LAppDefine.Priority.FORCE.getPriority());

                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            synthesizeAssistantSpeech(responseText);
                            // to get id for notif scheduler
                            taskDatabaseManager.fetchUnfinishedTaskByName(tasks -> {
                                if (tasks.isEmpty()) {
                                    return;
                                }
                                mainHandler.postDelayed(() -> openTaskDetailFragment(tasks.get(0)), 4000);
                            }, task.getTaskName());
                        }
                    }, 3000);
                }
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

    private void handleAllIntent(String recognizedSpeech) {
        Log.v("handleAllIntent", "handleAllIntent running");
        HttpRequest.handleAllIntent(recognizedSpeech, aiName, user.getId(), new HttpRequest.HttpRequestCallback() {
            @Override
            public void onSuccess(String intent, String responseText) {
                Log.v("TestIntentResponse", "Intent: " + intent + " response text: "+ responseText);
                mainHandler.post(() -> {
                    if (!intent.equals("null")) {
                        performIntent(intent, responseText);
                    } else {
                        if (!AssistiveModeHelper.isAssistiveModeEnabled(requireContext())) {
                            String focusResponse = AIRandomSpeech.generateAssistiveModeMessage();
                            synthesizeAssistantSpeech(focusResponse);
                        } else {
                            synthesizeAssistantSpeech(responseText);
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

    private void handlePrimarySecondaryRequest(String recognizedSpeech) {
        Log.v("handleAllIntent", "handleAllIntent running");
        HttpRequest.primarySecondaryRequest(recognizedSpeech, aiName, user.getId(), new HttpRequest.HttpRequestCallback() {
            @Override
            public void onSuccess(String intent, String responseText) {
                Log.v("TestIntentResponse", "Intent: " + intent + " response text: "+ responseText);
                mainHandler.post(() -> {
                    if (!intent.equals("null")) {
                        performIntent(intent, responseText);
                    } else {
                        if (!AssistiveModeHelper.isAssistiveModeEnabled(requireContext())) {
                            String focusResponse = AIRandomSpeech.generateAssistiveModeMessage();
                            synthesizeAssistantSpeech(focusResponse);
                        } else {
                            synthesizeAssistantSpeech(responseText);
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

    private void handleMarkInteraction(String recognizedSpeech) {
        Log.v("handleMarkInteraction", "handleMarkInteraction running");
        HttpRequest.markRequest(recognizedSpeech, aiName, user.getId(), new HttpRequest.HttpRequestCallback() {
            @Override
            public void onSuccess(String intent, String responseText) {
                Log.v("TestIntentResponse", "Intent: " + intent + " response text: "+ responseText);
                mainHandler.post(() -> {
                    if (!intent.equals("null")) {
                        performIntent(intent, responseText);
                    } else {
                        if (!AssistiveModeHelper.isAssistiveModeEnabled(requireContext())) {
                            String focusResponse = AIRandomSpeech.generateAssistiveModeMessage();
                            synthesizeAssistantSpeech(focusResponse);
                        } else {
                            synthesizeAssistantSpeech(responseText);
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

    private void handleDeleteInteraction(String recognizedSpeech) {
        Log.v("handleDeleteInteraction", "handleDeleteInteraction running");
        HttpRequest.deleteRequest(recognizedSpeech, aiName, user.getId(), new HttpRequest.HttpRequestCallback() {
            @Override
            public void onSuccess(String intent, String responseText) {
                Log.v("TestIntentResponse", "Intent: " + intent + " response text: "+ responseText);
                mainHandler.post(() -> {
                    if (!intent.equals("null")) {
                        performIntent(intent, responseText);
                    } else {
                        if (!AssistiveModeHelper.isAssistiveModeEnabled(requireContext())) {
                            String focusResponse = AIRandomSpeech.generateAssistiveModeMessage();
                            synthesizeAssistantSpeech(focusResponse);
                        } else {
                            synthesizeAssistantSpeech(responseText);
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

    private void handleEditInteraction(String recognizedSpeech) {
        Log.v("handleEditInteraction", "handleEditInteraction running");
        HttpRequest.editRequest(recognizedSpeech, aiName, user.getId(), new HttpRequest.HttpRequestCallback() {
            @Override
            public void onSuccess(String intent, String responseText) {
                Log.v("TestIntentResponse", "Intent: " + intent + " response text: "+ responseText);
                mainHandler.post(() -> {
                    if (!intent.equals("null")) {
                        performIntent(intent, responseText);
                    } else {
                        if (!AssistiveModeHelper.isAssistiveModeEnabled(requireContext())) {
                            String focusResponse = AIRandomSpeech.generateAssistiveModeMessage();
                            synthesizeAssistantSpeech(focusResponse);
                        } else {
                            synthesizeAssistantSpeech(responseText);
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

    private void handleAddInteraction(String recognizedSpeech) {
        Log.v("handleRegularInteraction", "handleRegularInteraction running");
        HttpRequest.addRequest(recognizedSpeech, aiName, user.getId(), new HttpRequest.HttpRequestCallback() {
            @Override
            public void onSuccess(String intent, String responseText) {
                Log.v("TestIntentResponse", "Intent: " + intent + " response text: "+ responseText);
                mainHandler.post(() -> {
                    if (!intent.equals("null")) {
                        performIntent(intent, responseText);
                    } else {
                        if (!AssistiveModeHelper.isAssistiveModeEnabled(requireContext())) {
                            String focusResponse = AIRandomSpeech.generateAssistiveModeMessage();
                            synthesizeAssistantSpeech(focusResponse);
                        } else {
                            synthesizeAssistantSpeech(responseText);
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

    private void handleEditTaskInteraction(String recognizedSpeech) {
        Log.v("handleEditTaskInteraction", "handleEditTaskInteraction running");
        HttpRequest.editTaskRequest(recognizedSpeech, aiName, user.getId(), new HttpRequest.HttpRequestCallback() {
            @Override
            public void onSuccess(String intent, String responseText) {
                Log.v("TestIntentResponse", "Intent: " + intent + " response text: "+ responseText);
                mainHandler.post(() -> {
                    if (intent.equalsIgnoreCase("edit task")) {
                        performIntentEdit(responseText);
                    } else if (intent.equalsIgnoreCase("null")) {
                        if (responseText.equalsIgnoreCase("done")) {
                            inEditTaskInteraction = false;
                            tempEditTaskName = "";
                            startRandomMotionFromGroup(LAppDefine.MotionGroup.AFFIRMATION.getId(), LAppDefine.Priority.FORCE.getPriority());

                            mainHandler.postDelayed(() -> synthesizeAssistantSpeech(AIRandomSpeech.generateTaskUpdated(taskToEdit.getTaskName())), 3000);
                            mainHandler.postDelayed(() -> openTaskDetailFragment(taskToEdit), 4000);
                        } else if (responseText.equalsIgnoreCase("unrecognized")) {
                            synthesizeAssistantSpeech("I'm sorry, I didn't understand, what else do you want to edit?");
                        } else {
                            performIntentEdit(responseText);
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

    private void handleSecondaryIntent(String recognizedSpeech) {
        Log.v("handleSecondaryIntent", "handleSecondaryIntent running");
        HttpRequest.secondaryIntentReq(recognizedSpeech, aiName, user.getId(), new HttpRequest.HttpRequestCallback() {
            @Override
            public void onSuccess(String intent, String responseText) {
                Log.v("TestIntentResponse", "Intent: " + intent + " response text: "+ responseText);
                mainHandler.post(() -> {
                    if (!intent.equals("null")) {
                        performIntent(intent, responseText);
                    } else {
                        if (!AssistiveModeHelper.isAssistiveModeEnabled(requireContext())) {
                            String focusResponse = AIRandomSpeech.generateAssistiveModeMessage();
                            synthesizeAssistantSpeech(focusResponse);
                        } else {
                            synthesizeAssistantSpeech(responseText);
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

    private void handleRequestTaskName(String recognizedSpeech) {
        Log.v("handleRequestTaskName", "handleRequestTaskName running");
        HttpRequest.requestTaskName(recognizedSpeech, new HttpRequest.HttpRequestCallback() {
            @Override
            public void onSuccess(String intent, String responseText) {
                Log.v("handleRequestTaskName", "intent: " + intent + " response: " + responseText);
                mainHandler.post(() -> {
                    Pattern taskNamePattern = Pattern.compile("TASK_NAME: (.+?)(?=\\n|$)");
                    String taskName = "";
                    Matcher taskNameMatcher = taskNamePattern.matcher(responseText);
                    if (taskNameMatcher.find()) {
                        taskName = taskNameMatcher.group(1);
                        Log.d("DEBUG_TASK_NAME", "Extracted task name: " + taskName);
                    }
                    isRequestNameFromGrok = false;
                    getTaskDetail(taskName);
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

    private void insertDialogue(String dialogue, boolean isAssistant) {
        // Ensure this code runs on a thread with a Looper
        if (Looper.myLooper() == Looper.getMainLooper()) {
            conversationDbManager.insertDialogue(dialogue, isAssistant);
        } else {
            mainHandler.post(() -> conversationDbManager.insertDialogue(dialogue, isAssistant));
        }
    }

    private void synthesizeAssistantSpeech (String dialogue) {
        SpeechSynthesis.synthesizeSpeechAsync(dialogue);
        insertDialogue(dialogue, true);
        setModelExpression(defaultExpression);
        startRandomMotionFromGroup(LAppDefine.MotionGroup.SPEAKING.getId(), LAppDefine.Priority.NORMAL.getPriority());

        Runnable checkSpeechAndAnimate = new Runnable() {
            @Override
            public void run() {
                if (SpeechSynthesis.isSpeaking()) {
                    startRandomMotionFromGroup(LAppDefine.MotionGroup.SPEAKING.getId(), LAppDefine.Priority.NORMAL.getPriority());
                    mainHandler.postDelayed(this, 1500); // Check every 2 seconds, adjust as needed
                }
            }
        };
        mainHandler.postDelayed(checkSpeechAndAnimate, 1500);
    }


    private void confirmAddTaskWithUser(String recognizedSpeech) {
        if (recognizedSpeech.equalsIgnoreCase("yes")) {
            confirmingAddTask = false;
            insertCompleteTask(tempTaskForAddEdit);
        } else if (recognizedSpeech.equalsIgnoreCase("no")) {
            synthesizeAssistantSpeech("As per your request, I did not add the task named " + tempTaskForAddEdit.getTaskName());
            confirmingAddTask = false;
        } else {
            synthesizeAssistantSpeech("I'm sorry, I didn't understand that. Are you sure you want to add task? Please say yes or no.");
        }
    }

    private void handleTaskDetailInteraction(String recognizedSpeech) {
        Log.v("handleTaskDetailInteraction", "handleTaskDetailInteraction running");

        final List<String> doneIntents = Arrays.asList("done", "finished", "all set", "i'm good", "thank you", "that's all");
        HttpRequest.taskDetailRequest(tempTask, recognizedSpeech, aiName, new HttpRequest.HttpRequestCallback() {
            @Override
            public void onSuccess(String intent, String responseText) {
                mainHandler.post(() -> {
                    if (intent.equalsIgnoreCase("null")) {
                        synthesizeAssistantSpeech("I'm sorry I didn't understand, if you need details of your task just tell me what you want to know. If you are done, you can say \"I'm done\".");
                    } else if (isDoneIntent(intent)) {
                        inTaskDetailInteraction = false;
                        synthesizeAssistantSpeech("If you need anything else, don't hesitate to tell me!");
                    } else {
                        synthesizeAssistantSpeech(responseText);
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


    private void openTaskDetailFragment(TaskModel task) {
        ((MainActivity) requireActivity()).showTaskDetailFragment(task);
    }

    private boolean containsKeyword(String speech, String[] keywords) {
        for (String keyword : keywords) {
            if (speech.toLowerCase().contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String getFirstKeyword(String speech, String[] keywords) {
        int firstIndex = speech.length();
        String firstKeyword = null;
        for (String keyword : keywords) {
            int index = speech.toLowerCase().indexOf(keyword);
            if (index != -1 && index < firstIndex) {
                firstIndex = index;
                firstKeyword = keyword;
            }
        }
        return firstKeyword;
    }

    private void fadeOutCardView() {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(cardViewSpeech, "alpha", 1f, 0f);
        fadeOut.setDuration(FADE_OUT_DURATION);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                cardViewSpeech.setVisibility(View.INVISIBLE);
            }
        });
        fadeOut.start();
    }

    @Override
    public void onPartialSpeechRecognized(String partialSpeech) {
        this.partialSpeech = partialSpeech;
        realTimeSpeechTextView.setText(partialSpeech);
    }

    @Override
    public void onStart() {
        super.onStart();
        LAppDelegate.getInstance().onStart(getActivity());
        SpeechSynthesis.initialize(); // Ensure the SpeechSynthesis executor is initialized when the fragment starts
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
        SpeechSynthesis.shutdown(); // Shutdown the SpeechSynthesis executor when the fragment stops
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
        if (wakeWordReceiver != null) {
            requireContext().unregisterReceiver(wakeWordReceiver);
        }

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

