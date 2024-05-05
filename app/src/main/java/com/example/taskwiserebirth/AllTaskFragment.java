package com.example.taskwiserebirth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

public class AllTaskFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private TaskViewPagerAdapter taskViewPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_all_task, container, false);

        ImageView imageView = rootView.findViewById(R.id.back_arrow);
        imageView.setOnClickListener(v -> {
            // Navigate to Add Task Fragment
            AddTaskFragment addTaskFragment = new AddTaskFragment();
            ((MainActivity) requireActivity()).replaceFragment(addTaskFragment);
        });

        tabLayout = rootView.findViewById(R.id.tab_layout);
        viewPager2 = rootView.findViewById(R.id.view_pager);
        taskViewPagerAdapter = new TaskViewPagerAdapter(requireActivity());
        viewPager2.setAdapter(taskViewPagerAdapter);

        tabLayout.addOnTabSelectedListener(tabSelectedListener);
        viewPager2.registerOnPageChangeCallback(pageChangeCallback);

        return rootView;
    }

    private final TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            viewPager2.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {}

        @Override
        public void onTabReselected(TabLayout.Tab tab) {}
    };

    private final ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            tabLayout.getTabAt(position).select();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tabLayout.removeOnTabSelectedListener(tabSelectedListener);
        viewPager2.unregisterOnPageChangeCallback(pageChangeCallback);
        tabLayout = null;
        viewPager2 = null;
        taskViewPagerAdapter = null;
    }
}
