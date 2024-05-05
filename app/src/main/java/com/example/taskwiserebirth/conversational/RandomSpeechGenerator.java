package com.example.taskwiserebirth.conversational;

import java.util.Random;

public class RandomSpeechGenerator {

    // Define your random speech messages
    private static final String[] randomMessages = {
            "Your new task %s has been added successfully.",
            "I have successfully added your new task %s.",
            "Task %s added successfully!",
            // Add more messages as needed
    };

    // Random object to generate random indices
    private static final Random random = new Random();

    // Method to generate a random speech message with the provided task name
    public static String generateRandomSpeech(String taskName) {
        // Generate a random index to select a random message
        int randomIndex = random.nextInt(randomMessages.length);
        String randomMessage = randomMessages[randomIndex];
        // Format the message with the task name and return
        return String.format(randomMessage, taskName);
    }
}