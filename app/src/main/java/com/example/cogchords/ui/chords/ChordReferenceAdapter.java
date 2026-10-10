package com.example.cogchords.ui.chords;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cogchords.R;
import com.example.cogchords.data.model.ChordModel;

import java.util.ArrayList;
import java.util.List;

public class ChordReferenceAdapter extends RecyclerView.Adapter<ChordReferenceAdapter.ChordViewHolder> {

    private List<ChordModel> chords = new ArrayList<>();

    public void setChords(List<ChordModel> newChords) {
        this.chords = newChords != null ? newChords : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chord_card, parent, false);
        return new ChordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChordViewHolder holder, int position) {
        ChordModel chord = chords.get(position);
        holder.bind(chord, position);
    }

    @Override
    public int getItemCount() {
        return chords.size();
    }

    static class ChordViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvQuality;
        private final TextView tvFrets;
        private final ChordDiagramView diagramView;

        public ChordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_chord_name);
            tvQuality = itemView.findViewById(R.id.tv_chord_quality);
            tvFrets = itemView.findViewById(R.id.tv_chord_frets);
            diagramView = itemView.findViewById(R.id.chord_diagram_view);
        }

        public void bind(ChordModel chord, int position) {
            tvName.setText(chord.getName());
            tvQuality.setText(chord.getQuality());
            tvFrets.setText(chord.getFretString());
            diagramView.setChord(chord);

            boolean isRainbow = com.example.cogchords.util.ThemeManager.isRainbowTheme(itemView.getContext());
            if (isRainbow) {
                int rainbowColor = com.example.cogchords.util.ThemeManager.getRainbowColorForPosition(itemView.getContext(), position);
                int rainbowChipBg = com.example.cogchords.util.ThemeManager.getRainbowChipBgForPosition(itemView.getContext(), position);

                tvName.setTextColor(rainbowColor);

                android.graphics.drawable.GradientDrawable qualityBg = new android.graphics.drawable.GradientDrawable();
                qualityBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                qualityBg.setCornerRadius(12f);
                qualityBg.setColor(rainbowChipBg);
                qualityBg.setStroke(2, rainbowColor);
                tvQuality.setBackground(qualityBg);
                tvQuality.setTextColor(rainbowColor);

                tvFrets.setTextColor(rainbowColor);
                diagramView.setCustomDotColor(rainbowColor);
            } else {
                tvName.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
                tvQuality.setBackgroundResource(R.drawable.bg_key_badge);
                tvQuality.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
                tvFrets.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
                diagramView.setCustomDotColor(com.example.cogchords.util.ThemeManager.getAccentColor(itemView.getContext()));
            }
        }
    }
}
