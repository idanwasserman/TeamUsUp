package com.idan.teamusup.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.idan.teamusup.R;
import com.idan.teamusup.activities.Activity_NewGameForm;
import com.idan.teamusup.adapters.AdapterGame;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.InstanceType;
import com.idan.teamusup.data.PlayerStats;
import com.idan.teamusup.logic.GameServiceImpl;
import com.idan.teamusup.logic.InstanceServiceImpl;
import com.idan.teamusup.logic.interfaces.InstanceService;
import com.idan.teamusup.services.MyLocation;
import com.idan.teamusup.services.UserDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class Fragment_Games extends Fragment {

    // Games list
    private ArrayList<Instance> gamesList;
    private RecyclerView games_LIST_games;

    // Map
    private GoogleMap mMap;
    private MarkerOptions markerOptions;
    private final int ZOOM = 15;

    // Buttons
    private MaterialButton games_BTN_newGame;
    private MaterialButton games_BTN_refreshLocation;

    // Game table
    private MaterialTextView[] gameTable;

    private Instance userInstance;
    private InstanceService instanceService;


    public Fragment_Games() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        this.userInstance = UserDatabase.getDatabase().getUser();
        this.instanceService = InstanceServiceImpl.getService();
        findViews(view);
        setGamesFromDatabase();
        initGameAdapter();
        initMap();
        initButtons();

        return view;
    }


    // Map

    private void initMap() {
        // Initialize map fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.google_map);
        // Async map
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(googleMap -> {
            this.mMap = googleMap;
            LatLng location = new LatLng(
                    this.userInstance.getLocation().getLat(),
                    this.userInstance.getLocation().getLng());
            // Set map to zoom on college location at first
            this.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, ZOOM));
            // Initialize marker options
            this.markerOptions = new MarkerOptions();
            this.markerOptions.position(location);
            // Add marker on map
            this.mMap.clear();
            this.mMap.addMarker(this.markerOptions);
        });
    }


    // Adapter

    private void initGameAdapter() {
        AdapterGame adapterGame = new AdapterGame(getActivity(), gamesList)
                .setGameItemClickListener(this.gameItemClickListener);

        this.games_LIST_games.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.HORIZONTAL, false));
        this.games_LIST_games.setHasFixedSize(true);
        this.games_LIST_games.setItemAnimator(new DefaultItemAnimator());
        this.games_LIST_games.setAdapter(adapterGame);
    }

    private final AdapterGame.GameItemClickListener gameItemClickListener = this::gameClicked;

    private void gameClicked(Instance game, int position) {
        // Update map
        MyLocation.getInstance().setFocusOnMapByLocation(
                this.mMap,
                this.markerOptions,
                new LatLng(
                        game.getLocation().getLat(),
                        game.getLocation().getLng()),
                ZOOM);

        // Update table
        updateTable(game.getAttributes().get(Constants.gameTable.name()));
    }

    private void updateTable(Object o) {
        if (o == null) return;

        List<Object> gameTable = (List<Object>) o;
        String[] textArr = GameServiceImpl.getService().convertGameTableToText(gameTable);
        int startIndex = PlayerStats.id.ordinal() + 1;
        for (int i = startIndex; i < PlayerStats.size.ordinal(); i++) {
            this.gameTable[i - startIndex].setText(textArr[i]);
        }
    }

    private void setGamesFromDatabase() {
        this.gamesList = (ArrayList<Instance>) this.instanceService
                .getAllInstancesByType(InstanceType.Game.name());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collections.sort(this.gamesList, Comparator.comparing(Instance::getCreatedTimestamp));
            Collections.reverse(this.gamesList);
        }
    }


    // Views

    private void initButtons() {
        games_BTN_newGame.setOnClickListener(v ->
                startActivity(new Intent(
                        Objects.requireNonNull(getActivity()).getApplicationContext(),
                        Activity_NewGameForm.class)));

        games_BTN_refreshLocation.setOnClickListener(v ->
                MyLocation.getInstance().setFocusOnMapByLocation(
                        this.mMap,
                        this.markerOptions,
                        new LatLng(
                                this.userInstance.getLocation().getLat(),
                                this.userInstance.getLocation().getLng()),
                        ZOOM));
    }

    private void findViews(View view) {
        this.games_LIST_games = view.findViewById(R.id.games_LIST_games);
        this.games_BTN_newGame = view.findViewById(R.id.games_BTN_newGame);
        this.games_BTN_refreshLocation = view.findViewById(R.id.games_BTN_refreshLocation);

        this.gameTable = new MaterialTextView[] {
                view.findViewById(R.id.table_TXT_player),
                view.findViewById(R.id.table_TXT_goals),
                view.findViewById(R.id.table_TXT_matchesPlayed),
                view.findViewById(R.id.table_TXT_wins),
                view.findViewById(R.id.table_TXT_draws),
                view.findViewById(R.id.table_TXT_losses),
                view.findViewById(R.id.table_TXT_points),
                view.findViewById(R.id.table_TXT_goalsScored),
                view.findViewById(R.id.table_TXT_goalsAgainst),
                view.findViewById(R.id.table_TXT_goalsDiff)
        };
    }

}