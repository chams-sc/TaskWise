package com.example.taskwiserebirth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class SMSFragment extends Fragment {
    View bottomNavigationView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_s_m_s, container, false);

        bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView); // Assuming bottom navigation view is defined in the activity layout

        // Set OnClickListener to the root layout
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBottomNavigationViewVisibility();
            }
        });

        return view;
    }

    private void toggleBottomNavigationViewVisibility() {
        if (bottomNavigationView.getVisibility() == View.VISIBLE) {
            bottomNavigationView.setVisibility(View.GONE);
        } else {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }
}
