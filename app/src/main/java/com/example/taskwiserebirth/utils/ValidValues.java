package com.example.taskwiserebirth.utils;

import java.util.HashSet;
import java.util.Set;

public class ValidValues {
    // Define the set of valid importance levels
    public static final Set<String> VALID_IMPORTANCE_LEVELS = new HashSet<>();
    public static final Set<String> VALID_URGENCY_LEVELS = new HashSet<>();
    public static final Set<String> VALID_RECURRENCE_LEVELS = new HashSet<>();

    static {
        VALID_IMPORTANCE_LEVELS.add("Not Important");
        VALID_IMPORTANCE_LEVELS.add("Somewhat Important");
        VALID_IMPORTANCE_LEVELS.add("Important");
        VALID_IMPORTANCE_LEVELS.add("Very Important");

        VALID_URGENCY_LEVELS.add("Not Urgent");
        VALID_URGENCY_LEVELS.add("Somewhat Urgent");
        VALID_URGENCY_LEVELS.add("Urgent");
        VALID_URGENCY_LEVELS.add("Very Urgent");

        VALID_RECURRENCE_LEVELS.add("Monday");
        VALID_RECURRENCE_LEVELS.add("Tuesday");
        VALID_RECURRENCE_LEVELS.add("Wednesday");
        VALID_RECURRENCE_LEVELS.add("Thursday");
        VALID_RECURRENCE_LEVELS.add("Friday");
        VALID_RECURRENCE_LEVELS.add("Saturday");
        VALID_RECURRENCE_LEVELS.add("Sunday");
    }
}
