package com.idan.teamusup.logic;

import android.util.Log;

import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.SortInstanceByLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class MyRandom {

    private static final String TAG = "TAG_MyRandom";
    private static MyRandom instance;

    private final Random rand;

    private MyRandom() {
        this.rand = new Random(System.currentTimeMillis());
    }

    public static MyRandom getInstance() {
        return instance;
    }

    public static void init() {
        if (instance == null) {
            instance = new MyRandom();
        }
    }

    public Instance[][] randomPlayersToTeams(ArrayList<Instance> allPlayers, int teamsSize) {
        // Sort players by level
        Collections.sort(allPlayers, new SortInstanceByLevel());

        // Random a number for each player
        double[] randArr = randomNumberForEachElement(allPlayers.size());

        // Calculate rank for each player base on his random number
        Integer[] rankArr = calculateRankForEachElement(randArr);

        // Calculate group number for each player
        int[] groupingArr = calculateGroupFromRankForEachElement(rankArr, teamsSize);

        // Collect players by groups
        return collectPlayersToTeams(allPlayers, teamsSize, groupingArr);
    }

    private Instance[][] collectPlayersToTeams(
            ArrayList<Instance> allPlayers, int teamsSize, int[] groupingArr) {
        int size = allPlayers.size();
        int playersInTeamSize = (int) Math.ceil((float) allPlayers.size() / teamsSize);
        int[] occupiedCells = new int[teamsSize];
        Instance[][] randomTeams = new Instance[teamsSize][playersInTeamSize];

        for (int i = 0; i < size; i++) {
            int currentPlayerTeam = groupingArr[i];
            randomTeams[currentPlayerTeam][occupiedCells[currentPlayerTeam]] = allPlayers.get(i);
            occupiedCells[currentPlayerTeam]++;
        }

        return randomTeams;
    }

    private int[] calculateGroupFromRankForEachElement(Integer[] rankArr, int teamsSize) {
        int size = rankArr.length;
        int[] groupingArr = new int[size];
        int playersInTeam = (int) Math.ceil((float) size / teamsSize);

        for (int i = 0; i < size; i++) {
            groupingArr[i] = (int) Math.floor((float) (rankArr[i] - 1) / playersInTeam);
        }

        return groupingArr;
    }

    private Integer[] calculateRankForEachElement(double[] randArr) {
        int size = randArr.length;

        // Copy the given into a temp array
        double[] temp = Arrays.copyOfRange(randArr, 0 , randArr.length);

        // Sort the temp array
        Arrays.sort(temp);

        // Iterate the temp array and store the element and its index into a Hashmap
        HashMap<Double, Integer> map = new HashMap<>();
        int index = 1;
        double prev = temp[0];
        map.put(prev, index);
        for (int i = 1; i < size; i++) {
            if (prev != temp[i]) {
                index++;
            }
            map.put(temp[i], index);
            prev = temp[i];
        }

        // Initialize the rank arr
        Integer[] rankArr = new Integer[size];

        // Iterate the original array and for each element, get the index from the hashmap and store it in the rank array
        for (int i = 0; i < size; i++) {
            rankArr[i] = map.get(randArr[i]);
            if (rankArr[i] == null) {
                rankArr[i] = this.rand.nextInt(size);
            }
        }

        return rankArr;
    }

    private double[] randomNumberForEachElement(int size) {
        double[] randArr = new double[size];

        for (int i = 0; i < size; i++) {
            randArr[i] = this.rand.nextDouble();
        }

        return randArr;
    }

    public LinkedList<Integer> randomGroupMatchesOrder(int teamsSize) {
        double[] randArr = randomNumberForEachElement(teamsSize);
        Integer[] rankArr = calculateRankForEachElement(randArr);
        LinkedList<Integer> groupMatchesOrder = new LinkedList<>();
        for (int rank : rankArr) {
            groupMatchesOrder.add(rank - 1);
        }

        Log.d(TAG, "randomGroupMatchesOrder: rankArr: " + Arrays.toString(rankArr));
        Log.d(TAG, "randomGroupMatchesOrder: linkedList: " + groupMatchesOrder.toString());

        return groupMatchesOrder;
    }
}
