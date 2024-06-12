package com.example.taskwiserebirth.task;

import android.util.Log;

import com.example.taskwiserebirth.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
            case "Somewhat Important":
                return 2.0;
            case "Important":
                return 3.0;
            case "Very Important":
                return 4.0;
            case "Not Important":
            default:
                return 1.0;
        }
    }

    private static double calculateUrgencyFactor(String urgencyLevel) {
        switch (urgencyLevel) {
            case "Somewhat Urgent":
                return 2.0;
            case "Urgent":
                return 3.0;
            case "Very Urgent":
                return 4.0;
            case "Not Urgent":
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
        final Date finalEarliestFinishedDeadline = earliestFinishedDeadline;
        final Date finalLongestFinishedDeadline = longestFinishedDeadline;

        // Sort unfinished tasks
        Collections.sort(unfinishedTasks, (task1, task2) -> {
            double priority1 = calculateTaskPriority(task1, currentDate, finalEarliestUnfinishedDeadline, finalLongestUnfinishedDeadline);
            double priority2 = calculateTaskPriority(task2, currentDate, finalEarliestUnfinishedDeadline, finalLongestUnfinishedDeadline);

            int priorityComparison = Double.compare(priority2, priority1); // Descending order

            if (priorityComparison == 0) {
                // If priorities are equal, sort by creation date
                return task1.getCreationDate().compareTo(task2.getCreationDate()); // Ascending order
            } else {
                return priorityComparison;
            }
        });

        // Sort finished tasks
        Collections.sort(finishedTasks, (task1, task2) -> {
            double priority1 = calculateTaskPriority(task1, currentDate, finalEarliestFinishedDeadline, finalLongestFinishedDeadline);
            double priority2 = calculateTaskPriority(task2, currentDate, finalEarliestFinishedDeadline, finalLongestFinishedDeadline);

            int priorityComparison = Double.compare(priority2, priority1); // Descending order

            if (priorityComparison == 0) {
                // If priorities are equal, sort by creation date
                return task1.getCreationDate().compareTo(task2.getCreationDate()); // Ascending order
            } else {
                return priorityComparison;
            }
        });

        List<TaskModel> sortedTasks = new ArrayList<>(unfinishedTasks);
        sortedTasks.addAll(finishedTasks);

        for (TaskModel task : sortedTasks) {
            double priorityScore = task.getStatus().equals("Finished") ? 0 : calculateTaskPriority(task, currentDate, finalEarliestUnfinishedDeadline, finalLongestUnfinishedDeadline);
            task.setPriorityScore(priorityScore);
            Log.v("sortTasksByPriority", "task name: " + task.getTaskName() + " score: " + task.getPriorityScore());
        }
        return sortedTasks;
    }


    public static String findPriorityCategory(String urgencyLevel, String importanceLevel) {

        if (importanceLevel.equals("None")) {
            return "No priority set";
        }
        if (importanceLevel.equals("Not Important") || importanceLevel.equals("Somewhat Important")) {
            if (urgencyLevel.equals("Not Urgent") || urgencyLevel.equals("Somewhat Urgent")) {
                return "Low Priority";
            } else {
                return "Medium Priority";
            }
        } else if (importanceLevel.equals("Important") || importanceLevel.equals("Very Important")) {
            if (urgencyLevel.equals("Not Urgent") || urgencyLevel.equals("Somewhat Urgent")) {
                return "High Priority";
            } else {
                return "Very High Priority";
            }
        } else {
            return "No priority set";
        }
    }
}
