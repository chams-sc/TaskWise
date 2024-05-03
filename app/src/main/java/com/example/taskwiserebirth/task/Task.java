package com.example.taskwiserebirth.task;

import org.bson.types.ObjectId;

import java.util.Date;

public class Task {
    private ObjectId id;
    private String taskName;
    private String deadline;
    private String recurrence;
    private String schedule;
    private String importanceLevel;
    private String urgencyLevel;
    private String notes;
    private String priorityCategory;
    private String status;
    private Date dateFinished;
    private boolean reminder;

    public Task () {

    }

    public Task(ObjectId id, String taskName, String deadlineString, String importanceLevel, String urgencyLevel, String priorityCategory, String schedule, String recurrence, boolean reminder, String notes, String status, Date dateFinished) {
        this.id = id;
        this.taskName = taskName;
        this.deadline = deadlineString;
        this.importanceLevel = importanceLevel;
        this.urgencyLevel = urgencyLevel;
        this.priorityCategory = priorityCategory;
        this.schedule = schedule;
        this.recurrence = recurrence;
        this.reminder = reminder;
        this.notes = notes;
        this.status = status;
        this.dateFinished = dateFinished;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDateFinished() {
        return dateFinished;
    }

    public void setDateFinished(Date dateFinished) {
        this.dateFinished = dateFinished;
    }

    public boolean isReminder() {
        return reminder;
    }

    public void setReminder(boolean reminder) {
        this.reminder = reminder;
    }
}
