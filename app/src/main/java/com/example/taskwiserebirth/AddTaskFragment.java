package com.example.taskwiserebirth;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;

public class AddTaskFragment extends Fragment {

    private String appId = "taskwise-bxyah";
    private String dbTag = "MONGO_DB";
    private String taskTag = "TASK_DETAILS";
    private App app;
    private ImageView calendarIcon;
    private String daysSelected = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_task, container, false);

        // Realm
        app = new App(new AppConfiguration.Builder(appId).build());

        FloatingActionButton fab = rootView.findViewById(R.id.fab);

        fab.setOnClickListener(v -> showBottomSheetDialog());

        return rootView;
    }

    private void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.add_task, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // Find the Importance and Urgency spinners in the bottom sheet layout
        Spinner importanceSpinner = bottomSheetView.findViewById(R.id.importance);
        Spinner urgencySpinner = bottomSheetView.findViewById(R.id.urgency);
        Spinner recurrenceSpinner = bottomSheetView.findViewById(R.id.RecurrenceEditText);

        EditText editTaskName = bottomSheetDialog.findViewById(R.id.taskName);
        EditText editDeadline = bottomSheetDialog.findViewById(R.id.deadline);
        EditText editDuration = bottomSheetDialog.findViewById(R.id.duration);
        EditText editSchedule = bottomSheetDialog.findViewById(R.id.schedule);
        EditText editNotes = bottomSheetDialog.findViewById(R.id.notes);

        CheckBox reminderCheckbox = bottomSheetDialog.findViewById(R.id.reminder);
        boolean reminderChecked = reminderCheckbox.isChecked();

        Button saveBtn = bottomSheetDialog.findViewById(R.id.saveButton);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskName = editTaskName.getText().toString().trim();
                String deadline = editDeadline.getText().toString().trim();
                String duration = editDuration.getText().toString().trim();
                String schedule = editSchedule.getText().toString().trim();

                boolean allFieldsFilled = !taskName.isEmpty() &&
                        !deadline.isEmpty() &&
                        !duration.isEmpty() &&
                        !schedule.isEmpty() &&
                        importanceSpinner.getSelectedItemPosition() != 0 &&
                        urgencySpinner.getSelectedItemPosition() != 0;

                if (taskName.isEmpty()) {
                    showError(editTaskName);
                }
                if (deadline.isEmpty()) {
                    showError(editDeadline);
                }
                if (duration.isEmpty()) {
                    showError(editDuration);
                }
                if (schedule.isEmpty()) {
                    showError(editSchedule);
                }
                if (importanceSpinner.getSelectedItemPosition() == 0) {
                    showError(importanceSpinner);
                }
                if (urgencySpinner.getSelectedItemPosition() == 0) {
                    showError(urgencySpinner);
                }

                if (allFieldsFilled) {
                    Toast.makeText(requireContext(), "All fields filled", Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss(); // Dismiss the bottom sheet dialog after saving
                } else {
                    // Notify the user that all fields are required
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Create an ArrayAdapter for Importance spinner
        ArrayAdapter<CharSequence> importanceAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.importance_array, android.R.layout.simple_spinner_item);
        importanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Create an ArrayAdapter for Urgency spinner
        ArrayAdapter<CharSequence> urgencyAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.urgency_array, android.R.layout.simple_spinner_item);
        urgencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> recurrenceAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.recurrence_array, android.R.layout.simple_spinner_item);
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set adapters to the spinners
        importanceSpinner.setAdapter(importanceAdapter);
        urgencySpinner.setAdapter(urgencyAdapter);
        recurrenceSpinner.setAdapter(recurrenceAdapter);

        // Set the background of the dialog to transparent
        ((View) bottomSheetView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        bottomSheetDialog.show();

        calendarIcon = bottomSheetView.findViewById(R.id.calendarIcon);
        calendarIcon.setOnClickListener(v -> showDatePicker(bottomSheetDialog));

        // Set OnItemSelectedListener on the recurrence spinner
        recurrenceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Specific Days")) {
                    // Show your custom dialog here
                    showDialogForCustomRecurrence(recurrenceSpinner, position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle nothing selected if needed
            }
        });
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

            // Do something with the selected days list
            Log.d("SPECIFIC_DAYS", selectedDays.toString());

            // Update spinner's text
            spinner.setSelection(pos);
            ((TextView) spinner.getSelectedView()).setText(daysSelected);

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();

        // Adjust dialog position to center horizontally and vertically
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

            // Combine date and time with "|"
            String dateTime = selectedDate + " | " + formattedTime;

            // Update EditText with selected date and time
            EditText editTextDateTime = bottomSheetDialog.findViewById(R.id.deadline); // assuming you have access to EditText directly
            if (editTextDateTime != null) {
                editTextDateTime.setText(dateTime);
            }
        });
        timePicker.show(requireActivity().getSupportFragmentManager(), "TIME_PICKER");
    }
}