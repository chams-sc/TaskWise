package com.example.taskwiserebirth.utils;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.taskwiserebirth.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class CalendarUtils {

    public static void displayTimeOfDay(View rootView) {
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

    public static List<Calendar> getDatesForCurrentMonth() {
        List<Calendar> calendarList = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();

        currentDate.set(Calendar.DAY_OF_MONTH, 1);

        int daysInMonth = currentDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int day = 1; day <= daysInMonth; day++) {
            Calendar date = (Calendar) currentDate.clone();
            date.set(Calendar.DAY_OF_MONTH, day);
            calendarList.add(date);
        }

        return calendarList;
    }

    public static int getCurrentDatePosition(List<Calendar> calendarList) {
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

    public static Date parseDeadline(String deadline) {
        if (deadline.equals("No deadline")) {
            return null;
        }

        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy | hh:mm a");
        try {
            return format.parse(deadline);
        } catch (ParseException e) {
            Log.e("ParseDate", String.valueOf(e));
            return null;
        }
    }

    /**
     * Checks if the given date is accepted based on current date.
     *
     * @param date The date string to be checked.
     * @return {@code true} if the given date is after the current date, {@code false} otherwise.
     */
    public static boolean isDateAccepted(String date) {
        Date currentDate = new Date();
        Date parsedDate = CalendarUtils.parseDeadline(date);
        return parsedDate != null && parsedDate.after(currentDate);
    }

    public static boolean isRecurrenceAccepted(String recurrence) {
        Pattern RECURRENCE_PATTERN = Pattern.compile("^(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)(,\\s*(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday))*$");

        return RECURRENCE_PATTERN.matcher(recurrence).matches();
    }

    public static String formatRecurrence(String value) {
        StringBuilder formattedRecurrence = new StringBuilder();
        Set<String> days = new HashSet<>(Arrays.asList(value.split(",\\s*")));

        for (String day : ValidValues.VALID_RECURRENCE_LEVELS) {
            if (days.contains(day)) {
                if (formattedRecurrence.length() > 0) {
                    formattedRecurrence.append(" | ");
                }
                formattedRecurrence.append(getAbbreviation(day));
            }
        }

        return formattedRecurrence.toString();
    }

    private static String getAbbreviation(String day) {
        switch (day) {
            case "Monday":
                return "M";
            case "Tuesday":
                return "T";
            case "Wednesday":
                return "W";
            case "Thursday":
                return "Th";
            case "Friday":
                return "F";
            case "Saturday":
                return "Sa";
            case "Sunday":
                return "Su";
            default:
                return "";
        }
    }

}
