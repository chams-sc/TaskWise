package com.example.taskwiserebirth.database;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.R;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder> {

    Context context;
    List<Task> tasks;

    public TaskAdapter(Context context, List<Task> tasks) {
        this.context = context;
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = tasks.get(position);
        holder.taskName.setText(currentTask.getTaskName());
        holder.deadline.setText(currentTask.getDeadline());
        holder.priority.setText(currentTask.getPriorityLevel()); // TODO: replace with priority

        // Attach OnClickListener to the ImageView in the ViewHolder
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show popup menu
                showPopupMenu(v, currentTask);
            }
        });
    }


    private void showPopupMenu(View v, final Task task) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.inflate(R.menu.show_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menuUpdate) {
                    // Handle edit action
                    editTask(task);
                    return true;
                } else if (itemId == R.id.menuDelete) {
                    // Handle delete action
                    deleteTask(task);
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void editTask(Task task) {
        // Implement edit functionality for the task
    }

    private void deleteTask(Task task) {
        // Implement delete functionality for the task
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }
}
