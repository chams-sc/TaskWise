package com.example.taskwiserebirth;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CardViewHolder extends RecyclerView.ViewHolder {

    TextView ItemName,Time,Urgency;
    public CardViewHolder(@NonNull View itemView) {
        super(itemView);
        ItemName = itemView.findViewById(R.id.titleoutput);
        Time = itemView.findViewById(R.id.descriptionoutput);
        Urgency = itemView.findViewById(R.id.timeoutput);
    }
}
