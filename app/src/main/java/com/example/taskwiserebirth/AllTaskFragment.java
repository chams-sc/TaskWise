package com.example.taskwiserebirth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AllTaskFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private int selectedTabIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_all_task, container, false);

        ImageView imageView = rootView.findViewById(R.id.back_arrow);
        imageView.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        viewPager = rootView.findViewById(R.id.view_pager);
        tabLayout = rootView.findViewById(R.id.tab_layout);

        TaskPagerAdapter pagerAdapter = new TaskPagerAdapter(requireActivity());
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Unfinished");
            } else if (position == 1) {
                tab.setText("Finished");
            }
        }).attach();

        // Set the saved selected tab index
        viewPager.post(() -> {
            viewPager.setCurrentItem(selectedTabIndex, false);
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    selectedTabIndex = position;
                }
            });
        });

        viewPager.setSaveEnabled(false);

        return rootView;
    }

    public void setSelectedTabIndex(int index) {
        selectedTabIndex = index;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Postpone the removal of fragments until after the current transaction is completed
        new Handler(Looper.getMainLooper()).post(() -> {
            // Release ViewPager2 and its adapter
            viewPager.setAdapter(null);
            viewPager = null;
            tabLayout = null;
        });
    }
}
