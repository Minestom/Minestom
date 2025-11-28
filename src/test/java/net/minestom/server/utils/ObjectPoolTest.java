package net.minestom.server.utils;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketVanilla;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectPoolTest {

    @Test
    public void pool() {
        var pool = PacketVanilla.PACKET_POOL;
        Set<NetworkBuffer> pooledBuffers = Collections.newSetFromMap(new IdentityHashMap<>());
        pool.clear();

        assertEquals(0, pool.count());
        var buffer = pool.get();
        pooledBuffers.add(buffer);

        buffer = pool.get();
        assertTrue(pooledBuffers.add(buffer));

        pool.add(buffer);
        assertEquals(1, pool.count());
        buffer = pool.get();
        assertEquals(0, pool.count());
        assertFalse(pooledBuffers.add(buffer));
    }

    @Test
    public void autoClose() {
        var pool = PacketVanilla.PACKET_POOL;
        assertEquals(0, pool.count());
        try (var ignored = pool.hold()) {
            assertEquals(0, pool.count());
        }
        assertEquals(1, pool.count());

        try (var ignored = pool.hold()) {
            assertEquals(0, pool.count());
        }
        assertEquals(1, pool.count());
    }
}
