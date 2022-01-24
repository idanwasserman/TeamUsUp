package com.idan.teamusup.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.idan.teamusup.R;
import com.idan.teamusup.activities.Activity_Login;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.dialogs.Dialog_EditProfilePicture;
import com.idan.teamusup.dialogs.Dialog_EditUsername;
import com.idan.teamusup.logic.Validator;
import com.idan.teamusup.services.UserDatabase;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Objects;


public class Fragment_Profile extends Fragment {

    private static final String TAG = "Fragment_Profile_TAG";

    private MaterialTextView profile_TXT_username;
    private MaterialButton profile_BTN_logout;
    private MaterialButton profile_BTN_editPicture;
    private MaterialButton profile_BTN_editUsername;
    private RoundedImageView profile_IMG_user;

    private Instance userInstance;

    public Fragment_Profile() {
        // Required empty public constructor
    }

    public interface OnCompleteEditingListener {
        void editUsername(String username);
        void editProfilePicture(String photoUrl);
    }

    private OnCompleteEditingListener onCompleteEditingListener;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            this.onCompleteEditingListener = (OnCompleteEditingListener) context;
        } catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCompleteEditingListener");
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        this.userInstance = UserDatabase.getDatabase().getUser();
        findViews(view);
        setUserDetails();
        initButtons();

        return view;
    }

    private void setUserDetails() {
        setImage((String) this.userInstance.getAttributes().get(Constants.photoUrl.name()));
        setUsernameHeader(this.userInstance.getName());
    }

    private void setUsernameHeader(String name) {
        String helloUsername = "Hello " + name;
        this.profile_TXT_username.setText(helloUsername);
    }

    private void setImage(String url) {
        if (url != null && !url.isEmpty()) {
            Glide
                    .with(this)
                    .load(url)
                    .into(this.profile_IMG_user);
        }
    }

    private void initButtons() {
        this.profile_BTN_logout.setOnClickListener(v -> logout());

        this.profile_BTN_editPicture.setOnClickListener(v ->
                new Dialog_EditProfilePicture(
                        this.userInstance.getName() + "_profile_pic",
                        (String) this.userInstance.getAttributes().get(Constants.photoUrl.name()),
                        this.editProfilePictureDialogListener)
                        .show(
                                getActivity().getSupportFragmentManager(),
                                "edit profile picture dialog"));

        this.profile_BTN_editUsername.setOnClickListener(v ->
                new Dialog_EditUsername(this.editUsernameDialogListener).show(
                        getActivity().getSupportFragmentManager(),
                        "edit username dialog"));
    }

    private void openEditUsernameDialog() {
        Dialog_EditUsername editUsername = new Dialog_EditUsername(this.editUsernameDialogListener);
        final String DIALOG_TAG = "edit username dialog";
        editUsername.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), DIALOG_TAG);
    }

    private final Dialog_EditProfilePicture.EditProfilePictureDialogListener
            editProfilePictureDialogListener = photoUrl -> {
                if (photoUrl == null || photoUrl.isEmpty()) return;
                setImage(photoUrl);
                this.onCompleteEditingListener.editProfilePicture(photoUrl);
            };

    private final Dialog_EditUsername.EditUsernameDialogListener
            editUsernameDialogListener = username -> {
                if (Validator.getInstance().isInvalidString(username, "Invalid username")) {
                    return;
                }
                setUsernameHeader(username);
                this.onCompleteEditingListener.editUsername(username);
            };

    private void editPicture() {
        Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        AuthUI.getInstance()
                .signOut(Objects.requireNonNull(getActivity()))
                .addOnCompleteListener(task -> {
                    Toast.makeText(
                            getActivity(),
                            "Goodbye " + this.userInstance.getName(),
                            Toast.LENGTH_SHORT)
                            .show();

                    Intent i = new Intent(getActivity(), Activity_Login.class);
                    startActivity(i);
                    getActivity().finish();
                });
    }

    private void findViews(View view) {
        this.profile_TXT_username = view.findViewById(R.id.profile_TXT_username);
        this.profile_BTN_logout = view.findViewById(R.id.profile_BTN_logout);
        this.profile_IMG_user = view.findViewById(R.id.profile_IMG_user);
        this.profile_BTN_editPicture = view.findViewById(R.id.profile_BTN_editPicture);
        this.profile_BTN_editUsername = view.findViewById(R.id.profile_BTN_editUsername);
    }

}