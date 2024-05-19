package com.example.taskwiserebirth.chatlogs;

import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.R;

public class ChatViewHolder extends RecyclerView.ViewHolder {
    public CardView leftChatLayout;
    public TextView leftChatTxtView;
    public TextView leftChatTimestamp;
    public CardView rightChatLayout;
    public TextView rightChatTxtView;
    public TextView rightChatTimestamp;

    public ChatViewHolder(View itemView) {
        super(itemView);
        leftChatLayout = itemView.findViewById(R.id.leftChatLayout);
        leftChatTxtView = itemView.findViewById(R.id.leftChatTxtView);
        leftChatTimestamp = itemView.findViewById(R.id.leftChatTimestamp);
        rightChatLayout = itemView.findViewById(R.id.rightChatLayout);
        rightChatTxtView = itemView.findViewById(R.id.rightChatTxtView);
        rightChatTimestamp = itemView.findViewById(R.id.rightChatTimestamp);
    }
}
