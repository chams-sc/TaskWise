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

    private static final String[] assistiveOffMessages = {
            "Oops, I'm taking a break from assistive mode right now and can't respond to that!",
            "Hey there! I'm off-duty from assistive mode at the moment and can't handle that request. Try me later?",
            "I'm in non-assistive mode right now, so I can't respond to that. Let's focus on your tasks instead!",
            "Assistive mode is currently off, so I can't help with that request. How about we stick to your task list?",
            "Sorry, I'm not in assistive mode right now and can't respond to that. Let's tackle something else!",
            "I'm on a little assistive mode vacation and can't process that request. Can we work on your tasks instead?",
            "Assistive mode is snoozing, so I can't respond to that. How can I help you with your tasks instead?",
            "Apologies, I'm not in assistive mode at the moment and can't handle that request. Let's keep task-focused!",
            "Assistive mode is off the clock, so I can't respond to that. Let's focus on your tasks for now!",
            "I'm not in assistive mode right now and can't handle that request, but I'm here to help with your tasks!"
    };

    private static final String[] AssistiveModeOn = {
            "Hey there! I'm all ears and ready to assist you. What's on your mind?",
            "Assistive mode is active! Feel free to ask me anything and I'll do my best to help.",
            "I'm in assistive mode and ready to tackle any questions you have. Let's get started!",
            "You've got my full attention! Ask away, and I'll be happy to assist.",
            "Assistive mode is on! Whatever you need, just let me know and I'll be here to help.",
            "I'm switched to assistive mode, ready to handle your requests. What can I do for you today?",
            "All systems go! I'm in assistive mode and ready to assist with any queries you have.",
            "Let's chat! I'm in assistive mode and eager to help with whatever you need.",
            "Your personal assistant is here and ready to help! What can I assist you with?",
            "I'm in full assistive mode and ready to answer your questions. How can I help you today?"
    };

    private static final String[] AssistiveModeOff = {
            "Assistive mode is now off. Let's zero in on your tasks!",
            "Assistive mode deactivated. What task are we tackling next?",
            "Assistive mode off. Ready to dive into your tasks?",
            "Assistive mode disabled. Need help with any specific task?",
            "Assistive mode ended. Which task would you like to work on?",
            "Assistive mode off. Let's focus and get those tasks done!",
            "Assistive mode is now off. What task can I assist you with?",
            "Assistive mode is disabled. Let's get to the tasks at hand!",
            "Assistive mode disengaged. Time to concentrate on your tasks!"
    };

    private static final String[] taskDueReminder = {
            "Remember, your task '%s' is due on %s. Let's wrap this up!",
            "Don't forget, the task '%s' needs to be completed by %s. You can do it!",
            "Heads up! The task '%s' is due on %s. Stay focused and finish strong!",
            "Just a reminder, '%s' has a deadline of %s. You're almost there!",
            "Keep in mind, the task '%s' must be done by %s. Let's get this done!",
            "Note that '%s' is due by %s. Keep pushing, you're doing great!",
            "Your task '%s' is set for completion on %s. Finish it and feel the accomplishment!",
            "Make sure to finish '%s' before %s. You're on the right track!",
            "The task '%s' has a due date of %s. You got this, keep at it!"
    };

    private static final String[] unfinishedTaskReminder = {
            "You still have not finished your task '%s'. Let's get it done!",
            "Your task '%s' is still pending. Keep pushing, you're almost there!",
            "Task '%s' is awaiting completion. You can do it!",
            "You haven't completed '%s' yet. Stay focused and finish strong!",
            "The task '%s' needs your attention. Don't give up now!",
            "Task '%s' is still on your list. Finish it and feel the accomplishment!",
            "Your task '%s' remains unfinished. Keep going, you're doing great!",
            "You still need to complete '%s'. Let's wrap this up!",
            "The task '%s' is still pending. You got this, keep at it!"
    };

    private static final String[] greetingMessages = {
            "Great to see you today! Hmm, let me check your tasks for today.",
            "Hey there! Ready to conquer the day? Let's see what tasks you've got waiting.",
            "Hello! Let's see what adventures await in your task list today.",
            "Hi! Let's make today awesome by checking what tasks you need to tackle.",
            "Good to see you! Give me a second while I fetch your tasks for the day.",
            "Hey! Ready to be productive? Let me grab your tasks for today.",
            "Welcome back! Let’s dive into your tasks and see what's on the agenda.",
            "Hi there! Let’s start the day right by seeing what tasks are up next.",
            "Good to see you back! Let’s see what’s on your plate for today. Just a moment!",
            "Hey superstar! Let's check out what tasks are lined up for you today.",
            "Hello champion! Let's take a peek at what you need to conquer today!",
            "Great to have you back! Hold on, let me see what tasks need your magic touch today.",
            "Hey rockstar! Give me a sec to check your tasks. Today’s going to be great!",
            "Good day! Let's see what exciting tasks await you today. Hang tight!",
            "Welcome back! Let’s check what’s on your task list for today.",
            "Hi there! Let's see what challenges you can tackle today.",
            "Good seeing you today! Let’s see what’s on the agenda. Checking your tasks now!"
    };

    public static String generateGreeting() {
        int randomIndex = random.nextInt(greetingMessages.length);
        return greetingMessages[randomIndex];
    }

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

    public static String generateAssistiveModeMessage() {
        int randomIndex = random.nextInt(assistiveOffMessages.length);
        return assistiveOffMessages[randomIndex];
    }

    public static String generateAssistiveModeOn() {
        int randomIndex = random.nextInt(AssistiveModeOn.length);
        return AssistiveModeOn[randomIndex];
    }

    public static String generateAssistiveModeOff() {
        int randomIndex = random.nextInt(AssistiveModeOff.length);
        return AssistiveModeOff[randomIndex];
    }

    public static String generateUnfinishedTaskReminder(String taskName) {
        int randomIndex = random.nextInt(unfinishedTaskReminder.length);
        String randomMessage = unfinishedTaskReminder[randomIndex];
        return String.format(randomMessage, taskName);
    }

    public static String generateTaskDueReminder(String taskName, String dueDate) {
        int randomIndex = random.nextInt(taskDueReminder.length);
        String randomMessage = taskDueReminder[randomIndex];
        return String.format(randomMessage, taskName, dueDate);
    }
}