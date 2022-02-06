package com.idan.teamusup.logic;

import android.content.res.Resources;
import android.util.Log;

import com.idan.teamusup.R;
import com.idan.teamusup.activities.Activity_RandomGroups;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.InstanceType;
import com.idan.teamusup.data.Location;
import com.idan.teamusup.data.Size;
import com.idan.teamusup.data.PlayerStats;
import com.idan.teamusup.services.UserDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameController {

    private static GameController instance;

    private static final int WIN_PTS = 3;
    private static final int DRAW_PTS = 1;
    private static final int LOSE_PTS = 0;
    private static final String TAG = "TAG_GameController";

    // Listeners
    private Activity_RandomGroups.UpdateGameTableListener updateGameTableListener;

    // Date & Location
    private final Date createdTimeStamp;
    private final Location location;

    // All participating players
    private final List<Instance> allPlayers;

    // All the teams that played
    private List<Instance>[] allTeams;

    // All matches Played
    private final List<Instance> allMatches;

    // Game stats
    private Map<String, Integer> playersGoalsTable;
    private Set<Set<String>> uniqueTeams;
    private List<Object> gameTable;

    // Helping objects
    private final Resources resources;
    private ArrayList<Instance>[] currentMatchTeams;
    private Integer[] currentMatchTeamsIndexes;
    private LinkedList<Integer> groupMatchesOrder;
    private final int teamsSize, timeSize, playerSize;


    public static GameController getInstance() {
        return instance;
    }

    public static GameController init(Resources resources, List<Instance> allPlayers, int[] size) {
        if (instance == null) {
            instance = new GameController(resources, allPlayers, size);
        }
        return instance;
    }

    private GameController(Resources resources, List<Instance> allPlayers, int[] size) {
        this.createdTimeStamp = new Date();
        this.location = UserDatabase.getDatabase().getUser().getLocation();

        this.teamsSize = size[Size.team.ordinal()];
        this.playerSize = size[Size.player.ordinal()];
        this.timeSize = size[Size.time.ordinal()];

        this.resources = resources;
        this.allPlayers = allPlayers;
        this.allMatches = new ArrayList<>();
        this.allTeams = new ArrayList[this.teamsSize];

        initHelpingObjects();
    }

    private void initHelpingObjects() {
        this.currentMatchTeams = new ArrayList[MatchController.NUM_OF_TEAMS];
        this.currentMatchTeamsIndexes = new Integer[MatchController.NUM_OF_TEAMS];

        this.groupMatchesOrder = MyRandom.getInstance().randomGroupMatchesOrder(teamsSize);
        this.uniqueTeams = new HashSet<>();

        this.playersGoalsTable = new HashMap<>();
        for (Instance player : allPlayers) {
            this.playersGoalsTable.put(player.getId(), 0);
        }

        this.gameTable = new ArrayList<>();
        for (Instance player : this.allPlayers) {
            Object[] arr = new Object[PlayerStats.size.ordinal()];
            Arrays.fill(arr, 0);

            arr[PlayerStats.id.ordinal()] = player.getId();
            arr[PlayerStats.name.ordinal()] = player.getName();

            this.gameTable.add(arr);
        }
    }

    public Resources getResources() {
        return resources;
    }

    public int getMatchNumber() {
        return this.allMatches.size() + 1;
    }

    public List<Instance> getAllPlayers() {
        return allPlayers;
    }

    public ArrayList<Instance>[] getCurrentMatchTeams() {
        return this.currentMatchTeams;
    }

    public Integer[] getCurrentMatchTeamsIndexes() {
        return currentMatchTeamsIndexes;
    }

    private String getTopScorer() {
        // Find the max num of goals a player scored
        int maxGoals = findMaxGoals();

        // find all player that scored the same amount of goals
        Set<String> topScorersIds = findPlayerIdsScoringMax(maxGoals);

        // Get the names of the relevant ids
        return getNamesFromIds(maxGoals, topScorersIds);
    }

    private String getNamesFromIds(int maxGoals, Set<String> topScorersIds) {
        StringBuilder sb = new StringBuilder();
        int counter = 0;
        for (Instance player : this.allPlayers) {
            if (topScorersIds.contains(player.getId())) {
                if (counter == 0) {
                    sb.append(player.getName());
                } else {
                    sb.append(", ").append(player.getName());
                }
                counter++;
            }
        }

        sb.append(" - (").append(maxGoals).append(")");
        return sb.toString();
    }

    public void cancelMatch() {
        this.groupMatchesOrder.addFirst(this.currentMatchTeamsIndexes[1]);
        this.groupMatchesOrder.addFirst(this.currentMatchTeamsIndexes[0]);
    }


    public void setUpdateGameTableListener
            (Activity_RandomGroups.UpdateGameTableListener updateGameTableListener) {
        this.updateGameTableListener = updateGameTableListener;
    }

    public void setAllTeams(ArrayList<Instance>[] allTeams) {
        this.allTeams = allTeams;
    }

    public void addTeamsToOrder(int winningTeamNumber, int losingTeamNumber) {
        this.groupMatchesOrder.addFirst(winningTeamNumber);
        this.groupMatchesOrder.addLast(losingTeamNumber);
    }

    public void addMatch(Instance match) {
        this.allMatches.add(match);

        // Update unique teams
        updateUniqueTeams(match);

        // Update player's goals table
        updatePlayersGoalsCounter(match);

        // Update game's table
        updateTable(match);

        this.updateGameTableListener.updateGameTable(this.gameTable);
    }

    private void updateTable(Instance match) {
        // Prepare stats objects from match attributes
        Map<String, Object> matchAttributes = match.getAttributes();
        if (matchAttributes == null) return;

        Map<String, Integer> matchGoalsTable = (Map<String, Integer>)
                matchAttributes.get(Constants.matchGoalsTable.name());

        Set<String>[] teamsPlayersIdsSetArray = (Set<String>[])
                matchAttributes.get(Constants.teamsPlayersIds.name());

        int[] score = (int[])
                matchAttributes.get(Constants.score.name());

        if (teamsPlayersIdsSetArray == null || score == null || matchGoalsTable == null) return;

        // Update stats for each team
        for (int i = 0; i < MatchController.NUM_OF_TEAMS; i++) {
            // Current team's player points earned from last game
            int[] points = getPointsFromScore(score, i);

            // Update each team's player stats
            for (String playerId : teamsPlayersIdsSetArray[i]) {
                Object[] currArr = getPlayerStatsArrById(playerId);

                updatePlayerGoals(currArr, matchGoalsTable.get(playerId));
                updatePoints(currArr, points);
                updateTeamGoals(currArr, score, i);
            }
        }
    }

    private void updateTeamGoals(Object[] currArr, int[] score, int teamIndex) {
        int opponentTeam = teamIndex ^ 1;

        int lastGoalsScored = (int) currArr[PlayerStats.goalsScored.ordinal()];
        int newGoalsScored = lastGoalsScored + score[teamIndex];
        currArr[PlayerStats.goalsScored.ordinal()] = newGoalsScored;

        int lastGoalsAgainst = (int) currArr[PlayerStats.goalsAgainst.ordinal()];
        int newGoalsAgainst = lastGoalsAgainst + score[opponentTeam];
        currArr[PlayerStats.goalsAgainst.ordinal()] = newGoalsAgainst;

        currArr[PlayerStats.goalsDiff.ordinal()] = newGoalsScored - newGoalsAgainst;
    }

    private void updatePlayerGoals(Object[] currArr, Integer matchPlayerGoals) {
        if (matchPlayerGoals == null) matchPlayerGoals = 0;
        int lastPlayerGoals = (int) currArr[PlayerStats.goals.ordinal()];
        currArr[PlayerStats.goals.ordinal()] = matchPlayerGoals + lastPlayerGoals;
    }

    private void updatePoints(Object[] currArr, int[] points) {
        int matchesPlayed = (int) currArr[PlayerStats.matchesPlayed.ordinal()];
        currArr[PlayerStats.matchesPlayed.ordinal()] = matchesPlayed + 1;

        int matchResult = (int) currArr[points[0]];
        currArr[points[0]] = matchResult + 1;

        int playerPoints = (int) currArr[PlayerStats.points.ordinal()];
        currArr[PlayerStats.points.ordinal()] = playerPoints + points[1];
    }

    private Object[] getPlayerStatsArrById(String playerId) {
        for (Object o : this.gameTable) {
            Object[] arr = (Object[]) o;
            if (playerId.equals(arr[PlayerStats.id.ordinal()])) {
                return arr;
            }
        }

        // In case an error happened and the current player doesn't have his own stats arr
        Object[] newArr = new Object[PlayerStats.size.ordinal()];
        newArr[PlayerStats.id.ordinal()] = playerId;
        newArr[PlayerStats.name.ordinal()] = getPlayerNameById(playerId);
        return newArr;
    }

    private String getPlayerNameById(String playerId) {
        for (Instance player : this.allPlayers) {
            if (player.getId().equals(playerId)) return player.getName();
        }
        return "";
    }

    private int[] getPointsFromScore(int[] score, int teamIndex) {
        int opponentTeam = teamIndex ^ 1;

        if (score[teamIndex] > score[opponentTeam]) {
            return new int[] { PlayerStats.wins.ordinal(), WIN_PTS };
        } else if (score[teamIndex] < score[opponentTeam]) {
            return new int[] { PlayerStats.losses.ordinal(), LOSE_PTS };
        } else {
            return new int[] { PlayerStats.draws.ordinal(), DRAW_PTS };
        }
    }

    private void updateUniqueTeams(Instance match) {
        Set<String>[] teamsPlayersIdsSetArray = (Set<String>[]) match
                .getAttributes()
                .get(Constants.teamsPlayersIds.name());
        assert teamsPlayersIdsSetArray != null;
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

    public void startNewMatch() {
        for (int i = 0; i < 2; i++) {
            this.currentMatchTeamsIndexes[i] = this.groupMatchesOrder.pollFirst();
        }
        fillCurrentMatchTeams();
    }

    private void fillCurrentMatchTeams() {
        int[] teamsIndexes = new int[] {
                this.currentMatchTeamsIndexes[0],
                this.currentMatchTeamsIndexes[1] };

        for (int i = 0; i < 2; i++) {
            if (this.allTeams[teamsIndexes[i]].size() != this.playerSize) {
                Integer lastTeamInOrder = this.groupMatchesOrder.peekLast();
                if (lastTeamInOrder == null) {
                    throw new RuntimeException("fillCurrentMatchTeams: lastTeamInOrder == null");
                }

                int missingPlayersAmount = this.playerSize - this.allTeams[teamsIndexes[i]].size();
                for (int j = 0; j < missingPlayersAmount; j++) {
                    int lastTeamInOrderSize = this.allTeams[lastTeamInOrder].size();
                    int randomIndex = MyRandom.getInstance().randomInt(lastTeamInOrderSize);
                    Instance playerToAdd = this.allTeams[lastTeamInOrder].remove(randomIndex);
                    this.allTeams[teamsIndexes[i]].add(playerToAdd);
                }
            }

            this.currentMatchTeams[i] = (ArrayList<Instance>) this.allTeams[teamsIndexes[i]];
        }
    }

    public Instance endGame() {
        if (this.allMatches.size() == 0) {
            return null;
        }

        int gameDayNum = InstanceServiceImpl.getService()
                .getAllInstancesByType(InstanceType.Game.name())
                .size() + 1;
        return InstanceServiceImpl.getService().createInstance(new Instance()
                .setType(InstanceType.Game)
                .setName(new StringBuilder()
                        .append(this.resources.getString(R.string.game_day_number))
                        .append(gameDayNum)
                        .toString())
                .setLocation(this.location)
                .setCreatedTimestamp(this.createdTimeStamp)
                .setAttributes(packAttributes()));
    }

    private Map<String, Object> packAttributes() {
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(Constants.gameTable.name(), this.gameTable);
        attributes.put(Constants.uniqueTeams.name(), this.uniqueTeams);

        attributes.put(Constants.topScorer.name(), getTopScorer());

        attributes.put(Constants.teamSize.name(), this.teamsSize);
        attributes.put(Constants.timeSize.name(), this.timeSize);
        attributes.put(Constants.playersSize.name(), this.playerSize);
        attributes.put(Constants.totalPlayers.name(), this.allPlayers.size());
        attributes.put(Constants.totalGames.name(), this.allMatches.size());

        return attributes;
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

}
