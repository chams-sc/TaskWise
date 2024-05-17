package com.example.taskwiserebirth.utils;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.example.taskwiserebirth.CustomSpinnerAdapter;
import com.example.taskwiserebirth.R;
import com.example.taskwiserebirth.database.TaskDatabaseManager;
import com.example.taskwiserebirth.task.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DialogUtils {
    private final FragmentActivity activity;
    private String daysSelected = null;
    private final TaskDatabaseManager taskDatabaseManager;
    private boolean isRecurrenceNone = true;
    private final List<Dialog> dialogs = new ArrayList<>();

    public DialogUtils (FragmentActivity activity, TaskDatabaseManager taskDatabaseManager) {
        this.activity = activity;
        this.taskDatabaseManager = taskDatabaseManager;
    }

    public void showBottomSheetDialog(Task task) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        View bottomSheetView = LayoutInflater.from(activity).inflate(R.layout.bottom_add_task, null);

        SystemUIHelper.setFlagsOnThePeekView();

        bottomSheetDialog.setContentView(bottomSheetView);
        setUpTaskForm(bottomSheetDialog, bottomSheetView, task);

        bottomSheetDialog.setOnDismissListener(dialogInterface -> {
            SystemUIHelper.setSystemUIVisibility((AppCompatActivity) activity);
        });

        bottomSheetDialog.show();
        dialogs.add(bottomSheetDialog);
    }

    private void setUpTaskForm(Dialog bottomSheetDialog, View bottomSheetView, Task task) {
        Spinner importanceSpinner = bottomSheetView.findViewById(R.id.importance);
        Spinner urgencySpinner = bottomSheetView.findViewById(R.id.urgency);
        Spinner recurrenceSpinner = bottomSheetView.findViewById(R.id.recurrence);

        setupSpinners(importanceSpinner, urgencySpinner, recurrenceSpinner);

        EditText editTaskName = bottomSheetView.findViewById(R.id.taskName);
        EditText editDeadline = bottomSheetView.findViewById(R.id.deadline);
        EditText editSchedule = bottomSheetView.findViewById(R.id.schedule);
        EditText editNotes = bottomSheetView.findViewById(R.id.notes);
        CheckBox reminder = bottomSheetView.findViewById(R.id.reminder);
        reminder.setChecked(true);          // automatically turn reminder on

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

        getRecurrenceSpinnerValue(recurrenceSpinner, editDeadline, editSchedule);

        Button saveBtn = bottomSheetView.findViewById(R.id.saveButton);
        saveBtn.setOnClickListener(v -> {
            Task newTask = setTaskFromFields(bottomSheetDialog, task);
            if (newTask != null) {
                if (task != null) {
                    taskDatabaseManager.updateTask(newTask);
                    daysSelected = task.getRecurrence();
                    bottomSheetDialog.dismiss();
                } else {
                    taskDatabaseManager.fetchUnfinishedTaskByName(new TaskDatabaseManager.TaskFetchListener() {
                        @Override
                        public void onTasksFetched(List<Task> tasks) {
                            if (tasks.isEmpty()) {
                                taskDatabaseManager.insertTask(newTask);
                                daysSelected = null;
                                bottomSheetDialog.dismiss();
                            } else {
                                editTaskName.setError("Task name already exists");
                            }
                        }
                    }, newTask.getTaskName());
                }
            }
        });

        ((View) bottomSheetView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        editDeadline.setOnClickListener(v -> showDatePicker(editDeadline));
        editSchedule.setOnClickListener(v -> {
            if (!isRecurrenceNone) {
                showTimePicker(editSchedule, null, true );
            } else {
                showDatePicker(editSchedule);
            }
        });
    }

    private void showDatePicker(EditText field) {
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setTheme(R.style.DatePickerTheme)
                .build();
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            String date = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date(selection));
            showTimePicker(field, date, false);
        });
        materialDatePicker.show(activity.getSupportFragmentManager(), "DATE_PICKER");
    }

    private void showTimePicker(EditText field, String selectedDate, boolean isGetTimeString) {
        MaterialTimePicker.Builder timePickerBuilder = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setTitleText("Select Time")
                .setHour(9)
                .setMinute(0)
                .setTheme(R.style.TimePickerTheme);

        MaterialTimePicker timePicker = timePickerBuilder.build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            int hourOfDay = timePicker.getHour();
            int minute = timePicker.getMinute();

            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s",
                    hourOfDay == 12 || hourOfDay == 0 ? 12 : hourOfDay % 12, minute, hourOfDay < 12 ? "AM" : "PM");

            if (isGetTimeString) {
                if (field != null) {
                    field.setText(formattedTime);
                }
                return;
            }

            String dateTime = selectedDate + " | " + formattedTime;

            if (field != null) {
                field.setText(dateTime);
            }
        });

        timePicker.show(activity.getSupportFragmentManager(), "TIME_PICKER");
    }

    private void setupSpinners(Spinner importanceSpinner, Spinner urgencySpinner, Spinner recurrenceSpinner) {
        List<String> importanceList = Arrays.asList(activity.getResources().getStringArray(R.array.importance_array));
        List<String> urgencyList = Arrays.asList(activity.getResources().getStringArray(R.array.urgency_array));
        List<String> recurrenceList = Arrays.asList(activity.getResources().getStringArray(R.array.recurrence_array));

        CustomSpinnerAdapter importanceAdapter = new CustomSpinnerAdapter(activity, R.layout.item_spinner, importanceList);
        CustomSpinnerAdapter urgencyAdapter = new CustomSpinnerAdapter(activity, R.layout.item_spinner, urgencyList);
        CustomSpinnerAdapter recurrenceAdapter = new CustomSpinnerAdapter(activity, R.layout.item_spinner, recurrenceList);

        importanceSpinner.setAdapter(importanceAdapter);
        urgencySpinner.setAdapter(urgencyAdapter);
        recurrenceSpinner.setAdapter(recurrenceAdapter);
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

        if (urgency.equals("None")) {
            urgency = setAutomaticUrgency(deadline);
        }

        if (schedule.isEmpty()) {
            schedule = "No schedule";
        }

        if (recurrence.equals("Specific Days")) {
            recurrence = daysSelected;
        }

        if (!validateFields(editTaskName, deadline, schedule, currentDate, recurrence)) {
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

        return newTask;
    }

    public static String setAutomaticUrgency(String deadline) {
        Date taskDeadline = CalendarUtils.parseDeadline(deadline);
        Date currentDate = new Date();

        if (taskDeadline == null) {
            return "Not Urgent";
        } else {
            long diffInMillis = taskDeadline.getTime()-currentDate.getTime();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            Log.d("NUM_DAYS", String.valueOf(diffInDays));
            if (diffInDays >= 8) {
                return "Not Urgent";
            } else if (diffInDays >= 2) {
                return "Somewhat Urgent";
            } else if (diffInDays >= 1) {
                return "Urgent";
            } else {
                return "Very Urgent";
            }
        }
    }

    private boolean validateFields(EditText taskName, String deadline, String schedule, Date currentDate, String recurrence) {
        boolean validDeadline = true;
        boolean validSchedule = true;

        if (!isRecurrenceNone) {
            return true;
        }

        // Check if deadline is not empty and not earlier than current date
        if (!deadline.equals("No deadline")) {
            DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy | hh:mm a", Locale.getDefault());
            try {
                Date deadlineDate = dateFormat.parse(deadline);
                if (deadlineDate.before(currentDate)) {
                    validDeadline = false;
                }
            } catch (ParseException e) {
                Log.e("ParseException", "Error encountered: " + e);
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
                }
                if (!deadline.equals("No deadline")) {
                    // Check if schedule is later than deadline
                    Date deadlineDate = dateFormat.parse(deadline);
                    if (scheduleDate.after(deadlineDate)) {
                        validSchedule = false;
                    }
                }
            } catch (ParseException e) {
                Log.e("ParseException", "Error encountered: " + e);
                validSchedule = false;
            }
        }

        // Check required fields
        boolean validFields = true;
        if (taskName.getText().toString().trim().isEmpty()) {
            taskName.setError("Required field");
            validFields = false;
        }

        if (!recurrence.equals("None")){
            validSchedule = true;
        }

        if (!validFields || !validDeadline || !validSchedule) {
            if (!validFields) {
                Toast.makeText(activity, "Task name is required", Toast.LENGTH_SHORT).show();
            } else if (!validDeadline) {
                Toast.makeText(activity, "Deadline cannot be earlier than current date and time", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Schedule cannot be earlier than current date and time or later than deadline", Toast.LENGTH_SHORT).show();
            }
            return false;
        } else {
            return true;
        }
    }

    private static int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    private void getRecurrenceSpinnerValue(Spinner recurrenceSpinner, EditText deadline, EditText schedule) {
        recurrenceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Specific Days")) {
                    showDialogForCustomRecurrence(recurrenceSpinner, position);
                }

                if (!selectedItem.equals("None")) {
                    deadline.setEnabled(false);
                    deadline.setText("No deadline");
                    schedule.setText("09:00 AM");     // set default time for recurrence
                    isRecurrenceNone = false;       // condition to set schedule to time only
                } else {
                    deadline.setEnabled(true);
                    schedule.setText("No schedule");
                    isRecurrenceNone = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showDialogForCustomRecurrence(Spinner spinner, int pos) {
        final Dialog bottomSheetDialog = new Dialog(activity);
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
        dialogs.add(bottomSheetDialog);

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

    private boolean isAnyCheckBoxChecked(List<CheckBox> checkBoxes) {
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                return true;
            }
        }
        return false;
    }

    public void dismissDialogs() {
        for (Dialog dialog : dialogs) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
        // Clear the list of dialogs
        dialogs.clear();
    }
}
