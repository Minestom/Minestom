package net.minestom.server.network;

import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.PooledBuffers;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PooledBuffersTest {

    @Test
    public void pool() {
        Set<BinaryBuffer> pooledBuffers = new HashSet<>();
        PooledBuffers.clear();

        assertEquals(0, PooledBuffers.count());
        var buffer = PooledBuffers.get();
        assertEquals(PooledBuffers.bufferSize(), buffer.capacity());
        pooledBuffers.add(buffer);

        buffer = PooledBuffers.get();
        assertTrue(pooledBuffers.add(buffer));

        PooledBuffers.add(buffer);
        assertEquals(1, PooledBuffers.count());
        buffer = PooledBuffers.get();
        assertEquals(0, PooledBuffers.count());
        assertFalse(pooledBuffers.add(buffer));
    }
}
