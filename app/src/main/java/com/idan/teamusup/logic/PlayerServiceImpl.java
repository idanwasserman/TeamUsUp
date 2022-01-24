package com.idan.teamusup.logic;

import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.InstanceType;
import com.idan.teamusup.data.Level;
import com.idan.teamusup.logic.interfaces.InstanceService;
import com.idan.teamusup.logic.interfaces.PlayerService;
import com.idan.teamusup.services.FirebaseRealtimeDB;
import com.idan.teamusup.services.UserDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerServiceImpl implements PlayerService {

    private InstanceService instanceService;

    public PlayerServiceImpl(InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    @Override
    public Instance addPlayer(Instance user, String name, Level level, String photoUrl) {
        // Check name
        if (Validator.getInstance().isInvalidString(name, "Invalid name")) return null;

        // Create new Player instance
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(Constants.level.name(), level);
        attributes.put(Constants.photoUrl.name(), photoUrl);
        return this.instanceService.createInstance(
                user,
                new Instance()
                        .setName(name)
                        .setType(InstanceType.Player)
                        .setAttributes(attributes));
    }

    @Override
    public List<Instance> convertTextToPlayers(Instance user, String text) {
        List<Instance> players = new ArrayList<>();
        if (text == null || text.isEmpty()) return players;

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(Constants.level.name(), Level.Normal);
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.isEmpty()) continue;

            players.add(
                    this.instanceService.createInstance(
                            user,
                            new Instance()
                                    .setName(line)
                                    .setAttributes(attributes)
                                    .setType(InstanceType.Player)));
        }
        return players;
    }

    @Override
    public void getNearbyPlayers(Instance user, FirebaseRealtimeDB.CallBack_Users callBack_users) {
        FirebaseRealtimeDB.getRealtimeDB().getNearbyUsersIds(
                user, 50, this.callBack_usersIds, callBack_users);
    }

    @Override
    public List<Instance> convertUsersToPlayers(Instance user, List<Instance> chosenUsers) {
        List<Instance> players = new ArrayList<>();
        List<Instance> playersInstances = this.instanceService.getAllInstancesByType(InstanceType.Player.name());
        for (Instance chosenUser : chosenUsers) {
            if (playersInstances.contains(new Instance().setId(chosenUser.getId()))) continue;

            chosenUser.setType(InstanceType.Player);
            chosenUser.getAttributes().put(Constants.level.name(), Level.Normal);
            UserDatabase.getDatabase().addInstance(chosenUser);
            players.add(chosenUser);
        }
        return players;
    }

    private final FirebaseRealtimeDB.CallBack_UsersIds callBack_usersIds =
            (usersIds, callBack_users) -> FirebaseRealtimeDB
                    .getRealtimeDB()
                    .getAllUsersByIds(usersIds, callBack_users);

}
