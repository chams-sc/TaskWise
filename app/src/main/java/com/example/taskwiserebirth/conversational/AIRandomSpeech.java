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
            "Bravo! Task %s is on deck. Stay focused, stay positive, and keep moving forward!",
            "Task %s has been added to your arsenal. You're making progress like a boss!",
            // Feel free to add even more if you need them
            // Add more messages as needed
    };

    // Method to generate a random speech message with the provided task name
    public static String generateTaskAdded(String taskName) {
        // Generate a random index to select a random message
        int randomIndex = random.nextInt(taskAddedMessages.length);
        String randomMessage = taskAddedMessages[randomIndex];
        // Format the message with the task name and return
        return String.format(randomMessage, taskName);
    }
}