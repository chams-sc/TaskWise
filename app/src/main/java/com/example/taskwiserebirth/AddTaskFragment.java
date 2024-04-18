package com.example.taskwiserebirth;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.database.DatabaseChangeListener;
import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.task.Task;
import com.example.taskwiserebirth.task.TaskAdapter;
import com.example.taskwiserebirth.task.TaskPriorityCalculator;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.mongodb.App;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class AddTaskFragment extends Fragment implements DatabaseChangeListener, NestedScrollView.OnScrollChangeListener, TaskAdapter.TaskActionListener {

    private String daysSelected = null;
    private TaskAdapter taskAdapter;


    private User user;
    private MongoCollection<Document> taskCollection;
    private final String TAG = "MongoDb";

    public AddTaskFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_task, container, false);

        // Realm initialization
        App app = MongoDbRealmHelper.initializeRealmApp();
        user = app.currentUser();
        MongoDbRealmHelper.addDatabaseChangeListener(this);
        taskCollection = MongoDbRealmHelper.getMongoCollection("UserTaskData");

        NestedScrollView nestedScrollView = rootView.findViewById(R.id.nestedScrollView);
        nestedScrollView.setOnScrollChangeListener(this);

        setUpCalendarRecyclerView(rootView);
        setUpCardRecyclerView(rootView);

        Task task = null;
        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(v -> showBottomSheetDialog(task));

        displayTimeOfDay(rootView);

        return rootView;
    }



    private void setUpCalendarRecyclerView(View rootView) {
        RecyclerView calendarRecyclerView = rootView.findViewById(R.id.calendarRecyclerView);

        TextView currentMonth = rootView.findViewById(R.id.monthTxt);
        // Get the current month
        Calendar calendar = Calendar.getInstance();
        int currentMonthIndex = calendar.get(Calendar.MONTH);
        String[] months = new DateFormatSymbols().getMonths();
        String currentMonthName = months[currentMonthIndex].toUpperCase();

        // Set the current month to the TextView
        currentMonth.setText(currentMonthName);

        // Get list of calendar dates
        List<Calendar> calendarList = getDatesForCurrentMonth();

        // Set up CalendarAdapter
        CalendarAdapter calendarAdapter = new CalendarAdapter(calendarList, date -> {
            // Handle calendar item click if needed
        });

        // Set up RecyclerView with LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        calendarRecyclerView.setLayoutManager(layoutManager);

        // Set RecyclerView adapter
        calendarRecyclerView.setAdapter(calendarAdapter);

        // Scroll to current date position
        int currentPosition = getCurrentDatePosition(calendarList);
        if (currentPosition != -1) {
            layoutManager.scrollToPosition(currentPosition);
        }
    }

    private int getCurrentDatePosition(List<Calendar> calendarList) {
        Calendar currentDate = Calendar.getInstance();
        int currentDay = currentDate.get(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < calendarList.size(); i++) {
            Calendar calendar = calendarList.get(i);
            if (calendar.get(Calendar.DAY_OF_MONTH) == currentDay) {
                return i;
            }
        }

        return -1; // Current date not found
    }

    private void setUpCardRecyclerView(View rootView) {
        RecyclerView cardRecyclerView = rootView.findViewById(R.id.tasksRecyclerView);

        // Dummy data for card items
        List<Task> tasks = new ArrayList<>();

        // Set up CardAdapter
        taskAdapter = new TaskAdapter(requireContext(), tasks, this);

        // Set up RecyclerView with LinearLayoutManager
        cardRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set RecyclerView adapter with the items list
        cardRecyclerView.setAdapter(taskAdapter);

        updateTaskRecyclerView();
    }

    private void updateTaskRecyclerView() {
        Document queryFilter = new Document("owner_id", user.getId());
        RealmResultTask<MongoCursor<Document>> findTask = taskCollection.find(queryFilter).iterator();
        findTask.getAsync(task -> {
            if (task.isSuccess()) {
                MongoCursor<Document> results = task.get();
                List<Task> tasks = new ArrayList<>();

                String priorityLevel = "";

                while (results.hasNext()) {
                    Document document = results.next();
                    ObjectId taskId = document.getObjectId("_id");
                    String taskName = document.getString("task_name");
                    String importanceLevel = document.getString("importance_level");
                    String urgencyLevel = document.getString("urgency_level");
                    String deadlineString = document.getString("deadline");
                    String schedule = document.getString("schedule");
                    String recurrence = document.getString("recurrence");
                    boolean reminder = document.getBoolean("reminder");
                    String notes = document.getString("notes");
                    String status = document.getString("status");

                    Task newTask = new Task(taskId, taskName, deadlineString, importanceLevel, urgencyLevel, priorityLevel, schedule, recurrence, reminder, notes, status);
                    tasks.add(newTask);
                }

                List<Task> sortedTasks = TaskPriorityCalculator.sortTasksByPriority(tasks, new Date());

                if(isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        taskAdapter.setTasks(sortedTasks);
                    });
                }
            } else {
                Log.e(TAG, "failed to find documents with: ", task.getError());
            }
        });
    }

    private void displayTimeOfDay(View rootView) {
        TextView timeOfDayTextView = rootView.findViewById(R.id.tasksText);
        ImageView timeOfDayImageView = rootView.findViewById(R.id.timeOfDayImageView);

        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        String timeOfDay;
        int drawableResId;

        if (hourOfDay >= 6 && hourOfDay < 12) {
            timeOfDay = "Morning";
            drawableResId = R.drawable.baseline_sun2;
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            timeOfDay = "Afternoon";
            drawableResId = R.drawable.baseline_sun2;
        } else if (hourOfDay >= 18) {
            timeOfDay = "Evening";
            drawableResId = R.drawable.baseline_night2;
        } else {
            timeOfDay = "Night";
            drawableResId = R.drawable.baseline_night2;
        }

        timeOfDayTextView.setText(timeOfDay);
        timeOfDayImageView.setImageResource(drawableResId);
    }


    private List<Calendar> getDatesForCurrentMonth() {
        List<Calendar> calendarList = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();

        // Set to the first day of the current month
        currentDate.set(Calendar.DAY_OF_MONTH, 1);

        int daysInMonth = currentDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int day = 1; day <= daysInMonth; day++) {
            Calendar date = (Calendar) currentDate.clone();
            date.set(Calendar.DAY_OF_MONTH, day);
            calendarList.add(date);
        }

        return calendarList;
    }

    private void showBottomSheetDialog(Task task) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.add_task, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        SystemUIHelper.adjustDialog((AppCompatActivity) getActivity(), bottomSheetDialog);

        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setUpViews(bottomSheetDialog, bottomSheetView, task);

        bottomSheetDialog.show();
    }

    private void setUpViews(BottomSheetDialog bottomSheetDialog, View bottomSheetView, Task task) {
        Spinner importanceSpinner = bottomSheetView.findViewById(R.id.importance);
        Spinner urgencySpinner = bottomSheetView.findViewById(R.id.urgency);
        Spinner recurrenceSpinner = bottomSheetView.findViewById(R.id.recurrence);

        setupSpinners(importanceSpinner, urgencySpinner, recurrenceSpinner);

        EditText editTaskName = bottomSheetView.findViewById(R.id.taskName);
        EditText editDeadline = bottomSheetView.findViewById(R.id.deadline);
        EditText editSchedule = bottomSheetView.findViewById(R.id.schedule);
        EditText editNotes = bottomSheetView.findViewById(R.id.notes);
        CheckBox reminder = bottomSheetView.findViewById(R.id.reminder);

        if(task != null) {
            editTaskName.setText(task.getTaskName());
            editDeadline.setText(task.getDeadline());
            editSchedule.setText(task.getSchedule());
            editNotes.setText(task.getNotes());
            reminder.setChecked(task.isReminder());

            if(!task.getRecurrence().isEmpty()) {
                if (task.getRecurrence().equals("None") || task.getRecurrence().equals("Daily")) {
                    recurrenceSpinner.setSelection(getIndex(recurrenceSpinner, task.getRecurrence()));
                } else {
                    recurrenceSpinner.setSelection(getIndex(recurrenceSpinner, "Specific Days"), false);
                    View selectedView = recurrenceSpinner.getSelectedView();
                    if (selectedView instanceof TextView) {
                        ((TextView) selectedView).setText(task.getRecurrence());
                    }
                }
            }

            importanceSpinner.setSelection(getIndex(importanceSpinner, task.getImportanceLevel()));
            urgencySpinner.setSelection(getIndex(urgencySpinner, task.getUrgencyLevel()));
        }

        getRecurrenceSpinnerValue(recurrenceSpinner);

        Button saveBtn = bottomSheetView.findViewById(R.id.saveButton);
        saveBtn.setOnClickListener(v -> {
            Task newTask = setTaskFromFields(bottomSheetDialog, task);
            if (task != null) {
                if (newTask != null) {
                    updateTask(newTask);
                    bottomSheetDialog.dismiss();
                    daysSelected = task.getRecurrence();
                }
            } else {
                if (newTask != null) {
                    insertTask(newTask);
                    bottomSheetDialog.dismiss();
                    daysSelected = null;
                }
            }
        });

        ((View) bottomSheetView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        editDeadline.setOnClickListener(v -> showDatePicker(editDeadline));
        editSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(editSchedule);
            }
        });
    }

    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0; // Default to first item if not found
    }

    private Task setTaskFromFields(Dialog bottomSheetDialog, Task task) {
        EditText editTaskName = bottomSheetDialog.findViewById(R.id.taskName);
        EditText editDeadline = bottomSheetDialog.findViewById(R.id.deadline);
        EditText editSchedule = bottomSheetDialog.findViewById(R.id.schedule);
        EditText editNotes = bottomSheetDialog.findViewById(R.id.notes);

        Spinner importanceSpinner = bottomSheetDialog.findViewById(R.id.importance);
        Spinner urgencySpinner = bottomSheetDialog.findViewById(R.id.urgency);
        Spinner recurrenceSpinner = bottomSheetDialog.findViewById(R.id.recurrence);

        CheckBox reminderCheckbox = bottomSheetDialog.findViewById(R.id.reminder);

        String deadline = editDeadline.getText().toString().trim();
        String schedule = editSchedule.getText().toString().trim();
        String notes = editNotes.getText().toString().trim();
        String recurrence = recurrenceSpinner.getSelectedItem().toString();
        String importance = importanceSpinner.getSelectedItem().toString();
        String urgency = urgencySpinner.getSelectedItem().toString();

        Date currentDate = new Date(); // UTC time

        if (deadline.isEmpty()) {
            deadline = "No deadline";
        }

        if (schedule.isEmpty()) {
            schedule = "No schedule";
        }

        if (recurrence.equals("Specific Days")) {
            recurrence = daysSelected;
        }

        if (!validateFields(editTaskName, deadline, schedule, currentDate)) {
            return null;
        }

        // if updating task
        Task newTask = new Task();
        if (task != null) {
            newTask.setId(task.getId());
        }

        newTask.setTaskName(editTaskName.getText().toString().trim());
        newTask.setImportanceLevel(importance);
        newTask.setUrgencyLevel(urgency);
        newTask.setDeadline(deadline);
        newTask.setSchedule(schedule);
        newTask.setRecurrence(recurrence);
        newTask.setReminder(reminderCheckbox.isChecked());
        newTask.setNotes(notes);
        newTask.setStatus("Unfinished");
        newTask.setCreationDate(currentDate);

        return newTask;
    }

    private boolean validateFields(EditText taskName, String deadline, String schedule, Date currentDate) {
        boolean validDeadline = true;
        boolean validSchedule = true;

        Log.d("deadline", deadline);

        // Check if deadline is not empty and not earlier than current date
        if (!deadline.equals("No deadline")) {
            DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy | hh:mm a", Locale.getDefault());
            try {
                Date deadlineDate = dateFormat.parse(deadline);
                if (deadlineDate.before(currentDate)) {
                    validDeadline = false;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                validDeadline = false;
            }
        }

        // Check if schedule is not empty and not earlier than current date
        if (!schedule.equals("No schedule")) {
            DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy | hh:mm a", Locale.getDefault());
            try {
                Date scheduleDate = dateFormat.parse(schedule);
                if (scheduleDate.before(currentDate)) {
                    validSchedule = false;
                } if (!deadline.equals("No deadline")) {
                    // Check if schedule is later than deadline
                    Date deadlineDate = dateFormat.parse(deadline);
                    if (scheduleDate.after(deadlineDate)) {
                        validSchedule = false;
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
                validSchedule = false;
            }
        }

        // Check required fields
        boolean validFields = true;
        if (taskName.getText().toString().trim().isEmpty()) {
            taskName.setError("Task name is required");
            validFields = false;
        }

        if (!validFields || !validDeadline || !validSchedule) {
            if (!validFields) {
                Toast.makeText(requireContext(), "Missing required fields", Toast.LENGTH_SHORT).show();
            } else if (!validDeadline) {
                Toast.makeText(requireContext(), "Deadline cannot be earlier than current date and time", Toast.LENGTH_SHORT).show();
            } else if (!validSchedule) {
                Toast.makeText(requireContext(), "Schedule cannot be earlier than current date and time or later than deadline", Toast.LENGTH_SHORT).show();
            }
            return false;
        } else {
            return true;
        }
    }

    private void updateTask(Task task) {
        if (user != null) {
            // Define the filter to find the task to be updated
            Document filter = new Document("owner_id", user.getId())
                    .append("_id", task.getId());

            // Define the update operation
            Document updateDocument = new Document("$set", new Document()
                    .append("task_name", task.getTaskName())
                    .append("importance_level", task.getImportanceLevel())
                    .append("urgency_level", task.getUrgencyLevel())
                    .append("deadline", task.getDeadline())
                    .append("schedule", task.getSchedule())
                    .append("recurrence", task.getRecurrence())
                    .append("reminder", task.isReminder())
                    .append("notes", task.getNotes())
                    .append("status", task.getStatus())
                    .append("creation_date", task.getCreationDate())
            );

            // Perform the update operation
            taskCollection.updateOne(filter, updateDocument).getAsync(result -> {
                if (result.isSuccess()) {
                    Log.d("Data", "Data Updated Successfully");
                    Toast.makeText(requireContext(), "Task updated", Toast.LENGTH_SHORT).show();
                    MongoDbRealmHelper.notifyDatabaseChangeListeners();
                } else {
                    Log.e("Data", "Failed to update data: " + result.getError().getMessage());
                }
            });
        }
    }


    private void insertTask(Task task) {
        if (user != null) {
            Document taskDocument = new Document("owner_id", user.getId())
                    .append("task_name", task.getTaskName())
                    .append("importance_level", task.getImportanceLevel())
                    .append("urgency_level", task.getUrgencyLevel())
                    .append("deadline", task.getDeadline())
                    .append("schedule", task.getSchedule())
                    .append("recurrence", task.getRecurrence())
                    .append("reminder", task.isReminder())
                    .append("notes", task.getNotes())
                    .append("status", task.getStatus())
                    .append("creation_date", task.getCreationDate());

            taskCollection.insertOne(taskDocument).getAsync(result -> {
                if (result.isSuccess()) {
                    Log.d("Data", "Data Inserted Successfully");
                    Toast.makeText(requireContext(), "Task saved", Toast.LENGTH_SHORT).show();
                    MongoDbRealmHelper.notifyDatabaseChangeListeners();
                } else {
                    Log.e("Data", "Failed to insert data: " + result.getError().getMessage());
                }
            });
        }
    }

    private void getRecurrenceSpinnerValue(Spinner recurrenceSpinner) {
        recurrenceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Specific Days")) {
                    showDialogForCustomRecurrence(recurrenceSpinner, position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle nothing selected if needed
            }
        });
    }

    private void setupSpinners(Spinner importanceSpinner, Spinner urgencySpinner, Spinner recurrenceSpinner) {
        List<String> importanceList = Arrays.asList(getResources().getStringArray(R.array.importance_array));
        List<String> urgencyList = Arrays.asList(getResources().getStringArray(R.array.urgency_array));
        List<String> recurrenceList = Arrays.asList(getResources().getStringArray(R.array.recurrence_array));

        CustomSpinnerAdapter importanceAdapter = new CustomSpinnerAdapter(requireContext(), R.layout.item_spinner, importanceList);
        CustomSpinnerAdapter urgencyAdapter = new CustomSpinnerAdapter(requireContext(), R.layout.item_spinner, urgencyList);
        CustomSpinnerAdapter recurrenceAdapter = new CustomSpinnerAdapter(requireContext(), R.layout.item_spinner, recurrenceList);

        importanceSpinner.setAdapter(importanceAdapter);
        urgencySpinner.setAdapter(urgencyAdapter);
        recurrenceSpinner.setAdapter(recurrenceAdapter);
    }

    private void showDialogForCustomRecurrence(Spinner spinner, int pos) {
        final Dialog bottomSheetDialog = new Dialog(requireContext());
        bottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottomSheetDialog.setContentView(R.layout.recurrence_picker);

        Button setButton = bottomSheetDialog.findViewById(R.id.setBtn);
        setButton.setEnabled(false);

        int[] checkBoxIds = {R.id.Monday, R.id.Tuesday, R.id.Wednesday, R.id.Thursday, R.id.Friday, R.id.Saturday, R.id.Sunday};
        List<CheckBox> checkBoxes = new ArrayList<>();
        for (int checkBoxId : checkBoxIds) {
            CheckBox checkBox = bottomSheetDialog.findViewById(checkBoxId);
            checkBoxes.add(checkBox);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> setButton.setEnabled(isAnyCheckBoxChecked(checkBoxes)));
        }

        bottomSheetDialog.setOnCancelListener(dialog -> spinner.setSelection(0));

        setButton.setOnClickListener(v -> {
            List<String> selectedDays = getSelectedDays(bottomSheetDialog);
            StringBuilder stringBuilder = new StringBuilder();

            for (String day : selectedDays) {
                stringBuilder.append(day).append(" | ");
            }

            if (stringBuilder.length() > 0) {
                stringBuilder.setLength(stringBuilder.length() - 3);
            }

            daysSelected = stringBuilder.toString();
            spinner.setSelection(pos);
            View selectedView = spinner.getSelectedView();
            if (selectedView instanceof TextView) {
                ((TextView) selectedView).setText(daysSelected);
            }

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = bottomSheetDialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = Gravity.CENTER;
            window.setAttributes(layoutParams);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setWindowAnimations(R.style.DialogAnimation);
        }
    }

    private boolean isAnyCheckBoxChecked(List<CheckBox> checkBoxes) {
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                return true;
            }
        }
        return false;
    }

    private List<String> getSelectedDays(Dialog dialog) {
        List<String> selectedDays = new ArrayList<>();
        int[] checkBoxIds = {R.id.Monday, R.id.Tuesday, R.id.Wednesday, R.id.Thursday, R.id.Friday, R.id.Saturday, R.id.Sunday};
        String[] dayNames = {"M", "Tu", "W", "Th", "F", "Sa", "Su"};

        for (int i = 0; i < checkBoxIds.length; i++) {
            CheckBox checkBox = dialog.findViewById(checkBoxIds[i]);
            if (checkBox.isChecked()) {
                selectedDays.add(dayNames[i]);
            }
        }
        return selectedDays;
    }

    private void showDatePicker(EditText field) {
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setTheme(R.style.DatePickerTheme)
                .build();
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            String date = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date(selection));
            showTimePicker(field, date);
        });
        materialDatePicker.show(requireActivity().getSupportFragmentManager(), "DATE_PICKER");
    }

    private void showTimePicker(EditText field, String selectedDate) {
        MaterialTimePicker.Builder timePickerBuilder = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setTitleText("Select Time")
                .setTheme(R.style.TimePickerTheme);

        MaterialTimePicker timePicker = timePickerBuilder.build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            int hourOfDay = timePicker.getHour();
            int minute = timePicker.getMinute();

            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s",
                    hourOfDay == 12 || hourOfDay == 0 ? 12 : hourOfDay % 12, minute, hourOfDay < 12 ? "AM" : "PM");

            String dateTime = selectedDate + " | " + formattedTime;

            if (field != null) {
                field.setText(dateTime);
            }
        });

        timePicker.show(requireActivity().getSupportFragmentManager(), "TIME_PICKER");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MongoDbRealmHelper.removeDatabaseChangeListener(this);
    }

    @Override
    public void onDatabaseChange() {
        updateTaskRecyclerView();
    }

    @Override
    public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        // If scrolling up visibility = true; else false
        boolean scrollingUp = scrollY < oldScrollY;
        ((HomeActivity) requireActivity()).toggleNavBarVisibility(scrollingUp, true);
    }

    @Override
    public void onEditTask(Task task) {
        showBottomSheetDialog(task);
    }

    @Override
    public void onDeleteTask(Task task) {
        Document queryFilter = new Document("owner_id", user.getId())
                .append("_id", task.getId());

        taskCollection.deleteOne(queryFilter).getAsync(result -> {
            if (result.isSuccess()) {
                Log.d("Data", "Task deleted successfully");
                MongoDbRealmHelper.notifyDatabaseChangeListeners();
                Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("Data", "Failed to delete task: " + result.getError().getMessage());
            }
        });
    }

    @Override
    public void onDoneTask(Task task) {
        Document queryFilter = new Document("owner_id", user.getId())
                .append("_id", task.getId());
        Document updateDocument = new Document("$set", new Document("status", "Done"));

        taskCollection.updateOne(queryFilter, updateDocument).getAsync(result -> {
            if (result.isSuccess()) {
                Log.d("Data", "Task status updated");
                MongoDbRealmHelper.notifyDatabaseChangeListeners();
                Toast.makeText(requireContext(), "Task status updated", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("Data", "Failed to update task status: " + result.getError().getMessage());
            }
        });
    }

}
