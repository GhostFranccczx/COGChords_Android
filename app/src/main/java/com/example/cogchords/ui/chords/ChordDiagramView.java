package com.example.cogchords.ui.chords;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.cogchords.R;
import com.example.cogchords.data.model.ChordModel;

public class ChordDiagramView extends View {

    private ChordModel chord;

    private Paint gridPaint;
    private Paint nutPaint;
    private Paint dotPaint;
    private Paint dotTextPaint;
    private Paint labelPaint;

    private static final int NUM_STRINGS = 6;
    private static final int NUM_FRETS = 5;

    public ChordDiagramView(Context context) {
        super(context);
        init();
    }

    public ChordDiagramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChordDiagramView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        int colorAccent = com.example.cogchords.util.ThemeManager.getAccentColor(getContext());
        int colorOnAccent = com.example.cogchords.util.ThemeManager.getOnAccentColor(getContext());
        int colorText = com.example.cogchords.util.ThemeManager.getTextColor(getContext());
        int colorOutline = com.example.cogchords.util.ThemeManager.getOutlineColor(getContext());

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(colorOutline);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dpToPx(1.2f));

        nutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nutPaint.setColor(colorText);
        nutPaint.setStyle(Paint.Style.STROKE);
        nutPaint.setStrokeWidth(dpToPx(3.5f));

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(colorAccent);
        dotPaint.setStyle(Paint.Style.FILL);

        dotTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotTextPaint.setColor(colorOnAccent);
        dotTextPaint.setTextAlign(Paint.Align.CENTER);
        dotTextPaint.setTextSize(dpToPx(9.5f));
        dotTextPaint.setFakeBoldText(true);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(colorText);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(dpToPx(11f));
        labelPaint.setFakeBoldText(true);
    }

    public void setChord(ChordModel chord) {
        this.chord = chord;
        invalidate();
    }

    public void setCustomDotColor(int color) {
        if (dotPaint != null) {
            dotPaint.setColor(color);
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int) dpToPx(110);
        int desiredHeight = (int) dpToPx(130);

        int width = resolveSize(desiredWidth, widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (chord == null) return;

        float width = getWidth();
        float height = getHeight();

        // Margins
        float topMargin = dpToPx(24);
        float bottomMargin = dpToPx(14);
        float leftMargin = dpToPx(24);
        float rightMargin = dpToPx(20);

        float fretboardWidth = width - leftMargin - rightMargin;
        float fretboardHeight = height - topMargin - bottomMargin;

        float stringSpacing = fretboardWidth / (NUM_STRINGS - 1);
        float fretSpacing = fretboardHeight / NUM_FRETS;

        // Draw Base Fret indicator if not at fret 1
        if (chord.getBaseFret() > 1) {
            Paint baseFretPaint = new Paint(labelPaint);
            baseFretPaint.setTextSize(dpToPx(10f));
            baseFretPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(chord.getBaseFret() + "fr", leftMargin - dpToPx(5),
                    topMargin + (fretSpacing / 1.5f), baseFretPaint);
        }

        // Draw Nut or top fret line
        if (chord.getBaseFret() == 1) {
            canvas.drawLine(leftMargin, topMargin, leftMargin + fretboardWidth, topMargin, nutPaint);
        } else {
            canvas.drawLine(leftMargin, topMargin, leftMargin + fretboardWidth, topMargin, gridPaint);
        }

        // Draw horizontal frets
        for (int f = 1; f <= NUM_FRETS; f++) {
            float y = topMargin + f * fretSpacing;
            canvas.drawLine(leftMargin, y, leftMargin + fretboardWidth, y, gridPaint);
        }

        // Draw vertical strings
        for (int s = 0; s < NUM_STRINGS; s++) {
            float x = leftMargin + s * stringSpacing;
            canvas.drawLine(x, topMargin, x, topMargin + fretboardHeight, gridPaint);
        }

        int[] frets = chord.getFrets();
        int[] fingers = chord.getFingers();
        float dotRadius = dpToPx(7f);

        for (int s = 0; s < NUM_STRINGS; s++) {
            float x = leftMargin + s * stringSpacing;
            int fretVal = frets[s];

            if (fretVal == -1) {
                // Muted String 'X' above nut
                canvas.drawText("✕", x, topMargin - dpToPx(8), labelPaint);
            } else if (fretVal == 0) {
                // Open String 'O' above nut
                canvas.drawText("○", x, topMargin - dpToPx(8), labelPaint);
            } else {
                // Fretted note: draw dot
                // Account for baseFret offset
                int relativeFret = fretVal;
                if (chord.getBaseFret() > 1) {
                    relativeFret = fretVal - chord.getBaseFret() + 1;
                }

                if (relativeFret >= 1 && relativeFret <= NUM_FRETS) {
                    float y = topMargin + (relativeFret - 0.5f) * fretSpacing;
                    canvas.drawCircle(x, y, dotRadius, dotPaint);

                    // Finger number if provided
                    if (fingers != null && s < fingers.length && fingers[s] > 0) {
                        float textY = y - ((dotTextPaint.descent() + dotTextPaint.ascent()) / 2);
                        canvas.drawText(String.valueOf(fingers[s]), x, textY, dotTextPaint);
                    }
                }
            }
        }
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}
