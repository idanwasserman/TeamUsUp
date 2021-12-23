package com.idan.teamusup.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.button.MaterialButton;
import com.idan.teamusup.R;
import com.idan.teamusup.activities.Activity_Home;
import com.idan.teamusup.activities.MainActivity;


public class Fragment_Profile extends Fragment {

    private MaterialButton profile_BTN_logout;

    public Fragment_Profile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        findViews(view);
        initButtons();

        return view;
    }

    private void initButtons() {
        profile_BTN_logout.setOnClickListener(v -> logout());
    }

    private void logout() {
        AuthUI.getInstance()
                .signOut(getActivity())
                .addOnCompleteListener(task -> {

                    Toast.makeText(
                            getActivity(),
                            "User Signed Out",
                            Toast.LENGTH_SHORT)
                            .show();

                    Intent i = new Intent(getActivity(), MainActivity.class);
                    startActivity(i);
                });
    }

    private void findViews(View view) {
        profile_BTN_logout = view.findViewById(R.id.profile_BTN_logout);
    }


}