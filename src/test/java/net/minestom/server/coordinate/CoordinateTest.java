package net.minestom.server.coordinate;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minestom.server.instance.Chunk;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CoordinateTest {

    @Test
    public void chunkIndex() {
        var index = CoordConversion.chunkIndex(2, 5);
        assertEquals(2, CoordConversion.chunkIndexGetX(index));
        assertEquals(5, CoordConversion.chunkIndexGetZ(index));

        index = CoordConversion.chunkIndex(-5, 25);
        assertEquals(-5, CoordConversion.chunkIndexGetX(index));
        assertEquals(25, CoordConversion.chunkIndexGetZ(index));

        index = CoordConversion.chunkIndex(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertEquals(Integer.MAX_VALUE, CoordConversion.chunkIndexGetX(index));
        assertEquals(Integer.MIN_VALUE, CoordConversion.chunkIndexGetZ(index));
    }

    @Test
    public void chunkCoordinate() {
        assertEquals(0, CoordConversion.globalToChunk(15));
        assertEquals(1, CoordConversion.globalToChunk(16));
        assertEquals(-1, CoordConversion.globalToChunk(-16));
        assertEquals(3, CoordConversion.globalToChunk(48));

        assertEquals(4, CoordConversion.globalToChunk(65));
        assertEquals(4, CoordConversion.globalToChunk(64));
        assertEquals(3, CoordConversion.globalToChunk(63));
        assertEquals(-2, CoordConversion.globalToChunk(-25));
        assertEquals(23, CoordConversion.globalToChunk(380));
    }

    @Test
    public void chunkCount() {
        assertEquals(289, ChunkRange.chunksCount(8));
        assertEquals(169, ChunkRange.chunksCount(6));
        assertEquals(121, ChunkRange.chunksCount(5));
        assertEquals(9, ChunkRange.chunksCount(1));
        assertEquals(1, ChunkRange.chunksCount(0));
        assertThrows(IllegalArgumentException.class, () -> ChunkRange.chunksCount(-1));
    }

    @Test
    public void vecAddition() {
        Vec temp = Vec.ZERO;
        assertEquals(0, temp.x());
        assertEquals(0, temp.y());
        assertEquals(0, temp.z());

        temp = temp.add(1);
        assertEquals(1, temp.x());
        assertEquals(1, temp.y());
        assertEquals(1, temp.z());

        temp = temp.add(1, 0, 0);
        assertEquals(2, temp.x());
        assertEquals(1, temp.y());
        assertEquals(1, temp.z());

        temp = temp.add(0, 1, 0);
        assertEquals(2, temp.x());
        assertEquals(2, temp.y());
        assertEquals(1, temp.z());

        temp = temp.add(0, 0, 1);
        assertEquals(2, temp.x());
        assertEquals(2, temp.y());
        assertEquals(2, temp.z());
    }

    @Test
    public void vecWith() {
        Vec temp = Vec.ZERO.withX(1);
        assertEquals(1, temp.x());
        assertEquals(0, temp.y());
        assertEquals(0, temp.z());

        temp = temp.withX(x -> x * 2 + 1);
        assertEquals(3, temp.x());
        assertEquals(0, temp.y());
        assertEquals(0, temp.z());
    }

    @Test
    public void vecNegCompare() {
        assertFalse(Vec.ZERO.samePoint(Vec.ONE.neg()));
        assertTrue(Vec.ZERO.samePoint(Vec.ZERO.neg()));
        assertTrue(Vec.ZERO.samePoint(new Vec(-0, 0, 0)));
        assertTrue(Vec.ZERO.samePoint(new Vec(-0, -0, -0)));
    }

    @Test
    public void vecNanCompare() {
        Vec nanX = new Vec(Double.NaN, 0, 0);
        Vec nanY = new Vec(0, Double.NaN, 0);
        Vec nanZ = new Vec(0, 0, Double.NaN);
        Vec nanAll = new Vec(Double.NaN, Double.NaN, Double.NaN);
        Vec normal = Vec.ZERO;

        // NaN is not equal to itself
        assertFalse(nanX.samePoint(nanX));
        assertFalse(nanY.samePoint(nanY));
        assertFalse(nanZ.samePoint(nanZ));
        assertFalse(nanAll.samePoint(nanAll));

        // NaN vectors are not equal to normal vectors
        assertFalse(nanX.samePoint(normal));
        assertFalse(nanY.samePoint(normal));
        assertFalse(nanZ.samePoint(normal));
        assertFalse(nanAll.samePoint(normal));

        // Different NaN vectors are not equal to each other
        assertFalse(nanX.samePoint(nanY));
        assertFalse(nanX.samePoint(nanZ));
        assertFalse(nanY.samePoint(nanZ));
        assertFalse(nanX.samePoint(nanAll));
    }

    @Test
    public void vecSameEpsilon() {
        final double TEST_EPSILON = 0.000000999999; // Less than 1e-6
        Vec v1 = new Vec(TEST_EPSILON, 0, 0);
        Vec v2 = new Vec(0, 0, 0);
        Vec v3 = new Vec(TEST_EPSILON, -TEST_EPSILON, TEST_EPSILON);
        Vec v4 = new Vec(0.001, 0, 0);
        Vec v5 = v1.add(TEST_EPSILON);

        // Vectors with small differences should be considered the same under epsilon
        assertTrue(v1.samePoint(v2, Vec.EPSILON));
        assertTrue(v2.samePoint(v3, Vec.EPSILON));
        assertTrue(v1.samePoint(v3, Vec.EPSILON));
        assertTrue(v5.samePoint(v1, Vec.EPSILON));

        // Vectors with larger differences should not be considered the same
        assertFalse(v1.samePoint(v4, Vec.EPSILON));

        // 0 epsilon should throw an exception
        assertThrows(IllegalArgumentException.class, () -> v5.samePoint(v5, 0));
    }

    @Test
    public void toSectionRelativeCoordinate() {
        assertEquals(8, CoordConversion.globalToSectionRelative(-40));
        assertEquals(12, CoordConversion.globalToSectionRelative(-20));
        assertEquals(0, CoordConversion.globalToSectionRelative(0));
        assertEquals(5, CoordConversion.globalToSectionRelative(5));
        assertEquals(15, CoordConversion.globalToSectionRelative(15));
        assertEquals(0, CoordConversion.globalToSectionRelative(16));
        assertEquals(4, CoordConversion.globalToSectionRelative(20));
        assertEquals(0, CoordConversion.globalToSectionRelative(32));
        assertEquals(1, CoordConversion.globalToSectionRelative(33));
    }

    @Test
    public void blockIndex() {
        // Test if the block index is correctly converted back and forth

        List<Vec> tempEquals = List.of(
                // Zero vector with zero, positive and negative Y value
                Vec.ZERO,
                Vec.ZERO.withY(1),
                Vec.ZERO.withY(-1),
                // One vector with positive and negative Y value
                Vec.ONE,
                Vec.ONE.withY(-1),
                // Vector with X/Z outside of chunk size
                new Vec(Chunk.CHUNK_SIZE_X + 1, 20, Chunk.CHUNK_SIZE_Z + 1),
                new Vec(Chunk.CHUNK_SIZE_X + 1, -20, Chunk.CHUNK_SIZE_Z + 1),
                // Vector with negative X/Z block pos
                new Vec(-1, 20, -1),
                new Vec(-1, -20, -1),
                // Check Y min and max value (23 bits, 2^23-1, -2^23+1)
                new Vec(0, 8_388_607, 0),
                new Vec(0, -8_388_607, 0)
        );

        for (Vec vec : tempEquals) {
            assertEquals(CoordConversion.chunkBlockIndexGetGlobal(CoordConversion.chunkBlockIndex(vec.blockX(), vec.blockY(), vec.blockZ()),
                    vec.chunkX(), vec.chunkZ()), vec);
        }

        // Test if the block index does convert to wrong values due to overflow

        List<Vec> tempNotEquals = List.of(
                // Above and below Y min and max value (> 2^23-1, < -2^23+1)
                // Integer overflows into the 24th bit which is not copied into block index,
                // so an error is expected here.
                new Vec(0, 8_388_608, 0),
                new Vec(0, -8_388_608, 0)
        );

        for (Vec vec : tempNotEquals) {
            assertNotEquals(CoordConversion.chunkBlockIndexGetGlobal(CoordConversion.chunkBlockIndex(vec.blockX(), vec.blockY(), vec.blockZ()),
                    vec.chunkX(), vec.chunkZ()), vec);
        }
    }

    @Test
    public void blockIndexDuplicate() {
        LongSet temp = new LongOpenHashSet();

        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                for (int y = -64; y < 364; y++) {
                    var vec = new Vec(x, y, z);
                    var index = CoordConversion.chunkBlockIndex(vec.blockX(), vec.blockY(), vec.blockZ());
                    assertTrue(temp.add(index), "Duplicate block index found: " + index + " " + vec);
                    assertEquals(CoordConversion.chunkBlockIndexGetGlobal(index, vec.chunkX(), vec.chunkZ()), vec);

                    assertEquals(CoordConversion.chunkBlockIndexGetX(index), x);
                    assertEquals(CoordConversion.chunkBlockIndexGetY(index), y);
                    assertEquals(CoordConversion.chunkBlockIndexGetZ(index), z);
                }
            }
        }
    }

    @Test
    public void blockIndexZero() {
        assertEquals(0, CoordConversion.chunkBlockIndex(0, 0, 0), "Bad default index for zero case! Bad sign bit?");
    }
}
