package com.example.cogchords.util;

import java.util.Arrays;

/**
 * One pen/marker stroke drawn over a song (pure Java).
 * Points are stored in dp, measured from the top-left of the song content, at the text size
 * the stroke was drawn with ({@link #fontSp}), so it can be scaled when the text size changes.
 */
public class DoodleStroke {

    public final int color;
    public final float widthDp;
    public final boolean marker;
    public final float fontSp;

    private float[] points = new float[32];
    private int size = 0; // number of floats used (2 per point)

    public DoodleStroke(int color, float widthDp, boolean marker, float fontSp) {
        this.color = color;
        this.widthDp = widthDp;
        this.marker = marker;
        this.fontSp = fontSp;
    }

    public void addPoint(float x, float y) {
        if (size + 2 > points.length) {
            points = Arrays.copyOf(points, points.length * 2);
        }
        points[size++] = x;
        points[size++] = y;
    }

    public int getPointCount() { return size / 2; }
    public float getX(int index) { return points[index * 2]; }
    public float getY(int index) { return points[index * 2 + 1]; }

    /** True if (x, y) is within {@code radius} of the stroke line (all values in the stroke's own dp space). */
    public boolean hits(float x, float y, float radius) {
        int count = getPointCount();
        if (count == 0) return false;
        float reach = radius + widthDp / 2f;

        if (count == 1) {
            return distance(x, y, getX(0), getY(0)) <= reach;
        }
        for (int i = 0; i < count - 1; i++) {
            if (distanceToSegment(x, y, getX(i), getY(i), getX(i + 1), getY(i + 1)) <= reach) {
                return true;
            }
        }
        return false;
    }

    private static float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static float distanceToSegment(float px, float py, float ax, float ay, float bx, float by) {
        float dx = bx - ax;
        float dy = by - ay;
        float lengthSq = dx * dx + dy * dy;
        if (lengthSq == 0f) return distance(px, py, ax, ay);
        float t = ((px - ax) * dx + (py - ay) * dy) / lengthSq;
        t = Math.max(0f, Math.min(1f, t));
        return distance(px, py, ax + t * dx, ay + t * dy);
    }
}
