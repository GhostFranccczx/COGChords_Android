package com.example.cogchords.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts strokes to and from a compact text format for storing in the database (pure Java).
 * One stroke per line:  color|widthDp|marker(0/1)|fontSp|x,y x,y x,y ...
 */
public class DoodleCodec {

    public static String encode(List<DoodleStroke> strokes) {
        StringBuilder sb = new StringBuilder();
        for (DoodleStroke s : strokes) {
            if (s.getPointCount() == 0) continue;
            if (sb.length() > 0) sb.append('\n');
            sb.append(s.color).append('|')
                    .append(fmt(s.widthDp)).append('|')
                    .append(s.marker ? 1 : 0).append('|')
                    .append(fmt(s.fontSp)).append('|');
            for (int i = 0; i < s.getPointCount(); i++) {
                if (i > 0) sb.append(' ');
                sb.append(fmt(s.getX(i))).append(',').append(fmt(s.getY(i)));
            }
        }
        return sb.toString();
    }

    public static List<DoodleStroke> decode(String data) {
        List<DoodleStroke> result = new ArrayList<>();
        if (data == null || data.isEmpty()) return result;

        for (String line : data.split("\n")) {
            try {
                String[] parts = line.split("\\|", -1);
                if (parts.length < 5) continue;

                DoodleStroke stroke = new DoodleStroke(
                        Integer.parseInt(parts[0]),
                        Float.parseFloat(parts[1]),
                        parts[2].equals("1"),
                        Float.parseFloat(parts[3]));

                for (String point : parts[4].trim().split(" ")) {
                    if (point.isEmpty()) continue;
                    String[] xy = point.split(",");
                    stroke.addPoint(Float.parseFloat(xy[0]), Float.parseFloat(xy[1]));
                }
                if (stroke.getPointCount() > 0) result.add(stroke);
            } catch (RuntimeException ignored) {
                // Skip a damaged line instead of losing all annotations
            }
        }
        return result;
    }

    private static String fmt(float v) {
        return Float.toString(Math.round(v * 10f) / 10f);
    }
}
