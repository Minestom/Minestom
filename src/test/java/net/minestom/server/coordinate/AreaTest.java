package net.minestom.server.coordinate;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static net.minestom.testing.TestUtils.assertPoint;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AreaTest {

    @Test
    public void lineArea() {
        Area.Line line = Area.line(new BlockVec(0, 0, 0), new BlockVec(3, 0, 0));
        Set<BlockVec> actual = new HashSet<>();
        for (BlockVec v : line) actual.add(v);
        Set<BlockVec> expected = Set.of(
                new BlockVec(0, 0, 0), new BlockVec(1, 0, 0), new BlockVec(2, 0, 0), new BlockVec(3, 0, 0)
        );
        assertEquals(expected, actual);

        // Diagonal line
        Area.Line diag = Area.line(new BlockVec(0, 0, 0), new BlockVec(2, 2, 2));
        actual.clear();
        for (BlockVec v : diag) actual.add(v);
        expected = Set.of(
                new BlockVec(0, 0, 0), new BlockVec(1, 1, 1), new BlockVec(2, 2, 2)
        );
        assertEquals(expected, actual);
    }

    @Test
    public void cuboidArea() {
        Area.Cuboid area = Area.cuboid(new BlockVec(1, 2, 3), new BlockVec(4, 5, 6));
        assertPoint(new BlockVec(1, 2, 3), area.min());
        assertPoint(new BlockVec(4, 5, 6), area.max());
    }

    @Test
    public void sectionArea() {
        Area.Cuboid section = Area.section(0, 0, 0);
        assertPoint(new BlockVec(0, 0, 0), section.min());
        assertPoint(new BlockVec(15, 15, 15), section.max());
    }

    @Test
    public void sphereArea() {
        Area.Sphere sphere = Area.sphere(new BlockVec(0, 0, 0), 5);
        assertPoint(new BlockVec(0, 0, 0), sphere.center());
        assertEquals(5, sphere.radius());
    }
}
