package com.example.cogchords.ui.songs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.cogchords.util.DoodleStroke;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * A transparent layer drawn over the song text for pen / highlighter doodles.
 *
 * Strokes are stored in dp relative to the song content, together with the text size they were
 * drawn at. When the text size changes, strokes are scaled around the text origin so they keep
 * pointing at the same words. While drawing is switched off, touches pass through to the text.
 */
public class DrawingView extends View {

    public enum Tool { PEN, MARKER, ERASER }

    public interface OnStrokesChangedListener {
        void onStrokesChanged();
    }

    // Where the song text starts inside the scroll content (matches the layout padding)
    private static final float TEXT_ORIGIN_X_DP = 18f;
    private static final float TEXT_ORIGIN_Y_DP = 16f;

    private static final float PEN_WIDTH_DP = 3f;
    private static final float MARKER_WIDTH_DP = 18f;
    private static final float ERASER_RADIUS_DP = 14f;
    private static final int MARKER_ALPHA = 0x66;
    private static final int MAX_UNDO = 30;

    private final List<DoodleStroke> strokes = new ArrayList<>();
    private final ArrayDeque<List<DoodleStroke>> undoStack = new ArrayDeque<>();
    private final Path path = new Path();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private DoodleStroke currentStroke;
    private boolean eraseSnapshotTaken;

    private boolean drawingEnabled = false;
    private Tool tool = Tool.PEN;
    private int color = 0xFFE53935;
    private float fontSizeSp = 15f;
    private float density;

    private OnStrokesChangedListener listener;

    public DrawingView(Context context) {
        super(context);
        init();
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    // ---------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------

    public void setOnStrokesChangedListener(OnStrokesChangedListener listener) {
        this.listener = listener;
    }

    public void setDrawingEnabled(boolean enabled) {
        this.drawingEnabled = enabled;
        if (!enabled) currentStroke = null;
        invalidate();
    }

    public void setTool(Tool tool) { this.tool = tool; }
    public void setColor(int color) { this.color = color; }

    /** Tell the view the current text size so strokes can follow the text when it is resized. */
    public void setFontSizeSp(float sp) {
        if (sp > 0f) {
            this.fontSizeSp = sp;
            invalidate();
        }
    }

    /** Replaces all strokes (e.g. after loading a song). Does not count as a user change. */
    public void setStrokes(List<DoodleStroke> newStrokes) {
        strokes.clear();
        strokes.addAll(newStrokes);
        undoStack.clear();
        currentStroke = null;
        invalidate();
    }

    public List<DoodleStroke> getStrokes() {
        return new ArrayList<>(strokes);
    }

    public boolean hasStrokes() {
        return !strokes.isEmpty();
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        List<DoodleStroke> previous = undoStack.pop();
        strokes.clear();
        strokes.addAll(previous);
        notifyChanged();
        invalidate();
    }

    public void clearAll() {
        if (strokes.isEmpty()) return;
        pushUndo();
        strokes.clear();
        notifyChanged();
        invalidate();
    }

    // ---------------------------------------------------------------------
    // Touch handling
    // ---------------------------------------------------------------------

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!drawingEnabled) return false; // let the text / scroll view handle it

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                eraseSnapshotTaken = false;
                if (tool == Tool.ERASER) {
                    eraseAt(event.getX(), event.getY());
                } else {
                    boolean marker = tool == Tool.MARKER;
                    currentStroke = new DoodleStroke(
                            marker ? withAlpha(color, MARKER_ALPHA) : color,
                            marker ? MARKER_WIDTH_DP : PEN_WIDTH_DP,
                            marker,
                            fontSizeSp);
                    currentStroke.addPoint(event.getX() / density, event.getY() / density);
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getHistorySize(); i++) {
                    handleMove(event.getHistoricalX(i), event.getHistoricalY(i));
                }
                handleMove(event.getX(), event.getY());
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (currentStroke != null) {
                    pushUndo();
                    strokes.add(currentStroke);
                    currentStroke = null;
                    notifyChanged();
                }
                invalidate();
                return true;

            default:
                return true;
        }
    }

    private void handleMove(float x, float y) {
        if (tool == Tool.ERASER) {
            eraseAt(x, y);
        } else if (currentStroke != null) {
            currentStroke.addPoint(x / density, y / density);
        }
    }

    private void eraseAt(float pxX, float pxY) {
        List<DoodleStroke> hit = new ArrayList<>();
        for (DoodleStroke s : strokes) {
            float scale = scaleFor(s);
            float x = TEXT_ORIGIN_X_DP + (pxX / density - TEXT_ORIGIN_X_DP) / scale;
            float y = TEXT_ORIGIN_Y_DP + (pxY / density - TEXT_ORIGIN_Y_DP) / scale;
            if (s.hits(x, y, ERASER_RADIUS_DP / scale)) hit.add(s);
        }
        if (hit.isEmpty()) return;

        if (!eraseSnapshotTaken) {
            pushUndo();
            eraseSnapshotTaken = true;
        }
        strokes.removeAll(hit);
        notifyChanged();
    }

    // ---------------------------------------------------------------------
    // Drawing
    // ---------------------------------------------------------------------

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (DoodleStroke s : strokes) {
            drawStroke(canvas, s);
        }
        if (currentStroke != null) {
            drawStroke(canvas, currentStroke);
        }
    }

    private void drawStroke(Canvas canvas, DoodleStroke s) {
        int count = s.getPointCount();
        if (count == 0) return;

        float scale = scaleFor(s);
        paint.setColor(s.color);
        paint.setStrokeWidth(s.widthDp * density);

        path.reset();
        float firstX = toPxX(s.getX(0), scale);
        float firstY = toPxY(s.getY(0), scale);
        path.moveTo(firstX, firstY);

        if (count == 1) {
            path.lineTo(firstX + 0.1f, firstY); // a dot
        } else {
            float prevX = firstX;
            float prevY = firstY;
            for (int i = 1; i < count; i++) {
                float x = toPxX(s.getX(i), scale);
                float y = toPxY(s.getY(i), scale);
                path.quadTo(prevX, prevY, (prevX + x) / 2f, (prevY + y) / 2f);
                prevX = x;
                prevY = y;
            }
            path.lineTo(prevX, prevY);
        }
        canvas.drawPath(path, paint);
    }

    private float scaleFor(DoodleStroke s) {
        return s.fontSp > 0f ? fontSizeSp / s.fontSp : 1f;
    }

    private float toPxX(float xDp, float scale) {
        return (TEXT_ORIGIN_X_DP + (xDp - TEXT_ORIGIN_X_DP) * scale) * density;
    }

    private float toPxY(float yDp, float scale) {
        return (TEXT_ORIGIN_Y_DP + (yDp - TEXT_ORIGIN_Y_DP) * scale) * density;
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private void pushUndo() {
        undoStack.push(new ArrayList<>(strokes));
        while (undoStack.size() > MAX_UNDO) undoStack.removeLast();
    }

    private void notifyChanged() {
        if (listener != null) listener.onStrokesChanged();
    }
}
