package net.minestom.server.utils;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.minestom.server.coordinate.CoordConversionUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public final class CoordConversionTest {
    @Test
    public void chunkCoordinate() {
        assertEquals(0, globalToChunk(15));
        assertEquals(1, globalToChunk(16));
        assertEquals(-1, globalToChunk(-16));
        assertEquals(3, globalToChunk(48));

        assertEquals(4, globalToChunk(65));
        assertEquals(4, globalToChunk(64));
        assertEquals(3, globalToChunk(63));
        assertEquals(-2, globalToChunk(-25));
        assertEquals(23, globalToChunk(380));
    }

    @Test
    public void chunkIndexTest() {
        var index = chunkIndex(2, 5);
        assertEquals(2, chunkIndexToChunkX(index));
        assertEquals(5, chunkIndexToChunkZ(index));

        index = chunkIndex(-5, 25);
        assertEquals(-5, chunkIndexToChunkX(index));
        assertEquals(25, chunkIndexToChunkZ(index));

        index = chunkIndex(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertEquals(Integer.MAX_VALUE, chunkIndexToChunkX(index));
        assertEquals(Integer.MIN_VALUE, chunkIndexToChunkZ(index));
    }

    @Test
    public void toSectionRelativeCoordinate() {
        assertEquals(8, globalToSection(-40));
        assertEquals(12, globalToSection(-20));
        assertEquals(0, globalToSection(0));
        assertEquals(5, globalToSection(5));
        assertEquals(15, globalToSection(15));
        assertEquals(0, globalToSection(16));
        assertEquals(4, globalToSection(20));
        assertEquals(0, globalToSection(32));
        assertEquals(1, globalToSection(33));
    }

    @Test
    public void blockIndexTest() {
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
                new Vec(16 + 1, 20, 16 + 1),
                new Vec(16 + 1, -20, 16 + 1),
                // Vector with negative X/Z block pos
                new Vec(-1, 20, -1),
                new Vec(-1, -20, -1),
                // Check Y min and max value (23 bits, 2^23-1, -2^23+1)
                new Vec(0, 8_388_607, 0),
                new Vec(0, -8_388_607, 0)
        );

        for (Vec vec : tempEquals) {
            assertEquals(blockIndexToGlobal(blockIndex(vec.blockX(), vec.blockY(), vec.blockZ()),
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
            assertNotEquals(blockIndexToGlobal(blockIndex(vec.blockX(), vec.blockY(), vec.blockZ()),
                    vec.chunkX(), vec.chunkZ()), vec);
        }
    }

    @Test
    public void blockIndexDuplicate() {
        Set<Long> temp = new HashSet<>();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = -64; y < 364; y++) {
                    var vec = new Vec(x, y, z);
                    var index = blockIndex(vec.blockX(), vec.blockY(), vec.blockZ());
                    assertTrue(temp.add((long) index), "Duplicate block index found: " + index + " " + vec);
                    assertEquals(blockIndexToGlobal(index, vec.chunkX(), vec.chunkZ()), vec);

                    assertEquals(blockIndexToChunkPositionX(index), x);
                    assertEquals(blockIndexToChunkPositionY(index), y);
                    assertEquals(blockIndexToChunkPositionZ(index), z);
                }
            }
        }
    }
}
