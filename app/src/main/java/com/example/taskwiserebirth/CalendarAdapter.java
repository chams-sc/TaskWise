package com.example.taskwiserebirth;

import android.graphics.Color;
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
        SimpleDateFormat sdfDate = new SimpleDateFormat("d", Locale.getDefault());
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MMM", Locale.getDefault());
        SimpleDateFormat sdfDayOfWeek = new SimpleDateFormat("EEE", Locale.getDefault());

        String monthName = sdfMonth.format(calendar.getTime());
        String dayOfMonth = sdfDate.format(calendar.getTime());
        String dayOfWeek = sdfDayOfWeek.format(calendar.getTime());

        holder.monthText.setText(monthName);
        holder.dateText.setText(dayOfMonth);
        holder.dayOfWeekText.setText(dayOfWeek);

        Calendar currentCalendar = Calendar.getInstance();
        int currentDayOfMonth = currentCalendar.get(Calendar.DAY_OF_MONTH);

        if (calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                && calendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
                && Integer.parseInt(dayOfMonth) == currentDayOfMonth) {
            // Set background color for today's date
            holder.cardView.setCardBackgroundColor(Color.parseColor("#383F51")); // Change color as needed
            // Set text color for today's date
            holder.dateText.setTextColor(Color.WHITE); // Change color as needed
            holder.monthText.setTextColor(Color.WHITE); // Change color as needed
        } else {
            // Set default background color for other dates
            holder.cardView.setCardBackgroundColor(Color.WHITE); // Change color as needed
            // Set default text color for other dates
            holder.dateText.setTextColor(Color.BLACK); // Change color as needed
            holder.monthText.setTextColor(Color.BLACK); // Change color as needed
        }

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
            cardView = itemView.findViewById(R.id.cardView1); // Initialize the CardView



// Load Inter Bold font from resources
            Typeface interBold = ResourcesCompat.getFont(itemView.getContext(), R.font.inter_bold);
            if (interBold != null) {
                // Set Inter Bold font to TextViews
                monthText.setTypeface(interBold);
                dateText.setTypeface(interBold);
            } else {
                // Fallback to default font
                monthText.setTypeface(Typeface.DEFAULT_BOLD);
                dateText.setTypeface(Typeface.DEFAULT_BOLD);
            }
            // Set Inter Bold font to TextViews
            monthText.setTypeface(interBold);
            dateText.setTypeface(interBold);
        }
    }

    interface OnItemClickListener {
        void onItemClick(Calendar date);
    }
}
