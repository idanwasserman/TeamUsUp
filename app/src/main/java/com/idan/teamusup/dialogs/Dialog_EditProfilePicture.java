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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.idan.teamusup.R;
import com.idan.teamusup.services.MyCamera;
import com.makeramen.roundedimageview.RoundedImageView;


public class Dialog_EditProfilePicture extends AppCompatDialogFragment {

    private static final String TAG = "TAG_Dialog_EditProfilePicture";
    private EditProfilePictureDialogListener editProfilePictureDialogListener;
    private RoundedImageView dialog_IMG_image;
    private MaterialButton dialog_BTN_camera;
    private String photoUrl, oldPhotoUrl, title;


    public Dialog_EditProfilePicture(
            String title,
            String oldPhotoUrl,
            EditProfilePictureDialogListener editProfilePictureDialogListener) {
        this.editProfilePictureDialogListener = editProfilePictureDialogListener;
        this.oldPhotoUrl = oldPhotoUrl;
        this.title = title;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_profile_picture, null);
        findViews(view);

        Log.d(TAG, this.oldPhotoUrl);
        if (this.oldPhotoUrl != null && !this.oldPhotoUrl.isEmpty()) {
            Glide
                    .with(getActivity())
                    .load(this.oldPhotoUrl)
                    .into(this.dialog_IMG_image);
        }

        this.dialog_BTN_camera.setOnClickListener(v -> openCamera());
        builder
                .setView(view)
                .setTitle("Edit profile picture")
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    // Do nothing
                })
                .setPositiveButton(R.string.submit, (dialog, which) ->
                        this.editProfilePictureDialogListener.editProfilePicture(this.photoUrl));

        return builder.create();
    }

    private void openCamera() {
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public interface EditProfilePictureDialogListener {
        void editProfilePicture(String photoUrl);
    }

    private void findViews(View view) {
        dialog_IMG_image = view.findViewById(R.id.dialog_IMG_image);
        dialog_BTN_camera = view.findViewById(R.id.dialog_BTN_camera);
    }

}
