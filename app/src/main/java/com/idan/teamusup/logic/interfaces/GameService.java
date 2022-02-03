package com.idan.teamusup.logic.interfaces;

import com.idan.teamusup.data.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface GameService {

    /**
     * @param team a list of players in the team
     * @param teamNumber the team number
     * @return the team's players as a text
     */
    String convertTeamToText(ArrayList<Instance> team, int teamNumber);

    /**
     * @param pointsTable a list of
     * @return game's points table as text
     */
    String[] convertPointsTableToText(List<int[]> pointsTable);

    /**
     * @param playersGoalsTable a map of player's id and his score amount
     * @return game's scoring table as text
     */
    String convertScoringTableToText(Map<String, Integer> playersGoalsTable);

}
