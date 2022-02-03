package com.idan.teamusup.adapters;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.idan.teamusup.R;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.Location;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdapterGame extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "AdapterGame_TAG";
    private Activity activity;
    private ArrayList<Instance> games;
    private GameItemClickListener gameItemClickListener;

    public AdapterGame(Activity activity, ArrayList<Instance> _games) {
        this.activity = activity;
        this.games = _games;
    }

    public AdapterGame setGameItemClickListener(GameItemClickListener gameItemClickListener) {
        this.gameItemClickListener = gameItemClickListener;
        return this;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GameViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.list_game,
                        parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int index) {
        GameViewHolder gameViewHolder = (GameViewHolder) holder;
        Instance game = getGame(index);
        String totalGames, topScorer, location, timeSize, teamSize, playerSize, date;
        Map<String, Object> attributes = game.getAttributes();

        topScorer = (String) attributes.get(Constants.topScorer.name());

        location = getCityNameByLocation(game.getLocation());

        totalGames = getNumberAsString(attributes, Constants.totalGames.name());
        timeSize = getNumberAsString(attributes, Constants.timeSize.name());
        teamSize = getNumberAsString(attributes, Constants.teamSize.name());
        playerSize = getNumberAsString(attributes, Constants.playersSize.name());

        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        date = formatter.format(game.getCreatedTimestamp());

        gameViewHolder.game_LBL_totalGames.setText(totalGames);
        gameViewHolder.game_LBL_topScorer.setText(topScorer);
        gameViewHolder.game_LBL_location.setText(location);
        gameViewHolder.game_LBL_time.setText(timeSize);
        gameViewHolder.game_LBL_teamSize.setText(teamSize);
        gameViewHolder.game_LBL_playersSize.setText(playerSize);
        gameViewHolder.game_LBL_date.setText(date);
    }

    private String getNumberAsString(Map<String, Object> attributes, String key) {
        Number number;
        try {
            number = (Number) attributes.get(key);
            if (number == null) {
                number = 0;
            }
        } catch (Exception e) {
            Log.d(TAG, "getNumberAsString: caught exception: " + e);
            number = 0;
        }
        return number.intValue() + "";
    }

    private String getCityNameByLocation(Location location) {
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(
                    location.getLat(), location.getLng(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] address = addresses.get(0).getAddressLine(0).split(",");
        // Example of addresses.get(0).getAddressLine(0) - "Rachel Hirshenzon St 32, Rehovot, Israel"
        return address[address.length - 2];
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    private Instance getGame(int index) {
        return games.get(index);
    }

    public interface GameItemClickListener {
        void gameClicked(Instance game, int position);
    }

    public class GameViewHolder extends RecyclerView.ViewHolder {

        public MaterialTextView game_LBL_totalGames;
        public MaterialTextView game_LBL_topScorer;
        public MaterialTextView game_LBL_location;
        public MaterialTextView game_LBL_time;
        public MaterialTextView game_LBL_teamSize;
        public MaterialTextView game_LBL_playersSize;
        public MaterialTextView game_LBL_date;

        public GameViewHolder(final View itemView) {
            super(itemView);
            this.game_LBL_totalGames = itemView.findViewById(R.id.game_LBL_totalGames);
            this.game_LBL_topScorer = itemView.findViewById(R.id.game_LBL_topScorer);
            this.game_LBL_location = itemView.findViewById(R.id.game_LBL_location);
            this.game_LBL_time = itemView.findViewById(R.id.game_LBL_time);
            this.game_LBL_teamSize = itemView.findViewById(R.id.game_LBL_teamSize);
            this.game_LBL_playersSize = itemView.findViewById(R.id.game_LBL_playersSize);
            this.game_LBL_date = itemView.findViewById(R.id.game_LBL_date);

            itemView.setOnClickListener(v -> gameItemClickListener.gameClicked(
                    getGame(getAdapterPosition()),
                    getAdapterPosition()));
        }
    }
}
