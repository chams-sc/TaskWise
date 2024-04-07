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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.Database.MongoDbRealmHelper;
import com.example.taskwiserebirth.Database.Task;
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
import java.util.TimeZone;

import io.realm.mongodb.App;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;

public class AddTaskFragment extends Fragment {

    private RecyclerView recyclerView;
    private CalendarAdapter calendarAdapter;
    private ImageView calendarIcon;
    private String daysSelected = null;

    // Realm
    private App app;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_task, container, false);
        recyclerView = rootView.findViewById(R.id.tasksRecyclerView);

        // Set up RecyclerView for the calendar
        setUpCalendarRecyclerView(rootView);

        // Set up RecyclerView for the card items
        setUpCardRecyclerView(rootView);

        // Realm initialization
        app = MongoDbRealmHelper.initializeRealmApp();

        // Floating action button setup
        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(v -> showBottomSheetDialog());

        // Display the time of day
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
        List<Item> items = new ArrayList<>();
        items.add(new Item("William", "9:00 AM - 11:00 AM", "Low Prio"));
        items.add(new Item("Archie", "9:00 AM - 11:00 AM", "Low Priority"));
        items.add(new Item("test", "9:00 AM - 11:00 AM", "Medium Prio"));
        items.add(new Item("test", "9:00 AM - 11:00 AM", "High Prio"));
        items.add(new Item("test", "9:00 AM - 11:00 AM", "Very High Prio"));
        items.add(new Item("test", "9:00 AM - 11:00 AM", "Important"));
        items.add(new Item("test", "9:00 AM - 11:00 AM", "Very Important"));
        items.add(new Item("test", "9:00 AM - 11:00 AM", "Low Prio"));

        // Set up CardAdapter
        CardAdapter cardAdapter = new CardAdapter(requireContext(), items);

        // Set up RecyclerView with LinearLayoutManager
        cardRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set RecyclerView adapter with the items list
        cardRecyclerView.setAdapter(cardAdapter);
    }





    private void displayTimeOfDay(View rootView) {
        // Find the TextView for displaying time of day
        TextView timeOfDayTextView = rootView.findViewById(R.id.tasksText);
        // Find the ImageView for displaying the corresponding drawable
        ImageView timeOfDayImageView = rootView.findViewById(R.id.timeOfDayImageView);

        // Get the current time in Philippine Time (UTC+8:00)
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String am_pm;

        // Determine whether it's morning, noon, evening, or night
        String timeOfDay;
        int drawableResId;

        if (hourOfDay >= 6 && hourOfDay < 12) {
            timeOfDay = "Morning";
            am_pm = "AM";
            drawableResId = R.drawable.baseline_sunny;
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            timeOfDay = "Afternoon";
            am_pm = "PM";
            drawableResId = R.drawable.baseline_sunny;
        } else if (hourOfDay >= 18 && hourOfDay < 24) {
            timeOfDay = "Evening";
            am_pm = "PM";
            drawableResId = R.drawable.baseline_night;
        } else {
            timeOfDay = "Night";
            am_pm = "PM";
            drawableResId = R.drawable.baseline_night;
        }

        // Adjust hour to be in 12-hour format
        if (hourOfDay == 0) {
            hourOfDay = 12;
        } else if (hourOfDay > 12) {
            hourOfDay -= 12;
        }

        // Format the current time
        String formattedTime = String.format(Locale.getDefault(), "%d:%02d %s", hourOfDay, minute, am_pm);

        // Display the time of day and current time in the desired format
        timeOfDayTextView.setText(formattedTime + " " + timeOfDay);
        // Set the corresponding drawable
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

        calendarIcon = bottomSheetView.findViewById(R.id.calendarIcon);
        calendarIcon.setOnClickListener(v -> showDatePicker(bottomSheetDialog));

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

        Task task = new Task();
        task.setTaskName(editTaskName.getText().toString());
        task.setCreationDate(currentDate);
        task.setImportanceLevel(importanceSpinner.getSelectedItem().toString());
        task.setUrgencyLevel(urgencySpinner.getSelectedItem().toString());
        task.setDeadline(editDeadline.getText().toString());
        task.setDuration(editDuration.getText().toString());
        task.setRecurrence(recurrence);
        task.setSchedule(editSchedule.getText().toString());
        task.setReminder(reminderCheckbox.isChecked());
        task.setNotes(editNotes.getText().toString());

        return task;
    }

    private void insertTask(Task task) {
        User user = app.currentUser();
        if (user != null) {
            MongoCollection<Document> mongoCollection = MongoDbRealmHelper.getMongoCollection("UserTaskData");

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

            mongoCollection.insertOne(taskDocument).getAsync(result -> {
                if (result.isSuccess()) {
                    Log.d("Data", "Data Inserted Successfully");
                } else {
                    Log.e("Data", "Failed to insert data: " + result.getError().getMessage());
                }
            });
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
            showTimePicker(bottomSheetDialog, date);
        });
        materialDatePicker.show(requireActivity().getSupportFragmentManager(), "DATE_PICKER");
    }

    private void showTimePicker(final Dialog bottomSheetDialog, final String selectedDate) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setTitleText("Pick Time")
                .setTheme(R.style.TimePickerTheme)
                .build();
        timePicker.addOnPositiveButtonClickListener(v -> {
            int hourOfDay = timePicker.getHour();
            int minute = timePicker.getMinute();

            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s",
                    hourOfDay == 12 || hourOfDay == 0 ? 12 : hourOfDay % 12, minute, hourOfDay < 12 ? "AM" : "PM");

            String dateTime = selectedDate + " | " + formattedTime;

            EditText editTextDateTime = bottomSheetDialog.findViewById(R.id.deadline);
            if (editTextDateTime != null) {
                editTextDateTime.setText(dateTime);
            }
        });
        timePicker.show(requireActivity().getSupportFragmentManager(), "TIME_PICKER");
    }

}
