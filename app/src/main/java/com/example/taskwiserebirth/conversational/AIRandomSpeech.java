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

    private static final String[] noTasksMessages = {
            "Hey, it looks like you don't have any tasks at the moment.",
            "You're all clear! No tasks on your list right now.",
            "Great news! You currently have no tasks to handle.",
            "Enjoy the break! There are no tasks for you right now.",
            "Take a breather! You have no tasks to worry about.",
            "Hooray! There are no tasks on your plate at the moment.",
            "All done! You don't have any tasks pending.",
            "Relax! You have no tasks to complete right now.",
            "You're free! No tasks are waiting for you.",
            "Good job! You currently have no tasks.",
            "Awesome! Your task list is empty for now.",
            "Sweet! No tasks to tackle at the moment.",
            "You're task-free right now! Enjoy the moment.",
            "Guess what? You have no tasks right now.",
            "Time to relax! There are no tasks for you.",
            "No tasks in sight! You're free to do what you like.",
            "You're off the hook! No tasks at the moment.",
            "Breathe easy! There are no tasks on your list.",
            "Take it easy! You have no tasks right now.",
            "Woohoo! You're task-free at the moment."
    };

    private static final String[] noTasksCompletedMessages = {
            "Hmmm, it seems like you didn't finish any tasks today. Make sure to take a break and come back to them later, okay?",
            "It looks like you didn't complete any tasks today. How are you feeling?",
            "Hey, it looks like no tasks were finished today.Don't stress, you can come back to them later!",
            "You didn't wrap up any tasks today, Remember to rest and revisit them later!",
            "No tasks finished today, but stay motivated!",
            "Looks like today wasn’t a task-finishing day. Don’t give up!",
            "Hey there, you didn’t get any tasks done today. Have you been doing well?",
            "No tasks completed today, but keep your chin up!",
            "Today wasn’t the day for finishing tasks, but keep trying!",
            "No worries, you didn't complete any tasks today. Tomorrow’s a fresh start!",
            "Hey, no tasks were checked off today. How are you holding up?",
            "Looks like today wasn’t productive in terms of tasks. How's your mental space?",
            "Not a single task finished today, but how are you feeling overall?",
            "No tasks got done today. Take a deep breath and check in with yourself.",
            "You didn’t finish any tasks today, but remember to come back to them later ok?",
            "It seems like no tasks were completed today. Stay determined!",
            "Hey, no tasks were wrapped up today. Keep up the effort!",
            "No tasks were done today, but keep pushing ahead!",
            "You didn’t check off any tasks today, but keep striving!",
            "Today wasn’t a task-completion day but keep your spirits high!"
    };

    private static final String[] finishedTaskCountMessages = {
            "Upon checking your records, you were able to finish %d tasks today. Great job!",
            "I reviewed your activity, and you completed %d tasks today. Impressive!",
            "After going through your records, it looks like you finished %d tasks today. Well done!",
            "According to your activity log, you managed to complete %d tasks today. Keep it up!",
            "Your task records show that you tackled %d tasks today. Fantastic effort!",
            "I've reviewed your progress, and you successfully completed %d tasks today. Excellent job!",
            "Your records indicate that you got through %d tasks today. Way to go!",
            "Looking at your task list, you finished %d tasks today. Great work!",
            "After checking your logs, I see you completed %d tasks today. Awesome job!",
            "Your records reflect that you managed to finish %d tasks today. Keep going!"
    };

    private static final String[] unfinishedTasksMessages = {
            "You already have %d unfinished tasks. Are you sure you want to add more?",
            "Heads up! You have %d tasks still pending. Do you want to add another one?",
            "You’ve got %d tasks still to be completed. Are you sure you want to add more?",
            "Just so you know, you have %d tasks left unfinished. Do you really want to add another?",
            "Reminder: You still have %d tasks pending. Are you sure you want to add more?",
            "You have %d tasks that are yet to be completed. Want to add more to your list?",
            "Currently, you have %d unfinished tasks. Do you want to add another one?",
            "Notice: %d tasks are still not done. Are you certain about adding more?",
            "You've got %d tasks still on your plate. Are you sure you want to add another?",
            "FYI, there are %d tasks that still need your attention. Do you want to add more?"
    };

    private static final String[] taskNotFoundMessage = {
            "I'm sorry, but there's no task named %s.",
            "Oops! I couldn't find a task called %s.",
            "Hmm, it seems like there's no task named %s.",
            "Sorry, but I don't see any task called %s.",
            "It looks like there's no task by the name of %s.",
            "I'm afraid there's no task called %s in your list.",
            "There's no task named %s. Can you double-check the name?",
            "I couldn't locate a task named %s. Maybe it was deleted?",
            "Unfortunately, there's no task called %s."
    };

    private static final String[] taskNotFoundAndVerify = {
            "I couldn't find a task named %s. Did you mean something else?",
            "Hmm, no task called %s here. Can you clarify the name?",
            "Oops! %s doesn't ring a bell. What task are you looking for?",
            "No luck finding %s. Could you specify the task again?",
            "Looks like there's no task named %s. Could you check the name for me?",
            "Sorry, %s isn't in the list. What task did you mean?",
            "I don't see a task called %s. Maybe try a different name?",
            "There's no task named %s. Do you have another name in mind?",
            "I'm afraid there's no task called %s. What task are you referring to?"
    };


    private static final String[] editPromptMessages = {
            "Sure, what do you want to edit?",
            "Of course! What would you like to change?",
            "Absolutely! What do you need to edit?",
            "Sure thing! What do you want to modify?",
            "No problem! What do you want to update?",
            "Got it! What would you like to edit?",
            "Of course! Which part would you like to change?",
            "Absolutely! What do you need to update?",
            "Sure thing! What part do you want to edit?"
    };

    private static final String[] followUpChangeMessages = {
            "Anything else you want to change?",
            "What else needs editing?",
            "Is there anything else you'd like to modify?",
            "Any other changes?",
            "Do you have any more updates?",
            "Anything else you want to tweak?",
            "Any other edits?",
            "What else do you need to adjust?",
            "Any more modifications?",
            "Is there anything else you'd like to update?"
    };

    private static final String[] focusModeMessages = {
            "I couldn't respond to such requests at the moment.",
            "Sorry, I'm currently in focus mode and can't handle that request.",
            "I'm in focus mode right now, so I can't process that request.",
            "Apologies, but I'm unable to respond to that while in focus mode.",
            "I'm in focus mode at the moment, so I can't help with that.",
            "Sorry, I can't handle that request while in focus mode.",
            "I'm currently focused and can't respond to that request.",
            "Apologies, but I'm in focus mode and can't address that request.",
            "I'm in focused mode right now, so I can't process that.",
            "Sorry, but I can't respond to that request while I'm in focus mode."
    };

    private static final String[] focusModeOn = {
            "Focus mode is now enabled. Let's get things done!",
            "Focus mode activated. Ready to tackle your tasks!",
            "You've entered focus mode. Time to concentrate!",
            "Focus mode on. What can I help you with?",
            "Focus mode initiated. Let's work efficiently!",
            "Focus mode is live. What's next on your list?",
            "Focus mode enabled. Let's make some progress!",
            "Focus mode started. How can I assist you?",
            "Focus mode engaged. Let's get to work!"
    };

    private static final String[] focusModeOff = {
            "Focus mode is now disabled. Take a break!",
            "Focus mode deactivated. You can relax now.",
            "Focus mode off. Time to unwind!",
            "Focus mode disabled. What would you like to do next?",
            "Focus mode ended. Feel free to switch tasks!",
            "Focus mode is off. Let's take it easy!",
            "Focus mode is now off. How can I assist you further?",
            "Focus mode is disabled. Ready for something different?",
            "Focus mode disengaged. What's next on your agenda?"
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

    public static String generateNoTasksMessages() {
        int randomIndex = random.nextInt(noTasksMessages.length);
        String randomMessage = noTasksMessages[randomIndex];

        return String.format(randomMessage);
    }

    public static String generateNoTasksCompleted() {
        int randomIndex = random.nextInt(noTasksCompletedMessages.length);
        String randomMessage = noTasksCompletedMessages[randomIndex];

        return String.format(randomMessage);
    }

    public static String generateFinishedTaskCountMessage(int finishedTaskCount) {
        int randomIndex = random.nextInt(finishedTaskCountMessages.length);
        String randomMessage = finishedTaskCountMessages[randomIndex];
        return String.format(randomMessage, finishedTaskCount);
    }

    public static String generateUnfinishedTasksMessage(int unfinishedTaskCount) {
        int randomIndex = random.nextInt(unfinishedTasksMessages.length);
        String randomMessage = unfinishedTasksMessages[randomIndex];
        return String.format(randomMessage, unfinishedTaskCount);
    }

    public static String generateTaskNotFound(String taskName) {
        int randomIndex = random.nextInt(taskNotFoundMessage.length);
        String randomMessage = taskNotFoundMessage[randomIndex];
        return String.format(randomMessage, taskName);
    }

    public static String generateTaskNotFoundAndVerify(String taskName) {
        int randomIndex = random.nextInt(taskNotFoundAndVerify.length);
        String randomMessage = taskNotFoundAndVerify[randomIndex];
        return String.format(randomMessage, taskName);
    }

    public static String generateEditPromptMessage() {
        int randomIndex = random.nextInt(editPromptMessages.length);
        return editPromptMessages[randomIndex];
    }

    public static String generateFollowUpChangeMessage() {
        int randomIndex = random.nextInt(followUpChangeMessages.length);
        return followUpChangeMessages[randomIndex];
    }

    public static String generateFocusModeMessage() {
        int randomIndex = random.nextInt(focusModeMessages.length);
        return focusModeMessages[randomIndex];
    }

    public static String generateFocusModeOn() {
        int randomIndex = random.nextInt(focusModeOn.length);
        return focusModeOn[randomIndex];
    }

    public static String generateFocusModeOff() {
        int randomIndex = random.nextInt(focusModeOff.length);
        return focusModeOff[randomIndex];
    }
}