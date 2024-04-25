package com.example.taskwiserebirth.database;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.taskwiserebirth.task.Task;

import org.bson.Document;

import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;

public class TaskDatabaseManager {

    private static final String TAG_TASK_DBM = "TASK_DB_MANAGER";
    private User user;
    private MongoCollection<Document> taskCollection;
    private Context context;

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

        Document updateDocument = new Document("$set", new Document("status", "Finished"));

        taskCollection.updateOne(queryFilter, updateDocument).getAsync(result -> {
            if (result.isSuccess()) {
                MongoDbRealmHelper.notifyDatabaseChangeListeners();
                Toast.makeText(context, "Task status updated", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG_TASK_DBM, "Failed to update task status: " + result.getError().getMessage());
            }
        });

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
                .append("creation_date", task.getCreationDate());

        if (task.getId() != null) {
            Document updateDocument = new Document("$set", taskDocument);
            updateDocument.remove("owner_id");

            return updateDocument;
        }

        return taskDocument;
    }
}
