package com.idan.teamusup.adapters;

import android.app.Activity;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textview.MaterialTextView;
import com.idan.teamusup.R;
import com.idan.teamusup.data.Constants;
import com.idan.teamusup.data.Instance;
import com.idan.teamusup.data.InstanceType;
import com.idan.teamusup.data.Level;

import java.util.ArrayList;
import java.util.Map;

public class PlayerAdapter_Small extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private ArrayList<Instance> players;
    private PlayerItemClickListener playerItemClickListener;

    public PlayerAdapter_Small(
            Activity activity,
            ArrayList<Instance> _players,
            PlayerItemClickListener playerItemClickListener) {
        this.activity = activity;
        this.players = _players;
        this.playerItemClickListener = playerItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlayerViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.list_player_small,
                        parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int index) {
        PlayerViewHolder playerViewHolder = (PlayerViewHolder) holder;
        Instance player = getPlayer(index);
        if (player == null)
            return;

        String photoUrl = getPhotoUrl(player);
        String levelStr = getLevel(player);

        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide
                    .with(activity)
                    .load(photoUrl)
                    .into(playerViewHolder.player_IMG_image);
        } else {
            playerViewHolder.player_IMG_image.setImageResource(R.drawable.icn_user);
        }
        playerViewHolder.player_LBL_level.setText(levelStr);
        playerViewHolder.player_LBL_name.setText(player.getName());
    }

    private String getPhotoUrl(Instance player) {
        Map<String, Object> attributes = player.getAttributes();
        String imageLink = (String) attributes.get("imageLink");
        if (imageLink != null) {
            attributes.remove("imageLink");
            attributes.put(Constants.photoUrl.name(), imageLink);
            return imageLink;
        }
        return (String) attributes.get(Constants.photoUrl.name());
    }

    private String getLevel(Instance player) {
        final String DEFAULT = Level.Normal.name();
        if (player.getType() != InstanceType.Player) return DEFAULT;

        try {
            return ((Level) player.getAttributes().get(Constants.level.name())).name();
        } catch (ClassCastException e) {
            player.getAttributes().put(Constants.level.name(), Level.Normal);
            return DEFAULT;
        }
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    private Instance getPlayer(int index) {
        try {
            return players.get(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public interface PlayerItemClickListener {
        void editPlayerClicked(Instance player, int position);
        void deletePlayerClicked(Instance player, int position);
        void playerClicked(Instance player, int position);
    }

    public class PlayerViewHolder extends RecyclerView.ViewHolder {

        public AppCompatImageView player_IMG_image;
        public AppCompatImageView player_IMG_edit;
        public AppCompatImageView player_IMG_delete;
        public MaterialTextView player_LBL_level;
        public MaterialTextView player_LBL_name;

        private long lastClickTime = 0;

        public PlayerViewHolder(final View itemView) {
            super(itemView);
            this.player_IMG_image = itemView.findViewById(R.id.player_IMG_image);
            this.player_IMG_edit = itemView.findViewById(R.id.player_IMG_edit);
            this.player_IMG_delete = itemView.findViewById(R.id.player_IMG_delete);
            this.player_LBL_level = itemView.findViewById(R.id.player_LBL_level);
            this.player_LBL_name = itemView.findViewById(R.id.player_LBL_name);

            itemView.setOnClickListener(v -> {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return;
                lastClickTime = SystemClock.elapsedRealtime();
                if (playerItemClickListener != null) {
                    playerItemClickListener.playerClicked(
                            getPlayer(getAdapterPosition()),
                            getAdapterPosition());
                }
            });

            player_IMG_edit.setOnClickListener(v -> {
                if (playerItemClickListener != null) {
                    playerItemClickListener.editPlayerClicked(
                            getPlayer(getAdapterPosition()),
                            getAdapterPosition());
                }
            });

            player_IMG_delete.setOnClickListener(v -> {
                if (playerItemClickListener != null) {
                    playerItemClickListener.deletePlayerClicked(
                            getPlayer(getAdapterPosition()),
                            getAdapterPosition());
                }
            });

        }
    }
}
