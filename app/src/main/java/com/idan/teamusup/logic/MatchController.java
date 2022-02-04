package com.idan.teamusup.logic;

import android.util.Log;

import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.InstanceType;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class MatchController {

    private static MatchController instance;

    public static final int NUM_OF_TEAMS = 2;
    private static final String TAG = "TAG_MatchController";
    public static final int ERROR = -1;
    private static final int DRAW = 2;
    public static final String DRAW_STR = "DRAW";

    private final ArrayList<Instance>[] teamsPlayers;
    private final int[] score;
    private final Map<String, Object> goalsTable;
    private final Date createdTimeStamp;
    private final Stack<Instance> goalsStack;

    public static MatchController getInstance() {
        return instance;
    }

    public static MatchController init(ArrayList<Instance>[] teamsPlayers) {
        if (instance == null) {
            instance = new MatchController(teamsPlayers);
        }
        return instance;
    }

    private MatchController(ArrayList<Instance>[] teamsPlayers) {
        this.teamsPlayers = teamsPlayers;
        this.score = new int[NUM_OF_TEAMS];
        this.goalsTable = new HashMap<>();
        this.createdTimeStamp = new Date();
        this.goalsStack = new Stack<>();
    }


    public String getScoreOfTeam(int teamNumber) {
        return this.score[teamNumber] + "";
    }

    public int getWinningTeamNumber() {
        //FIXME
        if (this.score[0] == this.score[1]) return DRAW;
        else {
            if (this.score[0] > this.score[1]) return 0;
            else return 1;
        }
    }

    private int getLosingTeamNumber() {
        //FIXME
        return getWinningTeamNumber() == 0 ? 1 : 0;
    }

    public boolean isDraw() {
        return this.score[0] == this.score[1];
    }

    public String getWinningTeamColor() {
        if (this.score[0] > this.score[1]) {
            return "BLUE";
        } else if (this.score[0] < this.score[1]) {
            return "RED";
        } else {
            return DRAW_STR;
        }
    }

    /**
     * @return an array of 2 sets contains ids of the match's teams' players
     */
    private Set<String>[] getTeamsPlayersIdsSetArray() {
        Set<String>[] teamsIdsArray = new Set[NUM_OF_TEAMS];
        for (int i = 0; i < NUM_OF_TEAMS; i++) {
            teamsIdsArray[i] = new HashSet<>();
            for (Instance player : this.teamsPlayers[i]) {
                if (player == null) continue;

                teamsIdsArray[i].add(player.getId());
            }
        }
        return teamsIdsArray;
    }

    private int getTeamNumberOfPlayer(Instance player) {
        if (this.teamsPlayers[0].contains(player)) return 0;
        else if (this.teamsPlayers[1].contains(player)) return 1;
        else return ERROR;
    }

    /**
     *
     * @param player the player that scored a goal
     * @return player's number team [0,1,ERROR]
     */
    public int goalScored(Instance player) {
        int teamNumber = getTeamNumberOfPlayer(player);
        if (teamNumber == ERROR) {
            Log.d(TAG, "team number of " + player.toString() + " is not clear");
            return teamNumber;
        }

        updateGoalsTable(player.getId(), 1);
        this.score[teamNumber]++;
        this.goalsStack.add(player);

        return teamNumber;
    }

    /**
     * update goals table
     * @param id the player's id that scored
     * @param goals 1 - the player scored , -1 - UNDO_GOAL pressed
     */
    private void updateGoalsTable(String id, int goals) {
        Integer prevGoals = (Integer) this.goalsTable.get(id);
        if (prevGoals == null) prevGoals = 0;
        this.goalsTable.put(id, goals + prevGoals);
    }

    /**
     * if stack is not empty than pops the last scoring player and cancel his goal
     * @return string describing result of method
     */
    public String undoLastGoal() {
        if (this.goalsStack.isEmpty()) return "No one scored yet";

        Instance lastGoalPlayer = this.goalsStack.pop();
        updateGoalsTable(lastGoalPlayer.getId(), -1);
        int teamNumber = getTeamNumberOfPlayer(lastGoalPlayer);
        if (teamNumber != ERROR)
        this.score[teamNumber]--;

        return (lastGoalPlayer.getName() + "'s last goal cancelled");
    }

    public Instance endMatch(String team) {
        GameController gameController = GameController.getInstance();
        Integer[] teamIndexes = gameController.getCurrentMatchTeamsIndexes();

        if (team == null) {
            gameController.addTeamsToOrder(
                    teamIndexes[getWinningTeamNumber()],
                    teamIndexes[getLosingTeamNumber()]);
        } else {
            if (team.equals(Constants.RED.name())) {
                gameController.addTeamsToOrder(
                        teamIndexes[1],
                        teamIndexes[0]);
            } else if (team.equals(Constants.BLUE.name())) {
                gameController.addTeamsToOrder(
                        teamIndexes[0],
                        teamIndexes[1]);
            }
        }

        instance = null;
        return InstanceServiceImpl.getService()
                .createInstance(new Instance()
                        .setType(InstanceType.Match)
                        .setName("Match #" + gameController.getMatchNumber())
                        .setCreatedTimestamp(this.createdTimeStamp)
                        .setAttributes(packMatchAttributes()));
    }

    public void cancelMatch() {
        GameController.getInstance().cancelMatch();
        instance = null;
    }

    private Map<String, Object> packMatchAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(Constants.teamsPlayersIds.name(), getTeamsPlayersIdsSetArray());
        attributes.put(Constants.score.name(), this.score);
        attributes.put(Constants.matchGoalsTable.name(), this.goalsTable);
        return attributes;
    }
}
