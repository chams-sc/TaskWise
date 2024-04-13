package com.example.taskwiserebirth;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Typeface;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private List<Calendar> calendarList;
    private OnItemClickListener listener;

    public CalendarAdapter(List<Calendar> calendarList, OnItemClickListener listener) {
        this.calendarList = calendarList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_item, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        Calendar calendar = calendarList.get(position);

        // Format date components
        String monthName = formatDate(calendar, "MMM");
        String dayOfMonth = formatDate(calendar, "d");
        String dayOfWeek = formatDate(calendar, "EEE");

        holder.monthText.setText(monthName);
        holder.dateText.setText(dayOfMonth);
        holder.dayOfWeekText.setText(dayOfWeek);

        setCardColors(holder, calendar);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(calendar);
            }
        });
    }

    @Override
    public int getItemCount() {
        return calendarList.size();
    }

    private String formatDate(Calendar calendar, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        try {
            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void setCardColors(CalendarViewHolder holder, Calendar calendar) {
        Calendar currentCalendar = Calendar.getInstance();
        int currentDayOfMonth = currentCalendar.get(Calendar.DAY_OF_MONTH);

        if (calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                && calendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
                && calendar.get(Calendar.DAY_OF_MONTH) == currentDayOfMonth) {
            // Set background color for today's date
            holder.cardView.setCardBackgroundColor(Color.parseColor("#383F51"));
            holder.dateText.setTextColor(Color.WHITE);
            holder.monthText.setTextColor(Color.WHITE);
            holder.dayOfWeekText.setTextColor(Color.WHITE);
        } else {
            // Set default background color for other dates
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.dateText.setTextColor(Color.BLACK);
            holder.monthText.setTextColor(Color.BLACK);
            holder.dayOfWeekText.setTextColor(Color.BLACK);
        }
    }


    // Inside CalendarViewHolder class
    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView monthText;
        TextView dateText;
        TextView dayOfWeekText;
        CardView cardView;

        CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            monthText = itemView.findViewById(R.id.monthText);
            dateText = itemView.findViewById(R.id.dateText);
            dayOfWeekText = itemView.findViewById(R.id.dayOfWeekText);
            cardView = itemView.findViewById(R.id.dateCardView); // Initialize the CardView
        }
    }

    interface OnItemClickListener {
        void onItemClick(Calendar date);
    }
}
