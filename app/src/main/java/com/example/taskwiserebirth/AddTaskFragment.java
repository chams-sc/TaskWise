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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.database.DatabaseChangeListener;
import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.database.Task;
import com.example.taskwiserebirth.database.TaskAdapter;
import com.example.taskwiserebirth.database.TaskPriorityCalculator;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import org.bson.Document;

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

public class AddTaskFragment extends Fragment implements DatabaseChangeListener, NestedScrollView.OnScrollChangeListener {

    // TODO: Please fix naming conventions, taskRecyclerView but the contents is the calendar. cardRecyclerView should be the taskRecyclerView.
    private RecyclerView recyclerView;
    private CalendarAdapter calendarAdapter;
    private ImageView calendarIcon;
    private EditText duration;
    private EditText deadline;
    private String daysSelected = null;
    private TaskAdapter taskAdapter;
    private View rootView;


    // Realm
    private App app;
    private User user;
    private MongoCollection<Document> taskCollection;
    private final String TAG = "MongoDb";
    private View bottomNavigationView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_task2, container, false);

        // Realm initialization
        app = MongoDbRealmHelper.initializeRealmApp();
        user = app.currentUser();
        MongoDbRealmHelper.addDatabaseChangeListener(this);
        taskCollection = MongoDbRealmHelper.getMongoCollection("UserTaskData");

        recyclerView = rootView.findViewById(R.id.tasksRecyclerView);

        NestedScrollView nestedScrollView = rootView.findViewById(R.id.nestedScrollView);
        nestedScrollView.setOnScrollChangeListener(this);

        bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);

        setUpCalendarRecyclerView(rootView);
        setUpCardRecyclerView(rootView);

        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(v -> showBottomSheetDialog());

        displayTimeOfDay(rootView);

        return rootView;
    }

    private void setUpCalendarRecyclerView(View rootView) {
        RecyclerView calendarRecyclerView = rootView.findViewById(R.id.tasksRecyclerView);

        // Get list of calendar dates
        List<Calendar> calendarList = getDatesForCurrentMonth();

        // Set up CalendarAdapter
        CalendarAdapter calendarAdapter = new CalendarAdapter(calendarList, date -> {
            // Handle calendar item click if needed
        });

        // Set up RecyclerView with LinearLayoutManager
        calendarRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Set RecyclerView adapter
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private void setUpCardRecyclerView(View rootView) {
        RecyclerView cardRecyclerView = rootView.findViewById(R.id.Cardrecyclerview1);

        // Dummy data for card items
        List<Task> tasks = new ArrayList<>();

        // Set up CardAdapter
        taskAdapter = new TaskAdapter(requireContext(), tasks);

        // Set up RecyclerView with LinearLayoutManager
        cardRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set RecyclerView adapter with the items list
        cardRecyclerView.setAdapter(taskAdapter);

        updateRecyclerView();
    }

    private void updateRecyclerView() {
        Document queryFilter = new Document("owner_id", user.getId());
        RealmResultTask<MongoCursor<Document>> findTask = taskCollection.find(queryFilter).iterator();
        findTask.getAsync(task -> {
            if (task.isSuccess()) {
                MongoCursor<Document> results = task.get();
                List<Task> tasks = new ArrayList<>();
                String priorityLevel = "";

                while (results.hasNext()) {
                    Document document = results.next();
                    // TODO: update with priority once implemented
                    String taskName = document.getString("task_name");
                    String importanceLevel = document.getString("importance_level");
                    String urgencyLevel = document.getString("urgency_level");

                    String deadlineString = document.getString("deadline");

                    Task newTask = new Task(taskName, deadlineString, importanceLevel, urgencyLevel, priorityLevel);
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
        } else if (hourOfDay >= 18 && hourOfDay < 24) {
            timeOfDay = "Evening";
            drawableResId = R.drawable.baseline_night2;
        } else {
            timeOfDay = "Night";
            drawableResId = R.drawable.baseline_night2;
        }

        timeOfDayTextView.setText(timeOfDay);
        timeOfDayImageView.setImageResource(drawableResId);
    }


    private void setUpRecyclerView() {
        List<Calendar> calendarList = getDatesForCurrentMonth();
        calendarAdapter = new CalendarAdapter(calendarList, date -> {
            // Handle item click if needed
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(calendarAdapter);

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

    private void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.add_task, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setUpViews(bottomSheetDialog, bottomSheetView);

        bottomSheetDialog.show();
    }

    private void setUpViews(BottomSheetDialog bottomSheetDialog, View bottomSheetView) {
        Spinner importanceSpinner = bottomSheetView.findViewById(R.id.importance);
        Spinner urgencySpinner = bottomSheetView.findViewById(R.id.urgency);
        Spinner recurrenceSpinner = bottomSheetView.findViewById(R.id.recurrence);

        Button saveBtn = bottomSheetView.findViewById(R.id.saveButton);
        saveBtn.setOnClickListener(v -> {
            if (validateFields(bottomSheetDialog)) {
                Task task = createTaskFromFields(bottomSheetDialog);
                insertTask(task);
                bottomSheetDialog.dismiss();
            }
            daysSelected = null;
        });

        setupSpinners(importanceSpinner, urgencySpinner, recurrenceSpinner);

        ((View) bottomSheetView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        deadline = bottomSheetView.findViewById(R.id.deadline);
        deadline.setOnClickListener(v -> showDatePicker(bottomSheetDialog));

        duration = bottomSheetDialog.findViewById(R.id.duration);
        duration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(bottomSheetDialog, null, true);
            }
        });

        setupRecurrenceSpinner(recurrenceSpinner);
    }

    private Task createTaskFromFields(Dialog bottomSheetDialog) {
        EditText editTaskName = bottomSheetDialog.findViewById(R.id.taskName);
        EditText editDeadline = bottomSheetDialog.findViewById(R.id.deadline);
        EditText editDuration = bottomSheetDialog.findViewById(R.id.duration);
        EditText editSchedule = bottomSheetDialog.findViewById(R.id.schedule);
        EditText editNotes = bottomSheetDialog.findViewById(R.id.notes);

        Spinner importanceSpinner = bottomSheetDialog.findViewById(R.id.importance);
        Spinner urgencySpinner = bottomSheetDialog.findViewById(R.id.urgency);
        Spinner recurrenceSpinner = bottomSheetDialog.findViewById(R.id.recurrence);

        CheckBox reminderCheckbox = bottomSheetDialog.findViewById(R.id.reminder);

        String recurrence = recurrenceSpinner.getSelectedItem().toString();
        Date currentDate = new Date(); // UTC time

        if (recurrence.equals("Specific Days")) {
            recurrence = daysSelected;
        }

        // Set No deadline if deadline is empty
        String deadline = editDeadline.getText().toString().trim();
        if (deadline.isEmpty()) {
            deadline = "No deadline";
        }

        Task task = new Task();
        task.setTaskName(editTaskName.getText().toString());
        task.setCreationDate(currentDate);
        task.setImportanceLevel(importanceSpinner.getSelectedItem().toString());
        task.setUrgencyLevel(urgencySpinner.getSelectedItem().toString());
        task.setDeadline(deadline);
        task.setDuration(editDuration.getText().toString());
        task.setRecurrence(recurrence);
        task.setSchedule(editSchedule.getText().toString());
        task.setReminder(reminderCheckbox.isChecked());
        task.setNotes(editNotes.getText().toString());

        return task;
    }

    private void insertTask(Task task) {
        if (user != null) {
            Document taskDocument = new Document("owner_id", user.getId())
                    .append("task_name", task.getTaskName())
                    .append("importance_level", task.getImportanceLevel())
                    .append("urgency_level", task.getUrgencyLevel())
                    .append("deadline", task.getDeadline())
                    .append("duration", task.getDuration())
                    .append("schedule", task.getSchedule())
                    .append("notes", task.getNotes())
                    .append("reminder", task.isReminder())
                    .append("notes", task.getNotes())
                    .append("creation_date", task.getCreationDate());

            taskCollection.insertOne(taskDocument).getAsync(result -> {
                if (result.isSuccess()) {
                    Log.d("Data", "Data Inserted Successfully");
                    MongoDbRealmHelper.notifyDatabaseChangeListeners();
                } else {
                    Log.e("Data", "Failed to insert data: " + result.getError().getMessage());
                }
            });
        }
    }

    private boolean validateFields(Dialog bottomSheetDialog) {
        EditText editTaskName = bottomSheetDialog.findViewById(R.id.taskName);
        Spinner importanceSpinner = bottomSheetDialog.findViewById(R.id.importance);
        Spinner urgencySpinner = bottomSheetDialog.findViewById(R.id.urgency);

        List<View> fieldsToValidate = Arrays.asList(editTaskName, importanceSpinner, urgencySpinner);
        boolean allFieldsFilled = true;
        for (View field : fieldsToValidate) {
            if (field instanceof EditText && ((EditText) field).getText().toString().trim().isEmpty()) {
                showError(field);
                allFieldsFilled = false;
            } else if (field instanceof Spinner && ((Spinner) field).getSelectedItemPosition() == 0) {
                showError(field);
                allFieldsFilled = false;
            }
        }

        if (allFieldsFilled) {
            Toast.makeText(requireContext(), "All fields are filled", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(requireContext(), "Missing required fields", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void setupRecurrenceSpinner(Spinner recurrenceSpinner) {
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
        ArrayAdapter<CharSequence> importanceAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.importance_array, android.R.layout.simple_spinner_item);
        importanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> urgencyAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.urgency_array, android.R.layout.simple_spinner_item);
        urgencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> recurrenceAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.recurrence_array, android.R.layout.simple_spinner_item);
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

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
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                setButton.setEnabled(isAnyCheckBoxChecked(checkBoxes));
            });
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
            ((TextView) spinner.getSelectedView()).setText(daysSelected);

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

    private void showDatePicker(final Dialog bottomSheetDialog) {
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setTheme(R.style.DatePickerTheme)
                .build();
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            String date = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date(selection));
            showTimePicker(bottomSheetDialog, date, false);
        });
        materialDatePicker.show(requireActivity().getSupportFragmentManager(), "DATE_PICKER");
    }

    private void showTimePicker(final Dialog bottomSheetDialog, final String selectedDate, final boolean isDuration) {
        MaterialTimePicker.Builder timePickerBuilder = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setTitleText("Pick Time")
                .setTheme(R.style.TimePickerTheme);

        if (isDuration) {
            timePickerBuilder.setTimeFormat(TimeFormat.CLOCK_24H)
                    .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                    .setTitleText("Task Duration");
        }

        MaterialTimePicker timePicker = timePickerBuilder.build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            int hourOfDay = timePicker.getHour();
            int minute = timePicker.getMinute();

            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s",
                    hourOfDay == 12 || hourOfDay == 0 ? 12 : hourOfDay % 12, minute, hourOfDay < 12 ? "AM" : "PM");

            String dateTime = selectedDate + " | " + formattedTime;

            if (!isDuration) {
                deadline.setText(dateTime);
            } else {
                if (hourOfDay == 0) {
                    formattedTime = String.format(Locale.getDefault(), "%d min", minute);
                } else if (minute == 0) {
                    formattedTime = String.format(Locale.getDefault(), "%d hr", hourOfDay);
                } else {
                    formattedTime = String.format(Locale.getDefault(), "%d hr and %d min", hourOfDay, minute);
                }
                duration.setText(formattedTime);
            }
        });

        timePicker.show(requireActivity().getSupportFragmentManager(), "TIME_PICKER");
    }


    private void showError(View view) {
        String message = "Required field";

        if (view instanceof EditText) {
            ((EditText) view).setError(message);
        } else if (view instanceof Spinner) {
            View selectedView = ((Spinner) view).getSelectedView();
            if (selectedView instanceof TextView) {
                ((TextView) selectedView).setError(message);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister this fragment as a listener
        MongoDbRealmHelper.removeDatabaseChangeListener(this);
    }


    @Override
    public void onDatabaseChange() {
        // Update RecyclerView whenever database changes
        updateRecyclerView();
    }


    @Override
    public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        // If scrolling up visibility = true; else false
        boolean scrollingUp = scrollY < oldScrollY;
        ((HomeActivity) requireActivity()).toggleNavBarVisibility(scrollingUp, true);
    }
}
