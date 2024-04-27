package com.example.taskwiserebirth.utils;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.taskwiserebirth.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
}
