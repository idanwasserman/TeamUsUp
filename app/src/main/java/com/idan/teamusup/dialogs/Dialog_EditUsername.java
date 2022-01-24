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


public class Dialog_EditUsername extends AppCompatDialogFragment {

    private EditUsernameDialogListener editUsernameDialogListener;
    private TextInputLayout form_TXTI_username;

    public Dialog_EditUsername(EditUsernameDialogListener editUsernameDialogListener) {
        this.editUsernameDialogListener = editUsernameDialogListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_username, null);
        findViews(view);

        builder
                .setView(view)
                .setTitle("Edit username")
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    // Do nothing
                })
                .setPositiveButton(R.string.submit, (dialog, which) ->
                        editUsernameDialogListener.editUsername(
                                this.form_TXTI_username.getEditText().getText().toString()));

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public interface EditUsernameDialogListener {
        void editUsername(String username);
    }

    private void findViews(View view) {
        this.form_TXTI_username = view.findViewById(R.id.form_TXTI_username);
    }

}
