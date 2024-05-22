package com.example.taskwiserebirth.task;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.R;

public class TaskViewHolder extends RecyclerView.ViewHolder {
    TextView taskName, deadline, priority;
    View menuView;
    public TaskViewHolder(@NonNull View itemView) {
        super(itemView);
        taskName = itemView.findViewById(R.id.taskNameTxt);
        deadline = itemView.findViewById(R.id.deadlineTxt);
        priority = itemView.findViewById(R.id.priority);
        menuView = itemView.findViewById(R.id.menuViewContainer);
    }
}
