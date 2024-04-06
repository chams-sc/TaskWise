package com.example.taskwiserebirth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardViewHolder> {

    Context context;
    List<Item> items;

    public CardAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CardViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view,parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.ItemName.setText(items.get(position).getItemName());
        holder.Time.setText(items.get(position).getTime());
        holder.Urgency.setText(items.get(position).getUrgency());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
