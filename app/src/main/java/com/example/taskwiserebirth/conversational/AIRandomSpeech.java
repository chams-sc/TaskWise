package com.example.taskwiserebirth.conversational;

import java.util.Random;

public class AIRandomSpeech {
    private static final Random random = new Random();

    private static final String[] taskAddedMessages = {
            "You've just upgraded your productivity level with task %s. Every step counts!",
            "Mark Twain once said: The secret of getting ahead is getting started. Task %s added successfully!",
            "Task %s has been added. Keep the momentum going, for every step forward brings you closer to your goals.",
            "With the addition of task %s, you've taken yet another step towards greatness! Keep that focus sharp and let productivity be your guiding star.",
            "Task %s joins the ranks of your conquests! Remember, every accomplishment starts with the decision to try.",
            "You're on fire! Task %s has been added. Keep going, you're unstoppable!",
            "Way to go! Task %s is now in your lineup. Remember, every small step counts!",
            "With task %s added, you're one step closer to your goals. Keep up the fantastic work!",
            "Task %s is locked and loaded. You've got this!",
            "Pat yourself on the back, task %s is officially part of your journey. Keep shining bright!",
            "Task %s is on deck. Stay focused, stay positive, and keep moving forward!",
            "Task %s has been added to your arsenal. You're making progress!",
            // Add more messages as needed
    };

    private static final String[] taskFinishedMessages = {
            "Congratulations! Task %s has been successfully completed. Great job!",
            "You're on a roll! Task %s is now finished. Keep up the amazing work!",
            "Task %s completed! Remember, the only way to do great work is to love what you do.",
            "With task %s finished, you're one step closer to your goals. Keep pushing forward!",
            "Well done! Task %s has been conquered. Celebrate your achievements!",
            "Task %s is officially off your to-do list. Keep the momentum going!",
            "Amazing work! Task %s has been completed. Your dedication is paying off!",
            "Task %s is done! Remember, every accomplishment starts with the decision to try.",
            "Celebrate your victory! Task %s has been successfully completed.",
            "You've done it! Task %s is finished. Keep up the fantastic work!",
            "Task %s is completed. Keep striving for excellence!",
            "Task %s is done and dusted. Keep the momentum going!",
            "Another task bites the dust! Task %s is now completed.",
            "Task %s is finished. Take a moment to celebrate your progress!",
            "With task %s completed, you're one step closer to success. Keep up the great work!",
            "Task %s is officially checked off. Keep the momentum going!",
            "Task %s is done! Keep moving forward with confidence.",
            "Task %s has been successfully completed. Keep aiming high!",
            // Add more messages as needed
    };

    private static final String[] taskUpdatedMessages = {
            "Hey, I've updated your task %s!",
            "Good news! Your task %s has been updated.",
            "Guess what? Task %s has been successfully updated.",
            "Just wanted to let you know, I've successfully updated the task: %s.",
            "Hey there! The task named %s has been updated.",
            "All set! The update for your task %s is complete.",
            "Your wish is my command! Task %s has been updated as requested.",
            "Done and done! The task update for %s is now complete.",
            "Ta-da! I've made the changes to your task %s.",
            "Mission accomplished! Your task update is complete for: %s.",
            "Bingo! I've updated the task %s for you.",
            "Voilà! Your task %s has been updated.",
            "Hooray! Task %s has been successfully updated.",
            "Great news! I've successfully updated the task: %s.",
            "Hi there! The task named %s has been updated and ready to roll.",
            "Done deal! The update for your task %s is complete.",
            "Consider it done! Task %s has been updated as requested.",
            "Success! The task update for %s is now complete.",
            "Tada! I've made the changes you requested to your task %s.",
            "Pat yourself on the back! Your task update is complete for: %s."
    };

    public static String generateTaskAdded(String taskName) {
        int randomIndex = random.nextInt(taskAddedMessages.length);
        String randomMessage = taskAddedMessages[randomIndex];

        return String.format(randomMessage, taskName);
    }

    public static String generateTaskFinished(String taskName) {
        int randomIndex = random.nextInt(taskFinishedMessages.length);
        String randomMessage = taskFinishedMessages[randomIndex];
        return String.format(randomMessage, taskName);
    }

    public static String generateTaskUpdated(String taskName) {
        int randomIndex = random.nextInt(taskUpdatedMessages.length);
        String randomMessage = taskUpdatedMessages[randomIndex];
        return String.format(randomMessage, taskName);
    }
}