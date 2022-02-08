package com.idan.teamusup.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.idan.teamusup.R;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Level;
import com.idan.teamusup.services.UserDatabase;


public class Dialog_EditLevel extends AppCompatDialogFragment {

    private static final Level DEFAULT_LEVEL = Level.Normal;
    private final EditLevelDialogListener editLevelDialogListener;
    private RadioGroup dialog_RG_level;
    private Level level;

    private RadioButton dialog_RB_beginner;
    private RadioButton dialog_RB_normal;
    private RadioButton dialog_RB_professional;

    public Dialog_EditLevel(EditLevelDialogListener editLevelDialogListener) {
        this.editLevelDialogListener = editLevelDialogListener;
        setLevel();
    }

    private void setLevel() {
        try {
            this.level = (Level) UserDatabase.getDatabase().getUser().getAttributes().get(Constants.level.name());
            return;
        } catch (Exception ignored) {}

        try {
            String levelStr = (String) UserDatabase.getDatabase().getUser().getAttributes().get(Constants.level.name());
            this.level = Level.valueOf(levelStr);
        } catch (Exception ignored) {}

        if (this.level == null) {
            this.level = DEFAULT_LEVEL;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_level, null);

        findViews(view);
        setRadioButtonChecked();
        this.dialog_RG_level.setOnCheckedChangeListener(this.onCheckedChangeListener);

        builder
                .setView(view)
                .setTitle(getResources().getString(R.string.edit_level))
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    // Do nothing
                })
                .setPositiveButton(R.string.submit, (dialog, which) ->
                        editLevelDialogListener.editLevel(this.level));

        return builder.create();
    }

    private void setRadioButtonChecked() {
        switch (this.level) {
            case Beginner:
                this.dialog_RB_beginner.setChecked(true);
                break;
            case Normal:
                this.dialog_RB_normal.setChecked(true);
                break;
            case Professional:
                this.dialog_RB_professional.setChecked(true);
                break;
        }
    }

    private final RadioGroup.OnCheckedChangeListener onCheckedChangeListener = (group, checkedId) -> {
        switch (checkedId) {
            case R.id.dialog_RB_beginner:
                this.level = Level.Beginner;
                break;
            case R.id.dialog_RB_normal:
                this.level = Level.Normal;
                break;
            case R.id.dialog_RB_professional:
                this.level = Level.Professional;
                break;
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public interface EditLevelDialogListener {
        void editLevel(Level level);
    }

    private void findViews(View view) {
        this.dialog_RG_level = view.findViewById(R.id.dialog_RG_level);
        this.dialog_RB_beginner = view.findViewById(R.id.dialog_RB_beginner);
        this.dialog_RB_normal = view.findViewById(R.id.dialog_RB_normal);
        this.dialog_RB_professional = view.findViewById(R.id.dialog_RB_professional);
    }

}
