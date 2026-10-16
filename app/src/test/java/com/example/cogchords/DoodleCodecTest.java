package com.example.cogchords;

import static org.junit.Assert.*;

import com.example.cogchords.util.DoodleCodec;
import com.example.cogchords.util.DoodleStroke;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class DoodleCodecTest {

    @Test
    public void strokesSurviveEncodeAndDecode() {
        DoodleStroke pen = new DoodleStroke(0xFFE53935, 3f, false, 15f);
        pen.addPoint(10.04f, 20.5f);
        pen.addPoint(30f, 40.25f);
        DoodleStroke marker = new DoodleStroke(0x66FDD835, 16f, true, 18f);
        marker.addPoint(5f, 6f);

        List<DoodleStroke> list = new ArrayList<>();
        list.add(pen);
        list.add(marker);

        List<DoodleStroke> decoded = DoodleCodec.decode(DoodleCodec.encode(list));
        assertEquals(2, decoded.size());

        assertEquals(0xFFE53935, decoded.get(0).color);
        assertEquals(15f, decoded.get(0).fontSp, 0.001f);
        assertFalse(decoded.get(0).marker);
        assertEquals(2, decoded.get(0).getPointCount());
        assertEquals(10.0f, decoded.get(0).getX(0), 0.001f);
        assertEquals(40.3f, decoded.get(0).getY(1), 0.001f);

        assertTrue(decoded.get(1).marker);
        assertEquals(1, decoded.get(1).getPointCount());
    }

    @Test
    public void emptyAndDamagedDataAreHandled() {
        assertTrue(DoodleCodec.decode(null).isEmpty());
        assertTrue(DoodleCodec.decode("").isEmpty());
        List<DoodleStroke> decoded = DoodleCodec.decode("garbage\n123|3.0|0|15.0|1.0,2.0 3.0,4.0\nx|y|z");
        assertEquals(1, decoded.size());
        assertEquals(2, decoded.get(0).getPointCount());
    }

    @Test
    public void hitTestUsesDistanceToTheLine() {
        DoodleStroke s = new DoodleStroke(0, 3f, false, 15f);
        s.addPoint(0f, 0f);
        s.addPoint(100f, 0f);
        assertTrue(s.hits(50f, 4f, 3f));   // within 3 + 1.5
        assertFalse(s.hits(50f, 6f, 3f));
        assertTrue(s.hits(-3f, 0f, 3f));   // just past the start
        assertFalse(s.hits(150f, 0f, 3f));
    }

    @Test
    public void singlePointStrokeCanBeHit() {
        DoodleStroke s = new DoodleStroke(0, 3f, false, 15f);
        s.addPoint(10f, 10f);
        assertTrue(s.hits(12f, 10f, 2f));
        assertFalse(s.hits(30f, 10f, 2f));
    }

    @Test
    public void strokeGrowsPastInitialCapacity() {
        DoodleStroke s = new DoodleStroke(0, 3f, false, 15f);
        for (int i = 0; i < 500; i++) s.addPoint(i, i * 2f);
        assertEquals(500, s.getPointCount());
        assertEquals(998f, s.getY(499), 0.001f);
    }
}
