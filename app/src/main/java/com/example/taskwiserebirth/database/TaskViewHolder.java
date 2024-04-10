package com.example.taskwiserebirth.database;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.R;

public class TaskViewHolder extends RecyclerView.ViewHolder {
    TextView taskName, deadline, priority;
    ImageView imageView;
    public TaskViewHolder(@NonNull View itemView) {
        super(itemView);
        taskName = itemView.findViewById(R.id.taskNameTxt);
        deadline = itemView.findViewById(R.id.deadlineTxt);
        priority = itemView.findViewById(R.id.priority);
    }
}
