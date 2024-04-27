package com.example.taskwiserebirth;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TaskViewPagerAdapter extends FragmentStateAdapter {

    public TaskViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new UnfinishedTaskFragment();
            case 1:
                return new FinishedTaskFragment();
            default:
                return new AddTaskFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
