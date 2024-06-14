package com.example.taskwiserebirth.task;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.R;
import com.example.taskwiserebirth.TaskDetailFragment;
import com.example.taskwiserebirth.utils.CalendarUtils;
import com.example.taskwiserebirth.utils.PopupMenuUtils;

import java.util.Date;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_FOLDER_EXPANDED = 0;
    private static final int TYPE_FOLDER_COLLAPSED = 1;
    private static final int TYPE_TASK = 2;
    private final Context context;
    private List<TaskModel> tasks;
    private final TaskActionListener actionListener;
    private Date selectedDate;
    private FragmentActivity activity;
    private final int closeToDueHours = 12; //TODO: edit close to due to be similar to CalendarUtils.calculateCloseToDue
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
        this.selectedDate = new Date();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOLDER_EXPANDED || viewType == TYPE_FOLDER_COLLAPSED) {
            // Inflate the appropriate layout based on the expanded state
            View itemView = LayoutInflater.from(context).inflate(
                    viewType == TYPE_FOLDER_EXPANDED ? R.layout.item_folder_expanded : R.layout.item_folder_collapsed,
                    parent,
                    false
            );
            return new FolderViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_task_cards, parent, false);
            return new TaskViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TaskModel currentTask = tasks.get(position);

        if (holder instanceof FolderViewHolder) {
            FolderViewHolder folderHolder = (FolderViewHolder) holder;
            folderHolder.folderName.setText(generateFolderName(currentTask.getChildTasks()));
            folderHolder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
            TaskAdapter nestedAdapter = new TaskAdapter(context, activity, currentTask.getChildTasks(), actionListener);
            folderHolder.recyclerView.setAdapter(nestedAdapter);

            folderHolder.recyclerView.setVisibility(currentTask.isExpanded() ? View.VISIBLE : View.GONE);

            folderHolder.itemView.setOnClickListener(v -> {
                currentTask.setExpanded(!currentTask.isExpanded());
                notifyItemChanged(position);
            });
        } else if (holder instanceof TaskViewHolder) {
            TaskViewHolder taskHolder = (TaskViewHolder) holder;
            taskHolder.taskName.setText(currentTask.getTaskName());
            taskHolder.priority.setText(currentTask.getPriorityCategory());

            int deadlineColor = getTaskDeadlineColor(currentTask);
            taskHolder.deadline.setText(currentTask.getDeadline());
            taskHolder.deadline.setTextColor(deadlineColor);
            taskHolder.menuView.setOnClickListener(v -> PopupMenuUtils.showPopupMenu(context, v, currentTask, actionListener, activity));
            taskHolder.itemView.setOnClickListener(v -> {
                TaskDetailFragment fragmentViewerCard = new TaskDetailFragment(currentTask);
                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.frame_layout, fragmentViewerCard, "TASK_DETAIL_FRAGMENT")
                        .addToBackStack(null)
                        .commit();
            });
            if (currentTask.getPriorityScore() == highestPriorityScore && currentTask.getStatus().equalsIgnoreCase("unfinished")) {
                taskHolder.topPriorityIcon.setVisibility(View.VISIBLE);
            } else {
                taskHolder.topPriorityIcon.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        TaskModel task = tasks.get(position);
        if (task.isExpandable()) {
            return task.isExpanded() ? TYPE_FOLDER_EXPANDED : TYPE_FOLDER_COLLAPSED;
        } else {
            return TYPE_TASK;
        }
    }

    private String generateFolderName(List<TaskModel> childTasks) {
        if (childTasks.isEmpty()) {
            return "Empty Folder";
        }

        StringBuilder folderNameBuilder = new StringBuilder();
        for (TaskModel child : childTasks) {
            folderNameBuilder.append(child.getTaskName()).append(", ");
        }

        // Remove the trailing comma and space
        if (folderNameBuilder.length() > 2) {
            folderNameBuilder.setLength(folderNameBuilder.length() - 2);
        }

        return folderNameBuilder.toString();
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
