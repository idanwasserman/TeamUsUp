package com.idan.teamusup.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.idan.teamusup.adapters.PlayerAdapter_Big;
import com.idan.teamusup.R;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.InstanceType;
import com.idan.teamusup.data.Size;
import com.idan.teamusup.data.SortInstanceByName;
import com.idan.teamusup.logic.GameController;
import com.idan.teamusup.logic.InstanceServiceImpl;
import com.idan.teamusup.logic.interfaces.InstanceService;
import com.idan.teamusup.services.UserDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Activity_NewGameForm extends AppCompatActivity {

    private static final String TAG = "Activity_NewGame_TAG";

    private ArrayList<Instance> allPlayers;
    private RecyclerView newGame_LIST_allPlayers;
    private PlayerAdapter_Big adapterAllPlayers;

    private ArrayList<Instance> gamePlayers;
    private RecyclerView newGame_LIST_gamePlayers;
    private PlayerAdapter_Big adapterGamePlayers;

    private Set<Instance> chosenPlayers;

    private MaterialButton form_BTN_submit;
    private TextInputLayout form_TXTI_playersSize;
    private TextInputLayout form_TXTI_teamsSize;
    private TextInputLayout form_TXTI_timeSize;

    private MaterialTextView newGame_TXT_numOfSelected;

    private Integer timeSize, teamsSize, playersSize;

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
        if ((this.playersSize = getIntegerFromTextInputLayout(this.form_TXTI_playersSize)) == null) {
            this.form_TXTI_playersSize.getEditText().setError("This field is required");
            return false;
        } else if (this.playersSize < 2 || this.playersSize > 11) {
            this.form_TXTI_playersSize.getEditText().setError("Proper values: [2-11]");
            return false;
        }

        if ((this.teamsSize = getIntegerFromTextInputLayout(this.form_TXTI_teamsSize)) == null) {
            this.form_TXTI_teamsSize.getEditText().setError("This field is required");
            return false;
        } else if (this.teamsSize < 2 || this. teamsSize > 4) {
            this.form_TXTI_teamsSize.getEditText().setError("Proper values: [2-4]");
            return false;
        }

        if ((this.timeSize = getIntegerFromTextInputLayout(this.form_TXTI_timeSize)) == null) {
            this.form_TXTI_timeSize.getEditText().setError("This field is required");
            return false;
        } else if (this.timeSize > 100 || this.timeSize < 1) {
            this.form_TXTI_timeSize.getEditText().setError("Proper values: [1-99]");
            return false;
        }

        if (this.gamePlayers.size() != this.teamsSize * this.playersSize) {
            return false;
        }
        if (this.gamePlayers.size() > this.playersSize * this.teamsSize) {
            Toast.makeText(this, "You chose too many players", Toast.LENGTH_SHORT).show();
            return false;
        } else if ((this.teamsSize > 2 && this.gamePlayers.size() < this.teamsSize * 2 + 1)
                ||
                (this.teamsSize == 2 && this.gamePlayers.size() < this.teamsSize * 2)) {
            Toast.makeText(this, "You chose too few players", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private Integer getIntegerFromTextInputLayout(TextInputLayout textInputLayout) {
        try {
            return Integer.parseInt(textInputLayout.getEditText().getText().toString());
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

    private PlayerAdapter_Big.PlayerItemClickListener
            adapterAllPlayersListener = (player, position) -> addPlayer(player, position);

    private PlayerAdapter_Big.PlayerItemClickListener
            adapterGamePlayersListener = (player, position) -> removePlayer(player, position);

    private void removePlayer(Instance player, int position) {
        this.gamePlayers.remove(player);
        this.adapterGamePlayers.notifyItemRemoved(position);

        this.allPlayers.add(player);
        Collections.sort(this.allPlayers, new SortInstanceByName());
        this.adapterAllPlayers.notifyDataSetChanged();

        this.chosenPlayers.remove(player);
        this.newGame_TXT_numOfSelected.setText(this.chosenPlayers.size() + "");
    }

    private void addPlayer(Instance player, int position) {
        this.gamePlayers.add(player);
        Collections.sort(this.gamePlayers, new SortInstanceByName());
        this.adapterGamePlayers.notifyDataSetChanged();

        this.allPlayers.remove(player);
        this.adapterAllPlayers.notifyItemRemoved(position);

        this.chosenPlayers.add(player);
        this.newGame_TXT_numOfSelected.setText(this.chosenPlayers.size() + "");
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
        this.form_TXTI_playersSize = findViewById(R.id.form_TXTI_playersSize);
        this.form_TXTI_teamsSize = findViewById(R.id.form_TXTI_teamsSize);
        this.form_TXTI_timeSize = findViewById(R.id.form_TXTI_timeSize);

        this.newGame_TXT_numOfSelected = findViewById(R.id.newGame_TXT_numOfSelected);
    }

}
