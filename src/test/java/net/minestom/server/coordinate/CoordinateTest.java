package net.minestom.server.coordinate;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minestom.server.instance.Chunk;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.utils.chunk.ChunkUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {

    @Test
    void chunkIndex() {
        var index = getChunkIndex(2, 5);
        assertEquals(2, getChunkCoordX(index));
        assertEquals(5, getChunkCoordZ(index));

        index = getChunkIndex(-5, 25);
        assertEquals(-5, getChunkCoordX(index));
        assertEquals(25, getChunkCoordZ(index));

        index = getChunkIndex(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertEquals(Integer.MAX_VALUE, getChunkCoordX(index));
        assertEquals(Integer.MIN_VALUE, getChunkCoordZ(index));
    }

    @Test
    void chunkCoordinate() {
        assertEquals(0, getChunkCoordinate(15));
        assertEquals(1, getChunkCoordinate(16));
        assertEquals(-1, getChunkCoordinate(-16));
        assertEquals(3, getChunkCoordinate(48));

        assertEquals(4, getChunkCoordinate(65));
        assertEquals(4, getChunkCoordinate(64));
        assertEquals(3, getChunkCoordinate(63));
        assertEquals(-2, getChunkCoordinate(-25));
        assertEquals(23, getChunkCoordinate(380));
    }

    @Test
    void chunkCount() {
        assertEquals(289, getChunkCount(8));
        assertEquals(169, getChunkCount(6));
        assertEquals(121, getChunkCount(5));
        assertEquals(9, getChunkCount(1));
        assertEquals(1, getChunkCount(0));
        assertThrows(IllegalArgumentException.class, () -> getChunkCount(-1));
    }

    @Test
    void vecAddition() {
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
    void vecWith() {
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
    void toSectionRelativeCoordinate() {
        assertEquals(8, ChunkUtils.toSectionRelativeCoordinate(-40));
        assertEquals(12, ChunkUtils.toSectionRelativeCoordinate(-20));
        assertEquals(0, ChunkUtils.toSectionRelativeCoordinate(0));
        assertEquals(5, ChunkUtils.toSectionRelativeCoordinate(5));
        assertEquals(15, ChunkUtils.toSectionRelativeCoordinate(15));
        assertEquals(0, ChunkUtils.toSectionRelativeCoordinate(16));
        assertEquals(4, ChunkUtils.toSectionRelativeCoordinate(20));
        assertEquals(0, ChunkUtils.toSectionRelativeCoordinate(32));
        assertEquals(1, ChunkUtils.toSectionRelativeCoordinate(33));
    }

    @Test
    void blockIndex() {
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
            assertEquals(getBlockPosition(getBlockIndex(vec.blockX(), vec.blockY(), vec.blockZ()),
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
            assertNotEquals(getBlockPosition(getBlockIndex(vec.blockX(), vec.blockY(), vec.blockZ()),
                    vec.chunkX(), vec.chunkZ()), vec);
        }
    }

    @Test
    void blockIndexDuplicate() {
        LongSet temp = new LongOpenHashSet();

        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                for (int y = -64; y < 364; y++) {
                    var vec = new Vec(x, y, z);
                    var index = getBlockIndex(vec.blockX(), vec.blockY(), vec.blockZ());
                    assertTrue(temp.add(index), "Duplicate block index found: " + index + " " + vec);
                    assertEquals(getBlockPosition(index, vec.chunkX(), vec.chunkZ()), vec);

                    assertEquals(blockIndexToChunkPositionX(index), x);
                    assertEquals(blockIndexToChunkPositionY(index), y);
                    assertEquals(blockIndexToChunkPositionZ(index), z);
                }
            }
        }
    }
}
