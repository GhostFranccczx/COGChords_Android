package com.example.cogchords.ui.chords;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.example.cogchords.util.PianoChord;
import com.example.cogchords.util.ThemeManager;

/**
 * Draws a piano keyboard with the notes of a chord highlighted. Everything is drawn in code,
 * so no images are needed and it follows the active app theme.
 */
public class PianoDiagramView extends View {

    private static final int MIN_WHITE_KEYS = 8;

    private PianoChord chord;

    private Paint whiteKeyPaint;
    private Paint blackKeyPaint;
    private Paint outlinePaint;
    private Paint highlightPaint;
    private Paint rootPaint;

    public PianoDiagramView(Context context) {
        super(context);
        init();
    }

    public PianoDiagramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PianoDiagramView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        int accent = ThemeManager.getAccentColor(getContext());
        int outline = ThemeManager.getOutlineColor(getContext());

        whiteKeyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whiteKeyPaint.setStyle(Paint.Style.FILL);
        whiteKeyPaint.setColor(0xFFF4F4F4);

        blackKeyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blackKeyPaint.setStyle(Paint.Style.FILL);
        blackKeyPaint.setColor(0xFF1E1E1E);

        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(dpToPx(1f));
        outlinePaint.setColor(outline);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.FILL);

        rootPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rootPaint.setStyle(Paint.Style.FILL);

        setCustomColor(accent);
    }

    public void setChord(PianoChord chord) {
        this.chord = chord;
        invalidate();
    }

    /** Colour used for the pressed keys; the root note is drawn a little darker. */
    public void setCustomColor(int color) {
        highlightPaint.setColor(color);
        rootPaint.setColor(ColorUtils.blendARGB(color, 0xFF000000, 0.35f));
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = resolveSize((int) dpToPx(120), widthMeasureSpec);
        int height = resolveSize((int) dpToPx(76), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (chord == null) return;

        int[] window = chord.getKeyWindow(MIN_WHITE_KEYS);
        int start = window[0];
        int end = window[1];
        int whiteCount = PianoChord.countWhiteKeys(start, end);
        int[] pressed = chord.getKeyPositions();

        float pad = dpToPx(2f);
        float keyboardWidth = getWidth() - 2 * pad;
        float keyHeight = getHeight() - 2 * pad;
        float whiteWidth = keyboardWidth / whiteCount;

        // White keys first
        int whiteIndex = 0;
        for (int p = start; p <= end; p++) {
            if (!PianoChord.isWhite(p)) continue;
            float left = pad + whiteIndex * whiteWidth;
            float right = left + whiteWidth;

            canvas.drawRect(left, pad, right, pad + keyHeight, paintFor(p, pressed, whiteKeyPaint));
            canvas.drawRect(left, pad, right, pad + keyHeight, outlinePaint);
            whiteIndex++;
        }

        // Black keys on top, centred on the line between two white keys
        float blackWidth = whiteWidth * 0.6f;
        float blackHeight = keyHeight * 0.6f;
        whiteIndex = 0;
        for (int p = start; p <= end; p++) {
            if (PianoChord.isWhite(p)) {
                whiteIndex++;
                continue;
            }
            float centre = pad + whiteIndex * whiteWidth;
            float left = centre - blackWidth / 2f;
            canvas.drawRect(left, pad, left + blackWidth, pad + blackHeight, paintFor(p, pressed, blackKeyPaint));
        }
    }

    private Paint paintFor(int position, int[] pressed, Paint normal) {
        for (int p : pressed) {
            if (p == position) {
                return chord.isRootKey(position) ? rootPaint : highlightPaint;
            }
        }
        return normal;
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
