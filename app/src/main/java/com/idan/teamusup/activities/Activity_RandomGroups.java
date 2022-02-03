package com.idan.teamusup.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.idan.teamusup.adapters.PlayerAdapter_Big;
import com.idan.teamusup.R;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.TeamDetails;
import com.idan.teamusup.logic.GameController;
import com.idan.teamusup.logic.GameServiceImpl;
import com.idan.teamusup.logic.MyRandom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@RequiresApi(api = Build.VERSION_CODES.N)
public class Activity_RandomGroups extends AppCompatActivity {

    // Views
    private MaterialButton random_BTN_start;
    private MaterialButton random_BTN_random;
    private MaterialButton random_BTN_view;
    private MaterialButton random_BTN_finish;
    private FrameLayout random_frame_recycler;
    private FrameLayout random_frame_text;
    private MaterialTextView table_TXT_scoring;
    private MaterialTextView[] standingTable;

    // Lists  recyclers and adapters
    private ArrayList<Instance>[] teams;
    private PlayerAdapter_Big[] adapters;
    private RecyclerView[] recyclerViews;
    private FrameLayout[] frameLayouts;
    private MaterialTextView[] textViews;

    private ArrayList<Instance> allPlayers;

    private Bundle bundle;
    private int teamsSize;

    private GameController gameController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_groups);

        findViews();
        unpackBundle();
        initObjects();
        hideUnnecessaryFramesAndText();
        randomPlayersToTeams();
        initAdapters();
        initButtons();
    }

    private void initObjects() {
        this.adapters = new PlayerAdapter_Big[teamsSize];
        this.allPlayers = (ArrayList<Instance>) GameController.getInstance().getAllPlayers();
        this.teams = new ArrayList[this.teamsSize];
        for (int i = 0; i < this.teamsSize; i++) {
            this.teams[i] = new ArrayList<>();
        }
        this.gameController = GameController.getInstance();
        this.gameController.setGameTablesUpdateListener(this.standingsTableUpdateListener);
    }

    private void endGame() {
        saveGame();
        startActivity(new Intent(this, Activity_Home.class));
        finishAffinity();
    }

    private void saveGame() {
        Instance game = this.gameController.endGame();
        if (game == null) {
            Toast.makeText(this, "No matches have been played", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Game saved in my db", Toast.LENGTH_SHORT).show();
    }

    private void startMatch() {
        int currentMatch = this.gameController.getMatchNumber();
        Toast.makeText(this, "Starting match #" + currentMatch, Toast.LENGTH_SHORT).show();

        GameController.getInstance().setAllTeams(this.teams);

        Intent intent = new Intent(this, Activity_MatchProgress.class);
        intent.putExtra(Constants.bundle.name(), this.bundle);
        startActivity(intent);
    }

    
    // Listeners

    private final GameController.GameTablesUpdateListener
            standingsTableUpdateListener = new GameController.GameTablesUpdateListener() {
        @Override
        public void updateStandingsTable(List<int[]> pointsTable) {
            String[] text = GameServiceImpl
                    .getService()
                    .convertPointsTableToText(pointsTable);

            int size = TeamDetails.size.ordinal();
            for (int i = 0; i < size; i++) {
                standingTable[i].setText(text[i]);
            }
        }

        @Override
        public void updateScoringTable(Map<String, Integer> playersGoalsTable) {
            table_TXT_scoring.setText(
                    GameServiceImpl
                            .getService()
                            .convertScoringTableToText(playersGoalsTable));
        }
    };


    // Random methods:

    private void randomPlayersToTeams() {
        Instance[][] randomTeamsArr = MyRandom.getInstance()
                .randomPlayersToTeams(this.allPlayers, this.teamsSize);

        for (int i = 0; i < this.teamsSize; i++) {
            this.teams[i].removeAll(this.teams[i]);
            Collections.addAll(this.teams[i], randomTeamsArr[i]);

            // remove all nulls
            while (this.teams[i].remove(null));

            this.textViews[i].setText(
                    GameServiceImpl
                            .getService()
                            .convertTeamToText(this.teams[i], i));
        }
    }

    private void randomAgain() {
        randomPlayersToTeams();
        updateAdapters();
    }


    // Game details:

    private void unpackBundle() {
        // Get GameDay details
        this.bundle = getIntent().getBundleExtra(Constants.bundle.name());
        this.teamsSize = this.bundle.getInt(Constants.teamsSize.name());
    }


    // adapters methods:

    @SuppressLint("NotifyDataSetChanged")
    private void updateAdapters() {
        for (int i = 0; i < this.teamsSize; i++) {
            this.adapters[i].notifyDataSetChanged();
        }
    }

    private void initAdapters() {
        for (int i = 0; i < this.teamsSize; i++) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                    this, LinearLayoutManager.HORIZONTAL, false);

            this.adapters[i] = new PlayerAdapter_Big(this, this.teams[i], null);

            this.recyclerViews[i].setLayoutManager(linearLayoutManager);
            this.recyclerViews[i].setHasFixedSize(true);
            this.recyclerViews[i].setItemAnimator(new DefaultItemAnimator());
            this.recyclerViews[i].setAdapter(this.adapters[i]);
        }
    }


    // views methods:

    private void hideUnnecessaryFramesAndText() {
        for (int i = this.teamsSize; i < 4; i++) {
            this.frameLayouts[i].setVisibility(View.INVISIBLE);
            this.textViews[i].setVisibility(View.INVISIBLE);
        }
    }

    private void findViews() {
        this.random_BTN_start = findViewById(R.id.random_BTN_start);
        this.random_BTN_random = findViewById(R.id.random_BTN_random);
        this.random_BTN_view = findViewById(R.id.random_BTN_view);
        this.random_BTN_finish = findViewById(R.id.random_BTN_finish);
        this.random_frame_recycler = findViewById(R.id.random_frame_recycler);
        this.random_frame_text = findViewById(R.id.random_frame_text);
        this.table_TXT_scoring = findViewById(R.id.table_TXT_scoring);

        this.frameLayouts = new FrameLayout[] {
                null, null, findViewById(R.id.frame3), findViewById(R.id.frame4)
        };

        this.recyclerViews = new RecyclerView[] {
                findViewById(R.id.random_LIST_team1),
                findViewById(R.id.random_LIST_team2),
                findViewById(R.id.random_LIST_team3),
                findViewById(R.id.random_LIST_team4)
        };

        this.textViews = new MaterialTextView[] {
                findViewById(R.id.random_TXT_team1),
                findViewById(R.id.random_TXT_team2),
                findViewById(R.id.random_TXT_team3),
                findViewById(R.id.random_TXT_team4)
        };

        this.standingTable = new MaterialTextView[] {
                findViewById(R.id.table_TXT_id),
                findViewById(R.id.table_TXT_points),
                findViewById(R.id.table_TXT_matches),
                findViewById(R.id.table_TXT_wins),
                findViewById(R.id.table_TXT_draws),
                findViewById(R.id.table_TXT_losses),
                findViewById(R.id.table_TXT_goalsScored),
                findViewById(R.id.table_TXT_goalsAgainst),
                findViewById(R.id.table_TXT_goalsDiff)
        };
    }

    private void changeViews() {
        int recyclerVisibility = this.random_frame_recycler
                .getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
        int textVisibility = recyclerVisibility == View.VISIBLE ? View.GONE : View.VISIBLE;
        this.random_frame_recycler.setVisibility(recyclerVisibility);
        this.random_frame_text.setVisibility(textVisibility);
    }

    private void initButtons() {
        this.random_BTN_random.setOnClickListener(v -> randomAgain());
        this.random_BTN_start.setOnClickListener(v -> startMatch());
        this.random_BTN_view.setOnClickListener(v -> changeViews());
        this.random_BTN_finish.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to end game?")
                .setCancelable(false)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, (dialog, which) -> endGame())
                .show();
    }

}


