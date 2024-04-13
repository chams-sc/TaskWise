package com.example.taskwiserebirth.task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

        Date deadline = parseDeadline(deadlineString);

        long timeRemaining = deadline.getTime() - currentDate.getTime();
        long timeMin = earliestDeadline.getTime() - currentDate.getTime();
        long timeMax = longestDeadline.getTime() - currentDate.getTime();

        double deadlineFactor = 1.0 - (double) (timeRemaining - timeMin) / (timeMax - timeMin);

        return Math.max(deadlineFactor, 0.0);
    }

    private static Date parseDeadline(String deadline) {
        if (deadline.equals("No deadline")) {
            return null;
        }

        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy | hh:mm a");
        try {
            return format.parse(deadline);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Task> sortTasksByPriority(List<Task> tasks, Date currentDate) {
        Date earliestDeadline = null;
        Date longestDeadline = null;

        List<Task> tasksWithDeadlines = new ArrayList<>();
        List<Task> tasksWithoutDeadlines = new ArrayList<>();

        for (Task task : tasks) {
            String priorityCategory = findPriorityCategory(task.getUrgencyLevel(), task.getImportanceLevel());
            task.setPriorityCategory(priorityCategory);

            if (task.getDeadline().equals("No deadline")) {
                tasksWithoutDeadlines.add(task);
            } else {
                tasksWithDeadlines.add(task);

                Date deadline = parseDeadline(task.getDeadline());

                if (earliestDeadline == null || deadline.before(earliestDeadline)) {
                    earliestDeadline = deadline;
                }
                if (longestDeadline ==  null || deadline.after(longestDeadline)) {
                    longestDeadline = deadline;
                }
            }
        }

        final Date finalEarliestDeadline = earliestDeadline;
        final Date finalLongestDeadline = longestDeadline;

        // Sort tasks with deadlines
        Collections.sort(tasksWithDeadlines, new Comparator<Task>() {
            @Override
            public int compare(Task task1, Task task2) {
                double priority1 = calculateTaskPriority(task1, currentDate, finalEarliestDeadline, finalLongestDeadline);
                double priority2 = calculateTaskPriority(task2, currentDate, finalEarliestDeadline, finalLongestDeadline);

                return Double.compare(priority2, priority1); // Descending order
            }
        });

        // Sort tasks without deadlines
        Collections.sort(tasksWithoutDeadlines, new Comparator<Task>() {
            @Override
            public int compare(Task task1, Task task2) {
                double priority1 = calculateTaskPriority(task1, currentDate, finalEarliestDeadline, finalLongestDeadline);
                double priority2 = calculateTaskPriority(task2, currentDate, finalEarliestDeadline, finalLongestDeadline);

                return Double.compare(priority2, priority1); // Descending order
            }
        });

        List<Task> sortedTasks = new ArrayList<>(tasksWithDeadlines);
        sortedTasks.addAll(tasksWithoutDeadlines);

        return sortedTasks;
    }

    public static String findPriorityCategory(String urgencyLevel, String importanceLevel) {
        if (urgencyLevel.equals("Not Urgent") || urgencyLevel.equals("Somewhat Urgent")) {
            if (importanceLevel.equals("Not Important") || importanceLevel.equals("Somewhat Important")) {
                return "Low Priority";
            } else {
                return "Medium Priority";
            }
        } else if (urgencyLevel.equals("Urgent") || urgencyLevel.equals("Very Urgent")){
            if (importanceLevel.equals("Not Important") || importanceLevel.equals("Somewhat Important")) {
                return "High Priority";
            } else {
                return "Very High Priority";
            }
        }
        else {
            return "Unknown Priority";
        }
    }
}
