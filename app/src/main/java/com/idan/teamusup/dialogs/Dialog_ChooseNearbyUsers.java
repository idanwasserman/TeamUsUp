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
import com.idan.teamusup.AdapterPlayer;
import com.idan.teamusup.R;
import com.idan.teamusup.data.Instance;

import java.util.ArrayList;
import java.util.List;


public class Dialog_ChooseNearbyUsers extends AppCompatDialogFragment {

    private ChooseNearbyUsersDialogListener chooseNearbyUsersDialogListener;

    private ArrayList<Instance> usersInstances;
    private RecyclerView dialog_LIST_users;
    private AdapterPlayer adapterPlayer;
    private MaterialTextView dialog_TXT_chosenUsers;
    private boolean[] chosenPositions;
    private String[] chosenUsersNames;
    private List<Instance> chosenUsers;

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
                .setTitle("Choose nearby users")
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    // Do nothing
                })
                .setPositiveButton(R.string.submit, (dialog, which) ->
                        chooseNearbyUsersDialogListener.submit(this.chosenUsers));

        return builder.create();
    }

    private void initAdapter() {
        this.adapterPlayer = new AdapterPlayer(getActivity(), this.usersInstances);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.HORIZONTAL, false);

        this.dialog_LIST_users.setLayoutManager(linearLayoutManager);
        this.dialog_LIST_users.setHasFixedSize(true);
        this.dialog_LIST_users.setItemAnimator(new DefaultItemAnimator());
        this.dialog_LIST_users.setAdapter(this.adapterPlayer);
        this.adapterPlayer.setPlayerItemClickListener(playerItemClickListener, false);

    }

    private final AdapterPlayer.PlayerItemClickListener
            playerItemClickListener = new AdapterPlayer.PlayerItemClickListener() {
        @Override
        public void editPlayerClicked(Instance player, int position) {
        }

        @Override
        public void deletePlayerClicked(Instance player, int position) {
        }

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
        final String TITLE = "Chosen players:";
        sb.append(TITLE);
        for (String name : this.chosenUsersNames) {
            if (name == null) continue;
            sb.append("\n" + name);
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
    }

    private void findViews(View view) {
        this.dialog_LIST_users = view.findViewById(R.id.dialog_LIST_users);
        this.dialog_TXT_chosenUsers = view.findViewById(R.id.dialog_TXT_chosenUsers);
    }

}
