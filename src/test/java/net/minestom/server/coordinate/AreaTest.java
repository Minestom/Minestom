package net.minestom.server.coordinate;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.minestom.testing.TestUtils.assertPoint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    // Tests for split method
    @Test
    public void splitSingleSection() {
        Area.Cuboid cuboid = Area.cuboid(new BlockVec(0, 0, 0), new BlockVec(10, 5, 5));
        List<Area.Cuboid> splits = cuboid.split();
        assertEquals(1, splits.size());
        Area.Cuboid sub = splits.get(0);
        assertPoint(new BlockVec(0, 0, 0), sub.min());
        assertPoint(new BlockVec(10, 5, 5), sub.max());
    }

    @Test
    public void splitMultiSectionX() {
        Area.Cuboid cuboid = Area.cuboid(new BlockVec(15, 0, 0), new BlockVec(17, 1, 1));
        List<Area.Cuboid> splits = cuboid.split();
        assertEquals(1, splits.size());
        Area.Cuboid sub = splits.getFirst();
        assertPoint(new BlockVec(15, 0, 0), sub.min());
        assertPoint(new BlockVec(17, 1, 1), sub.max());
    }


    @Test
    public void splitOnSingle() {
        BlockVec point = new BlockVec(5, 5, 5);
        List<Area.Cuboid> splits = Area.single(point).split();
        assertEquals(1, splits.size());
        Area.Cuboid sub = splits.getFirst();
        assertPoint(point, sub.min());
        assertPoint(point, sub.max());
    }

    @Test
    public void splitLineSingleSection() {
        Area.Line line = Area.line(new BlockVec(1, 2, 3), new BlockVec(2, 2, 3));
        List<Area.Cuboid> splits = line.split();
        assertEquals(1, splits.size());
        Area.Cuboid sub = splits.getFirst();
        assertPoint(new BlockVec(1, 2, 3), sub.min());
        assertPoint(new BlockVec(2, 2, 3), sub.max());
    }

    @Test
    public void splitLineCrossSection() {
        Area.Line line = Area.line(new BlockVec(15, 0, 0), new BlockVec(17, 0, 0));
        List<Area.Cuboid> splits = line.split();
        assertEquals(2, splits.size());
        boolean sec0 = false, sec1 = false;
        for (Area.Cuboid sub : splits) {
            if (sub.min().equals(new BlockVec(15, 0, 0)) && sub.max().equals(new BlockVec(15, 0, 0))) sec0 = true;
            if (sub.min().equals(new BlockVec(16, 0, 0)) && sub.max().equals(new BlockVec(17, 0, 0))) sec1 = true;
        }
        assertTrue(sec0);
        assertTrue(sec1);
    }

    @Test
    public void splitSphere() {
        Area.Sphere sphere = Area.sphere(new BlockVec(0, 0, 0), 1);
        List<Area.Cuboid> splits = sphere.split();
        // Blocks span two sections along x: sections -1 and 0
        assertEquals(2, splits.size());
        boolean foundNeg = false, foundZero = false;
        for (Area.Cuboid sub : splits) {
            if (sub.min().equals(new BlockVec(-1, -1, -1)) && sub.max().equals(new BlockVec(-1, 1, 1))) foundNeg = true;
            if (sub.min().equals(new BlockVec(0, -1, -1)) && sub.max().equals(new BlockVec(1, 1, 1))) foundZero = true;
        }
        assertTrue(foundNeg);
        assertTrue(foundZero);
    }

    @Test
    public void splitSectionArea() {
        Area.Cuboid section = Area.section(0, 0, 0);
        Set<Area.Cuboid> expected = Set.of(section);
        Set<Area.Cuboid> actual = new HashSet<>(section.split());
        assertEquals(expected, actual);
    }

    @Test
    public void splitCuboidMultiSectionsX() {
        Area.Cuboid cuboid = Area.cuboid(new BlockVec(0, 0, 0), new BlockVec(17, 1, 1));
        // No full 16x16x16 section is fully covered, so no split
        List<Area.Cuboid> splits = cuboid.split();
        assertEquals(1, splits.size());
        Area.Cuboid single = splits.getFirst();
        assertPoint(new BlockVec(0, 0, 0), single.min());
        assertPoint(new BlockVec(17, 1, 1), single.max());
    }

    @Test
    public void splitTwoFullSectionsX() {
        // Cuboid covers two full 16x16x16 sections along X
        Area.Cuboid cuboid = Area.cuboid(new BlockVec(0, 0, 0), new BlockVec(31, 15, 15));
        Set<Area.Cuboid> expected = Set.of(
                Area.section(0, 0, 0),
                Area.section(1, 0, 0)
        );
        Set<Area.Cuboid> actual = new HashSet<>(cuboid.split());
        assertEquals(expected, actual);
    }

    @Test
    public void splitFullGridSections() {
        // Cuboid covers a 2x2x2 grid of full sections
        Area.Cuboid cuboid = Area.cuboid(new BlockVec(0, 0, 0), new BlockVec(31, 31, 31));
        Set<Area.Cuboid> expected = new HashSet<>();
        for (int x = 0; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = 0; z <= 1; z++) {
                    expected.add(Area.section(x, y, z));
                }
            }
        }
        Set<Area.Cuboid> actual = new HashSet<>(cuboid.split());
        assertEquals(expected, actual);
    }

    @Test
    public void boundSingle() {
        Area.Single single = Area.single(new BlockVec(5, 10, 15));
        Area.Cuboid bound = single.bound();
        assertPoint(new BlockVec(5, 10, 15), bound.min());
        assertPoint(new BlockVec(5, 10, 15), bound.max());
    }

    @Test
    public void boundLine() {
        Area.Line line = Area.line(new BlockVec(1, 2, 3), new BlockVec(4, 5, 6));
        Area.Cuboid bound = line.bound();
        assertPoint(new BlockVec(1, 2, 3), bound.min());
        assertPoint(new BlockVec(4, 5, 6), bound.max());
    }

    @Test
    public void boundLineReversed() {
        Area.Line line = Area.line(new BlockVec(4, 5, 6), new BlockVec(1, 2, 3));
        Area.Cuboid bound = line.bound();
        assertPoint(new BlockVec(1, 2, 3), bound.min());
        assertPoint(new BlockVec(4, 5, 6), bound.max());
    }

    @Test
    public void boundLineDiagonal() {
        Area.Line line = Area.line(new BlockVec(-2, 10, -5), new BlockVec(3, -1, 2));
        Area.Cuboid bound = line.bound();
        assertPoint(new BlockVec(-2, -1, -5), bound.min());
        assertPoint(new BlockVec(3, 10, 2), bound.max());
    }

    @Test
    public void boundCuboid() {
        Area.Cuboid cuboid = Area.cuboid(new BlockVec(1, 2, 3), new BlockVec(4, 5, 6));
        Area.Cuboid bound = cuboid.bound();
        // Bounding box of a cuboid should be itself
        assertPoint(cuboid.min(), bound.min());
        assertPoint(cuboid.max(), bound.max());
    }

    @Test
    public void boundCuboidUnordered() {
        Area.Cuboid cuboid = Area.cuboid(new BlockVec(4, 5, 6), new BlockVec(1, 2, 3));
        Area.Cuboid bound = cuboid.bound();
        // Should still return correctly ordered bounds
        assertPoint(new BlockVec(1, 2, 3), bound.min());
        assertPoint(new BlockVec(4, 5, 6), bound.max());
    }

    @Test
    public void boundSphere() {
        Area.Sphere sphere = Area.sphere(new BlockVec(0, 0, 0), 3);
        Area.Cuboid bound = sphere.bound();
        assertPoint(new BlockVec(-3, -3, -3), bound.min());
        assertPoint(new BlockVec(3, 3, 3), bound.max());
    }

    @Test
    public void boundSphereOffset() {
        Area.Sphere sphere = Area.sphere(new BlockVec(10, 20, 30), 5);
        Area.Cuboid bound = sphere.bound();
        assertPoint(new BlockVec(5, 15, 25), bound.min());
        assertPoint(new BlockVec(15, 25, 35), bound.max());
    }

    @Test
    public void boundSphereZeroRadius() {
        Area.Sphere sphere = Area.sphere(new BlockVec(1, 2, 3), 0);
        Area.Cuboid bound = sphere.bound();
        assertPoint(new BlockVec(1, 2, 3), bound.min());
        assertPoint(new BlockVec(1, 2, 3), bound.max());
    }
}
