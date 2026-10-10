package com.example.cogchords.ui.lineups;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cogchords.R;
import com.example.cogchords.data.model.LineupSongDetail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LineupSongEditAdapter extends RecyclerView.Adapter<LineupSongEditAdapter.SongEditViewHolder> {

    public interface OnSongOrderChangeListener {
        void onSongRemoved(int position);
    }

    private List<LineupSongDetail> songs = new ArrayList<>();
    private final OnSongOrderChangeListener listener;

    public LineupSongEditAdapter(OnSongOrderChangeListener listener) {
        this.listener = listener;
    }

    public void setSongs(List<LineupSongDetail> newSongs) {
        this.songs = newSongs != null ? new ArrayList<>(newSongs) : new ArrayList<>();
        renumberPositions();
        notifyDataSetChanged();
    }

    public List<LineupSongDetail> getSongs() {
        return songs;
    }

    public void addSong(LineupSongDetail song) {
        songs.add(song);
        renumberPositions();
        notifyItemInserted(songs.size() - 1);
    }

    public void moveUp(int position) {
        if (position > 0 && position < songs.size()) {
            Collections.swap(songs, position, position - 1);
            renumberPositions();
            notifyItemMoved(position, position - 1);
            notifyItemChanged(position);
            notifyItemChanged(position - 1);
        }
    }

    public void moveDown(int position) {
        if (position >= 0 && position < songs.size() - 1) {
            Collections.swap(songs, position, position + 1);
            renumberPositions();
            notifyItemMoved(position, position + 1);
            notifyItemChanged(position);
            notifyItemChanged(position + 1);
        }
    }

    public void remove(int position) {
        if (position >= 0 && position < songs.size()) {
            songs.remove(position);
            renumberPositions();
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, songs.size() - position);
            if (listener != null) {
                listener.onSongRemoved(position);
            }
        }
    }

    private void renumberPositions() {
        for (int i = 0; i < songs.size(); i++) {
            songs.get(i).setPosition(i + 1);
        }
    }

    @NonNull
    @Override
    public SongEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lineup_song_edit, parent, false);
        return new SongEditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongEditViewHolder holder, int position) {
        LineupSongDetail song = songs.get(position);
        holder.bind(song, position, songs.size(), this);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongEditViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPosition;
        private final TextView tvTitle;
        private final TextView tvKey;
        private final TextView tvArtist;
        private final ImageView btnMoveUp;
        private final ImageView btnMoveDown;
        private final ImageView btnRemove;

        public SongEditViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tv_song_position);
            tvTitle = itemView.findViewById(R.id.tv_song_title);
            tvKey = itemView.findViewById(R.id.tv_song_key);
            tvArtist = itemView.findViewById(R.id.tv_song_artist);
            btnMoveUp = itemView.findViewById(R.id.btn_move_up);
            btnMoveDown = itemView.findViewById(R.id.btn_move_down);
            btnRemove = itemView.findViewById(R.id.btn_remove_song);
        }

        public void bind(LineupSongDetail song, int position, int totalCount, LineupSongEditAdapter adapter) {
            tvPosition.setText(String.valueOf(position + 1));
            tvTitle.setText(song.getTitle());

            if (song.getOriginalKey() != null && !song.getOriginalKey().trim().isEmpty()) {
                tvKey.setText("Key: " + song.getOriginalKey());
                tvKey.setVisibility(View.VISIBLE);
            } else {
                tvKey.setVisibility(View.GONE);
            }

            if (song.getArtist() != null && !song.getArtist().trim().isEmpty()) {
                tvArtist.setText("• " + song.getArtist());
                tvArtist.setVisibility(View.VISIBLE);
            } else {
                tvArtist.setVisibility(View.GONE);
            }

            // Up button disabled on first item
            btnMoveUp.setEnabled(position > 0);
            btnMoveUp.setAlpha(position > 0 ? 1.0f : 0.3f);
            btnMoveUp.setOnClickListener(v -> adapter.moveUp(holderPosition()));

            // Down button disabled on last item
            btnMoveDown.setEnabled(position < totalCount - 1);
            btnMoveDown.setAlpha(position < totalCount - 1 ? 1.0f : 0.3f);
            btnMoveDown.setOnClickListener(v -> adapter.moveDown(holderPosition()));

            btnRemove.setOnClickListener(v -> adapter.remove(holderPosition()));
        }

        private int holderPosition() {
            int pos = getBindingAdapterPosition();
            return pos != RecyclerView.NO_POSITION ? pos : getLayoutPosition();
        }
    }
}
