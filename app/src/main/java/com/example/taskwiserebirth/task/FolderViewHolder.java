package com.example.taskwiserebirth.task;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.R;

public class FolderViewHolder extends RecyclerView.ViewHolder {
    TextView folderName;
    RecyclerView recyclerView;

    public FolderViewHolder(@NonNull View itemView) {
        super(itemView);
        folderName = itemView.findViewById(R.id.folderNameTxt);
        recyclerView = itemView.findViewById(R.id.nestedRecyclerView);
    }
}
