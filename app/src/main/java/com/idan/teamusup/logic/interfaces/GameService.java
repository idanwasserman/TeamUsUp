package com.idan.teamusup.logic.interfaces;

import com.idan.teamusup.data.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface GameService {

    /**
     * check all size fields to start a new game
     * @param size array of size parameters
     * @param chosenPlayers amount of players that have been chosen for the game
     * @return null if all size fields are proper to start a new game,
     * else returns a string array with proper messages to user
     */
    String[] checkAllFields(Integer[] size, int chosenPlayers);

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
