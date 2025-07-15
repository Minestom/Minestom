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
    public void cuboidSingle() {
        Area.Cuboid area = Area.cuboid(BlockVec.ZERO, BlockVec.ZERO);
        Set<BlockVec> expected = Set.of(BlockVec.ZERO);
        Set<BlockVec> actual = new HashSet<>();
        for (BlockVec v : area) actual.add(v);
        assertEquals(expected, actual);
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

    // Exhaustive iteration tests for all Area types
    @Test
    public void singleAreaIteration() {
        Area.Single single = Area.single(new BlockVec(1, 2, 3));
        Set<BlockVec> expected = Set.of(new BlockVec(1, 2, 3));
        Set<BlockVec> actual = new HashSet<>();
        for (BlockVec v : single) actual.add(v);
        assertEquals(expected, actual);
    }

    @Test
    public void lineAreaReverse() {
        Area.Line line = Area.line(new BlockVec(3, 0, 0), new BlockVec(0, 0, 0));
        Set<BlockVec> expected = Set.of(
                new BlockVec(0, 0, 0), new BlockVec(1, 0, 0), new BlockVec(2, 0, 0), new BlockVec(3, 0, 0)
        );
        Set<BlockVec> actual = new HashSet<>();
        for (BlockVec v : line) actual.add(v);
        assertEquals(expected, actual);
    }

    @Test
    public void cuboidIteration() {
        Area.Cuboid cuboid = Area.cuboid(new BlockVec(0, 0, 0), new BlockVec(1, 1, 1));
        Set<BlockVec> expected = Set.of(
                new BlockVec(0, 0, 0), new BlockVec(0, 0, 1),
                new BlockVec(0, 1, 0), new BlockVec(0, 1, 1),
                new BlockVec(1, 0, 0), new BlockVec(1, 0, 1),
                new BlockVec(1, 1, 0), new BlockVec(1, 1, 1)
        );
        Set<BlockVec> actual = new HashSet<>();
        for (BlockVec v : cuboid) actual.add(v);
        assertEquals(expected, actual);
    }

    @Test
    public void cuboidIterationUnorderedEndpoints() {
        Area.Cuboid cuboid = Area.cuboid(new BlockVec(2, 3, 4), new BlockVec(1, 2, 3));
        Set<BlockVec> expected = new HashSet<>();
        for (int x = 1; x <= 2; x++) {
            for (int y = 2; y <= 3; y++) {
                for (int z = 3; z <= 4; z++) {
                    expected.add(new BlockVec(x, y, z));
                }
            }
        }
        Set<BlockVec> actual = new HashSet<>();
        for (BlockVec v : cuboid) actual.add(v);
        assertEquals(expected, actual);
    }

    @Test
    public void sphereIterationRadius1() {
        Area.Sphere sphere = Area.sphere(new BlockVec(0, 0, 0), 1);
        Set<BlockVec> expected = new HashSet<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    expected.add(new BlockVec(x, y, z));
                }
            }
        }
        Set<BlockVec> actual = new HashSet<>();
        for (BlockVec v : sphere) actual.add(v);
        assertEquals(expected, actual);
    }

    @Test
    public void offsetCuboidIteration() {
        Area.Cuboid cuboid = Area.cuboid(new BlockVec(0, 0, 0), new BlockVec(1, 1, 1));
        Area offset = cuboid.offset(1, 2, 3);
        Set<BlockVec> expected = new HashSet<>();
        for (int x = 1; x <= 2; x++) {
            for (int y = 2; y <= 3; y++) {
                for (int z = 3; z <= 4; z++) {
                    expected.add(new BlockVec(x, y, z));
                }
            }
        }
        Set<BlockVec> actual = new HashSet<>();
        for (BlockVec v : offset) actual.add(v);
        assertEquals(expected, actual);
    }

    @Test
    public void cubeArea() {
        Area.Cuboid cube = Area.cube(new BlockVec(0, 0, 0), 2);
        Set<BlockVec> expected = new HashSet<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    expected.add(new BlockVec(x, y, z));
                }
            }
        }
        Set<BlockVec> actual = new HashSet<>();
        for (BlockVec v : cube) actual.add(v);
        assertEquals(expected, actual);
    }
}
