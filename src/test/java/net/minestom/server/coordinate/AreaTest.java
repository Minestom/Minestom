package net.minestom.server.coordinate;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static net.minestom.testing.TestUtils.assertPoint;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AreaTest {

    @Test
    public void lineArea() {
        Area.Line line = Area.line(new Vec(0, 0, 0), new Vec(3, 0, 0));
        Set<Vec> actual = new HashSet<>();
        for (Vec v : line) actual.add(v);
        Set<Vec> expected = Set.of(
                new Vec(0, 0, 0), new Vec(1, 0, 0), new Vec(2, 0, 0), new Vec(3, 0, 0)
        );
        assertEquals(expected, actual);

        // Diagonal line
        Area.Line diag = Area.line(new Vec(0, 0, 0), new Vec(2, 2, 2));
        actual.clear();
        for (Vec v : diag) actual.add(v);
        expected = Set.of(
                new Vec(0, 0, 0), new Vec(1, 1, 1), new Vec(2, 2, 2)
        );
        assertEquals(expected, actual);
    }

    @Test
    public void cuboidArea() {
        Area.Cuboid area = Area.cuboid(new Vec(1, 2, 3), new Vec(4, 5, 6));
        assertPoint(new Vec(1, 2, 3), area.min());
        assertPoint(new Vec(4, 5, 6), area.max());
    }

    @Test
    public void sectionArea() {
        Area.Cuboid section = Area.section(0, 0, 0);
        assertPoint(new Vec(0, 0, 0), section.min());
        assertPoint(new Vec(15, 15, 15), section.max());
    }

    @Test
    public void sphereArea() {
        Area.Sphere sphere = Area.sphere(new Vec(0, 0, 0), 5);
        assertPoint(new Vec(0, 0, 0), sphere.center());
        assertEquals(5, sphere.radius());
    }
}
