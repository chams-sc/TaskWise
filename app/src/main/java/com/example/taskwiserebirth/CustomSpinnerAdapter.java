package com.example.taskwiserebirth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private List<String> items;
    private LayoutInflater inflater;

    public CustomSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<String> items) {
        super(context, resource, items);
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_spinner, parent, false);
        }
        TextView textView = convertView.findViewById(R.id.text1);
        textView.setText(items.get(position));
        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_spinner, parent, false);
        }
        TextView textView = convertView.findViewById(R.id.text1);
        textView.setText(items.get(position));
        return convertView;
    }
}

