package net.minestom.server.utils.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BlockIteratorTest {
    private void assertContains(List<Point> points, Point point) {
        assertTrue(points.contains(point), "Expected " + points + " to contain " + point);
    }

    @Test
    public void test2dOffsetppp() {
        Vec s = new Vec(0,  0.1, 0);
        Vec e = new Vec(2, 1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        assertEquals(new Vec(0, 0, 0), iterator.next());
        assertEquals(new Vec(1, 0, 0), iterator.next());
        assertEquals(new Vec(1, 1, 0), iterator.next());
        assertEquals(new Vec(2, 1, 0), iterator.next());
        assertEquals(new Vec(3, 1, 0), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void test2dOffsetppn() {
        Vec s = new Vec(0,  0.1, 0);
        Vec e = new Vec(-2, 1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        assertEquals(new Vec(0, 0, 0), iterator.next());
        assertEquals(new Vec(-1, 0, 0), iterator.next());
        assertEquals(new Vec(-2, 0, 0), iterator.next());
        assertEquals(new Vec(-2, 1, 0), iterator.next());
        assertEquals(new Vec(-3, 1, 0), iterator.next());
        assertEquals(new Vec(-4, 1, 0), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void test2dOffsetnpp() {
        Vec s = new Vec(0,  -0.1, 0);
        Vec e = new Vec(2, 1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        assertEquals(new Vec(0, -1, 0), iterator.next());
        assertEquals(new Vec(0, 0, 0), iterator.next());
        assertEquals(new Vec(1, 0, 0), iterator.next());
        assertEquals(new Vec(2, 0, 0), iterator.next());
        assertEquals(new Vec(2, 1, 0), iterator.next());
        assertEquals(new Vec(3, 1, 0), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void test2dOffsetnnp() {
        Vec s = new Vec(0,  -0.1, 0);
        Vec e = new Vec(-2, 1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        assertEquals(new Vec(0, -1, 0), iterator.next());
        assertEquals(new Vec(-1, -1, 0), iterator.next());
        assertEquals(new Vec(-1, 0, 0), iterator.next());
        assertEquals(new Vec(-2, 0, 0), iterator.next());
        assertEquals(new Vec(-3, 0, 0), iterator.next());
        assertEquals(new Vec(-3, 1, 0), iterator.next());
        assertEquals(new Vec(-4, 1, 0), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testZeroVelocity() {
        Vec s = new Vec(0,  0, 0);
        Vec e = new Vec(0, 0, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testExactEnd() {
        Vec s = new Vec(0.5,  0, 0.5);
        Vec e = new Vec(0, 1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 1);
        assertEquals(new Vec(0, 0, 0), iterator.next());
        assertEquals(new Vec(0, 1, 0), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testSameEnd() {
        Vec s = new Vec(0.5,  0, 0.5);
        Vec e = new Vec(0, 1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 0.5);
        assertEquals(new Vec(0, 0, 0), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void test3dExtraCollection() {
        Vec s = new Vec(0.1,  0.1, 0.1);
        Vec e = new Vec(1, 1, 1);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        List<Point> points = new ArrayList<>();
        while (iterator.hasNext()) {
            points.add(iterator.next());
        }

        Point[] validPoints = new Point[] {
            new Vec(0.0, 0.0, 0.0),
            new Vec(1.0, 0.0, 0.0),
            new Vec(0.0, 1.0, 0.0),
            new Vec(0.0, 0.0, 1.0),
            new Vec(1.0, 1.0, 1.0),
            new Vec(2.0, 1.0, 1.0),
            new Vec(1.0, 2.0, 1.0),
            new Vec(1.0, 1.0, 2.0),
            new Vec(2.0, 2.0, 2.0)
        };

        for (Point p : validPoints) {
            assertContains(points, p);
        }
        assertEquals(validPoints.length, points.size());
    }

    @Test
    public void test2dpp() {
        Vec s = new Vec(0,  0, 0);
        Vec e = new Vec(2, 1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        List<Point> points = new ArrayList<>();
        while (iterator.hasNext()) {
            points.add(iterator.next());
        }

        Point[] validPoints = new Point[] {
            new Vec(0.0, 0.0, 0.0),
            new Vec(1.0, 0.0, 0.0),
            new Vec(2.0, 0.0, 0.0),
            new Vec(1.0, 1.0, 0.0),
            new Vec(2.0, 1.0, 0.0),
            new Vec(3.0, 1.0, 0.0),
        };

        for (Point p : validPoints) {
            assertContains(points, p);
        }
        assertEquals(validPoints.length, points.size());
    }

    @Test
    public void test2dpn() {
        Vec s = new Vec(0,  0, 0);
        Vec e = new Vec(-2, 1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        List<Point> points = new ArrayList<>();
        while (iterator.hasNext()) {
            points.add(iterator.next());
        }

        Point[] validPoints = new Point[] {
            new Vec(0.0, 0.0, 0.0),
            new Vec(-1.0, 0.0, 0.0),
            new Vec(-2.0, 0.0, 0.0),
            new Vec(-3.0, 0.0, 0.0),
            new Vec(-2.0, 1.0, 0.0),
            new Vec(-3.0, 1.0, 0.0),
            new Vec(-4.0, 1.0, 0.0)
        };

        for (Point p : validPoints) {
            assertContains(points, p);
        }
        assertEquals(validPoints.length, points.size());
    }

    @Test
    public void test2dnn() {
        Vec s = new Vec(0,  0, 0);
        Vec e = new Vec(-2, -1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        List<Point> points = new ArrayList<>();
        while (iterator.hasNext()) {
            points.add(iterator.next());
        }

        Point[] validPoints = new Point[] {
                new Vec(0.0, 0.0, 0.0),
                new Vec(-1.0, 0.0, 0.0),
                new Vec(0.0, -1.0, 0.0),
                new Vec(-1.0, -1.0, 0.0),
                new Vec(-2.0, -1.0, 0.0),
                new Vec(-3.0, -1.0, 0.0),
                new Vec(-2.0, -2.0, 0.0),
                new Vec(-3.0, -2.0, 0.0),
                new Vec(-4.0, -2.0, 0.0)
        };

        for (Point p : validPoints) {
            assertContains(points, p);
        }
        assertEquals(validPoints.length, points.size());
    }

    @Test
    public void falling() {
        Vec s = new Vec(0,  42, 0);
        Vec e = new Vec(0, -10, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 14.142135623730951);

        for (int y = 42; y >= 27; --y) assertEquals(new Vec(0, y, 0), iterator.next());
        assertFalse(iterator.hasNext());
    }
}