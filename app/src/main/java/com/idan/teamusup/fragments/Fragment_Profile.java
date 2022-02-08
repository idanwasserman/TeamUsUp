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
import com.idan.teamusup.dialogs.Dialog_EditLevel;
import com.idan.teamusup.dialogs.Dialog_EditProfilePicture;
import com.idan.teamusup.dialogs.Dialog_EditUsername;
import com.idan.teamusup.logic.InstanceServiceImpl;
import com.idan.teamusup.logic.Validator;
import com.idan.teamusup.services.UserDatabase;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Map;
import java.util.Objects;


public class Fragment_Profile extends Fragment {

    private MaterialTextView profile_TXT_username;
    private MaterialTextView profile_TXT_level;
    private MaterialButton profile_BTN_logout;
    private MaterialButton profile_BTN_editPicture;
    private MaterialButton profile_BTN_editUsername;
    private MaterialButton profile_BTN_editLevel;
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
        setLevel(this.userInstance.getAttributes());
    }

    private void setLevel(Map<String, Object> attributes) {
        String levelStr = InstanceServiceImpl.getService()
                .getLevelStringFromAttributes(attributes);
        this.profile_TXT_level.setText(levelStr);
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
                                Objects.requireNonNull(getActivity()).getSupportFragmentManager(),
                                "edit profile picture dialog"));

        this.profile_BTN_editUsername.setOnClickListener(v ->
                new Dialog_EditUsername(this.editUsernameDialogListener).show(
                        Objects.requireNonNull(getActivity()).getSupportFragmentManager(),
                        "edit username dialog"));

        this.profile_BTN_editLevel.setOnClickListener(v ->
                new Dialog_EditLevel(this.editLevelDialogListener).show(
                        Objects.requireNonNull(getActivity()).getSupportFragmentManager(),
                        "edit level dialog"));
    }

    private final Dialog_EditProfilePicture.EditProfilePictureDialogListener
            editProfilePictureDialogListener = photoUrl -> {
        if (photoUrl == null || photoUrl.isEmpty()) return;
        setImage(photoUrl);
        this.onCompleteEditingListener.editProfilePicture(photoUrl);
    };

    private final Dialog_EditUsername.EditUsernameDialogListener
            editUsernameDialogListener = username -> {
        if (Validator.getInstance().isInvalidString(
                username,
                Objects.requireNonNull(getActivity())
                        .getResources().getString(R.string.invalid_username))) {
            return;
        }
        setUsernameHeader(username);
        this.onCompleteEditingListener.editUsername(username);
    };

    private final Dialog_EditLevel.EditLevelDialogListener
            editLevelDialogListener = level -> {
        this.userInstance.getAttributes().put(Constants.level.name(), level);
        this.profile_TXT_level.setText(level.name());
    };

    private void logout() {
        AuthUI.getInstance()
                .signOut(Objects.requireNonNull(getActivity()))
                .addOnCompleteListener(task -> {
                    Toast.makeText(
                            getActivity(),
                            new StringBuilder()
                                    .append(Objects.requireNonNull(getActivity())
                                            .getResources().getString(R.string.goodbye))
                                    .append(" ")
                                    .append(this.userInstance.getName()),
                            Toast.LENGTH_SHORT)
                            .show();

                    startActivity(new Intent(getActivity(), Activity_Login.class));
                    getActivity().finish();
                });
    }

    private void findViews(View view) {
        this.profile_TXT_username = view.findViewById(R.id.profile_TXT_username);
        this.profile_TXT_level = view.findViewById(R.id.profile_TXT_level);
        this.profile_BTN_logout = view.findViewById(R.id.profile_BTN_logout);
        this.profile_IMG_user = view.findViewById(R.id.profile_IMG_user);
        this.profile_BTN_editPicture = view.findViewById(R.id.profile_BTN_editPicture);
        this.profile_BTN_editUsername = view.findViewById(R.id.profile_BTN_editUsername);
        this.profile_BTN_editLevel = view.findViewById(R.id.profile_BTN_editLevel);
    }

}