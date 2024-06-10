package com.example.taskwiserebirth.task;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.R;
import com.example.taskwiserebirth.TaskDetailFragment;
import com.example.taskwiserebirth.utils.CalendarUtils;
import com.example.taskwiserebirth.utils.PopupMenuUtils;

import java.util.Date;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder> {

    private final Context context;
    private List<TaskModel> tasks;
    private final TaskActionListener actionListener;
    private Date selectedDate;
    private FragmentActivity activity;
    private final int closeToDueHours = 12;
    private double highestPriorityScore;

    public interface TaskActionListener {
        void onEditTask(TaskModel task);
        void onDeleteTask(TaskModel task);
        void onDoneTask(TaskModel task);
    }

    public TaskAdapter(Context context, FragmentActivity activity, List<TaskModel> tasks, TaskActionListener listener) {
        this.context = context;
        this.activity = activity;
        this.tasks = tasks;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_task_cards, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskModel currentTask = tasks.get(position);

        holder.itemView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));

        holder.taskName.setText(currentTask.getTaskName());
        holder.priority.setText(currentTask.getPriorityCategory());

        int deadlineColor = getTaskDeadlineColor(currentTask);

        holder.deadline.setText(currentTask.getDeadline());
        holder.deadline.setTextColor(deadlineColor);

        holder.menuView.setOnClickListener(v -> PopupMenuUtils.showPopupMenu(context, v, currentTask, actionListener, activity));

        holder.itemView.setOnClickListener(v -> {
            TaskDetailFragment fragmentViewerCard = new TaskDetailFragment(currentTask);
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

            // Add and show TaskDetailFragment without replacing the current fragment
            transaction.add(R.id.frame_layout, fragmentViewerCard, "TASK_DETAIL_FRAGMENT")
                    .addToBackStack(null)
                    .commit();
        });

        // Set the visibility of the top priority icon
        if (currentTask.getPriorityScore() == highestPriorityScore && currentTask.getStatus().equalsIgnoreCase("unfinished")) {
            holder.topPriorityIcon.setVisibility(View.VISIBLE);
        } else {
            holder.topPriorityIcon.setVisibility(View.INVISIBLE);
        }
    }

    private int getTaskDeadlineColor(TaskModel task) {
        if (task.getStatus().equals("Finished")) {
            return ContextCompat.getColor(context, R.color.green);
        } else {
            Date taskDeadline = CalendarUtils.parseDeadline(task.getDeadline());
            if (taskDeadline == null) {
                return ContextCompat.getColor(context, R.color.blue);
            } else {
                if (taskDeadline.before(selectedDate)) {
                    return ContextCompat.getColor(context, R.color.ash_gray);
                } else {
                    long diffMillis = taskDeadline.getTime() - selectedDate.getTime();
                    long diffHours = diffMillis / (60 * 60 * 1000); // millis to hours

                    if (diffHours <= closeToDueHours) {
                        return ContextCompat.getColor(context, R.color.red);
                    }
                }
            }
        }
        return ContextCompat.getColor(context, R.color.blue);
    }

    private void calculateHighestPriorityScore() {
        highestPriorityScore = 0;
        for (TaskModel task : tasks) {
            if (task.getPriorityScore() > highestPriorityScore) {
                highestPriorityScore = task.getPriorityScore();
            }
            Log.v("calculateHighestPriorityScore", String.valueOf(task.getPriorityScore()));
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        activity = null;
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<TaskModel> tasks) {
        this.tasks = tasks;
        calculateHighestPriorityScore();
        notifyDataSetChanged();
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }
}
