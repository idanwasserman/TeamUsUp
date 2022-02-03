package com.idan.teamusup.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.idan.teamusup.R;
import com.idan.teamusup.adapters.PlayerAdapter_Big;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.InstanceType;
import com.idan.teamusup.data.Size;
import com.idan.teamusup.data.SortInstanceByName;
import com.idan.teamusup.logic.GameController;
import com.idan.teamusup.logic.GameServiceImpl;
import com.idan.teamusup.logic.InstanceServiceImpl;
import com.idan.teamusup.logic.interfaces.InstanceService;
import com.idan.teamusup.services.UserDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Activity_NewGameForm extends AppCompatActivity {

    // All players
    private ArrayList<Instance> allPlayers;
    private RecyclerView newGame_LIST_allPlayers;
    private PlayerAdapter_Big adapterAllPlayers;

    // Game players (chose from all players)
    private ArrayList<Instance> gamePlayers;
    private RecyclerView newGame_LIST_gamePlayers;
    private PlayerAdapter_Big adapterGamePlayers;
    private Set<Instance> chosenPlayers;

    // Views
    private MaterialButton form_BTN_submit;
    private TextInputLayout[] formSizes;
    private MaterialTextView newGame_TXT_numOfSelected;

    // Details
    private Integer timeSize, teamsSize, playersSize;

    // Helping objects
    private InstanceService instanceService;
    private Instance userInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game_form);

        this.instanceService = InstanceServiceImpl.getService();
        this.userInstance = UserDatabase.getDatabase().getUser();

        findViews();
        setPlayersLists();
        initAdapters();

        this.form_BTN_submit.setOnClickListener(v -> submit());
    }

    private void submit() {
        if (checkAllFields()) {
            startNewGame();
        }
    }

    private void startNewGame() {
        GameController.init(
                new ArrayList<>(this.chosenPlayers),
                getSizeArray());
        Intent intent = new Intent(this, Activity_RandomGroups.class);
        intent.putExtra(Constants.bundle.name(), packBundle());
        startActivity(intent);
        finish();
    }

    private int[] getSizeArray() {
        int[] size = new int[Size.size.ordinal()];
        size[Size.team.ordinal()] = this.teamsSize;
        size[Size.player.ordinal()] = this.playersSize;
        size[Size.time.ordinal()] = this.timeSize;
        return size;
    }

    private Bundle packBundle() {
        Bundle bundle = new Bundle();

        bundle.putBoolean(Constants.isNew.name(), true);
        bundle.putInt(Constants.playersSize.name(), this.playersSize);
        bundle.putInt(Constants.teamsSize.name(), this.teamsSize);
        bundle.putInt(Constants.timeSize.name(), this.timeSize);

        return bundle;
    }

    private boolean checkAllFields() {
        this.playersSize = getIntegerFromTextInputLayout(this.formSizes[Size.player.ordinal()]);
        this.teamsSize = getIntegerFromTextInputLayout(this.formSizes[Size.team.ordinal()]);
        this.timeSize = getIntegerFromTextInputLayout(this.formSizes[Size.time.ordinal()]);

        Integer[] size = new Integer[Size.size.ordinal()];
        size[Size.team.ordinal()] = this.teamsSize;
        size[Size.player.ordinal()] = this.playersSize;
        size[Size.time.ordinal()] = this.timeSize;

        String[] result =  GameServiceImpl
                .getService()
                .checkAllFields(size, this.gamePlayers.size());
        if (result == null) return true;

        if (result[0].equals(Constants.Toast.name())) {
            Toast.makeText(this, result[1], Toast.LENGTH_SHORT).show();
        } else {
            int errPosition = Size.valueOf(result[0]).ordinal();
            int arrSize = Size.size.ordinal();
            for (int i = 0; i < arrSize; i++) {
                if (i == errPosition) {
                    this.formSizes[i].setError(result[1]);
                } else {
                    this.formSizes[i].setError(null);
                }
            }
        }

        return false;
    }

    private Integer getIntegerFromTextInputLayout(TextInputLayout textInputLayout) {
        try {
            Editable editable = Objects.requireNonNull(textInputLayout.getEditText()).getText();
            if (editable == null) return null;
            return Integer.parseInt(editable.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void initAdapters() {
        this.adapterAllPlayers = new PlayerAdapter_Big(this, allPlayers, this.adapterAllPlayersListener);
        this.adapterGamePlayers = new PlayerAdapter_Big(this, gamePlayers, this.adapterGamePlayersListener);

        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);

        this.newGame_LIST_allPlayers.setLayoutManager(linearLayoutManager1);
        this.newGame_LIST_allPlayers.setHasFixedSize(true);
        this.newGame_LIST_allPlayers.setItemAnimator(new DefaultItemAnimator());
        this.newGame_LIST_allPlayers.setAdapter(this.adapterAllPlayers);

        this.newGame_LIST_gamePlayers.setLayoutManager(linearLayoutManager2);
        this.newGame_LIST_gamePlayers.setHasFixedSize(true);
        this.newGame_LIST_gamePlayers.setItemAnimator(new DefaultItemAnimator());
        this.newGame_LIST_gamePlayers.setAdapter(this.adapterGamePlayers);
    }

    private final PlayerAdapter_Big.PlayerItemClickListener
            adapterAllPlayersListener = this::addPlayer;

    private final PlayerAdapter_Big.PlayerItemClickListener
            adapterGamePlayersListener = this::removePlayer;

    @SuppressLint("NotifyDataSetChanged")
    private void removePlayer(Instance player, int position) {
        this.gamePlayers.remove(player);
        this.adapterGamePlayers.notifyItemRemoved(position);

        this.allPlayers.add(player);
        Collections.sort(this.allPlayers, new SortInstanceByName());
        this.adapterAllPlayers.notifyDataSetChanged();

        this.chosenPlayers.remove(player);
        updateSelectedNumberText();
    }

    private void updateSelectedNumberText() {
        this.newGame_TXT_numOfSelected.setText(String.valueOf(this.chosenPlayers.size()));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addPlayer(Instance player, int position) {
        this.gamePlayers.add(player);
        Collections.sort(this.gamePlayers, new SortInstanceByName());
        this.adapterGamePlayers.notifyDataSetChanged();

        this.allPlayers.remove(player);
        this.adapterAllPlayers.notifyItemRemoved(position);

        this.chosenPlayers.add(player);
        updateSelectedNumberText();
    }

    private void setPlayersLists() {
        this.allPlayers = (ArrayList<Instance>) this.instanceService
                .getAllInstancesByType(InstanceType.Player.name());
        Collections.sort(this.allPlayers, new SortInstanceByName());

        this.gamePlayers = new ArrayList<>();
        this.gamePlayers.add(this.userInstance);

        this.chosenPlayers = new HashSet<>();
        this.chosenPlayers.add(this.userInstance);
    }

    private void findViews() {
        this.newGame_LIST_allPlayers = findViewById(R.id.newGame_LIST_allPlayers);
        this.newGame_LIST_gamePlayers = findViewById(R.id.newGame_LIST_gamePlayers);

        this.form_BTN_submit = findViewById(R.id.form_BTN_submit);
        this.formSizes = new TextInputLayout[Size.size.ordinal()];
        this.formSizes[Size.team.ordinal()] = findViewById(R.id.form_TXTI_teamsSize);
        this.formSizes[Size.player.ordinal()] = findViewById(R.id.form_TXTI_playersSize);
        this.formSizes[Size.time.ordinal()] = findViewById(R.id.form_TXTI_timeSize);

        this.newGame_TXT_numOfSelected = findViewById(R.id.newGame_TXT_numOfSelected);
    }

}
