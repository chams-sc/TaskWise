package com.example.taskwiserebirth.task;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.R;
import com.example.taskwiserebirth.utils.CalendarUtils;

import java.util.Date;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder> {

    private Context context;
    private List<Task> tasks;
    private TaskActionListener actionListener;
    private Date selectedDate;
    private final int closeToDueHours = 12;

    public interface TaskActionListener {
        void onEditTask(Task task);
        void onDeleteTask(Task task);
        void onDoneTask(Task task);
    }

    public TaskAdapter(Context context, List<Task> tasks, TaskActionListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskViewHolder(LayoutInflater.from(context).inflate(R.layout.item_task_cards, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = tasks.get(position);
        holder.taskName.setText(currentTask.getTaskName());
        holder.priority.setText(currentTask.getPriorityCategory());

        int deadlineColor = getTaskDeadlineColor(currentTask);

        holder.deadline.setText(currentTask.getDeadline());
        holder.deadline.setTextColor(deadlineColor);

        holder.imageView.setOnClickListener(v ->
                showPopupMenu(v, currentTask));
    }

    private int getTaskDeadlineColor (Task task) {

        if (task.getStatus().equals("Finished")) {
            return ContextCompat.getColor(context, R.color.green);
        // Unfinished tasks
        } else {
            Date taskDeadline = CalendarUtils.parseDeadline(task.getDeadline());
            // No deadline
            if (taskDeadline == null) {
                return ContextCompat.getColor(context, R.color.blue);
            } else {
                // Past due
                if (taskDeadline.before(selectedDate)) {
                    return ContextCompat.getColor(context, R.color.ash_gray);
                // Close to due
                } else {
                    long diffMillis = taskDeadline.getTime() - selectedDate.getTime();
                    long diffHours = diffMillis / (60 * 60 * 1000); // millis to hours

                    if (diffHours <= closeToDueHours) {
                        return ContextCompat.getColor(context, R.color.red);
                    }
                }
            }
        }
        // Unfinished with reasonable deadline
        return ContextCompat.getColor(context, R.color.blue);
    }

    private void showPopupMenu(View v, final Task task) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.show_menu, popupMenu.getMenu());
        
        Menu menu = popupMenu.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            // Set text color for normal state
            SpannableString spannable = new SpannableString(menuItem.getTitle());
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.dark)), 0, spannable.length(), 0);
            menuItem.setTitle(spannable);
        }

        popupMenu.setOnMenuItemClickListener(item -> {

            // Get the selected MenuItem
            SpannableString selectedSpannable = new SpannableString(item.getTitle());
            selectedSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), 0, selectedSpannable.length(), 0);
            item.setTitle(selectedSpannable);

            int itemId = item.getItemId();
            if (itemId == R.id.menuEdit) {
                actionListener.onEditTask(task);
                return true;
            } else if (itemId == R.id.menuDelete) {
                actionListener.onDeleteTask(task);
                return true;
            } else if (itemId == R.id.menuDone) {
                actionListener.onDoneTask(task);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }
}
