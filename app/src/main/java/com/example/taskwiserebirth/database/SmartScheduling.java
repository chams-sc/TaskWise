package com.example.taskwiserebirth.database;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartScheduling {

    // TODO: not included but let it stay for now

    private static String tag = "SmartSched";
    public static void calculateSmartSchedule(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (isSmartSchedulingPossible(task)) {
                calculateAndSetSmartSchedule(task);
                Log.d(tag, task.getSmartSchedule());
            }
        }
    }

    private static void calculateAndSetSmartSchedule(Task task) {

    }

    private static boolean isSmartSchedulingPossible(Task task) {
        if (task.getDeadline().equals("No deadline"))
            return false;

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy | hh:mm a");
        Date deadlineDate;
        try {
            deadlineDate = formatter.parse(task.getDeadline());
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // Handle parsing error
        }

        Date currentDate = new Date();

        return !deadlineDate.before(currentDate)
                && !task.getDuration().isEmpty()
                && !task.getSchedule().isEmpty();
    }

    private static long parseDuration(String durationString) {
        // Regular expression pattern to match the input string
        Pattern pattern = Pattern.compile("(\\d+)\\s*(hr)?\\s*(and)?\\s*(\\d+)?\\s*(min)?");

        Matcher matcher = pattern.matcher(durationString);

        int hours = 0;
        int minutes = 0;

        // If the pattern matches the input string
        if (matcher.find()) {
            // Extract hours and minutes from the matched groups
            if (matcher.group(1) != null) {
                hours = Integer.parseInt(matcher.group(1));
            }
            if (matcher.group(4) != null) {
                minutes = Integer.parseInt(matcher.group(4));
            }
        }

        // Calculate total duration in milliseconds
        long totalMillis = hours * 60 * 60 * 1000 + minutes * 60 * 1000;

        return totalMillis;
    }
}
