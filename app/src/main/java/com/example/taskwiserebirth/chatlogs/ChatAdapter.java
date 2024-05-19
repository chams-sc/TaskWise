package com.example.taskwiserebirth.chatlogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.R;
import com.example.taskwiserebirth.utils.CalendarUtils;

import java.util.Date;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatViewHolder> {
    private List<ChatMessage> chatMessages;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_recycler_row, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        String formattedTimestamp = CalendarUtils.formatTimestamp(new Date(message.getTimestamp()));

        if (message.getRole().equals("user")) {
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatTxtView.setText(message.getDialogue());
            holder.rightChatTimestamp.setVisibility(View.VISIBLE);
            holder.rightChatTimestamp.setText(formattedTimestamp);
            holder.leftChatTimestamp.setVisibility(View.GONE);
        } else if (message.getRole().equals("assistant")) {
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftChatTxtView.setText(message.getDialogue());
            holder.leftChatTimestamp.setVisibility(View.VISIBLE);
            holder.leftChatTimestamp.setText(formattedTimestamp);
            holder.rightChatTimestamp.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }
}
