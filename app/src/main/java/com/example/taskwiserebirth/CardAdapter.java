package com.example.taskwiserebirth;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
        return new CardViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.titleView.setText(items.get(position).getTitle());
        holder.timeView.setText(items.get(position).getTime());
        holder.priorityView.setText(items.get(position).getPrio());
        holder.imageView.setImageResource(items.get(position).getMenu());
    }


    @Override
    public int getItemCount() {
        return items.size();
    }
}
