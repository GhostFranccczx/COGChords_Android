package com.example.cogchords.ui.songs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cogchords.R;
import com.example.cogchords.data.entity.Song;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    public interface OnSongClickListener {
        void onSongClick(Song song);
        void onSongEdit(Song song);
        void onSongDelete(Song song);
    }

    private List<Song> songs = new ArrayList<>();
    private final OnSongClickListener listener;

    public SongAdapter(OnSongClickListener listener) {
        this.listener = listener;
    }

    public void setSongs(List<Song> newSongs) {
        this.songs = newSongs != null ? newSongs : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.bind(song, position, listener);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvArtist;
        private final TextView tvKey;
        private final TextView tvCategory;
        private final ImageView btnOptions;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_song_title);
            tvArtist = itemView.findViewById(R.id.tv_song_artist);
            tvKey = itemView.findViewById(R.id.tv_song_key);
            tvCategory = itemView.findViewById(R.id.tv_song_category);
            btnOptions = itemView.findViewById(R.id.btn_song_options);
        }

        public void bind(Song song, int position, OnSongClickListener listener) {
            tvTitle.setText(song.getTitle());

            boolean isRainbow = com.example.cogchords.util.ThemeManager.isRainbowTheme(itemView.getContext());
            if (isRainbow) {
                int rainbowColor = com.example.cogchords.util.ThemeManager.getRainbowColorForPosition(itemView.getContext(), position);
                int rainbowChipBg = com.example.cogchords.util.ThemeManager.getRainbowChipBgForPosition(itemView.getContext(), position);
                int secondRainbowColor = com.example.cogchords.util.ThemeManager.getRainbowColorForPosition(itemView.getContext(), position + 3);

                // Colorful dynamic title tint
                tvTitle.setTextColor(rainbowColor);

                // Colorful Key Badge with rainbow border & background
                android.graphics.drawable.GradientDrawable keyBg = new android.graphics.drawable.GradientDrawable();
                keyBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                keyBg.setCornerRadius(16f);
                keyBg.setColor(rainbowChipBg);
                keyBg.setStroke(2, rainbowColor);
                tvKey.setBackground(keyBg);
                tvKey.setTextColor(rainbowColor);

                // Colorful Category Badge with complementary rainbow color
                android.graphics.drawable.GradientDrawable catBg = new android.graphics.drawable.GradientDrawable();
                catBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                catBg.setCornerRadius(16f);
                catBg.setColor(com.example.cogchords.util.ThemeManager.getRainbowChipBgForPosition(itemView.getContext(), position + 3));
                catBg.setStroke(2, secondRainbowColor);
                tvCategory.setBackground(catBg);
                tvCategory.setTextColor(secondRainbowColor);

                btnOptions.setColorFilter(rainbowColor);
            } else {
                tvTitle.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
                tvKey.setBackgroundResource(R.drawable.bg_key_badge);
                tvKey.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
                tvCategory.setBackgroundResource(R.drawable.bg_key_badge);
                tvCategory.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
                btnOptions.clearColorFilter();
            }

            if (song.getArtist() != null && !song.getArtist().trim().isEmpty()) {
                tvArtist.setText(song.getArtist());
                tvArtist.setVisibility(View.VISIBLE);
            } else {
                tvArtist.setVisibility(View.GONE);
            }

            if (song.getOriginalKey() != null && !song.getOriginalKey().trim().isEmpty()) {
                tvKey.setText("Key: " + song.getOriginalKey());
                tvKey.setVisibility(View.VISIBLE);
            } else {
                tvKey.setVisibility(View.GONE);
            }

            if (song.getCategory() != null && !song.getCategory().trim().isEmpty()) {
                tvCategory.setText(song.getCategory());
                tvCategory.setVisibility(View.VISIBLE);
            } else {
                tvCategory.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSongClick(song);
                }
            });

            btnOptions.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.inflate(R.menu.menu_song_item);
                popup.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.action_edit) {
                        if (listener != null) listener.onSongEdit(song);
                        return true;
                    } else if (id == R.id.action_delete) {
                        if (listener != null) listener.onSongDelete(song);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }
}
