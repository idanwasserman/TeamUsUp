package com.idan.teamusup.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.idan.teamusup.services.LocaleHelper;
import com.idan.teamusup.R;

import java.util.Objects;


public class Fragment_Settings extends Fragment {

    private MaterialButton settings_BTN_changeLanguage;
    private RadioGroup settings_RG_language;
    private RadioButton settings_RB_english, settings_RB_hebrew;
    private String radioGroupLanguage;

    public Fragment_Settings() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        findViews(view);
        initRadioGroupListeners();
        initDefaultRadioButtons();
        initButtons();

        return view;
    }

    private void initDefaultRadioButtons() {
        if (LocaleHelper.HEBREW.equals(LocaleHelper.getCurrLanguage())) {
            this.settings_RB_hebrew.setChecked(true);
        } else {
            this.settings_RB_english.setChecked(true);
        }
    }

    private void initRadioGroupListeners() {
        this.settings_RG_language.setOnCheckedChangeListener(this.onCheckedLanguageChangeListener);
    }

    private void initButtons() {
        this.settings_BTN_changeLanguage.setOnClickListener(v -> changeLanguage(this.radioGroupLanguage));
    }

    private final RadioGroup.OnCheckedChangeListener onCheckedLanguageChangeListener = (group, checkedId) -> {
        switch (checkedId) {
            case R.id.settings_RB_english:
                this.radioGroupLanguage = LocaleHelper.ENGLISH;
                break;
            case R.id.settings_RB_hebrew:
                this.radioGroupLanguage = LocaleHelper.HEBREW;
                break;
        }
    };

    private void changeLanguage(String language) {
        if (LocaleHelper.getCurrLanguage().equals(language)) return;

        LocaleHelper.setLocale(getContext(), language);
        Intent intent = Objects.requireNonNull(getActivity()).getIntent();
        getActivity().finish();
        startActivity(intent);
    }

    private void findViews(View view) {
        this.settings_BTN_changeLanguage = view.findViewById(R.id.settings_BTN_changeLanguage);
        this.settings_RG_language = view.findViewById(R.id.settings_RG_language);
        this.settings_RB_english = view.findViewById(R.id.settings_RB_english);
        this.settings_RB_hebrew = view.findViewById(R.id.settings_RB_hebrew);
    }
}