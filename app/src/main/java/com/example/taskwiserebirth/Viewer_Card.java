package com.example.taskwiserebirth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class Viewer_Card extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_viewer__card, container, false);

        ImageView backArrowImageView = rootView.findViewById(R.id.back_arrow);
        backArrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to previous fragment
                requireActivity().onBackPressed();
            }
        });

        return rootView;
    }
}
