package com.idan.teamusup.logic;

import android.util.Log;

import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.InstanceType;
import com.idan.teamusup.data.Location;
import com.idan.teamusup.data.TeamDetails;
import com.idan.teamusup.services.UserDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameController {

    private static GameController instance;
    private static final String TAG = "TAG_GameController";

    // Date & Location
    private Date createdTimeStamp;
    private Location location;

    // All participating players
    private List<Instance> allPlayers;

    // All the teams that played
    private List<Instance>[] allTeams;

    // All matches Played
    private List<Instance> allMatches;

    // Game stats
    private Map<String, Integer> playersGoalsTable;
    private List<int[]> pointsTable;
    private Set<Set<String>> uniqueTeams;


    // Helping objects
    private ArrayList<Instance>[] currentMatchTeams;
    private int[] currentMatchTeamsIndexes;
    private LinkedList<Integer> groupMatchesOrder;
    private int teamsSize;

    private GameTablesUpdateListener gameTablesUpdateListener;


    public static GameController getInstance() {
        return instance;
    }

    public static GameController init(List<Instance> allPlayers, int teamsSize) {
        if (instance == null) {
            instance = new GameController(allPlayers, teamsSize);
        }
        return instance;
    }

    private GameController(List<Instance> allPlayers, int teamsSize) {
        this.createdTimeStamp = new Date();
        this.location = UserDatabase.getDatabase().getUser().getLocation();
        this.teamsSize = teamsSize;

        this.allPlayers = allPlayers;
        this.allMatches = new ArrayList<>();
        this.allTeams = new ArrayList[this.teamsSize];

        initHelpingObjects();
    }

    private void initHelpingObjects() {
        this.currentMatchTeams = new ArrayList[2];
        this.currentMatchTeamsIndexes = new int[2];

        this.groupMatchesOrder = MyRandom.getInstance().randomGroupMatchesOrder(teamsSize);
        this.uniqueTeams = new HashSet<>();
        this.pointsTable = new ArrayList<>();

        this.playersGoalsTable = new HashMap<>();
        for (Instance player : allPlayers) {
            this.playersGoalsTable.put(player.getId(), 0);
        }
    }

    public interface GameTablesUpdateListener {
        void updateStandingsTable(List<int[]> pointsTable);
        void updateScoringTable(Map<String, Integer> playersGoalsTable);
    }

    public void setGameTablesUpdateListener(GameTablesUpdateListener gameTablesUpdateListener) {
        this.gameTablesUpdateListener = gameTablesUpdateListener;
    }

    public void setAllTeams(ArrayList<Instance>[] allTeams) {
        this.allTeams = allTeams;
    }

    public List<Instance> getAllPlayers() {
        return allPlayers;
    }



    public void addTeamsToOrder(int winningTeamNumber, int losingTeamNumber) {
        this.groupMatchesOrder.addFirst(winningTeamNumber);
        this.groupMatchesOrder.addLast(losingTeamNumber);
    }

    public void addMatch(Instance match) {
        // Update points table
        updatePointsTable(match);

        // Update unique teams
        updateUniqueTeams(match);

        // Update player's goals table
        updatePlayersGoalsCounter(match);

        this.allMatches.add(match);

        this.gameTablesUpdateListener.updateStandingsTable(this.pointsTable);
        this.gameTablesUpdateListener.updateScoringTable(this.playersGoalsTable);
    }

    private void updatePointsTable(Instance match) {
        // Get teams hash code
        Set<String>[] teamsPlayersIdsSetArray = (Set<String>[]) match
                .getAttributes()
                .get(Constants.teamsPlayersIds.name());
        int[] score = (int[]) match.getAttributes().get(Constants.score.name());
        int[] hashCodes = new int[] {
                teamsPlayersIdsSetArray[0].hashCode(),
                teamsPlayersIdsSetArray[1].hashCode()
        };

        for (int i = 0; i < 2; i++) {
            // Get teams details array
            int[] teamDetails = findTeamDetails(hashCodes[i]);

            // Update array
            updateTeamDetailsArray(teamDetails, score, i);
        }
    }

    private int[] findTeamDetails(int hashCode) {
        for (int[] teamDetails : this.pointsTable) {
            if (teamDetails[TeamDetails.id.ordinal()] == hashCode) {
                return teamDetails;
            }
        }

        int[] newTeamDetails = new int[TeamDetails.size.ordinal()];
        newTeamDetails[TeamDetails.id.ordinal()] = hashCode;
        this.pointsTable.add(newTeamDetails);
        return newTeamDetails;
    }

    private void updateTeamDetailsArray(int[] teamDetails, int[] score, int i) {
        teamDetails[TeamDetails.matches.ordinal()] += 1;

        int points = 0;
        if (i == 0) {
            if (score[0] > score[1]) {
                teamDetails[TeamDetails.wins.ordinal()]++;
                points = 3;
            } else if (score[0] < score[1]) {
                teamDetails[TeamDetails.losses.ordinal()]++;
            } else {
                teamDetails[TeamDetails.draws.ordinal()]++;
                points = 1;
            }
        } else if (i == 1) {
            if (score[0] > score[1]) {
                teamDetails[TeamDetails.losses.ordinal()]++;
            } else if (score[0] < score[1]) {
                teamDetails[TeamDetails.wins.ordinal()]++;
                points = 3;
            } else {
                teamDetails[TeamDetails.draws.ordinal()]++;
                points = 1;
            }
        }

        teamDetails[TeamDetails.goalsScored.ordinal()] += score[i];
        teamDetails[TeamDetails.goalsAgainst.ordinal()] += score[i ^ 1];
        teamDetails[TeamDetails.goalsDiff.ordinal()] += (score[i] - score[i ^ 1]);
        teamDetails[TeamDetails.points.ordinal()] += points;
    }

    private void updateUniqueTeams(Instance match) {
        Set<String>[] teamsPlayersIdsSetArray = (Set<String>[]) match
                .getAttributes()
                .get(Constants.teamsPlayersIds.name());
        boolean b1 = this.uniqueTeams.add(teamsPlayersIdsSetArray[0]);
        Log.d(TAG, "updateUniqueTeams: team1 is " + (b1 ? "new" : "old"));
        boolean b2 = this.uniqueTeams.add(teamsPlayersIdsSetArray[1]);
        Log.d(TAG, "updateUniqueTeams: team2 is " + (b2 ? "new" : "old"));
    }

    private void updatePlayersGoalsCounter(Instance match) {
        Map<String, Integer> matchGoalsTable = (Map<String, Integer>) match
                .getAttributes()
                .get(Constants.matchGoalsTable.name());
        if (matchGoalsTable == null) return;

        Set<String> keys = matchGoalsTable.keySet();
        for (String key : keys) {
            Integer matchGoals = matchGoalsTable.get(key);
            if (matchGoals == null) continue;

            Integer prevGoals = this.playersGoalsTable.get(key);
            if (prevGoals == null) prevGoals = 0;

            this.playersGoalsTable.put(key, prevGoals + matchGoals);
        }
    }

    public ArrayList<Instance>[] getCurrentMatchTeams() {
        return this.currentMatchTeams;
    }

    public int[] getCurrentMatchTeamsIndexes() {
        return currentMatchTeamsIndexes;
    }

    public void startingNewMatch() {
        Log.d(TAG, "startingNewMatch: groupMatchesOrder: " + groupMatchesOrder.toString());

        int team1 = this.groupMatchesOrder.pollFirst();
        int team2 = this.groupMatchesOrder.pollFirst();

        this.currentMatchTeamsIndexes[0] = team1;
        this.currentMatchTeamsIndexes[1] = team2;

        this.currentMatchTeams[0] = (ArrayList<Instance>) this.allTeams[team1];
        this.currentMatchTeams[1] = (ArrayList<Instance>) this.allTeams[team2];
    }

    public Instance endGame() {
        if (this.allMatches.size() == 0) {
            return null;
        }

        return InstanceServiceImpl.getService().createInstance(new Instance()
                .setType(InstanceType.Game)
                .setName("GameDay #?")
                .setLocation(this.location)
                .setCreatedTimestamp(this.createdTimeStamp)
                .setAttributes(packAttributes()));
    }

    private Map<String, Object> packAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(Constants.pointsTable.name(), this.pointsTable);
        attributes.put(Constants.uniqueTeams.name(), this.uniqueTeams);

        attributes.put(Constants.totalGames.name(), (int) this.allMatches.size());
        attributes.put(Constants.topScorer.name(), getTopScorer());
        attributes.put(Constants.teamSize.name(), (int) this.teamsSize);

        return attributes;
    }

    private String getTopScorer() {
        // Find the max num of goals a player scored
        int maxGoals = findMaxGoals();

        // find all player that scored the same amount of goals
        Set<String> topScorersIds = findPlayerIdsScoringMax(maxGoals);

        // Get the names of the relevant ids
        return getNamesFromIds(topScorersIds);
    }

    private String getNamesFromIds(Set<String> topScorersIds) {
        StringBuilder sb = new StringBuilder();
        int counter = 0;
        for (Instance player : this.allPlayers) {
            if (topScorersIds.contains(player.getId())) {
                if (counter == 0) {
                    sb.append(player.getName());
                } else {
                    sb.append("," + player.getName());
                }
                counter++;
            }
        }

        return sb.toString();
    }

    private Set<String> findPlayerIdsScoringMax(int maxGoals) {
        Set<String> topScorersIds = new HashSet<>();
        Set<String> keys = this.playersGoalsTable.keySet();
        for (String key : keys) {
            Integer currentGoals = this.playersGoalsTable.get(key);
            if (currentGoals != null && currentGoals == maxGoals) {
                topScorersIds.add(key);
            }
        }
        return topScorersIds;
    }

    private int findMaxGoals() {
        int maxGoals = -1;
        for (Integer value : this.playersGoalsTable.values()) {
            if (value != null && value > maxGoals) {
                maxGoals = value;
            }
        }
        return maxGoals;
    }

    public int getMatchNumber() {
        return this.allMatches.size() + 1;
    }

}
