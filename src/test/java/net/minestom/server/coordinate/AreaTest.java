package net.minestom.server.coordinate;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.minestom.testing.TestUtils.assertPoint;
import static org.junit.jupiter.api.Assertions.*;

public class AreaTest {

    @Test
    public void lineArea() {
        Area.Line line = Area.line(new BlockVec(0, 0, 0), new BlockVec(3, 0, 0));
        Set<BlockVec> actual = new HashSet<>();
        for (BlockVec v : line) actual.add(v);
        Set<BlockVec> expected = Set.of(new BlockVec(0, 0, 0), new BlockVec(1, 0, 0), new BlockVec(2, 0, 0), new BlockVec(3, 0, 0));
        assertEquals(expected, actual);

        // Diagonal line
        Area.Line diag = Area.line(new BlockVec(0, 0, 0), new BlockVec(2, 2, 2));
        actual.clear();
        for (BlockVec v : diag) actual.add(v);
        expected = Set.of(new BlockVec(0, 0, 0), new BlockVec(1, 1, 1), new BlockVec(2, 2, 2));
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
        Set<BlockVec> expected = Set.of(new BlockVec(0, 0, 0), new BlockVec(1, 0, 0), new BlockVec(2, 0, 0), new BlockVec(3, 0, 0));
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
                new BlockVec(1, 1, 0), new BlockVec(1, 1, 1));
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
        // Only blocks within distance 1.0 from center should be included
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance <= 1.0) {
                        expected.add(new BlockVec(x, y, z));
                    }
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
        Area.Cuboid sub = splits.getFirst();
        assertPoint(new BlockVec(0, 0, 0), sub.min());
        assertPoint(new BlockVec(10, 5, 5), sub.max());
    }

    @Test
    public void splitMultiSectionX() {
        Area.Cuboid cuboid = Area.cuboid(new BlockVec(15, 0, 0), new BlockVec(17, 1, 1));
        List<Area.Cuboid> splits = cuboid.split();
        assertEquals(2, splits.size());
        boolean foundSec0 = false, foundSec1 = false;
        for (Area.Cuboid sub : splits) {
            if (sub.min().equals(new BlockVec(15, 0, 0)) && sub.max().equals(new BlockVec(15, 1, 1)))
                foundSec0 = true;
            if (sub.min().equals(new BlockVec(16, 0, 0)) && sub.max().equals(new BlockVec(17, 1, 1)))
                foundSec1 = true;
        }
        assertTrue(foundSec0);
        assertTrue(foundSec1);
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
        assertEquals(2, splits.size()); // Now returns individual single-block cuboids

        // Verify all splits are single blocks that match the line
        Set<BlockVec> expectedBlocks = Set.of(new BlockVec(1, 2, 3), new BlockVec(2, 2, 3));
        Set<BlockVec> splitBlocks = new HashSet<>();
        for (Area.Cuboid split : splits) {
            for (BlockVec block : split) {
                splitBlocks.add(block);
            }
        }
        assertEquals(expectedBlocks, splitBlocks);
    }

    @Test
    public void splitLineCrossSection() {
        Area.Line line = Area.line(new BlockVec(15, 0, 0), new BlockVec(17, 0, 0));
        List<Area.Cuboid> splits = line.split();
        assertEquals(3, splits.size()); // Now returns individual single-block cuboids

        // Verify all splits are single blocks that match the line
        Set<BlockVec> expectedBlocks = Set.of(
                new BlockVec(15, 0, 0),
                new BlockVec(16, 0, 0),
                new BlockVec(17, 0, 0)
        );
        Set<BlockVec> splitBlocks = new HashSet<>();
        for (Area.Cuboid split : splits) {
            for (BlockVec block : split) {
                splitBlocks.add(block);
            }
        }
        assertEquals(expectedBlocks, splitBlocks);
    }

    @Test
    public void splitSphere() {
        Area.Sphere sphere = Area.sphere(new BlockVec(0, 0, 0), 1);
        List<Area.Cuboid> splits = sphere.split();
        // A sphere with radius 1 centered at origin will span multiple sections
        // since it includes blocks from (-1,-1,-1) to (1,1,1) range
        assertTrue(splits.size() > 0);

        // Verify that split covers exactly the sphere blocks
        Set<BlockVec> allSplitBlocks = new HashSet<>();
        for (Area.Cuboid split : splits) {
            for (BlockVec block : split) {
                allSplitBlocks.add(block);
            }
        }

        Set<BlockVec> sphereBlocks = new HashSet<>();
        for (BlockVec block : sphere) {
            sphereBlocks.add(block);
        }

        // All sphere blocks should be covered by splits
        assertTrue(allSplitBlocks.containsAll(sphereBlocks));
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
        // Spans two sections, should be split into 2 cuboids
        List<Area.Cuboid> splits = cuboid.split();
        assertEquals(2, splits.size());
        boolean foundSec0 = false, foundSec1 = false;
        for (Area.Cuboid sub : splits) {
            if (sub.min().equals(new BlockVec(0, 0, 0)) && sub.max().equals(new BlockVec(15, 1, 1)))
                foundSec0 = true;
            if (sub.min().equals(new BlockVec(16, 0, 0)) && sub.max().equals(new BlockVec(17, 1, 1)))
                foundSec1 = true;
        }
        assertTrue(foundSec0);
        assertTrue(foundSec1);
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

    // Additional comprehensive iterator tests
    @Test
    public void lineIteratorEdgeCases() {
        // Test zero-length line
        Area.Line zeroLine = Area.line(new BlockVec(5, 5, 5), new BlockVec(5, 5, 5));
        Set<BlockVec> expected = Set.of(new BlockVec(5, 5, 5));
        Set<BlockVec> actual = new HashSet<>();
        for (BlockVec v : zeroLine) actual.add(v);
        assertEquals(expected, actual);

        // Test negative coordinates
        Area.Line negativeLine = Area.line(new BlockVec(-2, -3, -4), new BlockVec(-1, -2, -3));
        expected = Set.of(new BlockVec(-2, -3, -4), new BlockVec(-1, -2, -3));
        actual.clear();
        for (BlockVec v : negativeLine) actual.add(v);
        assertEquals(expected, actual);
    }

    @Test
    public void sphereIteratorVariousRadii() {
        // Test radius 0 (single block)
        Area.Sphere sphere0 = Area.sphere(new BlockVec(0, 0, 0), 0);
        Set<BlockVec> expected = Set.of(new BlockVec(0, 0, 0));
        Set<BlockVec> actual = new HashSet<>();
        for (BlockVec v : sphere0) actual.add(v);
        assertEquals(expected, actual);

        // Test radius 2
        Area.Sphere sphere2 = Area.sphere(new BlockVec(0, 0, 0), 2);
        actual.clear();
        for (BlockVec v : sphere2) actual.add(v);

        // Verify all blocks are within radius 2
        for (BlockVec block : actual) {
            double distance = Math.sqrt(block.blockX() * block.blockX() +
                    block.blockY() * block.blockY() +
                    block.blockZ() * block.blockZ());
            assertTrue(distance <= 2.0, "Block " + block + " is outside radius 2, distance: " + distance);
        }
    }

    @Test
    public void cuboidIteratorLargeArea() {
        Area.Cuboid largeCuboid = Area.cuboid(new BlockVec(0, 0, 0), new BlockVec(3, 2, 1));
        Set<BlockVec> actual = new HashSet<>();
        for (BlockVec v : largeCuboid) actual.add(v);

        // Should have 4 * 3 * 2 = 24 blocks
        assertEquals(24, actual.size());

        // Verify all expected blocks are present
        for (int x = 0; x <= 3; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = 0; z <= 1; z++) {
                    assertTrue(actual.contains(new BlockVec(x, y, z)));
                }
            }
        }
    }

    // Comprehensive split() tests
    @Test
    public void splitSingleInDifferentSections() {
        // Test single blocks in different sections
        Area.Single single1 = Area.single(new BlockVec(0, 0, 0));
        assertEquals(1, single1.split().size());

        Area.Single single2 = Area.single(new BlockVec(16, 16, 16));
        assertEquals(1, single2.split().size());

        Area.Single single3 = Area.single(new BlockVec(-1, -1, -1));
        assertEquals(1, single3.split().size());
    }

    @Test
    public void splitCuboidFullSectionOptimization() {
        // Test that full sections are properly identified
        Area.Cuboid fullSection = Area.section(1, 1, 1);
        List<Area.Cuboid> splits = fullSection.split();
        assertEquals(1, splits.size());
        assertEquals(fullSection, splits.getFirst());
    }

    @Test
    public void splitCuboidPartialSections() {
        // Cuboid that partially fills multiple sections
        Area.Cuboid partial = Area.cuboid(new BlockVec(14, 14, 14), new BlockVec(18, 18, 18));
        List<Area.Cuboid> splits = partial.split();
        assertEquals(8, splits.size()); // 2x2x2 sections

        // Verify no section boundary violations
        for (Area.Cuboid split : splits) {
            int secMinX = Math.floorDiv(split.min().blockX(), 16);
            int secMaxX = Math.floorDiv(split.max().blockX(), 16);
            int secMinY = Math.floorDiv(split.min().blockY(), 16);
            int secMaxY = Math.floorDiv(split.max().blockY(), 16);
            int secMinZ = Math.floorDiv(split.min().blockZ(), 16);
            int secMaxZ = Math.floorDiv(split.max().blockZ(), 16);

            assertEquals(secMinX, secMaxX, "Split crosses section boundary in X");
            assertEquals(secMinY, secMaxY, "Split crosses section boundary in Y");
            assertEquals(secMinZ, secMaxZ, "Split crosses section boundary in Z");
        }
    }

    @Test
    public void splitSphereFullAndPartialSections() {
        // Large sphere that should have both full and partial sections
        Area.Sphere largeSphere = Area.sphere(new BlockVec(16, 16, 16), 20);
        List<Area.Cuboid> splits = largeSphere.split();

        // Verify that split covers all sphere blocks
        Set<BlockVec> allSplitBlocks = new HashSet<>();
        for (Area.Cuboid split : splits) {
            for (BlockVec block : split) {
                allSplitBlocks.add(block);
            }
        }

        Set<BlockVec> sphereBlocks = new HashSet<>();
        for (BlockVec block : largeSphere) {
            sphereBlocks.add(block);
        }

        // All sphere blocks should be covered by splits
        assertTrue(allSplitBlocks.containsAll(sphereBlocks));

        // No extra blocks should be in splits (beyond minimal bounding)
        for (BlockVec block : allSplitBlocks) {
            // Check if block is within the sphere's bounding box
            assertTrue(block.blockX() >= 16 - 20 && block.blockX() <= 16 + 20);
            assertTrue(block.blockY() >= 16 - 20 && block.blockY() <= 16 + 20);
            assertTrue(block.blockZ() >= 16 - 20 && block.blockZ() <= 16 + 20);
        }
    }

    @Test
    public void splitSphereZeroRadius() {
        Area.Sphere pointSphere = Area.sphere(new BlockVec(5, 5, 5), 0);
        List<Area.Cuboid> splits = pointSphere.split();
        assertEquals(1, splits.size());

        Area.Cuboid split = splits.getFirst();
        assertEquals(new BlockVec(5, 5, 5), split.min());
        assertEquals(new BlockVec(5, 5, 5), split.max());
    }

    @Test
    public void splitNegativeCoordinates() {
        // Test areas in negative coordinate space
        Area.Cuboid negativeCuboid = Area.cuboid(new BlockVec(-20, -20, -20), new BlockVec(-5, -5, -5));
        List<Area.Cuboid> splits = negativeCuboid.split();
        assertFalse(splits.isEmpty());

        // Verify all splits are section-aligned
        for (Area.Cuboid split : splits) {
            int secMinX = Math.floorDiv(split.min().blockX(), 16);
            int secMaxX = Math.floorDiv(split.max().blockX(), 16);
            assertEquals(secMinX, secMaxX, "Split crosses section boundary in negative space");
        }
    }

    @Test
    public void iteratorAndSplitConsistency() {
        // Verify that split() covers exactly the same blocks as iterator()
        for (Area area : areas()) {
            Set<BlockVec> iteratorBlocks = new HashSet<>();
            for (BlockVec block : area) {
                iteratorBlocks.add(block);
            }

            Set<BlockVec> splitBlocks = new HashSet<>();
            for (Area.Cuboid split : area.split()) {
                for (BlockVec block : split) {
                    splitBlocks.add(block);
                }
            }

            // Split should contain exactly the same blocks as iterator - no more, no less
            assertEquals(iteratorBlocks, splitBlocks,
                    "Split blocks don't exactly match iterator blocks for " + area.getClass().getSimpleName() +
                            ". Iterator has " + iteratorBlocks.size() + " blocks, split has " + splitBlocks.size() + " blocks");
        }
    }

    @Test
    public void splitSectionAlignment() {
        // Verify all splits are properly section-aligned
        for (Area area : areas()) {
            for (Area.Cuboid split : area.split()) {
                // Each split should be within a single section
                int secMinX = Math.floorDiv(split.min().blockX(), 16);
                int secMaxX = Math.floorDiv(split.max().blockX(), 16);
                int secMinY = Math.floorDiv(split.min().blockY(), 16);
                int secMaxY = Math.floorDiv(split.max().blockY(), 16);
                int secMinZ = Math.floorDiv(split.min().blockZ(), 16);
                int secMaxZ = Math.floorDiv(split.max().blockZ(), 16);

                assertEquals(secMinX, secMaxX, "Split crosses section boundary in X for " + area.getClass().getSimpleName());
                assertEquals(secMinY, secMaxY, "Split crosses section boundary in Y for " + area.getClass().getSimpleName());
                assertEquals(secMinZ, secMaxZ, "Split crosses section boundary in Z for " + area.getClass().getSimpleName());
            }
        }
    }

    private static List<Area> areas() {
        return List.of(
                Area.single(new BlockVec(7, 8, 9)),
                Area.line(new BlockVec(0, 0, 0), new BlockVec(5, 3, 2)),
                Area.cuboid(new BlockVec(10, 10, 10), new BlockVec(12, 12, 12)),
                Area.sphere(new BlockVec(0, 0, 0), 3),
                Area.line(new BlockVec(14, 0, 0), new BlockVec(34, 0, 0)), // Multi-section line
                Area.sphere(new BlockVec(8, 8, 8), 2), // Small multi-section sphere
                Area.cuboid(new BlockVec(-5, -5, -5), new BlockVec(5, 5, 5)) // Negative coordinates
        );
    }
}
