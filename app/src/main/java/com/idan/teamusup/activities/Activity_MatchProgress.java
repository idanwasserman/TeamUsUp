package com.idan.teamusup.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.idan.teamusup.adapters.PlayerAdapter_Big;
import com.idan.teamusup.R;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.logic.GameController;
import com.idan.teamusup.logic.MatchController;

import java.util.ArrayList;
import java.util.Locale;


public class Activity_MatchProgress extends AppCompatActivity {

    private final static int NUM_OF_TEAMS = 2;
    private final static long ONE_MINUTE_IN_MILLIS = 60000;
    private static final String TIME_FORMAT = "%02d:%02d";
    private static final int VIBRATION_DURATION = 500;
    private static final int MILLIS_IN_A_SEC = 1000;

    // Views
    private MaterialTextView match_TXT_title;
    private MaterialTextView[] match_TXT_score;
    private MaterialTextView match_TXT_countDown;
    private MaterialButton match_BTN_startPause;
    private MaterialButton match_BTN_reset;
    private MaterialButton match_BTN_undo;
    private MaterialButton match_BTN_end;

    // Lists recyclers and adapters
    private RecyclerView[] recyclerViews;
    private ArrayList<Instance>[] teams;
    private PlayerAdapter_Big[] adapters;

    // Timer objects
    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long timeLeftInMillis = ONE_MINUTE_IN_MILLIS;
    private int timeSize;

    // Helping object
//    private Bundle bundle;
    private Vibrator vibrator;
    private MatchController matchController;
    private GameController gameController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_progress);
        
        findViews();
        unpackBundle();
        initObjects();
        initAdapters();
        initButtons();
        updateCountDownText();
    }


    private void initObjects() {
        this.gameController = GameController.getInstance();
        this.adapters = new PlayerAdapter_Big[2];
        this.timeLeftInMillis = this.timeSize * ONE_MINUTE_IN_MILLIS;
        this.vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        this.match_TXT_title.setText(String.format(
                Locale.getDefault(),
                getResources().getString(R.string.match_number),
                this.gameController.getMatchNumber()));

        this.gameController.startNewMatch();
        this.teams = this.gameController.getCurrentMatchTeams();

        this.matchController = MatchController.init(this.teams);
    }

    private void unpackBundle() {
        Bundle bundle = getIntent().getBundleExtra(Constants.bundle.name());
        this.timeSize = bundle.getInt(Constants.timeSize.name());
    }

    private void endMatch() {
        // Check that timer is over
        if (this.timerRunning) {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.end_match_timer_running))
                    .setCancelable(false)
                    .setNegativeButton(R.string.no, null)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        pauseTimer();
                        endMatch();
                    })
                    .show();
        } else {
            // Check if the game is in a draw
            if (this.matchController.isDraw()) {
                new AlertDialog.Builder(this)
                        .setMessage(getResources().getString(R.string.pick_winning_team))
                        .setCancelable(false)
                        .setNegativeButton(R.string.BLUE, (dialog, which) ->
                                endMatch(Constants.BLUE.name()))
                        .setPositiveButton(R.string.RED, (dialog, which) ->
                                endMatch(Constants.RED.name()))
                        .show();
            } else {
                endMatch(null);
            }
        }
    }

    private void endMatch(String team) {
        this.gameController.addMatch(
                this.matchController.endMatch(team));
        notifyUserMatchResults();
        finish();
    }

    private void notifyUserMatchResults() {
        String winningTeamColor = this.matchController.getWinningTeamColor();
        String text;
        if (winningTeamColor.equals(MatchController.DRAW_STR)) {
            text = getResources().getString(R.string.match_ended_draw);
        } else {
            text = new StringBuilder()
                    .append(getResources().getString(R.string.team))
                    .append(" ")
                    .append(winningTeamColor)
                    .append(" ")
                    .append(getResources().getString(R.string.won))
                    .toString();
        }
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void undoLastGoal() {
        String msg = this.matchController.undoLastGoal();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        this.match_TXT_score[0].setText(
                this.matchController.getScoreOfTeam(0));
        this.match_TXT_score[1].setText(
                this.matchController.getScoreOfTeam(1));
    }


    // Adapter methods:

    private void initAdapters() {
        for (int i = 0; i < NUM_OF_TEAMS; i++) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                    this, LinearLayoutManager.HORIZONTAL, false);

            this.adapters[i] = new PlayerAdapter_Big(
                    this, this.teams[i], this.playerItemClickListener);

            this.recyclerViews[i].setLayoutManager(linearLayoutManager);
            this.recyclerViews[i].setHasFixedSize(true);
            this.recyclerViews[i].setItemAnimator(new DefaultItemAnimator());
            this.recyclerViews[i].setAdapter(this.adapters[i]);
        }
    }

    private final PlayerAdapter_Big.PlayerItemClickListener
            playerItemClickListener = this::updateScores;

    private void updateScores(Instance player, int position) {
        if (player == null) return;

        String text = new StringBuilder()
                .append(player.getName())
                .append(" ")
                .append(getResources().getString(R.string.scored))
                .toString();
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        int teamNumber = this.matchController.goalScored(player);
        if (teamNumber == MatchController.ERROR) return;

        this.match_TXT_score[teamNumber].setText(
                this.matchController.getScoreOfTeam(teamNumber));
    }


    // Timer methods:

    private void startTimer() {
        this.countDownTimer = new CountDownTimer(this.timeLeftInMillis, MILLIS_IN_A_SEC) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                vibrator.vibrate(MILLIS_IN_A_SEC);
                String text = getResources().getString(R.string.time_is_over);
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                timerRunning = false;
                match_BTN_startPause.setBackgroundColor(
                        ContextCompat.getColor(getApplicationContext(), R.color.kind_of_blue));
                match_BTN_startPause.setText(R.string.start);
                match_BTN_startPause.setVisibility(View.INVISIBLE);
                match_BTN_reset.setVisibility(View.VISIBLE);
            }
        }.start();

        this.timerRunning = true;
        this.match_BTN_startPause.setBackgroundColor(
                ContextCompat.getColor(getApplicationContext(), R.color.kind_of_red));
        this.match_BTN_startPause.setText(R.string.pause);
        this.match_BTN_reset.setVisibility(View.INVISIBLE);
    }

    private void lastMinuteAlert() {
        String text = getResources().getString(R.string.last_minute);
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        this.vibrator.vibrate(VIBRATION_DURATION);
    }

    private void pauseTimer() {
        this.countDownTimer.cancel();
        this.timerRunning = false;
        this.match_BTN_startPause.setBackgroundColor(
                ContextCompat.getColor(getApplicationContext(), R.color.kind_of_blue));
        this.match_BTN_startPause.setText(R.string.start);
        this.match_BTN_reset.setVisibility(View.VISIBLE);
    }

    private void resetTimer() {
        this.timeLeftInMillis = this.timeSize * ONE_MINUTE_IN_MILLIS;
        updateCountDownText();
        this.match_BTN_reset.setVisibility(View.INVISIBLE);
        this.match_BTN_startPause.setVisibility(View.VISIBLE);
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / MILLIS_IN_A_SEC) / 60;
        int seconds = (int) (timeLeftInMillis / MILLIS_IN_A_SEC) % 60;
        String timeLeftFormatted = String.format(
                Locale.getDefault() ,TIME_FORMAT, minutes, seconds);
        this.match_TXT_countDown.setText(timeLeftFormatted);
        if (this.timerRunning && minutes == 1 && seconds == 0) lastMinuteAlert();
    }


    // Views methods:

    private void initButtons() {
        this.match_BTN_startPause.setOnClickListener(v -> {
            if (this.timerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });
        this.match_BTN_reset.setOnClickListener(v -> resetTimer());
        this.match_BTN_undo.setOnClickListener(v -> undoLastGoal());
        this.match_BTN_end.setOnClickListener(v -> endMatch());
    }

    private void findViews() {
        this.match_TXT_score = new MaterialTextView[] {
                findViewById(R.id.match_TXT_score1),
                findViewById(R.id.match_TXT_score2)
        };
        this.match_TXT_countDown = findViewById(R.id.match_TXT_countDown);
        this.match_BTN_startPause = findViewById(R.id.match_BTN_startPause);
        this.match_BTN_reset = findViewById(R.id.match_BTN_reset);
        this.match_BTN_undo = findViewById(R.id.match_BTN_undo);
        this.match_BTN_end = findViewById(R.id.match_BTN_end);
        this.match_TXT_title = findViewById(R.id.match_TXT_title);
        this.recyclerViews = new RecyclerView[] {
                findViewById(R.id.match_LIST_team1),
                findViewById(R.id.match_LIST_team2)
        };
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.exit_match_alert))
                .setCancelable(false)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    this.matchController.cancelMatch();
                    finish();
                })
                .show();
    }
}


