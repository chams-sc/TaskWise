package com.example.taskwiserebirth.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
public class TaskPriorityCalculator {

    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

    public static double calculateTaskPriority(Task task, Date currentDate, Date earliestDeadline, Date longestDeadline) {
        double importanceFactor = calculateImportanceFactor(task.getImportanceLevel());
        double urgencyFactor = calculateUrgencyFactor(task.getUrgencyLevel());
        double deadlineFactor = calculateDeadlineFactor(task.getDeadline(), currentDate, earliestDeadline, longestDeadline);

        return (importanceFactor * urgencyFactor) + deadlineFactor;
    }

    private static double calculateImportanceFactor(String importanceLevel) {
        return 0;
    }

    private static double calculateUrgencyFactor(String urgencyLevel) {
        return 0;
    }

    private static double calculateDeadlineFactor(String deadline, Date currentDate, Date earliestDeadline, Date longestDeadline) {
        return 0;
    }

}
