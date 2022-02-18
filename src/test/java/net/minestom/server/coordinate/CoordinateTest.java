package net.minestom.server.coordinate;

import net.minestom.server.utils.chunk.ChunkUtils;
import org.junit.jupiter.api.Test;

import static net.minestom.server.utils.chunk.ChunkUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CoordinateTest {

    @Test
    public void chunkIndex() {
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
    public void chunkCoordinate() {
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
    public void chunkCount() {
        assertEquals(289, getChunkCount(8));
        assertEquals(169, getChunkCount(6));
        assertEquals(121, getChunkCount(5));
        assertEquals(9, getChunkCount(1));
        assertEquals(1, getChunkCount(0));
        assertThrows(IllegalArgumentException.class, () -> getChunkCount(-1));
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
    public void toSectionRelativeCoordinate() {
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
}
