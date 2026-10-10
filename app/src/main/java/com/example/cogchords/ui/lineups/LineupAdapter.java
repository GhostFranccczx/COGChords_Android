package com.example.cogchords.ui.lineups;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cogchords.R;
import com.example.cogchords.data.entity.Lineup;

import java.util.ArrayList;
import java.util.List;

public class LineupAdapter extends RecyclerView.Adapter<LineupAdapter.LineupViewHolder> {

    public interface OnLineupClickListener {
        void onLineupClick(Lineup lineup);
        void onLineupEdit(Lineup lineup);
        void onLineupDelete(Lineup lineup);
    }

    private List<Lineup> lineups = new ArrayList<>();
    private final OnLineupClickListener listener;

    public LineupAdapter(OnLineupClickListener listener) {
        this.listener = listener;
    }

    public void setLineups(List<Lineup> newLineups) {
        this.lineups = newLineups != null ? newLineups : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LineupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lineup, parent, false);
        return new LineupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LineupViewHolder holder, int position) {
        Lineup lineup = lineups.get(position);
        holder.bind(lineup, position, listener);
    }

    @Override
    public int getItemCount() {
        return lineups.size();
    }

    static class LineupViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvDate;
        private final ImageView ivIcon;
        private final ImageView btnOptions;

        public LineupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_lineup_name);
            tvDate = itemView.findViewById(R.id.tv_lineup_date);
            ivIcon = itemView.findViewById(R.id.iv_lineup_icon);
            btnOptions = itemView.findViewById(R.id.btn_lineup_options);
        }

        public void bind(Lineup lineup, int position, OnLineupClickListener listener) {
            tvName.setText(lineup.getName());

            boolean isRainbow = com.example.cogchords.util.ThemeManager.isRainbowTheme(itemView.getContext());
            if (isRainbow) {
                int rainbowColor = com.example.cogchords.util.ThemeManager.getRainbowColorForPosition(itemView.getContext(), position);
                tvName.setTextColor(rainbowColor);
                if (ivIcon != null) {
                    ivIcon.setColorFilter(rainbowColor);
                }
                if (btnOptions != null) {
                    btnOptions.setColorFilter(rainbowColor);
                }
            } else {
                tvName.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
                if (ivIcon != null) {
                    ivIcon.clearColorFilter();
                }
                if (btnOptions != null) {
                    btnOptions.clearColorFilter();
                }
            }

            if (lineup.getDate() != null && !lineup.getDate().trim().isEmpty()) {
                tvDate.setText(lineup.getDate());
                tvDate.setVisibility(View.VISIBLE);
            } else {
                tvDate.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLineupClick(lineup);
                }
            });

            btnOptions.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.inflate(R.menu.menu_lineup_item);
                popup.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.action_edit_lineup) {
                        if (listener != null) listener.onLineupEdit(lineup);
                        return true;
                    } else if (id == R.id.action_delete_lineup) {
                        if (listener != null) listener.onLineupDelete(lineup);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }
}
