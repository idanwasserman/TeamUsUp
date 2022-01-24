package com.idan.teamusup.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.idan.teamusup.R;
import com.idan.teamusup.data.Generator;
import com.idan.teamusup.data.Level;
import com.idan.teamusup.services.MyCamera;


public class Dialog_AddPlayerManually extends AppCompatDialogFragment {

    private static final String TAG = "TAG_Dialog_AddPlayerManually";
    private final AddPlayerDialogListener addPlayerDialogListener;

    private TextInputLayout dialog_TXTI_name;
    private RadioGroup dialog_RG_level;
    private MaterialButton dialog_BTN_camera;
    private ImageView dialog_IMG_image;

    private String photoUrl;
    private Level level = Level.Normal; // default

    public Dialog_AddPlayerManually(AddPlayerDialogListener addPlayerDialogListener) {
        if (addPlayerDialogListener == null) {
            throw new RuntimeException("Must implement AddPlayerDialogListener");
        }
        this.addPlayerDialogListener = addPlayerDialogListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_player_manually, null);

        findViews(view);
        this.dialog_BTN_camera.setOnClickListener(v -> openCamera());
        this.dialog_RG_level.setOnCheckedChangeListener(this.onCheckedChangeListener);

        final String dialogTitle = "Add player manually";
        builder
                .setView(view)
                .setTitle(dialogTitle)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    // Do nothing
                })
                .setPositiveButton(R.string.submit, (dialog, which) ->
                        addPlayerDialogListener.submit(
                                getName(),
                                this.level,
                                this.photoUrl));

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public interface AddPlayerDialogListener {
        void submit(String name, Level level, String photoUrl);
    }

    private String getName() {
        return this.dialog_TXTI_name.getEditText().getText().toString();
    }

    private void openCamera() {
        String title = getName();
        if (title == null || title.isEmpty()) {
            title = Generator.getInstance().generateRandomString();
        }
        MyCamera.getInstance().openCamera(title, this.activityResultLauncher, photo -> {
            dialog_IMG_image.setImageBitmap(photo);
            photoUrl = MyCamera.getInstance().getImageUri(getActivity(), photo);
        });
    }

    private final ActivityResultLauncher<Intent>
            activityResultLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            MyCamera.getInstance().onCameraResult(result.getData());
                        } else {
                            Log.d(TAG, "Camera is not OK!");
                        }
                    });

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

    private void findViews(View view) {
        this.dialog_TXTI_name = view.findViewById(R.id.dialog_TXTI_name);
        this.dialog_RG_level = view.findViewById(R.id.dialog_RG_level);
        this.dialog_BTN_camera = view.findViewById(R.id.dialog_BTN_camera);
        this.dialog_IMG_image = view.findViewById(R.id.dialog_IMG_image);
    }

}
