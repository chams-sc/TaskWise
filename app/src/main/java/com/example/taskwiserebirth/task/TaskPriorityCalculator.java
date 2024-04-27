package com.example.taskwiserebirth.task;

import com.example.taskwiserebirth.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TaskPriorityCalculator {

    public static double calculateTaskPriority(Task task, Date currentDate, Date earliestDeadline, Date longestDeadline) {
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
            case "Not Important":
                return 1.0;
            case "Somewhat Important":
                return 2.0;
            case "Important":
                return 3.0;
            case "Very Important":
                return 4.0;
            default:
                return 0.5;
        }
    }

    private static double calculateUrgencyFactor(String urgencyLevel) {
        switch (urgencyLevel) {
            case "Not Urgent":
                return 1.0;
            case "Somewhat Urgent":
                return 2.0;
            case "Urgent":
                return 3.0;
            case "Very Urgent":
                return 4.0;
            default:
                return 0.5;
        }
    }

    private static double calculateDeadlineFactor(String deadlineString, Date currentDate, Date earliestDeadline, Date longestDeadline) {
        if (deadlineString.equals("No deadline")) {
            return 0.0;
        }

        Date deadline = CalendarUtils.parseDeadline(deadlineString);

        long timeRemaining = deadline.getTime() - currentDate.getTime();
        long timeMin = earliestDeadline.getTime() - currentDate.getTime();
        long timeMax = longestDeadline.getTime() - currentDate.getTime();

        double deadlineFactor = 1.0 - (double) (timeRemaining - timeMin) / (timeMax - timeMin);

        return Math.max(deadlineFactor, 0.0);
    }

    public static List<Task> sortTasksByPriority(List<Task> tasks, Date currentDate) {
        Date earliestDeadline = null;
        Date longestDeadline = null;

        List<Task> tasksWithDeadlines = new ArrayList<>();
        List<Task> tasksWithoutDeadlines = new ArrayList<>();
        List<Task> finishedTasks = new ArrayList<>();

        for (Task task : tasks) {
            String priorityCategory = findPriorityCategory(task.getUrgencyLevel(), task.getImportanceLevel());
            task.setPriorityCategory(priorityCategory);

            if (task.getDeadline().equals("No deadline")) {
                tasksWithoutDeadlines.add(task);
            } else {
                tasksWithDeadlines.add(task);

                Date deadline = CalendarUtils.parseDeadline(task.getDeadline());

                if (earliestDeadline == null || deadline.before(earliestDeadline)) {
                    earliestDeadline = deadline;
                }
                if (longestDeadline ==  null || deadline.after(longestDeadline)) {
                    longestDeadline = deadline;
                }
            }

            if (task.getStatus().equals("Finished")) {
                if (tasksWithDeadlines.contains(task)) {
                    tasksWithDeadlines.remove(task);
                } else if (tasksWithoutDeadlines.contains(task)) {
                    tasksWithoutDeadlines.remove(task);
                }
                finishedTasks.add(task);
            }
        }

        final Date finalEarliestDeadline = earliestDeadline;
        final Date finalLongestDeadline = longestDeadline;

        // Sort tasks with deadlines
        Collections.sort(tasksWithDeadlines, (task1, task2) -> {
            double priority1 = calculateTaskPriority(task1, currentDate, finalEarliestDeadline, finalLongestDeadline);
            double priority2 = calculateTaskPriority(task2, currentDate, finalEarliestDeadline, finalLongestDeadline);

            return Double.compare(priority2, priority1); // Descending order
        });

        // Sort tasks without deadlines
        Collections.sort(tasksWithoutDeadlines, (task1, task2) -> {
            double priority1 = calculateTaskPriority(task1, currentDate, finalEarliestDeadline, finalLongestDeadline);
            double priority2 = calculateTaskPriority(task2, currentDate, finalEarliestDeadline, finalLongestDeadline);

            return Double.compare(priority2, priority1); // Descending order
        });

        List<Task> sortedTasks = new ArrayList<>(tasksWithDeadlines);
        sortedTasks.addAll(tasksWithoutDeadlines);
        sortedTasks.addAll(finishedTasks);
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
