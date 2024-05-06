package com.example.taskwiserebirth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.taskwiserebirth.task.Task;


public class TaskDetailFragment extends Fragment {

    private final Task task;

    public TaskDetailFragment (Task task) {
        this.task = task;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_detail, container, false);

        ImageView backArrowImageView = rootView.findViewById(R.id.back_arrow);
        backArrowImageView.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        setUpTaskDetail(rootView);

        return rootView;
    }

    private void setUpTaskDetail(View view) {
        TextView taskName = view.findViewById(R.id.taskNameDetail);
        TextView sched = view.findViewById(R.id.schedDetail);
        TextView deadline = view.findViewById(R.id.deadlineDetail);
        TextView importance = view.findViewById(R.id.importanceDetail);
        TextView urgency = view.findViewById(R.id.urgencyDetail);
        TextView recurrence = view.findViewById(R.id.recurrenceDetail);
        TextView notes = view.findViewById(R.id.notesDetail);
        CheckBox reminder = view.findViewById(R.id.remindMeCheckBox);

        taskName.setText(task.getTaskName());
        sched.setText(task.getSchedule());
        deadline.setText(task.getDeadline());
        importance.setText(task.getImportanceLevel());
        urgency.setText(task.getUrgencyLevel());
        recurrence.setText(task.getRecurrence());
        notes.setText(task.getNotes());
        reminder.setChecked(task.isReminder());
    }
}
