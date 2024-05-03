package com.example.taskwiserebirth.database;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.taskwiserebirth.task.Task;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class TaskDatabaseManager {

    private static final String TAG_TASK_DBM = "TASK_DB_MANAGER";
    private final User user;
    private final MongoCollection<Document> taskCollection;
    private final Context context;

    private final String finishedStatus = "Finished";
    private final String unfinishedStatus = "Unfinished";
    public interface TaskFetchListener {
        void onTasksFetched(List<Task> tasks);
    }

    public TaskDatabaseManager (User user, Context context) {
        this.user = user;
        this.taskCollection = MongoDbRealmHelper.getMongoCollection("UserTaskData");
        this.context = context;
    }

    public void insertTask(Task task) {
        if (user != null) {
            Document taskDocument = taskToDocument(task);

            taskCollection.insertOne(taskDocument).getAsync(result -> {
                if (result.isSuccess()) {
                    Toast.makeText(context, "Task saved", Toast.LENGTH_SHORT).show();
                    MongoDbRealmHelper.notifyDatabaseChangeListeners();
                } else {
                    Log.e(TAG_TASK_DBM, "Failed to insert task: " + result.getError().getMessage());
                }
            });
        }
    }

    public void updateTask(Task task) {
        Document filter = new Document("owner_id", user.getId())
                .append("_id", task.getId());

        Document updateDocument = taskToDocument(task);

        taskCollection.updateOne(filter, updateDocument).getAsync(result -> {
            if (result.isSuccess()) {
                Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show();
                MongoDbRealmHelper.notifyDatabaseChangeListeners();
            } else {
                Log.e(TAG_TASK_DBM, "Failed to update data: " + result.getError().getMessage());
            }
        });
    }

    public void deleteTask(Task task) {
        Document queryFilter = new Document("owner_id", user.getId())
                .append("_id", task.getId());

        taskCollection.deleteOne(queryFilter).getAsync(result -> {
            if (result.isSuccess()) {
                MongoDbRealmHelper.notifyDatabaseChangeListeners();
                Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG_TASK_DBM, "Failed to delete task: " + result.getError().getMessage());
            }
        });
    }

    public void markTaskAsFinished(Task task) {
        Document queryFilter = new Document("owner_id", user.getId())
                .append("_id", task.getId());

        Document updateDocument = new Document(
                "$set", new Document("status", finishedStatus)
                .append("date_finished", new Date())
        );

        taskCollection.updateOne(queryFilter, updateDocument).getAsync(result -> {
            if (result.isSuccess()) {
                MongoDbRealmHelper.notifyDatabaseChangeListeners();
                Toast.makeText(context, "Task status updated", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG_TASK_DBM, "Failed to update task status: " + result.getError().getMessage());
            }
        });
    }

    public void fetchTaskByName(TaskFetchListener listener, String taskName) {
        if (user != null) {
            Document taskNameFilter = new Document("owner_id", user.getId())
                    .append("task_name", new Document("$regex", taskName).append("$options", "i"));

            taskCollection.findOne(taskNameFilter).getAsync(task -> {
                if (task.isSuccess()) {
                    Document document = task.get();
                    if (document != null) {
                        Task taskFetched = documentToTask(document);
                        List<Task> tasks = new ArrayList<>();
                        tasks.add(taskFetched);

                        if (listener != null) {
                            listener.onTasksFetched(tasks);
                        }
                    } else {
                        Toast.makeText(context, taskName + " is not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Failed to fetch task", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void fetchSelectedDayTasks(TaskFetchListener listener, Date selectedDate) {
        if (user != null) {
            Document unfinishedFilter = new Document("owner_id", user.getId())
                    .append("status", unfinishedStatus);

            Document finishedFilter = new Document("owner_id", user.getId())
                    .append("status", finishedStatus)
                    .append("date_finished", new Document("$gte", getStartOfDay(selectedDate))
                            .append("$lt", getEndOfDay(selectedDate)));

            List<Document> queryFilters = new ArrayList<>();
            Date currentDate = new Date();

            if (selectedDate.before(getStartOfDay(currentDate))) {
                queryFilters.add(finishedFilter);
            } else if (selectedDate.after(getEndOfDay(currentDate))) {
                queryFilters.add(unfinishedFilter);
            } else {
                queryFilters.add(unfinishedFilter);
                queryFilters.add(finishedFilter);
            }

            taskCollection.find(new Document("$or", queryFilters)).iterator().getAsync(task -> {
                if (task.isSuccess()) {
                    MongoCursor<Document> results = task.get();
                    List<Task> tasks = new ArrayList<>();

                    while (results.hasNext()) {
                        Document document = results.next();
                        Task newTask = documentToTask(document);
                        tasks.add(newTask);
                    }

                    if (listener != null) {
                        listener.onTasksFetched(tasks);
                    }
                } else {
                    Log.e(TAG_TASK_DBM, "Failed to fetch tasks: " + task.getError().getMessage());
                }
            });
        }
    }

    public void fetchAllTasks(TaskFetchListener listener, boolean statusFinished) {
        if (user != null) {
            Document unfinishedFilter = new Document("owner_id", user.getId())
                    .append("status", unfinishedStatus);

            Document finishedFilter = new Document("owner_id", user.getId())
                    .append("status", finishedStatus);

            Document queryFilter;

            if (statusFinished) {
                queryFilter = finishedFilter;
            } else {
                queryFilter = unfinishedFilter;
            }

            taskCollection.find(queryFilter).iterator().getAsync(task -> {
                if (task.isSuccess()) {
                    MongoCursor<Document> results = task.get();
                    List<Task> tasks = new ArrayList<>();

                    while (results.hasNext()) {
                        Document document = results.next();
                        Task newTask = documentToTask(document);
                        tasks.add(newTask);
                    }

                    if (listener != null) {
                        listener.onTasksFetched(tasks);
                    }
                } else {
                    Log.e(TAG_TASK_DBM, "Failed to fetch tasks: " + task.getError().getMessage());
                }
            });
        }
    }

    private Task documentToTask(Document document) {
        ObjectId taskId = document.getObjectId("_id");
        String taskName = document.getString("task_name");
        String importanceLevel = document.getString("importance_level");
        String urgencyLevel = document.getString("urgency_level");
        String deadlineString = document.getString("deadline");
        String schedule = document.getString("schedule");
        String recurrence = document.getString("recurrence");
        boolean reminder = document.getBoolean("reminder");
        String notes = document.getString("notes");
        String status = document.getString("status");
        Date dateFinished = document.getDate("date_finished");

        return new Task(taskId, taskName, deadlineString, importanceLevel, urgencyLevel, "", schedule, recurrence, reminder, notes, status, dateFinished);
    }

    private Document taskToDocument(Task task) {
        Document taskDocument = new Document("owner_id", user.getId())
                .append("task_name", task.getTaskName())
                .append("importance_level", task.getImportanceLevel())
                .append("urgency_level", task.getUrgencyLevel())
                .append("deadline", task.getDeadline())
                .append("schedule", task.getSchedule())
                .append("recurrence", task.getRecurrence())
                .append("reminder", task.isReminder())
                .append("notes", task.getNotes())
                .append("status", task.getStatus())
                .append("date_finished", task.getDateFinished());

        if (task.getId() != null) {
            Document updateDocument = new Document("$set", taskDocument);
            updateDocument.remove("owner_id");

            return updateDocument;
        }

        return taskDocument;
    }

    public static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

}
