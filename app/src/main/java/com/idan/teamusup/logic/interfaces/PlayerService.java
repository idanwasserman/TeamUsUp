package com.idan.teamusup.logic.interfaces;

import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.Level;
import com.idan.teamusup.services.FirebaseRealtimeDB;

import java.util.List;

public interface PlayerService {

    Instance addPlayer(Instance user, String name, Level level, String photoUrl);

    List<Instance> convertTextToPlayers(Instance user, String text);

    void getNearbyPlayers(Instance user, FirebaseRealtimeDB.CallBack_Users callBack_users);

    List<Instance> convertUsersToPlayers(Instance user, List<Instance> chosenUsers);
}
