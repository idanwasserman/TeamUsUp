package com.idan.teamusup.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.idan.teamusup.AdapterGame;
import com.idan.teamusup.R;
import com.idan.teamusup.activities.Activity_NewGame;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.InstanceType;
import com.idan.teamusup.services.UserDatabase;
import com.idan.teamusup.dialogs.Dialog_CreateGameForm;
import com.idan.teamusup.logic.InstanceServiceImpl;
import com.idan.teamusup.logic.interfaces.InstanceService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class Fragment_Games extends Fragment {

    private static final String TAG = "TAG_Fragment_Games";
    // Games list
    private ArrayList<Instance> gamesList;
    private RecyclerView games_LIST_games;
    private AdapterGame adapterGame;

    // Map
    private GoogleMap mMap;
    private MarkerOptions markerOptions;
    private final int ZOOM = 15;

    // Buttons
    private MaterialButton games_BTN_newGame;
    private MaterialButton games_BTN_refreshLocation;

    private Instance userInstance;
    private InstanceService instanceService;

    public Fragment_Games() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;// = inflater.inflate(R.layout.fragment_games, container, false);

        try {
            view = inflater.inflate(R.layout.fragment_games, null);
            // ... rest of body of onCreateView() ...
        } catch (Exception e) {
            Log.e(TAG, "onCreateView: ", e);
            throw e;
        }

        this.userInstance = UserDatabase.getDatabase().getUser();
        this.instanceService = new InstanceServiceImpl();
        findViews(view);
        setGamesFromDatabase();
        initGameAdapter();
        initMap();
        initButtons();

        return view;
    }

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

    private LatLng getLocationFromBundle() {
        Bundle bundle = getActivity().getIntent().getBundleExtra(Constants.bundle.name());
        return new LatLng(
                bundle.getDouble(Constants.lat.name()),
                bundle.getDouble(Constants.lng.name())
        );
    }

    private void initButtons() {
        games_BTN_newGame.setOnClickListener(v -> startActivity(new Intent(getActivity().getApplicationContext(), Activity_NewGame.class)));//openCreateGameDialog());
        games_BTN_refreshLocation.setOnClickListener(v ->
                setFocusOnMapByLocation(new LatLng(
                        this.userInstance.getLocation().getLat(),
                        this.userInstance.getLocation().getLng())));
    }

    private void openCreateGameDialog() {
        Dialog_CreateGameForm createGameForm = new Dialog_CreateGameForm(
                        (AppCompatActivity) getActivity(),
                        createGameFormDialogListener);
        final String DIALOG_TAG = "create game form dialog";
        createGameForm.show(getActivity().getSupportFragmentManager(), DIALOG_TAG);
    }

    private Dialog_CreateGameForm.CreateGameFormDialogListener createGameFormDialogListener =
            (playersSize, teamsSize, timeSize, isOk) -> {
        if (isOk) {
            Toast.makeText(getActivity(), "Good details", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity().getApplicationContext(), Activity_NewGame.class);
            intent.putExtra(Constants.bundle.name(), packBundle(playersSize, teamsSize, timeSize));
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "Wrong details", Toast.LENGTH_SHORT).show();
        }
    };

    private Bundle packBundle(int playersSize, int teamsSize, int timeSize) {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.playersSize.name(), playersSize);
        bundle.putInt(Constants.teamsSize.name(), teamsSize);
        bundle.putInt(Constants.timeSize.name(), timeSize);
        return bundle;
    }

    private void initGameAdapter() {
        this.adapterGame = new AdapterGame(getActivity(), gamesList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                        getActivity(), LinearLayoutManager.HORIZONTAL, false);

        this.games_LIST_games.setLayoutManager(linearLayoutManager);
        this.games_LIST_games.setHasFixedSize(true);
        this.games_LIST_games.setItemAnimator(new DefaultItemAnimator());
        this.games_LIST_games.setAdapter(this.adapterGame);
        this.adapterGame.setGameItemClickListener(this.gameItemClickListener);
    }

    private AdapterGame.GameItemClickListener gameItemClickListener = (game, position) ->
            setFocusOnMapByLocation(new LatLng(
                    game.getLocation().getLat(),
                    game.getLocation().getLng()));

    private void setFocusOnMapByLocation(LatLng latLng) {
        this.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM));
        this.markerOptions.position(latLng);
        this.mMap.clear();
        this.mMap.addMarker(this.markerOptions);
    }

    private void setGamesFromDatabase() {
        gamesList = (ArrayList<Instance>) instanceService
                .getAllInstancesByType(InstanceType.Game.name());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collections.sort(gamesList, Comparator.comparing(Instance::getCreatedTimestamp));
        }
    }

    private void updateDatabase(boolean sort) {

    }

    private void findViews(View view) {
        this.games_LIST_games = view.findViewById(R.id.games_LIST_games);
        this.games_BTN_newGame = view.findViewById(R.id.games_BTN_newGame);
        this.games_BTN_refreshLocation = view.findViewById(R.id.games_BTN_refreshLocation);
    }
}