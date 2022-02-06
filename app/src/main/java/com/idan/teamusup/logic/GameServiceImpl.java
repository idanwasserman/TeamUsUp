package com.idan.teamusup.logic;

import android.content.res.Resources;

import com.idan.teamusup.R;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.PlayerStats;
import com.idan.teamusup.data.Size;
import com.idan.teamusup.logic.interfaces.GameService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GameServiceImpl implements GameService {

    private static final int MIN_PLAYERS_SIZE = 2;
    private static final int MAX_PLAYERS_SIZE = 11;
    private static final int MIN_TEAMS_SIZE = 2;
    private static final int MAX_TEAMS_SIZE = 4;
    private static final int MIN_TIME_SIZE = 1;
    private static final int MAX_TIME_SIZE = 99;

    private final Resources resources;
    private static GameServiceImpl service;

    private GameServiceImpl(Resources resources) {
        this.resources = resources;
    }

    public static GameServiceImpl init(Resources resources) {
        if (service == null) {
            service = new GameServiceImpl(resources);
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
            return new String[] {
                    Size.player.name(),
                    this.resources.getString(R.string.this_field_is_required) };
        } else if (playersSize < MIN_PLAYERS_SIZE || playersSize > MAX_PLAYERS_SIZE) {
            String errText = String.format(
                    Locale.getDefault(),
                    this.resources.getString(R.string.proper_values),
                    MIN_PLAYERS_SIZE, MAX_PLAYERS_SIZE);
            return new String[] { Size.player.name(), errText };
        }


        Integer teamsSize = size[Size.team.ordinal()];
        if (teamsSize == null) {
            return new String[] {
                    Size.team.name(),
                    this.resources.getString(R.string.this_field_is_required) };
        } else if (teamsSize < MIN_TEAMS_SIZE || teamsSize > MAX_TEAMS_SIZE) {
            String errText = String.format(
                    Locale.getDefault(),
                    this.resources.getString(R.string.proper_values),
                    MIN_TEAMS_SIZE, MAX_TEAMS_SIZE);
            return new String[] { Size.team.name(), errText };
        }


        Integer timeSize = size[Size.time.ordinal()];
        if (timeSize == null) {
            return new String[] {
                    Size.time.name(),
                    this.resources.getString(R.string.this_field_is_required) };
        } else if (timeSize < MIN_TIME_SIZE || timeSize > MAX_TIME_SIZE) {
            String errText = String.format(
                    Locale.getDefault(),
                    this.resources.getString(R.string.proper_values),
                    MIN_TIME_SIZE, MAX_TIME_SIZE);
            return new String[] { Size.time.name(), errText };
        }


        if (teamsSize == 2) {
            if (chosenPlayers != 2 * playersSize) {
                return new String[] {
                        Constants.Toast.name(),
                        this.resources.getString(R.string.two_teams_exact_amount) };
            }
        } else {
            if (chosenPlayers < ((teamsSize - 1) * playersSize) + 1) {
                return new String[] {
                        Constants.Toast.name(),
                        this.resources.getString(R.string.not_enough_players) };
            } else if (chosenPlayers > (teamsSize * playersSize)) {
                return new String[] {
                        Constants.Toast.name(),
                        this.resources.getString(R.string.too_many_players) };
            }
        }

        return null;
    }

    @Override
    public String convertTeamToText(ArrayList<Instance> team, int teamNumber) {
        StringBuilder sb = new StringBuilder();

        sb      .append(this.resources.getString(R.string.team_number))
                .append(teamNumber+1);
//                .append(":");

        for (Instance player : team) {
            if (player == null) continue;
            sb.append("\n").append(player.getName());
        }

        return sb.toString();
    }

    @Override
    public String[] convertGameTableToText(List<Object> gameTable) {
        // Sort table
        myTableBubbleSort(gameTable);

        // Set table's rows & cols
        int rows = gameTable.size();
        int cols = PlayerStats.size.ordinal();

        // Initialize arrays
        String[] textArr = new String[cols];
        StringBuilder[] sbArr = new StringBuilder[cols];
        for (int i = 0; i < cols; i++) {
            sbArr[i] = new StringBuilder();
        }

        // For each row
        for (int i = 0; i < rows; i++) {

            // Current player stats array
            Object[] currArr = getObjectArrFromList(gameTable, i);

            // For each column
            int startIndex = PlayerStats.id.ordinal() + 1;
            for (int j = startIndex; j < cols; j++) {
                String str = currArr[j].toString();
                if (j == startIndex) {
                    sbArr[j].append(i + 1).append(")   ");
                } else {
                    double d = Double.parseDouble(str);
                    int num = (int) d;
                    str = num + "";
                }
                sbArr[j].append(str).append("\n");
            }
        }

        for (int i = 0; i < cols; i++) {
            textArr[i] = sbArr[i].toString();
        }
        return textArr;
    }

    /**
     * bubble sort of a list according to player's stats\n
     * sorting according to those parameters: player's points -> player's goals -> player's team's goals
     * @param gameTable the game table list to sort, each element is a player's game stats
     */
    private void myTableBubbleSort(List<Object> gameTable) {
        int n = gameTable.size();

        for (int i = 0; i < n - 1; i++) {

            for (int j = 0; j < n - i - 1; j++) {
                boolean swap = false;

                Object[] jArr = getObjectArrFromList(gameTable, j);
                Object[] jPlusOneArr = getObjectArrFromList(gameTable, j + 1);

                int pointsIndex = PlayerStats.points.ordinal();
                Number jPoints = (Number) jArr[pointsIndex];
                Number jPlusOnePoints = (Number) jPlusOneArr[pointsIndex];

                if (jPoints.intValue() == jPlusOnePoints.intValue()) {
                    // If players have the same amount of points -> check amount of goals

                    int goalsIndex = PlayerStats.goals.ordinal();
                    Number jGoals = (Number) jArr[goalsIndex];
                    Number jPlusOneGoals = (Number) jPlusOneArr[goalsIndex];

                    if (jGoals.intValue() == jPlusOneGoals.intValue()) {
                        // If players have the same amount of goals -> check goals scored by team

                        int goalsScoredIndex = PlayerStats.goalsScored.ordinal();
                        Number jGS = (Number) jArr[goalsScoredIndex];
                        Number jPlusOneGS = (Number) jPlusOneArr[goalsScoredIndex];

                        if (jGS.intValue() < jPlusOneGS.intValue()) {
                            swap = true;
                        }
                    } else if (jGoals.intValue() < jPlusOneGoals.intValue()) {
                        swap = true;
                    }
                } else if (jPoints.intValue() < jPlusOnePoints.intValue()) {
                    swap = true;
                }

                if (swap) {
                    Collections.swap(gameTable, j , j + 1);
                }

            }
        }
    }

    private Object[] getObjectArrFromList(List<Object> gameTable, int index) {
        try {
            return (Object[]) gameTable.get(index);
        } catch (Exception e) {
            return ((List<Object>) gameTable.get(index)).toArray(new Object[0]);
        }
    }
}
