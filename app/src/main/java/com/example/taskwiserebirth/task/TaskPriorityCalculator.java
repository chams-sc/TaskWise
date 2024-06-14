package com.example.taskwiserebirth.task;

import android.util.Log;

import com.example.taskwiserebirth.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskPriorityCalculator {

    public static double calculateTaskPriority(TaskModel task, Date currentDate, Date earliestDeadline, Date longestDeadline) {
        double importanceFactor = calculateImportanceFactor(task.getImportanceLevel());
        double urgencyFactor = calculateUrgencyFactor(task.getUrgencyLevel());
        double deadlineFactor = calculateDeadlineFactor(task.getDeadline(), currentDate, earliestDeadline, longestDeadline);

        if (deadlineFactor == 0) {
            return importanceFactor * urgencyFactor;
        }
        else {
            return (importanceFactor * urgencyFactor) + deadlineFactor;
        }
    }

    private static double calculateImportanceFactor(String importanceLevel) {
        switch (importanceLevel) {
            case IMPORTANCE_SOMEWHAT_IMPORTANT:
                return 2.0;
            case IMPORTANCE_IMPORTANT:
                return 3.0;
            case IMPORTANCE_VERY_IMPORTANT:
                return 4.0;
            case IMPORTANCE_NOT_IMPORTANT:
            default:
                return 1.0;
        }
    }

    private static double calculateUrgencyFactor(String urgencyLevel) {
        switch (urgencyLevel) {
            case URGENCY_SOMEWHAT_URGENT:
                return 2.0;
            case URGENCY_URGENT:
                return 3.0;
            case URGENCY_VERY_URGENT:
                return 4.0;
            case URGENCY_NOT_URGENT:
            default:
                return 1.0;
        }
    }

    private static double calculateDeadlineFactor(String deadlineString, Date currentDate, Date earliestDeadline, Date longestDeadline) {
        Date deadline;
        if (deadlineString.equals("No deadline")) {
            deadline = new Date(Long.MAX_VALUE);
        } else {
            deadline = CalendarUtils.parseDeadline(deadlineString);
        }

        long timeRemaining = deadline.getTime() - currentDate.getTime();
        long timeMin = earliestDeadline.getTime() - currentDate.getTime();
        long timeMax = longestDeadline.getTime() - currentDate.getTime();

        if (timeMax == timeMin) {
            return 1.0; // Handle the case where earliest and longest deadlines are the same
        }

        double deadlineFactor = 1.0 - (double) (timeRemaining - timeMin) / (timeMax - timeMin);

        return Math.max(deadlineFactor, 0.0);
    }

    public static List<TaskModel> sortTasksByPriority(List<TaskModel> tasks, Date currentDate) {
        Date earliestUnfinishedDeadline = null;
        Date longestUnfinishedDeadline = null;
        Date earliestFinishedDeadline = null;
        Date longestFinishedDeadline = null;

        List<TaskModel> unfinishedTasks = new ArrayList<>();
        List<TaskModel> finishedTasks = new ArrayList<>();

        for (TaskModel task : tasks) {
            String priorityCategory = findPriorityCategory(task.getUrgencyLevel(), task.getImportanceLevel());
            task.setPriorityCategory(priorityCategory);

            Date deadline = task.getDeadline().equals("No deadline") ? new Date(Long.MAX_VALUE) : CalendarUtils.parseDeadline(task.getDeadline());

            if (task.getStatus().equals("Finished")) {
                finishedTasks.add(task);
                if (earliestFinishedDeadline == null || deadline.before(earliestFinishedDeadline)) {
                    earliestFinishedDeadline = deadline;
                }
                if (longestFinishedDeadline == null || deadline.after(longestFinishedDeadline)) {
                    longestFinishedDeadline = deadline;
                }
            } else {
                unfinishedTasks.add(task);
                if (earliestUnfinishedDeadline == null || deadline.before(earliestUnfinishedDeadline)) {
                    earliestUnfinishedDeadline = deadline;
                }
                if (longestUnfinishedDeadline == null || deadline.after(longestUnfinishedDeadline)) {
                    longestUnfinishedDeadline = deadline;
                }
            }
        }

        final Date finalEarliestUnfinishedDeadline = earliestUnfinishedDeadline;
        final Date finalLongestUnfinishedDeadline = longestUnfinishedDeadline;

        Map<Double, List<TaskModel>> groupedTasks = new HashMap<>();
        for (TaskModel task : unfinishedTasks) {
            double priorityScore = calculateTaskPriority(task, currentDate, finalEarliestUnfinishedDeadline, finalLongestUnfinishedDeadline);
            task.setPriorityScore(priorityScore);
            if (!groupedTasks.containsKey(priorityScore)) {
                groupedTasks.put(priorityScore, new ArrayList<>());
            }
            groupedTasks.get(priorityScore).add(task);
        }

        List<TaskModel> sortedTasks = new ArrayList<>();
        for (Map.Entry<Double, List<TaskModel>> entry : groupedTasks.entrySet()) {
            if (entry.getValue().size() > 1) {
                TaskModel folder = new TaskModel();
                folder.setFolder(true);
                folder.setPriorityScore(entry.getKey());
                folder.setChildTasks(entry.getValue());

                sortedTasks.add(folder);
            } else {
                sortedTasks.addAll(entry.getValue());
            }
        }

        Collections.sort(sortedTasks, (task1, task2) -> Double.compare(task2.getPriorityScore(), task1.getPriorityScore()));

        final Date finalEarliestFinishedDeadline = earliestFinishedDeadline;
        final Date finalLongestFinishedDeadline = longestFinishedDeadline;

        Collections.sort(finishedTasks, (task1, task2) -> {
            double priority1 = calculateTaskPriority(task1, currentDate, finalEarliestFinishedDeadline, finalLongestFinishedDeadline);
            double priority2 = calculateTaskPriority(task2, currentDate, finalEarliestFinishedDeadline, finalLongestFinishedDeadline);

            int priorityComparison = Double.compare(priority2, priority1); // Descending order

            if (priorityComparison == 0) {
                return task1.getCreationDate().compareTo(task2.getCreationDate()); // Ascending order
            } else {
                return priorityComparison;
            }
        });

        sortedTasks.addAll(finishedTasks);

        for (TaskModel task : sortedTasks) {
            if (!task.isFolder()) {
                double priorityScore = calculateTaskPriority(task, currentDate, finalEarliestUnfinishedDeadline, finalLongestUnfinishedDeadline);
                task.setPriorityScore(priorityScore);
            }
            Log.v("sortTasksByPriority", "task name: " + task.getTaskName() + " score: " + task.getPriorityScore());
        }

        return sortedTasks;
    }


    public static String findPriorityCategory(String urgencyLevel, String importanceLevel) {

        if (importanceLevel.equals(IMPORTANCE_NONE)) {
            return PRIORITY_NO_SET;
        }
        if (importanceLevel.equals(IMPORTANCE_NOT_IMPORTANT) || importanceLevel.equals(IMPORTANCE_SOMEWHAT_IMPORTANT)) {
            if (urgencyLevel.equals(URGENCY_NOT_URGENT) || urgencyLevel.equals(URGENCY_SOMEWHAT_URGENT)) {
                return PRIORITY_LOW;
            } else {
                return PRIORITY_MEDIUM;
            }
        } else if (importanceLevel.equals(IMPORTANCE_IMPORTANT) || importanceLevel.equals(IMPORTANCE_VERY_IMPORTANT)) {
            if (urgencyLevel.equals(URGENCY_NOT_URGENT) || urgencyLevel.equals(URGENCY_SOMEWHAT_URGENT)) {
                return PRIORITY_HIGH;
            } else {
                return PRIORITY_VERY_HIGH;
            }
        } else {
            return PRIORITY_NO_SET;
        }
    }

    public static double getPriorityFraction(String priority) {
        switch (priority) {
            case PRIORITY_VERY_HIGH:
                return 3.0 / 4.0;
            case PRIORITY_HIGH:
                return 2.0 / 3.0;
            case PRIORITY_MEDIUM:
                return 1.0 / 2.0;
            case PRIORITY_LOW:
            case PRIORITY_NO_SET:
            default:
                return 1.0 / 3.0;
        }
    }

    public static final String IMPORTANCE_NONE = "None";
    public static final String IMPORTANCE_NOT_IMPORTANT = "Not Important";
    public static final String IMPORTANCE_SOMEWHAT_IMPORTANT = "Somewhat Important";
    public static final String IMPORTANCE_IMPORTANT = "Important";
    public static final String IMPORTANCE_VERY_IMPORTANT = "Very Important";

    public static final String URGENCY_NOT_URGENT = "Not Urgent";
    public static final String URGENCY_SOMEWHAT_URGENT = "Somewhat Urgent";
    public static final String URGENCY_URGENT = "Urgent";
    public static final String URGENCY_VERY_URGENT = "Very Urgent";

    public static final String PRIORITY_NO_SET = "No priority set";
    public static final String PRIORITY_LOW = "Low Priority";
    public static final String PRIORITY_MEDIUM = "Medium Priority";
    public static final String PRIORITY_HIGH = "High Priority";
    public static final String PRIORITY_VERY_HIGH = "Very High Priority";
}
