package com.example.taskwiserebirth.utils;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.taskwiserebirth.R;
import com.example.taskwiserebirth.task.TaskModel;
import com.example.taskwiserebirth.task.TaskPriorityCalculator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class CalendarUtils {

    private static String TAG_CALENDAR_UTILS = "CalendarUtils";
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

    public static String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    public static Date parseDeadline(String deadline) {
        if (deadline.equals("No deadline")) {
            return null;
        }

        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy | hh:mm a");
        try {
            return format.parse(deadline);
        } catch (ParseException e) {
            Log.e(TAG_CALENDAR_UTILS, String.valueOf(e));
            return null;
        }
    }


    public static boolean isDateInCorrectFormat(String input) {
        // Define the format pattern
        String formatPattern = "^\\d{2}-\\d{2}-\\d{4} \\| \\d{2}:\\d{2} (AM|PM)$";
        // Check if the input matches the pattern
        return Pattern.matches(formatPattern, input);
    }

    /**
     * Checks if the given date is accepted based on current date.
     *
     * @param date The date string to be checked.
     * @return {@code true} if the given date is after the current date, {@code false} otherwise.
     */
    public static boolean isDateAccepted(String date) {
        Log.v("CalendarUtils", "isDateAccepted running for date: " + date);
        Date currentDate = new Date();
        Date parsedDate = CalendarUtils.parseDeadline(date);
        Log.v("CalendarUtils", "Current date: " + currentDate + ", Parsed date: " + parsedDate);
        return parsedDate != null && parsedDate.after(currentDate);
    }

    public static boolean isDateAccepted(String schedule, String deadline) {
        Date scheduleDate = CalendarUtils.parseDeadline(schedule);
        Date deadlineDate = CalendarUtils.parseDeadline(deadline);
        return scheduleDate != null && deadlineDate != null && !scheduleDate.after(deadlineDate);
    }

    public static boolean isRecurrenceAccepted(String recurrence) {
        Pattern RECURRENCE_PATTERN = Pattern.compile("^(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)(,\\s*(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday))*$");

        return RECURRENCE_PATTERN.matcher(recurrence).matches();
    }

    public static String formatRecurrence(String value) {
        StringBuilder formattedRecurrence = new StringBuilder();
        Set<String> days = new HashSet<>(Arrays.asList(value.split(",\\s*")));

        // List of days in the correct order
        List<String> orderedDays = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");

        for (String day : orderedDays) {
            if (days.contains(day)) {
                if (formattedRecurrence.length() > 0) {
                    formattedRecurrence.append(" | ");
                }
                formattedRecurrence.append(getDayAbbreviation(day));
            }
        }

        for (String day : days) {
            if (!orderedDays.contains(day)) {
                Log.e(TAG_CALENDAR_UTILS, "Invalid day in recurrence: " + day);
            }
        }

        return formattedRecurrence.toString();
    }

    public static long findNextRecurrence(TaskModel task) {
        String recurrence = task.getRecurrence();

        String[] scheduleTimeParts = task.getSchedule().split("\\s+");

        // Extract hour, minute, and AM/PM from schedule time
        String[] timeParts = scheduleTimeParts[0].split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        String amPm = scheduleTimeParts[1];

        // Adjust hour for 12-hour format
        if (amPm.equalsIgnoreCase("PM") && hour < 12) {
            hour += 12;
        } else if (amPm.equalsIgnoreCase("AM") && hour == 12) {
            hour = 0;
        }

        // Create Calendar instance for the next alarm time
        Calendar nextAlarmTime = Calendar.getInstance();
        nextAlarmTime.set(Calendar.HOUR_OF_DAY, hour);
        nextAlarmTime.set(Calendar.MINUTE, minute);
        nextAlarmTime.set(Calendar.SECOND, 0);
        nextAlarmTime.set(Calendar.MILLISECOND, 0);

        // Check if the scheduled time for today has already passed
        if (nextAlarmTime.getTimeInMillis() <= System.currentTimeMillis()) {
            // Move to the next day
            nextAlarmTime.add(Calendar.DATE, 1);
        }

        // Handle daily recurrence
        if (recurrence.equalsIgnoreCase("daily")) {
            // Return the time in milliseconds until the next occurrence
            return nextAlarmTime.getTimeInMillis() - System.currentTimeMillis();
        }

        String[] recurrenceDays = recurrence.split("\\s*\\|\\s*");
        // Find the next occurring day in the recurrence
        while (true) {
            int today = nextAlarmTime.get(Calendar.DAY_OF_WEEK);
            for (String day : recurrenceDays) {
                int dayOfWeek = getDayOfWeek(day.trim());
                if (today == dayOfWeek) {
                    // Set the time to the next occurrence day at the specified time
                    return nextAlarmTime.getTimeInMillis() - System.currentTimeMillis();
                }
            }
            // Move to the next day
            nextAlarmTime.add(Calendar.DATE, 1);
        }
    }

    public static long calculateScheduleTime(String schedule) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy | hh:mm a", Locale.getDefault());
            Date scheduledDate = dateFormat.parse(schedule);

            if (scheduledDate.getTime() <= System.currentTimeMillis()) {
                return 0;
            }

            return scheduledDate.getTime() - System.currentTimeMillis();
        } catch (ParseException e) {
            Log.e(TAG_CALENDAR_UTILS, String.valueOf(e));
        }
        return 0;
    }

    public static long calculateDefaultInterval() {
        // Default interval is every Saturday at 9:00 am
        // Get the current time
        Calendar now = Calendar.getInstance();

        // Check if today is Saturday and has not yet passed 9 AM
        if (now.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY &&
                now.get(Calendar.HOUR_OF_DAY) < 9) {
            // Calculate the interval until 9 AM today
            Calendar today9AM = (Calendar) now.clone();
            today9AM.set(Calendar.HOUR_OF_DAY, 9);
            today9AM.set(Calendar.MINUTE, 0);
            today9AM.set(Calendar.SECOND, 0);
            today9AM.set(Calendar.MILLISECOND, 0);

            return today9AM.getTimeInMillis() - now.getTimeInMillis();
        } else {
            // Calculate the day difference until next Saturday
            int daysUntilSaturday = Calendar.SATURDAY - now.get(Calendar.DAY_OF_WEEK);
            if (daysUntilSaturday <= 0) {
                daysUntilSaturday += 7; // If today is Saturday, add 7 to go to the next Saturday
            }

            // Set the time to Saturday 9 AM
            Calendar nextSaturday = (Calendar) now.clone();
            nextSaturday.add(Calendar.DAY_OF_YEAR, daysUntilSaturday);
            nextSaturday.set(Calendar.HOUR_OF_DAY, 9);
            nextSaturday.set(Calendar.MINUTE, 0);
            nextSaturday.set(Calendar.SECOND, 0);
            nextSaturday.set(Calendar.MILLISECOND, 0);

            // Calculate the interval between current time and next Saturday 9 AM
            return nextSaturday.getTimeInMillis() - now.getTimeInMillis();
        }
    }

    public static long calculateDailyInterval(String schedule) {
        // Parse the schedule time in the format "09:00 PM" or "08:00 AM"
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        try {
            Date time = dateFormat.parse(schedule);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);

            // Get current time
            Calendar now = Calendar.getInstance();

            // Set alarm time for today
            calendar.set(Calendar.YEAR, now.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, now.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

            // Check if alarm time is in the past, if so, set it for tomorrow
            if (calendar.before(now)) {
                calendar.add(Calendar.DATE, 1);
            }

            // Calculate interval between current time and alarm time
            return calendar.getTimeInMillis() - now.getTimeInMillis();
        } catch (ParseException e) {
            Log.e(TAG_CALENDAR_UTILS, e.getMessage());
            return 0;
        }
    }

    public static long calculateCloseToDueInterval(String deadlineString, int hoursBeforeDeadline) {
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy | hh:mm a");
        try {
            Date deadline = dateFormat.parse(deadlineString);

            // Subtract 12 hours from the deadline
            Calendar calendar = Calendar.getInstance();
            if (deadline != null) {
                calendar.setTime(deadline);
            }
            calendar.add(Calendar.HOUR_OF_DAY, -hoursBeforeDeadline);

            // Calculate the difference between the adjusted deadline time and the current time
            long timeDifferenceMillis = calendar.getTimeInMillis() - System.currentTimeMillis();

            // If the time difference is negative, set the alarm for now
            if (timeDifferenceMillis < 0) {
                timeDifferenceMillis = 0;
            }

            return timeDifferenceMillis;
        } catch (ParseException e) {
            Log.e(TAG_CALENDAR_UTILS, "Error parsing deadline: " + deadlineString, e);
        }

        // Default interval (1 minute) if parsing fails or deadline is not provided
        return 60000;
    }

    public static long calculatePastDueInterval() {
        Calendar nextAlarmTime = Calendar.getInstance();
        nextAlarmTime.set(Calendar.HOUR_OF_DAY, 8);
        nextAlarmTime.set(Calendar.MINUTE, 0);
        nextAlarmTime.set(Calendar.SECOND, 0);
        nextAlarmTime.set(Calendar.MILLISECOND, 0);

        // Check if the time has already passed for today, and set for tomorrow if it has
        if (nextAlarmTime.getTimeInMillis() <= System.currentTimeMillis()) {
            nextAlarmTime.add(Calendar.DATE, 1);
        }

        return nextAlarmTime.getTimeInMillis() - System.currentTimeMillis();
    }

    /**
     * Calculates the interval in milliseconds until a reminder should be triggered for a task
     * based on its priority and deadline.
     *
     * The reminder time is determined as a fraction of the remaining time until the deadline.
     * Higher priority tasks will have reminders set sooner relative to their deadlines.
     *
     * @param task The task for which to calculate the reminder interval.
     * @return The interval in milliseconds until the reminder should be triggered.
     */
    public static long calculateCloseToDueInterval(TaskModel task) {
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy | hh:mm a");
        try {
            Date deadline = dateFormat.parse(task.getDeadline());
            if (deadline == null) {
                return 60000; // Handle case where deadline is not provided or incorrect
            }

            // Get the priority category and fraction
            String priorityCategory = TaskPriorityCalculator.findPriorityCategory(task.getUrgencyLevel(), task.getImportanceLevel());
            double priorityFraction = TaskPriorityCalculator.getPriorityFraction(priorityCategory);

            // Calculate the total remaining time in milliseconds
            long totalRemainingTime = deadline.getTime() - System.currentTimeMillis();

            // Calculate the time interval based on the priority fraction
            long reminderTime = (long) (totalRemainingTime * priorityFraction);

            // Calculate the trigger time for the reminder
            long triggerAtMillis = deadline.getTime() - reminderTime;

            // Calculate the time difference between now and the trigger time
            long timeDifferenceMillis = triggerAtMillis - System.currentTimeMillis();

            // If the time difference is negative, set the alarm for now
            if (timeDifferenceMillis < 0) {
                timeDifferenceMillis = 0;
            }

            return timeDifferenceMillis;
        } catch (ParseException e) {
            Log.e(TAG_CALENDAR_UTILS, "Error parsing deadline: " + task.getDeadline(), e);
        }

        // Default interval (1 minute) if parsing fails or deadline is not provided
        return 60000;
    }

    // for task detail fragment ui
    public static String formatDeadline(String deadlineString) {
        if (!deadlineString.equals("No deadline")) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy | hh:mm a", Locale.getDefault());
                Date date = inputFormat.parse(deadlineString);

                SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, MMM d\nhh:mm a", Locale.getDefault());
                return outputFormat.format(date);
            } catch (ParseException e) {
                Log.e(TAG_CALENDAR_UTILS, e.getMessage());
                return deadlineString;
            }
        } else {
            return deadlineString;
        }
    }

    /**
     * Formats a date string from "MM-DD-YYYY | hh:mm AM/PM" to "MMMM dd, yyyy hh:mm a".
     *
     * @param dateString the input date string in the format "MM-DD-YYYY | hh:mm AM/PM"
     * @return the formatted date string in the format "MMMM dd, yyyy hh:mm a"
     * @throws ParseException if the input date string cannot be parsed
     */
    public static String formatDateString(String dateString) {
        // Define the input and output date formats
        SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy | hh:mm a", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.ENGLISH);

        // Parse the input date string to a Date object
        Date date = null;
        try {
            date = inputFormat.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // Format the Date object to the desired output format
        return outputFormat.format(date);
    }

    /**
     * Formats a date string from "MM-DD-YYYY | hh:mm AM/PM" to "MMMM dd, yyyy".
     *
     * @param dateString the input date string in the format "MM-DD-YYYY | hh:mm AM/PM"
     * @return the formatted date string in the format "MMMM dd, yyyy"
     * @throws ParseException if the input date string cannot be parsed
     */
    public static String getFormattedDate(String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy | hh:mm a", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);

        Date date = null;
        try {
            date = inputFormat.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return outputFormat.format(date);
    }

    /**
     * Extracts the time part from a date string in the format "MM-DD-YYYY | hh:mm AM/PM".
     *
     * @param dateString the input date string in the format "MM-DD-YYYY | hh:mm AM/PM"
     * @return the time part of the date string in the format "hh:mm a"
     * @throws ParseException if the input date string cannot be parsed
     */
    public static String getFormattedTime(String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy | hh:mm a", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

        Date date = null;
        try {
            date = inputFormat.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return outputFormat.format(date);
    }


    public static String formatCustomDeadline(Date date) {
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, 'at' hh:mm a", Locale.US);
        return outputFormat.format(date);
    }

    public static int getDayOfWeek(String day) {
        switch (day) {
            case "Su":
                return Calendar.SUNDAY;
            case "M":
                return Calendar.MONDAY;
            case "Tu":
                return Calendar.TUESDAY;
            case "W":
                return Calendar.WEDNESDAY;
            case "Th":
                return Calendar.THURSDAY;
            case "F":
                return Calendar.FRIDAY;
            case "Sa":
                return Calendar.SATURDAY;
            default:
                throw new IllegalArgumentException("Invalid day of week: " + day);
        }
    }

    public static String getDayName(String dayAbbreviation) {
        switch (dayAbbreviation) {
            case "Su":
                return "Sunday";
            case "M":
                return "Monday";
            case "Tu":
                return "Tuesday";
            case "W":
                return "Wednesday";
            case "Th":
                return "Thursday";
            case "F":
                return "Friday";
            case "Sa":
                return "Saturday";
            default:
                throw new IllegalArgumentException("Invalid day of week: " + dayAbbreviation);
        }
    }

    public static String convertRecurrenceToFullDayNames(String recurrence) {
        if (recurrence == null || recurrence.isEmpty()) {
            return "";
        }

        // Split the recurrence string by " | "
        String[] dayAbbreviations = recurrence.split(" \\| ");
        List<String> fullDayNames = new ArrayList<>();

        // Convert each abbreviation to the full day name
        for (String abbreviation : dayAbbreviations) {
            fullDayNames.add(getDayName(abbreviation));
        }

        // Join the full day names with " | "
        return String.join(" | ", fullDayNames);
    }

    public static String convertRecurrenceToDayNames(String recurrence) {
        if (recurrence == null || recurrence.isEmpty()) {
            return "";
        }

        // Split the recurrence string by " | "
        String[] dayAbbreviations = recurrence.split(" \\| ");
        List<String> fullDayNames = new ArrayList<>();

        // Convert each abbreviation to the full day name
        for (String abbreviation : dayAbbreviations) {
            fullDayNames.add(getDayName(abbreviation.trim()));
        }

        // Join the full day names with commas and "and"
        return formatDaysList(fullDayNames);
    }

    private static String formatDaysList(List<String> days) {
        if (days.size() == 1) {
            return days.get(0);
        }

        StringBuilder formattedRecurrence = new StringBuilder();
        for (int i = 0; i < days.size(); i++) {
            if (i == days.size() - 1) {
                formattedRecurrence.append("and ").append(days.get(i));
            } else {
                formattedRecurrence.append(days.get(i)).append(", ");
            }
        }

        return formattedRecurrence.toString().replace(", and", " and");
    }

    public static String getDayAbbreviation(String day) {
        switch (day) {
            case "Monday":
                return "M";
            case "Tuesday":
                return "Tu";
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

    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static String formatTimestamp(Date date) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        SimpleDateFormat sameWeekFormat = new SimpleDateFormat("EEEE 'at' hh:mm a", Locale.getDefault());
        SimpleDateFormat sameYearFormat = new SimpleDateFormat("MMM dd 'at' hh:mm a", Locale.getDefault());
        SimpleDateFormat differentYearFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

        Calendar current = Calendar.getInstance();
        Calendar messageDate = Calendar.getInstance();
        messageDate.setTime(date);

        // Check if the message is from today
        if (isSameDay(current, messageDate)) {
            return "Today at " + timeFormat.format(date);
        }

        // Check if the message is from the same week
        if (isSameWeek(current, messageDate)) {
            return sameWeekFormat.format(date);
        }

        // Check if the message is from the same year
        if (current.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR)) {
            return sameYearFormat.format(date);
        }

        // Default case: different year
        return differentYearFormat.format(date);
    }

    private static boolean isSameDay(Calendar current, Calendar messageDate) {
        return current.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                current.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR);
    }

    private static boolean isSameWeek(Calendar current, Calendar messageDate) {
        // Check if the message is from the same week
        return current.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                current.get(Calendar.WEEK_OF_YEAR) == messageDate.get(Calendar.WEEK_OF_YEAR);
    }
}
