package com.example.cogchords.ui.lineups;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cogchords.R;
import com.example.cogchords.data.model.LineupSongDetail;

import java.util.ArrayList;
import java.util.List;

public class LineupSongViewAdapter extends RecyclerView.Adapter<LineupSongViewAdapter.ViewHolder> {

    public interface OnSongOpenListener {
        void onSongOpen(LineupSongDetail song);
    }

    private List<LineupSongDetail> songs = new ArrayList<>();
    private final OnSongOpenListener listener;

    public LineupSongViewAdapter(OnSongOpenListener listener) {
        this.listener = listener;
    }

    public void setSongs(List<LineupSongDetail> newSongs) {
        this.songs = newSongs != null ? newSongs : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lineup_song_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LineupSongDetail song = songs.get(position);
        holder.bind(song, listener);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPosition;
        private final TextView tvTitle;
        private final TextView tvKey;
        private final TextView tvArtist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tv_song_position);
            tvTitle = itemView.findViewById(R.id.tv_song_title);
            tvKey = itemView.findViewById(R.id.tv_song_key);
            tvArtist = itemView.findViewById(R.id.tv_song_artist);
        }

        public void bind(LineupSongDetail song, OnSongOpenListener listener) {
            tvPosition.setText(String.valueOf(song.getPosition()));
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

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSongOpen(song);
                }
            });
        }
    }
}
