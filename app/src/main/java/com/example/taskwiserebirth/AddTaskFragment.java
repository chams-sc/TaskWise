package com.example.taskwiserebirth;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;

public class AddTaskFragment extends Fragment {

    private String appId = "taskwise-bxyah";
    private String tag = "MongoDb";
    private App app;
    private EditText editTextDate;
    private EditText editTextTime;
    private ImageView calendarIcon;
    private CheckBox checkBox;
    private Spinner spinner;
    private Button addButton;
    private List<String> selectedItems;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_task, container, false);

        // Realm
        app = new App(new AppConfiguration.Builder(appId).build());

        FloatingActionButton fab = rootView.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });

        return rootView;
    }

    private void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.add_task, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Adjust soft input mode to prevent interference with EditText
        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // Find the Importance and Urgency spinners in the bottom sheet layout
        Spinner importanceSpinner = bottomSheetView.findViewById(R.id.Importance1);
        Spinner urgencySpinner = bottomSheetView.findViewById(R.id.Urgency1);
        Spinner recurrenceSpinner = bottomSheetView.findViewById(R.id.RecurrenceEditText);

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
        calendarIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(bottomSheetDialog);
            }
        });

        // Set OnItemSelectedListener on the recurrence spinner
        recurrenceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Specific Days")) {
                    // Show your custom dialog here
                    showDialogForCustomRecurrence();
                    recurrenceSpinner.setSelection(2);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle nothing selected if needed
            }
        });
    }

    private void showDialogForCustomRecurrence() {
        final Dialog bottomSheetDialog = new Dialog(requireContext());
        bottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottomSheetDialog.setContentView(R.layout.recurrence_picker);

        Button setButton = bottomSheetDialog.findViewById(R.id.setBtn);

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> selectedDays = getSelectedDays(bottomSheetDialog);
                // Do something with the selected days list
                Log.d("SHIT", selectedDays.toString());
                bottomSheetDialog.dismiss();
            }
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
        String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

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
        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                String date = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date(selection));
                timePicker(bottomSheetDialog, date);
            }
        });
        materialDatePicker.show(requireActivity().getSupportFragmentManager(), "DATE_PICKER");
    }

    private void showTimePicker(final Dialog bottomSheetDialog, final String selectedDate) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), R.style.TimePickerTheme,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Format the selected time in 12-hour format
                        String formattedTime;
                        if (hourOfDay >= 12) {
                            formattedTime = String.format(Locale.getDefault(), "%02d:%02d PM", hourOfDay == 12 ? 12 : hourOfDay - 12, minute);
                        } else {
                            formattedTime = String.format(Locale.getDefault(), "%02d:%02d AM", hourOfDay == 0 ? 12 : hourOfDay, minute);
                        }

                        // Combine date and time with "|"
                        String dateTime = selectedDate + " | " + formattedTime;

                        // Update EditText with selected date and time
                        EditText editTextDateTime = bottomSheetDialog.findViewById(R.id.DeadlineEditText);
                        if (editTextDateTime != null) {
                            editTextDateTime.setText(dateTime);
                        }
                    }
                }, hour, minute, false);

        timePickerDialog.show();
    }

    private void timePicker(final Dialog bottomSheetDialog, final String selectedDate) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setTitleText("Pick Time")
                .setTheme(R.style.TimePickerTheme)
                .build();
        timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hourOfDay = timePicker.getHour();
                int minute = timePicker.getMinute();

                // Format the selected time in 12-hour format
                String formattedTime;
                if (hourOfDay >= 12) {
                    formattedTime = String.format(Locale.getDefault(), "%02d:%02d PM", hourOfDay == 12 ? 12 : hourOfDay - 12, minute);
                } else {
                    formattedTime = String.format(Locale.getDefault(), "%02d:%02d AM", hourOfDay == 0 ? 12 : hourOfDay, minute);
                }

                // Combine date and time with "|"
                String dateTime = selectedDate + " | " + formattedTime;

                // Update EditText with selected date and time
                EditText editTextDateTime = bottomSheetDialog.findViewById(R.id.DeadlineEditText); // assuming you have access to EditText directly
                if (editTextDateTime != null) {
                    editTextDateTime.setText(dateTime);
                }
            }
        });
        timePicker.show(requireActivity().getSupportFragmentManager(), "TIME_PICKER");
    }
}