package net.minestom.server.coordinate;

import net.minestom.server.instance.block.BlockFace;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class BlockVecTest {

    @Test
    public void testConstructors() {
        // Test primary constructor
        BlockVec vec1 = new BlockVec(5, 10, 15);
        assertEquals(5, vec1.blockX());
        assertEquals(10, vec1.blockY());
        assertEquals(15, vec1.blockZ());

        // Test double constructor (floors values)
        BlockVec vec2 = new BlockVec(5.7, 10.3, 15.9);
        assertEquals(5, vec2.blockX());
        assertEquals(10, vec2.blockY());
        assertEquals(15, vec2.blockZ());

        // Test negative double values
        BlockVec vec3 = new BlockVec(-5.7, -10.3, -15.9);
        assertEquals(-6, vec3.blockX());
        assertEquals(-11, vec3.blockY());
        assertEquals(-16, vec3.blockZ());

        // Test single value constructor (int)
        BlockVec vec4 = new BlockVec(7);
        assertEquals(7, vec4.blockX());
        assertEquals(7, vec4.blockY());
        assertEquals(7, vec4.blockZ());

        // Test single value constructor (double)
        BlockVec vec5 = new BlockVec(7.5);
        assertEquals(7, vec5.blockX());
        assertEquals(7, vec5.blockY());
        assertEquals(7, vec5.blockZ());

        // Test double value constructor (int)
        BlockVec vec6 = new BlockVec(6, 7);
        assertEquals(6, vec6.blockX());
        assertEquals(0, vec6.blockY());
        assertEquals(7, vec6.blockZ());
    }

    @Test
    public void testConstants() {
        assertEquals(0, BlockVec.ZERO.blockX());
        assertEquals(0, BlockVec.ZERO.blockY());
        assertEquals(0, BlockVec.ZERO.blockZ());

        assertEquals(1, BlockVec.ONE.blockX());
        assertEquals(1, BlockVec.ONE.blockY());
        assertEquals(1, BlockVec.ONE.blockZ());

        assertEquals(Point.SECTION_SIZE, BlockVec.SECTION.blockX());
        assertEquals(Point.SECTION_SIZE, BlockVec.SECTION.blockY());
        assertEquals(Point.SECTION_SIZE, BlockVec.SECTION.blockZ());
    }

    @Test
    public void testWithBlock() {
        BlockVec base = new BlockVec(10, 20, 30);

        // Test withBlockX
        BlockVec modified = base.withBlockX(15);
        assertEquals(15, modified.blockX());
        assertEquals(20, modified.blockY());
        assertEquals(30, modified.blockZ());

        // Test withBlockX with operator
        modified = base.withBlockX(x -> x * 2);
        assertEquals(20, modified.blockX());
        assertEquals(20, modified.blockY());
        assertEquals(30, modified.blockZ());

        // Test withBlockY
        modified = base.withBlockY(25);
        assertEquals(10, modified.blockX());
        assertEquals(25, modified.blockY());
        assertEquals(30, modified.blockZ());

        // Test withBlockY with operator
        modified = base.withBlockY(y -> y + 5);
        assertEquals(10, modified.blockX());
        assertEquals(25, modified.blockY());
        assertEquals(30, modified.blockZ());

        // Test withBlockZ
        modified = base.withBlockZ(35);
        assertEquals(10, modified.blockX());
        assertEquals(20, modified.blockY());
        assertEquals(35, modified.blockZ());

        // Test withBlockZ with operator
        modified = base.withBlockZ(z -> z - 10);
        assertEquals(10, modified.blockX());
        assertEquals(20, modified.blockY());
        assertEquals(20, modified.blockZ());
    }

    @Test
    public void testIntegerArithmetic() {
        BlockVec v1 = new BlockVec(10, 20, 30);
        BlockVec v2 = new BlockVec(5, 8, 12);

        // Test add with integers
        BlockVec result = v1.add(5, 10, 15);
        assertEquals(15, result.blockX());
        assertEquals(30, result.blockY());
        assertEquals(45, result.blockZ());

        // Test add with BlockVec
        result = v1.add(v2);
        assertEquals(15, result.blockX());
        assertEquals(28, result.blockY());
        assertEquals(42, result.blockZ());

        // Test add with single value
        result = v1.add(3);
        assertEquals(13, result.blockX());
        assertEquals(23, result.blockY());
        assertEquals(33, result.blockZ());

        // Test sub with integers
        result = v1.sub(5, 10, 15);
        assertEquals(5, result.blockX());
        assertEquals(10, result.blockY());
        assertEquals(15, result.blockZ());

        // Test sub with BlockVec
        result = v1.sub(v2);
        assertEquals(5, result.blockX());
        assertEquals(12, result.blockY());
        assertEquals(18, result.blockZ());

        // Test sub with single value
        result = v1.sub(3);
        assertEquals(7, result.blockX());
        assertEquals(17, result.blockY());
        assertEquals(27, result.blockZ());
    }

    @Test
    public void testMultiplicationDivision() {
        BlockVec base = new BlockVec(10, 20, 30);

        // Test mul with integers
        BlockVec result = base.mul(2, 3, 4);
        assertEquals(20, result.blockX());
        assertEquals(60, result.blockY());
        assertEquals(120, result.blockZ());

        // Test mul with BlockVec
        BlockVec v2 = new BlockVec(2, 3, 4);
        result = base.mul(v2);
        assertEquals(20, result.blockX());
        assertEquals(60, result.blockY());
        assertEquals(120, result.blockZ());

        // Test mul with single value
        result = base.mul(2);
        assertEquals(20, result.blockX());
        assertEquals(40, result.blockY());
        assertEquals(60, result.blockZ());

        // Test div with integers
        result = base.div(2, 4, 5);
        assertEquals(5, result.blockX());
        assertEquals(5, result.blockY());
        assertEquals(6, result.blockZ());

        // Test div with BlockVec
        result = base.div(new BlockVec(2, 4, 5));
        assertEquals(5, result.blockX());
        assertEquals(5, result.blockY());
        assertEquals(6, result.blockZ());

        // Test div with single value
        result = base.div(2);
        assertEquals(5, result.blockX());
        assertEquals(10, result.blockY());
        assertEquals(15, result.blockZ());
    }

    @Test
    public void testMinMax() {
        BlockVec v1 = new BlockVec(10, 20, 30);
        BlockVec v2 = new BlockVec(15, 5, 25);

        // Test min with BlockVec
        BlockVec result = v1.min(v2);
        assertEquals(10, result.blockX());
        assertEquals(5, result.blockY());
        assertEquals(25, result.blockZ());

        // Test min with coordinates
        result = v1.min(15, 5, 25);
        assertEquals(10, result.blockX());
        assertEquals(5, result.blockY());
        assertEquals(25, result.blockZ());

        // Test min with single value
        result = v1.min(15);
        assertEquals(10, result.blockX());
        assertEquals(15, result.blockY());
        assertEquals(15, result.blockZ());

        // Test max with BlockVec
        result = v1.max(v2);
        assertEquals(15, result.blockX());
        assertEquals(20, result.blockY());
        assertEquals(30, result.blockZ());

        // Test max with coordinates
        result = v1.max(15, 5, 25);
        assertEquals(15, result.blockX());
        assertEquals(20, result.blockY());
        assertEquals(30, result.blockZ());

        // Test max with single value
        result = v1.max(15);
        assertEquals(15, result.blockX());
        assertEquals(20, result.blockY());
        assertEquals(30, result.blockZ());
    }

    @Test
    public void testNegAbs() {
        BlockVec positive = new BlockVec(10, 20, 30);
        BlockVec negative = new BlockVec(-10, -20, -30);
        BlockVec mixed = new BlockVec(-10, 20, -30);

        // Test neg
        BlockVec result = positive.neg();
        assertEquals(-10, result.blockX());
        assertEquals(-20, result.blockY());
        assertEquals(-30, result.blockZ());

        result = negative.neg();
        assertEquals(10, result.blockX());
        assertEquals(20, result.blockY());
        assertEquals(30, result.blockZ());

        // Test abs
        result = negative.abs();
        assertEquals(10, result.blockX());
        assertEquals(20, result.blockY());
        assertEquals(30, result.blockZ());

        result = mixed.abs();
        assertEquals(10, result.blockX());
        assertEquals(20, result.blockY());
        assertEquals(30, result.blockZ());
    }

    @Test
    public void testCross() {
        BlockVec v1 = new BlockVec(1, 0, 0);
        BlockVec v2 = new BlockVec(0, 1, 0);

        // Cross product of unit vectors
        BlockVec result = v1.cross(v2);
        assertEquals(0, result.blockX());
        assertEquals(0, result.blockY());
        assertEquals(1, result.blockZ());

        // Reverse order
        result = v2.cross(v1);
        assertEquals(0, result.blockX());
        assertEquals(0, result.blockY());
        assertEquals(-1, result.blockZ());

        // More complex case
        BlockVec v3 = new BlockVec(2, 3, 4);
        BlockVec v4 = new BlockVec(5, 6, 7);
        result = v3.cross(v4);
        assertEquals(-3, result.blockX());
        assertEquals(6, result.blockY());
        assertEquals(-3, result.blockZ());
    }

    @Test
    public void testSamePoint() {
        BlockVec v1 = new BlockVec(10, 20, 30);
        BlockVec v2 = new BlockVec(10, 20, 30);
        BlockVec v3 = new BlockVec(10, 20, 31);

        // Test with BlockVec
        assertTrue(v1.samePoint(v2));
        assertFalse(v1.samePoint(v3));

        // Test with coordinates
        assertTrue(v1.samePoint(10, 20, 30));
        assertFalse(v1.samePoint(10, 20, 31));
        assertFalse(v1.samePoint(11, 20, 30));
        assertFalse(v1.samePoint(10, 21, 30));
    }

    @Test
    public void testRelative() {
        BlockVec base = new BlockVec(10, 20, 30);

        assertEquals(new BlockVec(10, 21, 30), base.relative(BlockFace.TOP));
        assertEquals(new BlockVec(10, 19, 30), base.relative(BlockFace.BOTTOM));
        assertEquals(new BlockVec(11, 20, 30), base.relative(BlockFace.EAST));
        assertEquals(new BlockVec(9, 20, 30), base.relative(BlockFace.WEST));
        assertEquals(new BlockVec(10, 20, 31), base.relative(BlockFace.SOUTH));
        assertEquals(new BlockVec(10, 20, 29), base.relative(BlockFace.NORTH));
    }

    @Test
    public void testApply() {
        BlockVec base = new BlockVec(10, 20, 30);

        // Test operator that doubles all values
        BlockVec result = base.apply((x, y, z) -> new BlockVec(x * 2, y * 2, z * 2));
        assertEquals(20, result.blockX());
        assertEquals(40, result.blockY());
        assertEquals(60, result.blockZ());

        // Test operator that creates constant
        result = base.apply((_, _, _) -> BlockVec.ZERO);
        assertEquals(BlockVec.ZERO, result);
    }

    @Test
    public void testAsBlockVec() {
        Point vec = new BlockVec(5, 10, 15);
        assertSame(vec, vec.asBlockVec());
    }

    @Test
    public void testDoubleOperations() {
        BlockVec base = new BlockVec(10, 20, 30);

        // Test that double operations return Vec
        Vec result = base.add(1.5, 2.5, 3.5);
        assertInstanceOf(Vec.class, result);
        assertEquals(11.5, result.x(), 0.001);
        assertEquals(22.5, result.y(), 0.001);
        assertEquals(33.5, result.z(), 0.001);

        // Test withX returns Vec
        result = base.withX(15.5);
        assertInstanceOf(Vec.class, result);
        assertEquals(15.5, result.x(), 0.001);

        // Test normalize returns Vec
        result = base.normalize();
        assertInstanceOf(Vec.class, result);
    }

    @Test
    public void testRandomBlockVectors() {
        Random random = new Random(12345L);

        for (int i = 0; i < 100; i++) {
            int x = random.nextInt(2000) - 1000;
            int y = random.nextInt(2000) - 1000;
            int z = random.nextInt(2000) - 1000;

            BlockVec vec = new BlockVec(x, y, z);
            assertEquals(x, vec.blockX());
            assertEquals(y, vec.blockY());
            assertEquals(z, vec.blockZ());

            // Test that operations preserve immutability
            BlockVec original = vec;
            BlockVec modified = vec.add(1, 2, 3);
            assertNotEquals(original, modified);
            assertEquals(x, original.blockX());
            assertEquals(x + 1, modified.blockX());
        }
    }

    @Test
    public void testCoordinateConversions() {
        BlockVec vec = new BlockVec(32, 64, 96);

        // Test x/y/z methods return double values
        assertEquals(32.0, vec.x());
        assertEquals(64.0, vec.y());
        assertEquals(96.0, vec.z());

        // Test section coordinates
        assertEquals(2, vec.sectionX());
        assertEquals(4, vec.sectionY());
        assertEquals(6, vec.sectionZ());

        // Test chunk coordinates
        assertEquals(2, vec.chunkX());
        assertEquals(6, vec.chunkZ());
    }
}

