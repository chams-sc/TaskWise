package com.example.taskwiserebirth.task;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.R;

public class TaskViewHolder extends RecyclerView.ViewHolder {
    TextView taskName, schedOrDeadlineTxt, priority, recurrenceTxt;
    View menuView, topPriorityIcon, cardBg;
    ImageView recurrenceIcon;
    CardView taskCard;
    public TaskViewHolder(@NonNull View itemView) {
        super(itemView);
        taskName = itemView.findViewById(R.id.taskNameTxt);
        schedOrDeadlineTxt = itemView.findViewById(R.id.deadlineSchedTxt);
        priority = itemView.findViewById(R.id.priority);
        menuView = itemView.findViewById(R.id.menuViewContainer);
        topPriorityIcon = itemView.findViewById(R.id.topPriorityIcon);
        cardBg = itemView.findViewById(R.id.cardContainerBg);
        recurrenceIcon = itemView.findViewById(R.id.recurrenceIcon);
        recurrenceTxt = itemView.findViewById(R.id.recurrenceTxt);
        taskCard = itemView.findViewById(R.id.taskCard);
    }
}
