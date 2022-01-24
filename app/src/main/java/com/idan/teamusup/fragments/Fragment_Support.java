package com.idan.teamusup.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;
import com.idan.teamusup.R;


public class Fragment_Support extends Fragment {

    private LottieAnimationView lottie_SPC_construction;


    public Fragment_Support() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_support, container, false);

        findViews(view);

        return view;
    }

    private void findViews(View view) {
        lottie_SPC_construction = view.findViewById(R.id.lottie_SPC_construction);
    }
}