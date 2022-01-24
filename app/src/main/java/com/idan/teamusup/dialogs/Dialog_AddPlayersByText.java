package com.idan.teamusup.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.idan.teamusup.R;


public class Dialog_AddPlayersByText extends AppCompatDialogFragment {

    private final AddPlayersByTextDialogListener addPlayersByTextDialogListener;

    private TextInputLayout dialog_TXTI_text;

    public Dialog_AddPlayersByText(AddPlayersByTextDialogListener addPlayersByTextDialogListener) {
        if (addPlayersByTextDialogListener == null) {
            throw new RuntimeException("Must implement AddPlayersByTextDialogListener");
        }
        this.addPlayersByTextDialogListener = addPlayersByTextDialogListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_players_by_text, null);

        findViews(view);
        final String HINT = "Enter a list of players' names separated with new lines\n\n" +
                "For example:\n\n" +
                "Idan W\nGuy I\nMessi\nBrad Pitt\nDonald Trump\nAlice\nBob\n\n[Total: 7]";
        this.dialog_TXTI_text.getEditText().setHint(HINT);
        final String dialogTitle = "Add players by text";
        builder
                .setView(view)
                .setTitle(dialogTitle)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    // Do nothing
                })
                .setPositiveButton(R.string.submit, (dialog, which) ->
                        addPlayersByTextDialogListener.submit(getTextFromLayout()));

        return builder.create();
    }

    private String getTextFromLayout() {
        return this.dialog_TXTI_text.getEditText().getText().toString();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public interface AddPlayersByTextDialogListener {
        void submit(String text);
    }

    private void findViews(View view) {
        this.dialog_TXTI_text = view.findViewById(R.id.dialog_TXTI_text);
    }

}
