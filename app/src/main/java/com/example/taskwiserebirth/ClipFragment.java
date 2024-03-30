package com.example.taskwiserebirth;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
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
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ClipFragment extends Fragment {

    private RecyclerView recyclerView;
    private CalendarAdapter calendarAdapter;
    private TextView selectedDateText;
    private EditText editTextDateTime;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_clip, container, false);
        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        recyclerView = rootView.findViewById(R.id.calendarRecyclerView);


        // Set up RecyclerView for the calendar
        setUpRecyclerView();

        // Display current time and time of day
        displayTimeOfDay(rootView);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });

        return rootView;
    }

    private void setUpRecyclerView() {
        List<Calendar> calendarList = getDatesInRange(-1, 1); // Get dates for one month before and after today
        calendarAdapter = new CalendarAdapter(calendarList, new CalendarAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Calendar date) {
                updateSelectedDateText(date);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(calendarAdapter);
    }

    private List<Calendar> getDatesInRange(int monthsBefore, int monthsAfter) {
        List<Calendar> calendarList = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();

        for (int i = monthsBefore; i <= monthsAfter; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, i);
            calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to first day of the month
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int day = 1; day <= daysInMonth; day++) {
                Calendar date = (Calendar) calendar.clone();
                date.set(Calendar.DAY_OF_MONTH, day);
                calendarList.add(date);
            }
        }

        return calendarList;
    }

    private void updateSelectedDateText(Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault());
        selectedDateText.setText(sdf.format(date.getTime()));
    }

    private void displayTimeOfDay(View rootView) {
        // Find the TextView for displaying time of day
        TextView timeOfDayTextView = rootView.findViewById(R.id.timeOfDayTextView);

        // Get the current time in Philippine Time (UTC+8:00)
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String am_pm;

        // Determine whether it's morning or evening
        String timeOfDay;
        if (hourOfDay >= 6 && hourOfDay < 12) {
            timeOfDay = "Morning";
            am_pm = "AM";
        } else {
            timeOfDay = "Evening";
            am_pm = "PM";
            // Convert to 12-hour format
            if (hourOfDay >= 12) {
                hourOfDay -= 12;
            }
        }

        // Adjust hour to be in 12-hour format
        if (hourOfDay == 0) {
            hourOfDay = 12;
        }

        // Format the current time
        String formattedTime = String.format(Locale.getDefault(), "%d:%02d %s", hourOfDay, minute, am_pm);

        // Display the time of day and current time in the desired format
        timeOfDayTextView.setText(formattedTime + " " + timeOfDay);

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

        // Show the dialog
        bottomSheetDialog.show();

        // Find the ImageView button in the bottom sheet layout
        ImageView imageViewButton = bottomSheetView.findViewById(R.id.imageView3);

        // Set OnClickListener on the ImageView button
        imageViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show DatePickerDialog when the ImageView button is clicked
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

    private void showDatePicker(final Dialog bottomSheetDialog) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Format the selected date
                        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year);

                        // Show TimePickerDialog after setting the date
                        showTimePicker(bottomSheetDialog, formattedDate);
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private void showTimePicker(final Dialog bottomSheetDialog, final String selectedDate) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
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
                        editTextDateTime = bottomSheetDialog.findViewById(R.id.DeadlineEditText);
                        if (editTextDateTime != null) {
                            editTextDateTime.setText(dateTime);
                        }
                    }
                }, hour, minute, false);

        timePickerDialog.show();
    }
}
