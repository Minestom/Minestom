package net.minestom.server.utils.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlockIteratorTest {
    private void assertContains(List<Point> points, Point point) {
        assertTrue(points.contains(point), "Expected " + points + " to contain " + point);
    }

    @Test
    void test2dOffsetppp() {
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
    void test2dOffsetppn() {
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
    void test2dOffsetnpp() {
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
    void test2dOffsetnnp() {
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
    void testZeroVelocity() {
        Vec s = new Vec(0,  0, 0);
        Vec e = new Vec(0, 0, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);
        assertFalse(iterator.hasNext());
    }

    @Test
    void testLongDistance() {
        Vec s = new Vec(42.5, 0, 51.5);
        Vec e = new Vec(-12, 0, -36);
        BlockIterator iterator = new BlockIterator(s, e, 0, 37);

        List<Point> points = new ArrayList<>();
        while (iterator.hasNext()) {
            points.add(iterator.next());
        }

        Point[] validPoints = new Point[]{
                new Vec(42.0, 0.0, 51.0),
                new Vec(42.0, 0.0, 50.0),
                new Vec(41.0, 0.0, 50.0),
                new Vec(42.0, 0.0, 49.0),
                new Vec(41.0, 0.0, 49.0),
                new Vec(41.0, 0.0, 48.0),
                new Vec(41.0, 0.0, 47.0),
                new Vec(40.0, 0.0, 47.0),
                new Vec(41.0, 0.0, 46.0),
                new Vec(40.0, 0.0, 46.0),
                new Vec(40.0, 0.0, 45.0),
                new Vec(40.0, 0.0, 44.0),
                new Vec(39.0, 0.0, 44.0),
                new Vec(40.0, 0.0, 43.0),
                new Vec(39.0, 0.0, 43.0),
                new Vec(39.0, 0.0, 42.0),
                new Vec(39.0, 0.0, 41.0),
                new Vec(38.0, 0.0, 41.0),
                new Vec(39.0, 0.0, 40.0),
                new Vec(38.0, 0.0, 40.0),
                new Vec(38.0, 0.0, 39.0),
                new Vec(38.0, 0.0, 38.0),
                new Vec(37.0, 0.0, 38.0),
                new Vec(38.0, 0.0, 37.0),
                new Vec(37.0, 0.0, 37.0),
                new Vec(37.0, 0.0, 36.0),
                new Vec(37.0, 0.0, 35.0),
                new Vec(36.0, 0.0, 35.0),
                new Vec(37.0, 0.0, 34.0),
                new Vec(36.0, 0.0, 34.0),
                new Vec(36.0, 0.0, 33.0),
                new Vec(36.0, 0.0, 32.0),
                new Vec(35.0, 0.0, 32.0),
                new Vec(36.0, 0.0, 31.0),
                new Vec(35.0, 0.0, 31.0),
                new Vec(35.0, 0.0, 30.0),
                new Vec(35.0, 0.0, 29.0),
                new Vec(34.0, 0.0, 29.0),
                new Vec(35.0, 0.0, 28.0),
                new Vec(34.0, 0.0, 28.0),
                new Vec(34.0, 0.0, 27.0),
                new Vec(34.0, 0.0, 26.0),
                new Vec(33.0, 0.0, 26.0),
                new Vec(34.0, 0.0, 25.0),
                new Vec(33.0, 0.0, 25.0),
                new Vec(33.0, 0.0, 24.0),
                new Vec(33.0, 0.0, 23.0),
                new Vec(32.0, 0.0, 23.0),
                new Vec(33.0, 0.0, 22.0),
                new Vec(32.0, 0.0, 22.0),
                new Vec(32.0, 0.0, 21.0),
                new Vec(32.0, 0.0, 20.0),
                new Vec(31.0, 0.0, 20.0),
                new Vec(32.0, 0.0, 19.0),
                new Vec(31.0, 0.0, 19.0),
                new Vec(31.0, 0.0, 18.0),
                new Vec(31.0, 0.0, 17.0),
                new Vec(30.0, 0.0, 17.0),
                new Vec(31.0, 0.0, 16.0),
                new Vec(30.0, 0.0, 16.0)
        };

        for (Point p : validPoints) {
            assertContains(points, p);
        }
        assertEquals(validPoints.length, points.size());
    }

    @Test
    void testSkipping() {
        Vec s = new Vec(0.5, 40, 0.5);
        Vec e = new Vec(27, 0, 21);
        BlockIterator iterator = new BlockIterator(s, e, 0, 34);

        List<Point> points = new ArrayList<>();
        while (iterator.hasNext()) {
            points.add(iterator.next());
        }

        Point[] validPoints = new Point[]{
                new Vec(0.0, 40.0, 0.0),
                new Vec(1.0, 40.0, 0.0),
                new Vec(1.0, 40.0, 1.0),
                new Vec(2.0, 40.0, 1.0),
                new Vec(2.0, 40.0, 2.0),
                new Vec(3.0, 40.0, 2.0),
                new Vec(3.0, 40.0, 3.0),
                new Vec(4.0, 40.0, 3.0),
                new Vec(5.0, 40.0, 3.0),
                new Vec(4.0, 40.0, 4.0),
                new Vec(5.0, 40.0, 4.0),
                new Vec(6.0, 40.0, 4.0),
                new Vec(6.0, 40.0, 5.0),
                new Vec(7.0, 40.0, 5.0),
                new Vec(7.0, 40.0, 6.0),
                new Vec(8.0, 40.0, 6.0),
                new Vec(8.0, 40.0, 7.0),
                new Vec(9.0, 40.0, 7.0),
                new Vec(10.0, 40.0, 7.0),
                new Vec(10.0, 40.0, 8.0),
                new Vec(11.0, 40.0, 8.0),
                new Vec(11.0, 40.0, 9.0),
                new Vec(12.0, 40.0, 9.0),
                new Vec(12.0, 40.0, 10.0),
                new Vec(13.0, 40.0, 10.0),
                new Vec(14.0, 40.0, 10.0),
                new Vec(13.0, 40.0, 11.0),
                new Vec(14.0, 40.0, 11.0),
                new Vec(15.0, 40.0, 11.0),
                new Vec(15.0, 40.0, 12.0),
                new Vec(16.0, 40.0, 12.0),
                new Vec(16.0, 40.0, 13.0),
                new Vec(17.0, 40.0, 13.0),
                new Vec(17.0, 40.0, 14.0),
                new Vec(18.0, 40.0, 14.0),
                new Vec(19.0, 40.0, 14.0),
                new Vec(19.0, 40.0, 15.0),
                new Vec(20.0, 40.0, 15.0),
                new Vec(20.0, 40.0, 16.0),
                new Vec(21.0, 40.0, 16.0),
                new Vec(21.0, 40.0, 17.0),
                new Vec(22.0, 40.0, 17.0),
                new Vec(23.0, 40.0, 17.0),
                new Vec(22.0, 40.0, 18.0),
                new Vec(23.0, 40.0, 18.0),
                new Vec(24.0, 40.0, 18.0),
                new Vec(24.0, 40.0, 19.0),
                new Vec(25.0, 40.0, 19.0),
                new Vec(25.0, 40.0, 20.0),
                new Vec(26.0, 40.0, 20.0),
                new Vec(26.0, 40.0, 21.0),
                new Vec(27.0, 40.0, 21.0)
        };

        for (Point p : validPoints) {
            assertContains(points, p);
        }
        assertEquals(validPoints.length, points.size());
    }

    @Test
    void testExactEnd() {
        Vec s = new Vec(0.5,  0, 0.5);
        Vec e = new Vec(0, 1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 1);
        assertEquals(new Vec(0, 0, 0), iterator.next());
        assertEquals(new Vec(0, 1, 0), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void testSameEnd() {
        Vec s = new Vec(0.5,  0, 0.5);
        Vec e = new Vec(0, 1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 0.5);
        assertEquals(new Vec(0, 0, 0), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void test3dExtraCollection() {
        Vec s = new Vec(0.1,  0.1, 0.1);
        Vec e = new Vec(1, 1, 1);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        List<Point> points = new ArrayList<>();
        while (iterator.hasNext()) {
            points.add(iterator.next());
        }

        // todo(mattw): I need to confirm that these are correct
        Point[] validPoints = new Point[]{
                new Vec(0.0, 0.0, 0.0),
                new Vec(1.0, 1.0, 0.0),
                new Vec(0.0, 1.0, 1.0),
                new Vec(1.0, 0.0, 1.0),
                new Vec(1.0, 0.0, 0.0),
                new Vec(0.0, 1.0, 0.0),
                new Vec(0.0, 0.0, 1.0),
                new Vec(1.0, 1.0, 1.0),
                new Vec(2.0, 2.0, 1.0),
                new Vec(1.0, 2.0, 2.0),
                new Vec(2.0, 1.0, 2.0),
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
    void test2dpp() {
        Vec s = new Vec(0,  0, 0);
        Vec e = new Vec(2, 1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        List<Point> points = new ArrayList<>();
        while (iterator.hasNext()) {
            points.add(iterator.next());
        }

        Point[] validPoints = new Point[]{
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
    void test2dpn() {
        Vec s = new Vec(0,  0, 0);
        Vec e = new Vec(-2, 1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        List<Point> points = new ArrayList<>();
        while (iterator.hasNext()) {
            points.add(iterator.next());
        }

        Point[] validPoints = new Point[]{
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
    void test2dnn() {
        Vec s = new Vec(0,  0, 0);
        Vec e = new Vec(-2, -1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        List<Point> points = new ArrayList<>();
        while (iterator.hasNext()) {
            points.add(iterator.next());
        }

        Point[] validPoints = new Point[]{
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
    void falling() {
        Vec s = new Vec(0,  42, 0);
        Vec e = new Vec(0, -10, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 14.142135623730951);

        for (int y = 42; y >= 27; --y) assertEquals(new Vec(0, y, 0), iterator.next());
        assertFalse(iterator.hasNext());
    }
}