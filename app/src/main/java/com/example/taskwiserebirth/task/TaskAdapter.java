package com.example.taskwiserebirth.task;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;


import androidx.annotation.NonNull;


import androidx.core.content.ContextCompat;
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
        holder.taskName.setText(tasks.get(position).getTaskName());
        holder.deadline.setText(tasks.get(position).getDeadline());
        holder.priority.setText(tasks.get(position).getPriorityCategory());

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
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.show_menu, popupMenu.getMenu());

        // Get the Menu object
        Menu menu = popupMenu.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            // Set text color for normal state
            SpannableString spannable = new SpannableString(menuItem.getTitle());
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.dark)), 0, spannable.length(), 0);
            menuItem.setTitle(spannable);
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {

                // Get the selected MenuItem
                SpannableString selectedSpannable = new SpannableString(item.getTitle());
                selectedSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), 0, selectedSpannable.length(), 0);
                item.setTitle(selectedSpannable);

                int itemId = item.getItemId();
                if (itemId == R.id.menuEdit) {
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
        Log.d("menu", task.getTaskName());
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
