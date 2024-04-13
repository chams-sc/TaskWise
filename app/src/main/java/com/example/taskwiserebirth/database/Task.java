package com.example.taskwiserebirth.database;

import java.util.Date;

public class Task {
    private String taskName;
    private Date creationDate;
    private String deadline;
    private String duration;
    private String recurrence;
    private String schedule;
    private String smartSchedule;
    private String importanceLevel;
    private String urgencyLevel;
    private String notes;
    private String priorityCategory;
    private boolean reminder;
    private boolean taskChunking;

    public Task() {
    }

    public Task(String taskName, String deadlineString, String importanceLevel, String urgencyLevel, String priorityCategory, String duration, String schedule) {
        this.taskName = taskName;
        this.deadline = deadlineString;
        this.importanceLevel = importanceLevel;
        this.urgencyLevel = urgencyLevel;
        this.priorityCategory = priorityCategory;
        this.duration = duration;
        this.schedule = schedule;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getSmartSchedule() {
        return smartSchedule;
    }

    public void setSmartSchedule(String smartSchedule) {
        this.smartSchedule = smartSchedule;
    }

    public String getImportanceLevel() {
        return importanceLevel;
    }

    public void setImportanceLevel(String importanceLevel) {
        this.importanceLevel = importanceLevel;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPriorityCategory() {
        return priorityCategory;
    }

    public void setPriorityCategory(String priorityCategory) {
        this.priorityCategory = priorityCategory;
    }

    public boolean isReminder() {
        return reminder;
    }

    public void setReminder(boolean reminder) {
        this.reminder = reminder;
    }

    public boolean isTaskChunking() {
        return taskChunking;
    }

    public void setTaskChunking(boolean taskChunking) {
        this.taskChunking = taskChunking;
    }

}
