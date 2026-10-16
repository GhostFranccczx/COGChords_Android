package com.example.cogchords.ui.chords;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cogchords.R;
import com.example.cogchords.data.model.ChordModel;
import com.example.cogchords.util.PianoChord;

import java.util.ArrayList;
import java.util.List;

public class ChordReferenceAdapter extends RecyclerView.Adapter<ChordReferenceAdapter.ChordViewHolder> {

    private List<ChordModel> chords = new ArrayList<>();
    private List<PianoChord> pianoChords = new ArrayList<>();
    private boolean pianoMode = false;

    /** Shows guitar chords (fretboard diagrams). */
    public void setChords(List<ChordModel> newChords) {
        this.chords = newChords != null ? newChords : new ArrayList<>();
        this.pianoMode = false;
        notifyDataSetChanged();
    }

    /** Shows piano chords (keyboard diagrams). */
    public void setPianoChords(List<PianoChord> newChords) {
        this.pianoChords = newChords != null ? newChords : new ArrayList<>();
        this.pianoMode = true;
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
        if (pianoMode) {
            holder.bindPiano(pianoChords.get(position), position);
        } else {
            holder.bindGuitar(chords.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return pianoMode ? pianoChords.size() : chords.size();
    }

    static class ChordViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvQuality;
        private final TextView tvFrets;
        private final ChordDiagramView diagramView;
        private final PianoDiagramView pianoView;

        public ChordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_chord_name);
            tvQuality = itemView.findViewById(R.id.tv_chord_quality);
            tvFrets = itemView.findViewById(R.id.tv_chord_frets);
            diagramView = itemView.findViewById(R.id.chord_diagram_view);
            pianoView = itemView.findViewById(R.id.piano_diagram_view);
        }

        void bindGuitar(ChordModel chord, int position) {
            tvName.setText(chord.getName());
            tvQuality.setText(chord.getQuality());
            tvFrets.setText(chord.getFretString());

            diagramView.setVisibility(View.VISIBLE);
            pianoView.setVisibility(View.GONE);
            diagramView.setChord(chord);

            diagramView.setCustomDotColor(applyThemeColors(position));
        }

        void bindPiano(PianoChord chord, int position) {
            tvName.setText(chord.getName());
            tvQuality.setText(chord.getQuality());
            tvFrets.setText(chord.getNotesString());

            diagramView.setVisibility(View.GONE);
            pianoView.setVisibility(View.VISIBLE);
            pianoView.setChord(chord);

            pianoView.setCustomColor(applyThemeColors(position));
        }

        /** Colours the card text for the active theme and returns the colour for the diagram. */
        private int applyThemeColors(int position) {
            Context context = itemView.getContext();
            boolean isRainbow = com.example.cogchords.util.ThemeManager.isRainbowTheme(context);

            if (isRainbow) {
                int rainbowColor = com.example.cogchords.util.ThemeManager.getRainbowColorForPosition(context, position);
                int rainbowChipBg = com.example.cogchords.util.ThemeManager.getRainbowChipBgForPosition(context, position);

                tvName.setTextColor(rainbowColor);

                android.graphics.drawable.GradientDrawable qualityBg = new android.graphics.drawable.GradientDrawable();
                qualityBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                qualityBg.setCornerRadius(12f);
                qualityBg.setColor(rainbowChipBg);
                qualityBg.setStroke(2, rainbowColor);
                tvQuality.setBackground(qualityBg);
                tvQuality.setTextColor(rainbowColor);

                tvFrets.setTextColor(rainbowColor);
                return rainbowColor;
            }

            tvName.setTextColor(com.example.cogchords.util.ThemeManager.getOnSurfaceColor(context));
            tvQuality.setBackgroundResource(R.drawable.bg_key_badge);
            tvQuality.setTextColor(com.example.cogchords.util.ThemeManager.getOnSurfaceVariantColor(context));
            tvFrets.setTextColor(com.example.cogchords.util.ThemeManager.getOnSurfaceVariantColor(context));
            return com.example.cogchords.util.ThemeManager.getAccentColor(context);
        }
    }
}
