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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.idan.teamusup.adapters.PlayerAdapter_Big;
import com.idan.teamusup.R;
import com.idan.teamusup.data.Instance;

import java.util.ArrayList;
import java.util.List;


public class Dialog_ChooseNearbyUsers extends AppCompatDialogFragment {

    private final ChooseNearbyUsersDialogListener chooseNearbyUsersDialogListener;

    private final ArrayList<Instance> usersInstances;
    private RecyclerView dialog_LIST_users;
    private MaterialTextView dialog_TXT_chosenUsers;
    private final boolean[] chosenPositions;
    private final String[] chosenUsersNames;
    private final List<Instance> chosenUsers;

    public Dialog_ChooseNearbyUsers(
            ChooseNearbyUsersDialogListener chooseNearbyUsersDialogListener,
            ArrayList<Instance> playersInstances) {
        this.chooseNearbyUsersDialogListener = chooseNearbyUsersDialogListener;
        this.usersInstances = playersInstances;
        this.chosenPositions = new boolean[this.usersInstances.size()];
        this.chosenUsersNames = new String[this.usersInstances.size()];
        this.chosenUsers = new ArrayList<>();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_choose_nearby_users, null);
        findViews(view);

        initAdapter();
        builder
                .setView(view)
                .setTitle(getResources().getString(R.string.choose_nearby_users))
                .setNegativeButton(R.string.cancel, (dialog, which) ->
                        chooseNearbyUsersDialogListener.cancel())
                .setPositiveButton(R.string.submit, (dialog, which) ->
                        chooseNearbyUsersDialogListener.submit(this.chosenUsers));

        return builder.create();
    }

    private void initAdapter() {
        PlayerAdapter_Big adapterPlayer = new PlayerAdapter_Big(getActivity(), this.usersInstances, this.playerItemClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.HORIZONTAL, false);

        this.dialog_LIST_users.setLayoutManager(linearLayoutManager);
        this.dialog_LIST_users.setHasFixedSize(true);
        this.dialog_LIST_users.setItemAnimator(new DefaultItemAnimator());
        this.dialog_LIST_users.setAdapter(adapterPlayer);

    }

    private final PlayerAdapter_Big.PlayerItemClickListener
            playerItemClickListener = new PlayerAdapter_Big.PlayerItemClickListener() {
        @Override
        public void playerClicked(Instance player, int position) {
            if (chosenPositions[position]) {
                removePlayer(player, position);
            } else {
                addPlayer(player, position);
            }
            updateChosenPlayersText();
        }
    };

    private void updateChosenPlayersText() {
        StringBuilder sb = new StringBuilder();
        final String TITLE = getResources().getString(R.string.chosen_players);
        sb.append(TITLE);
        for (String name : this.chosenUsersNames) {
            if (name == null) continue;
            sb.append("\n").append(name);
        }
        this.dialog_TXT_chosenUsers.setText(sb.toString());
    }

    private void removePlayer(Instance player, int position) {
        this.chosenUsersNames[position] = null;
        this.chosenPositions[position] = false;
        this.chosenUsers.remove(player);
    }

    private void addPlayer(Instance player, int position) {
        this.chosenUsersNames[position] = player.getName();
        this.chosenPositions[position] = true;
        this.chosenUsers.add(player);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public interface ChooseNearbyUsersDialogListener {
        void submit(List<Instance> players);

        void cancel();
    }

    private void findViews(View view) {
        this.dialog_LIST_users = view.findViewById(R.id.dialog_LIST_users);
        this.dialog_TXT_chosenUsers = view.findViewById(R.id.dialog_TXT_chosenUsers);
    }

}
