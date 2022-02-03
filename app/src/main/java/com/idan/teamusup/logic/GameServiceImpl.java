package com.idan.teamusup.logic;

import androidx.annotation.NonNull;

import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.Size;
import com.idan.teamusup.data.TeamDetails;
import com.idan.teamusup.logic.interfaces.GameService;
import com.idan.teamusup.services.UserDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class GameServiceImpl implements GameService {

    private static final int MIN_PLAYERS_SIZE = 2;
    private static final int MAX_PLAYERS_SIZE = 11;
    private static final int MIN_TEAMS_SIZE = 2;
    private static final int MAX_TEAMS_SIZE = 4;
    private static final int MIN_TIME_SIZE = 1;
    private static final int MAX_TIME_SIZE = 99;


    private static GameServiceImpl service;
    private final UserDatabase database;

    private GameServiceImpl() {
        database = UserDatabase.getDatabase();
    }

    public static GameServiceImpl init() {
        if (service == null) {
            service = new GameServiceImpl();
        }
        return service;
    }

    public static GameServiceImpl getService() {
        return service;
    }

    @Override
    public String[] checkAllFields(Integer[] size, int chosenPlayers) {
        Integer playersSize = size[Size.player.ordinal()];
        if (playersSize == null) {
            return new String[] { Size.player.name(), "This field is required" };
        } else if (playersSize < MIN_PLAYERS_SIZE || playersSize > MAX_PLAYERS_SIZE) {
            String errText = String.format(
                    Locale.getDefault(),
                    "Proper values: [%d-%d]",
                    MIN_PLAYERS_SIZE, MAX_PLAYERS_SIZE);
            return new String[] { Size.player.name(), errText };
        }


        Integer teamsSize = size[Size.team.ordinal()];
        if (teamsSize == null) {
            return new String[] {Size.team.name(), "This field is required"};
        } else if (teamsSize < MIN_TEAMS_SIZE || teamsSize > MAX_TEAMS_SIZE) {
            String errText = String.format(
                    Locale.getDefault(),
                    "Proper values: [%d-%d]",
                    MIN_TEAMS_SIZE, MAX_TEAMS_SIZE);
            return new String[] { Size.time.name(), errText };
        }


        Integer timeSize = size[Size.time.ordinal()];
        if (timeSize == null) {
            return new String[] {Size.time.name(), "This field is required"};
        } else if (timeSize < MIN_TIME_SIZE || timeSize > MAX_TIME_SIZE) {
            String errText = String.format(
                    Locale.getDefault(),
                    "Proper values: [%d-%d]",
                    MIN_TIME_SIZE, MAX_TIME_SIZE);
            return new String[] { Size.time.name(), errText };
        }


        if (teamsSize == 2) {
            if (chosenPlayers != 2 * playersSize) {
                return new String[] {
                        Constants.Toast.name(),
                        "For 2 teams you must select exact amount of players" };
            }
        } else {
            if (chosenPlayers < ((teamsSize - 1) * playersSize) + 1) {
                return new String[] {
                        Constants.Toast.name(),
                        "You didn't choose enough players" };
            } else if (chosenPlayers > (teamsSize * playersSize)) {
                return new String[] {
                        Constants.Toast.name(),
                        "You chose too many players" };
            }
        }

        return null;
    }

    @Override
    public String convertTeamToText(ArrayList<Instance> team, int teamNumber) {
        StringBuilder sb = new StringBuilder();

        sb.append("Team #").append(teamNumber+1).append(":");
        for (Instance player : team) {
            if (player == null) continue;
            sb.append("\n").append(player.getName());
        }

        return sb.toString();
    }

    @Override
    public String[] convertPointsTableToText(List<int[]> pointsTable) {
        String[] text = new String[TeamDetails.size.ordinal()];

        myPointsTableBubbleSort(pointsTable);

        int cols = TeamDetails.size.ordinal();
        for (int i = 0; i < cols; i++) {
            StringBuilder sb = new StringBuilder();
            int rows = pointsTable.size();
            for (int j = 0; j < rows; j++) {
                if (i == 0) {
                    sb.append("\n").append(pointsTable.get(j)[i] % 100);
                } else {
                    sb.append("\n").append(pointsTable.get(j)[i]);
                }
            }
            text[i] = sb.toString();
        }

        return text;
    }

    private void myPointsTableBubbleSort(List<int[]> pointsTable) {
        int n = pointsTable.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (pointsTable.get(j)[TeamDetails.points.ordinal()] <
                        pointsTable.get(j + 1)[TeamDetails.points.ordinal()]) {
                    Collections.swap(pointsTable, j , j + 1);
                }
            }
        }
    }

    @Override
    public String convertScoringTableToText(Map<String, Integer> playersGoalsTable) {
        // For each unique amount of goals -> a linked list of players' names
        Map<Integer, LinkedList<String>> helper = getIntegerLinkedListMap(playersGoalsTable);

        // Sort goals set to descending order list
        List<Integer> sortedGoals = new ArrayList<>(helper.keySet());
        Collections.sort(sortedGoals);
        Collections.reverse(sortedGoals);

        // Build scoring table
        return buildScoringTable(sortedGoals, helper).toString();
    }

    private StringBuilder buildScoringTable(List<Integer> sortedGoals, Map<Integer, LinkedList<String>> helper) {
        StringBuilder sb = new StringBuilder();
        int size = sortedGoals.size();
        int position = 1, counter = 1;
        for (int i = 0; i < size; i++) {
            Integer currGoals = sortedGoals.get(i);
            LinkedList<String> currNamesList = helper.get(currGoals);
            if (currNamesList == null) continue;
            for (String name : currNamesList) {
                sb.append(position).append(") ").append(currGoals).append(" - ").append(name).append("\n");
                counter++;
            }
            position = counter;
        }
        return sb;
    }

    @NonNull
    private Map<Integer, LinkedList<String>> getIntegerLinkedListMap(
            Map<String, Integer> playersGoalsTable) {
        Map<Integer, LinkedList<String>> helper = new HashMap<>();

        Set<String> keys = playersGoalsTable.keySet();
        for (String key : keys) {
            Integer value = playersGoalsTable.get(key);
            if (value == null) continue;

            LinkedList<String> currValList = helper.get(value);
            if (currValList == null) {
                currValList = new LinkedList<>();
                helper.put(value, currValList);
            }

            currValList.add(getNameById(key));
        }
        return helper;
    }

    private String getNameById(String id) {
        Instance instance = database.getInstanceById(id);
        if (instance == null) {
            return "null";
        } else {
            return instance.getName();
        }
    }
}
