package net.minestom.server.coordinate;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

public class AreaTest {

    @Test
    public void testSingleFillArea() {
        Area area = Area.fill(new Vec(0, 0, 0), new Vec(1, 1, 1));

        Set<Point> points = points(area);

        assertTrue(points.contains(new Vec(0, 0, 0)), "Point(0, 0, 0) should be in the area");
        assertFalse(points.contains(new Vec(0, 0, 1)), "Point(0, 0, 1) should not be in the area");
    }

    @Test
    public void testSmallFillArea() {
        Area area = Area.fill(new Vec(0, 0, 0), new Vec(2, 2, 2));

        Set<Point> points = points(area);

        assertTrue(points.contains(new Vec(0, 0, 0)), "Point(0, 0, 0) should be in the area");
        assertTrue(points.contains(new Vec(1, 1, 1)), "Point(1, 1, 1) should be in the area");
        assertFalse(points.contains(new Vec(0, 0, 3)), "Point(0, 0, 3) should not be in the area");
    }

    @Test
    public void testLargeFillArea() {
        Area area = Area.fill(new Vec(0, 0, 0), new Vec(100, 100, 100));

        Set<Point> points = points(area);

        assertTrue(points.contains(new Vec(0, 0, 0)), "Point(0, 0, 0) should be in the area");
        assertTrue(points.contains(new Vec(50, 50, 50)), "Point(50, 50, 50) should be in the area");
        assertTrue(points.contains(new Vec(99, 99, 99)), "Point(99, 99, 99) should be in the area");
        assertFalse(points.contains(new Vec(0, 0, 101)), "Point(0, 0, 101) should not be in the area");
    }

    @Test
    public void testSingleCollectionArea() {
        Area area = Area.collection(List.of(new Vec(0, 0, 0)));

        Set<Point> points = points(area);

        assertTrue(points.contains(new Vec(0, 0, 0)), "Point(0, 0, 0) should be in the area");
        assertFalse(points.contains(new Vec(0, 0, 1)), "Point(0, 0, 1) should not be in the area");
    }

    @Test
    public void testSmallCollectionArea() {
        Area area = Area.collection(List.of(new Vec(0, 0, 0), new Vec(1, 0, 0)));

        Set<Point> points = points(area);

        assertTrue(points.contains(new Vec(0, 0, 0)), "Point(0, 0, 0) should be in the area");
        assertTrue(points.contains(new Vec(1, 0, 0)), "Point(1, 0, 0) should be in the area");
        assertFalse(points.contains(new Vec(0, 0, 1)), "Point(0, 0, 1) should not be in the area");
        assertFalse(points.contains(new Vec(2, 0, 0)), "Point(2, 0, 0) should not be in the area");
    }

    @Test
    public void testUnionFillCollection() {
        Area area = Area.union(
            Area.fill(new Vec(0, 0, 0), new Vec(2, 2, 2)),
            Area.collection(List.of(new Vec(2, 1, 1)))
        );

        Set<Point> points = points(area);

        assertTrue(points.contains(new Vec(0, 0, 0)), "Point(0, 0, 0) should be in the area");
        assertTrue(points.contains(new Vec(1, 1, 1)), "Point(1, 1, 1) should be in the area");
        assertTrue(points.contains(new Vec(2, 1, 1)), "Point(2, 1, 1) should be in the area");
        assertFalse(points.contains(new Vec(0, 0, 3)), "Point(0, 0, 3) should not be in the area");
        assertFalse(points.contains(new Vec(2, 0, 0)), "Point(2, 0, 0) should not be in the area");
        assertFalse(points.contains(new Vec(3, 0, 0)), "Point(3, 0, 0) should not be in the area");
    }

    @Test
    public void testIntersectionFillCollection() {
        Area area = Area.intersection(
            Area.fill(new Vec(0, 0, 0), new Vec(2, 2, 2)),
            Area.collection(List.of(new Vec(1, 1, 1)))
        );

        Set<Point> points = points(area);

        assertFalse(points.contains(new Vec(0, 0, 0)), "Point(0, 0, 0) should not be in the area");
        assertTrue(points.contains(new Vec(1, 1, 1)), "Point(1, 1, 1) should be in the area");
        assertFalse(points.contains(new Vec(0, 0, 1)), "Point(0, 0, 1) should not be in the area");
        assertFalse(points.contains(new Vec(1, 0, 0)), "Point(1, 0, 0) should not be in the area");
        assertFalse(points.contains(new Vec(0, 1, 0)), "Point(0, 1, 0) should not be in the area");
        assertFalse(points.contains(new Vec(0, 0, 2)), "Point(0, 0, 2) should not be in the area");
    }

    private Set<Point> points(Area area) {
        return StreamSupport.stream(area.spliterator(), false)
                .collect(Collectors.toSet());
    }
}
