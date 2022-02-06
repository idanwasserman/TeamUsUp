package com.idan.teamusup.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.idan.teamusup.adapters.PlayerAdapter_Small;
import com.idan.teamusup.R;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.InstanceType;
import com.idan.teamusup.data.Level;
import com.idan.teamusup.data.SortInstanceByName;
import com.idan.teamusup.dialogs.Dialog_AddPlayerManually;
import com.idan.teamusup.dialogs.Dialog_AddPlayersByText;
import com.idan.teamusup.dialogs.Dialog_ChooseNearbyUsers;
import com.idan.teamusup.logic.InstanceServiceImpl;
import com.idan.teamusup.logic.PlayerServiceImpl;
import com.idan.teamusup.logic.interfaces.InstanceService;
import com.idan.teamusup.logic.interfaces.PlayerService;
import com.idan.teamusup.services.FirebaseRealtimeDB;
import com.idan.teamusup.services.UserDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class Fragment_Friends extends Fragment {

    //Views
    private MaterialTextView friends_TXT_emptyTitle;
    private FrameLayout friends_FRAME_searching;
    // Floating Action Buttons
    private FloatingActionButton friends_FAB_addFriends;
    private FloatingActionButton friends_FAB_byManual;
    private FloatingActionButton friends_FAB_byText;
    private FloatingActionButton friends_FAB_byLocation;
    // FABs Texts
    private MaterialTextView friends_TXT_manual;
    private MaterialTextView friends_TXT_text;
    private MaterialTextView friends_TXT_location;
    // FABs animations
    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;
    // FABs helping boolean
    private boolean clicked = false;

    // Players/Friends list
    private ArrayList<Instance> playersInstances;
    private RecyclerView friends_LIST_players;
    private PlayerAdapter_Small adapterPlayer;
    private ArrayList<Instance> usersNearby;

    // Helping objects
    private PlayerService playerService;
    private InstanceService instanceService;
    private Instance userInstance;

    private AppCompatActivity activity;

    public Fragment_Friends() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        this.userInstance = UserDatabase.getDatabase().getUser();
        this.instanceService = InstanceServiceImpl.getService();
        this.playerService = PlayerServiceImpl.getService();

        findViews(view);
        findAnimations();
        setPlayersFromDatabase();
        initAdapterPlayer();
        initButtons();

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (AppCompatActivity) context;
    }

    // Adapter methods:
    private void initAdapterPlayer() {
        this.adapterPlayer = new PlayerAdapter_Small(this.activity, this.playersInstances, this.playerItemClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                this.activity, LinearLayoutManager.VERTICAL, false);

        this.friends_LIST_players.setLayoutManager(linearLayoutManager);//.setLayoutManager(new GridLayoutManager(this.activity, 2));//
        this.friends_LIST_players.setHasFixedSize(true);
        this.friends_LIST_players.setItemAnimator(new DefaultItemAnimator());
        this.friends_LIST_players.setAdapter(this.adapterPlayer);
    }

    private void setPlayersFromDatabase() {
        this.playersInstances = (ArrayList<Instance>) this.instanceService
                .getAllInstancesByType(InstanceType.Player.name());
        if (this.playersInstances.isEmpty()) {
            this.friends_TXT_emptyTitle.setVisibility(View.VISIBLE);
        } else {
            sortPlayers();
        }
    }

    private void sortPlayers() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collections.sort(this.playersInstances, new SortInstanceByName());
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateAdapterPlayer() {
        sortPlayers();
        adapterPlayer.notifyDataSetChanged();
    }

    private void addPlayer(String name, Level level, String photoUrl) {
        Instance playerInstance = this.playerService.addPlayer(
                this.userInstance, name, level, photoUrl);
        if (playerInstance == null) return;

        // Add player to list
        this.playersInstances.add(playerInstance);
        if (this.playersInstances.size() == 1) {
            this.friends_TXT_emptyTitle.setVisibility(View.INVISIBLE);
        }
        updateAdapterPlayer();

        // Notify user
        String playerAddedText = new StringBuilder()
                .append(playerInstance.getName())
                .append(" ")
                .append(getResources().getString(R.string.added))
                .toString();
        Toast.makeText(this.activity, playerAddedText, Toast.LENGTH_SHORT).show();
    }

    private void convertText(String text) {
        List<Instance> playersFromText = this.playerService
                .convertTextToPlayers(this.userInstance, text);

        String toastText;
        if (playersFromText.size() == 0) {
            toastText = getResources().getString(R.string.zero_players_added);
            Toast.makeText(this.activity, toastText, Toast.LENGTH_SHORT).show();
            return;
        }

        // Add players to list
        this.playersInstances.addAll(playersFromText);
        updateAdapterPlayer();

        // Notify user
        toastText = new StringBuilder()
                .append(getResources().getString(R.string.added))
                .append(" ")
                .append(playersFromText.size())
                .append(" ")
                .append(getResources().getString(R.string.new_players))
                .toString();
        Toast.makeText(this.activity, toastText, Toast.LENGTH_SHORT).show();
    }

    private void removePlayer(int position, String playerId) {
        this.playersInstances.remove(position);
        if (this.playersInstances.size() == 0) {
            this.friends_TXT_emptyTitle.setVisibility(View.VISIBLE);
        }
        this.adapterPlayer.notifyItemRemoved(position);
        UserDatabase.getDatabase().removeInstanceById(playerId);
    }

    private void addPlayers(List<Instance> users) {
        this.usersNearby = (ArrayList<Instance>) users;
        final int DELAY_IN_SECONDS = 2;
        Handler handler = new Handler();
        handler.postDelayed(this.runnableMethod, DELAY_IN_SECONDS * 1000);
    }

    private void addChosenPlayers(List<Instance> chosenUsers) {
        this.friends_FRAME_searching.setVisibility(View.INVISIBLE);

        if (chosenUsers.size() == 0) {
            String text = getResources().getString(R.string.zero_users_chosen);
            Toast.makeText(this.activity, text, Toast.LENGTH_SHORT).show();
            return;
        }

        List<Instance> players = this.playerService.convertUsersToPlayers(this.userInstance, chosenUsers);

        // Add players to list
        this.playersInstances.addAll(players);
        updateAdapterPlayer();

        // Notify user
        String toastText = new StringBuilder()
                .append(getResources().getString(R.string.added))
                .append(" ")
                .append(players.size())
                .append(" ")
                .append(getResources().getString(R.string.new_players))
                .toString();
        Toast.makeText(this.activity, toastText, Toast.LENGTH_SHORT).show();
    }


    private final Runnable runnableMethod = () -> {
        Dialog_ChooseNearbyUsers dialog_chooseNearbyUsers = new Dialog_ChooseNearbyUsers(
                this.chooseNearbyUsersDialogListener, this.usersNearby);
        dialog_chooseNearbyUsers.setCancelable(false);
        dialog_chooseNearbyUsers.show(
                this.activity.getSupportFragmentManager(),
                "choose nearby users dialog");
    };

    // Listeners:
    private final Dialog_AddPlayerManually.AddPlayerDialogListener
            addPlayerDialogListener = this::addPlayer;

    private final Dialog_AddPlayersByText.AddPlayersByTextDialogListener
            addPlayersByTextDialogListener = this::convertText;

    private final Dialog_ChooseNearbyUsers.ChooseNearbyUsersDialogListener
            chooseNearbyUsersDialogListener = new Dialog_ChooseNearbyUsers.ChooseNearbyUsersDialogListener() {
        @Override
        public void submit(List<Instance> players) {
            addChosenPlayers(players);
        }

        @Override
        public void cancel() {
            friends_FRAME_searching.setVisibility(View.INVISIBLE);
        }
    };

    private final FirebaseRealtimeDB.CallBack_Users
            callBack_users = this::addPlayers;

    private final PlayerAdapter_Small.PlayerItemClickListener
            playerItemClickListener = new PlayerAdapter_Small.PlayerItemClickListener() {
        @Override
        public void editPlayerClicked(Instance player, int position) {
            editPlayer(player, position);
        }

        @Override
        public void deletePlayerClicked(Instance player, int position) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(new StringBuilder()
                            .append(getResources().getString(R.string.delete_alert))
                            .append(" ")
                            .append(player.getName())
                            .toString())
                    .setCancelable(false)
                    .setNegativeButton(R.string.no, null)
                    .setPositiveButton(R.string.yes, (dialog, which) ->
                            removePlayer(position, player.getId()))
                    .show();
        }

        @Override
        public void playerClicked(Instance player, int position) {

        }
    };

    private void editPlayer(Instance player, int position) {
        new Dialog_AddPlayerManually(
                (Dialog_AddPlayerManually.AddPlayerDialogListener) (name, level, photoUrl) ->
                        editPlayer(player, position, name, level, photoUrl),
                player.getName(),
                (Level) player.getAttributes().get(Constants.level.name()),
                (String) player.getAttributes().get(Constants.photoUrl.name()),
                true)
                        .show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(),
                                "edit player dialog");
    }

    private void editPlayer(Instance player, int position, String name, Level level, String photoUrl) {
        player.setName(name);
        Map<String, Object> attributes = player.getAttributes();
        attributes.put(Constants.level.name(), level);
        attributes.put(Constants.photoUrl.name(), photoUrl);
        this.adapterPlayer.notifyItemChanged(position);
    }


    // Buttons:
    private void initButtons() {
        this.friends_FAB_addFriends.setOnClickListener(v -> onAddPlayersButtonClicked());

        this.friends_FAB_byManual.setOnClickListener(v ->
                new Dialog_AddPlayerManually(this.addPlayerDialogListener).show(
                        Objects.requireNonNull(getActivity()).getSupportFragmentManager(),
                        "add player manually dialog"));

        this.friends_FAB_byText.setOnClickListener(v ->
                new Dialog_AddPlayersByText(this.addPlayersByTextDialogListener).show(
                        Objects.requireNonNull(getActivity()).getSupportFragmentManager(),
                        "add players by text dialog"));

        this.friends_FAB_byLocation.setOnClickListener(v -> byLocationFabClicked());
    }

    private void byLocationFabClicked() {
        onAddPlayersButtonClicked();
        this.friends_FRAME_searching.setVisibility(View.VISIBLE);
        this.playerService.getNearbyPlayers(this.userInstance, this.callBack_users);
    }


    // FAB buttons logic:
    private void onAddPlayersButtonClicked() {
        setVisibility(this.clicked);
        setAnimation(this.clicked);
        setClickable(this.clicked);
        this.clicked = !this.clicked;
    }

    private void setVisibility(boolean clicked) {
        int visibility = clicked ? View.INVISIBLE : View.VISIBLE;

        this.friends_FAB_byManual.setVisibility(visibility);
        this.friends_FAB_byText.setVisibility(visibility);
        this.friends_FAB_byLocation.setVisibility(visibility);
        this.friends_TXT_manual.setVisibility(visibility);
        this.friends_TXT_text.setVisibility(visibility);
        this.friends_TXT_location.setVisibility(visibility);
    }

    private void setAnimation(boolean clicked) {
        Animation animation;
        if (!clicked) {
            animation = this.fromBottom;
            this.friends_FAB_addFriends.startAnimation(this.rotateOpen);
        } else {
            animation = this.toBottom;
            this.friends_FAB_addFriends.startAnimation(this.rotateClose);
        }
        this.friends_FAB_byManual.setAnimation(animation);
        this.friends_FAB_byText.setAnimation(animation);
        this.friends_FAB_byLocation.setAnimation(animation);
    }

    private void setClickable(boolean clicked) {
        this.friends_FAB_byManual.setClickable(!clicked);
        this.friends_FAB_byText.setClickable(!clicked);
        this.friends_FAB_byLocation.setClickable(!clicked);
    }


    // fragment's views:
    private void findViews(View view) {
        this.friends_LIST_players = view.findViewById(R.id.friends_LIST_players);

        this.friends_FAB_addFriends = view.findViewById(R.id.friends_FAB_addFriends);
        this.friends_FAB_byManual = view.findViewById(R.id.friends_FAB_byManual);
        this.friends_FAB_byText = view.findViewById(R.id.friends_FAB_byText);
        this.friends_FAB_byLocation = view.findViewById(R.id.friends_FAB_byLocation);

        this.friends_TXT_manual = view.findViewById(R.id.friends_TXT_manual);
        this.friends_TXT_text = view.findViewById(R.id.friends_TXT_text);
        this.friends_TXT_location = view.findViewById(R.id.friends_TXT_location);

        this.friends_FRAME_searching = view.findViewById(R.id.friends_FRAME_searching);
        this.friends_TXT_emptyTitle = view.findViewById(R.id.friends_TXT_emptyTitle);
    }

    private void findAnimations() {
        this.rotateOpen = AnimationUtils.loadAnimation(this.activity, R.anim.rotate_open_anim);
        this.rotateClose = AnimationUtils.loadAnimation(this.activity, R.anim.rotate_close_anim);
        this.fromBottom = AnimationUtils.loadAnimation(this.activity, R.anim.from_bottom_anim);
        this.toBottom = AnimationUtils.loadAnimation(this.activity, R.anim.to_bottom_anim);
    }
}