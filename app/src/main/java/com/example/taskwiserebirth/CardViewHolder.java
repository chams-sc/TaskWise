package com.example.taskwiserebirth;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CardViewHolder extends RecyclerView.ViewHolder {

    ImageView imageView;
    TextView titleView, timeView, priorityView;


    public CardViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.mMEnus);
        titleView = itemView.findViewById(R.id.titleoutput);
        timeView = itemView.findViewById(R.id.timeoutput);
        priorityView = itemView.findViewById(R.id.priority);


    }
}