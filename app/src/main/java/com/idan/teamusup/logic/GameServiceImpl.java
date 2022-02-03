package com.idan.teamusup.logic;

import androidx.annotation.NonNull;

import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.TeamDetails;
import com.idan.teamusup.logic.interfaces.GameService;
import com.idan.teamusup.services.UserDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameServiceImpl implements GameService {

    private static GameServiceImpl service;
    private UserDatabase database;

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
    public String convertTeamToText(ArrayList<Instance> team, int teamNumber) {
        StringBuilder sb = new StringBuilder();

        sb.append("Team #" + (teamNumber+1) + ":");
        for (Instance player : team) {
            sb.append("\n" + player.getName());
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
                    sb.append("\n" + pointsTable.get(j)[i] % 10);
                } else {
                    sb.append("\n" + pointsTable.get(j)[i]);
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

            for (String name : currNamesList) {
                sb.append(position + ") " + currGoals + " - " + name + "\n");
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
