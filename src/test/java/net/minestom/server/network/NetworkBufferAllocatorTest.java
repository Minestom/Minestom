package net.minestom.server.network;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static net.minestom.server.network.NetworkBuffer.RAW_BYTES;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;
import static org.junit.jupiter.api.Assertions.*;

public class NetworkBufferAllocatorTest {
    static class ConfinedArenaStrategy implements NetworkBufferAllocator.ArenaStrategy {
        final ArrayList<Arena> arenas = new ArrayList<>();
        int called = 0;

        @Override
        public @NotNull Arena acquire() {
            final Arena arena = Arena.ofConfined();
            arenas.add(arena);
            return arena;
        }

        @Override
        public void release(@NotNull Arena arena) {
            Assertions.assertTrue(arenas.remove(arena));
            arena.close();
            called++;
        }

        public void close() {
            arenas.forEach(Arena::close);
            arenas.clear();
        }
    }

    @Test
    public void testConfinedArena() {
        final NetworkBuffer buffer;
        try (var arena = Arena.ofConfined()) {
            buffer = NetworkBufferAllocator.staticAllocator().arena(arena).allocate(256);
            buffer.write(VAR_INT, Integer.MAX_VALUE);
            buffer.write(RAW_BYTES, "Hello".getBytes(StandardCharsets.UTF_8));
            assertEquals(Integer.MAX_VALUE, buffer.read(VAR_INT));
        }
        assertThrows(IllegalStateException.class, () -> buffer.read(RAW_BYTES));
    }

    @Test
    public void testConfinedArenaCopy() {
        final NetworkBuffer buffer;
        try (var arena = Arena.ofConfined()) {
            var settings = NetworkBufferAllocator.staticAllocator().arena(arena);
            var confinedBuffer = settings.allocate(256);
            confinedBuffer.write(VAR_INT, Integer.MAX_VALUE);
            confinedBuffer.write(RAW_BYTES, "Hello".getBytes(StandardCharsets.UTF_8));
            assertEquals(Integer.MAX_VALUE, confinedBuffer.read(VAR_INT));
            buffer = confinedBuffer.copy(settings, confinedBuffer.readIndex(), confinedBuffer.readableBytes(),0, confinedBuffer.readableBytes());
        }
        assertThrows(IllegalStateException.class, () -> buffer.read(RAW_BYTES));
    }

    @Test
    public void testConfinedArenaGlobalCopy() {
        var stringBytes = "Hello".getBytes(StandardCharsets.UTF_8);
        final NetworkBuffer buffer;
        try (var arena = Arena.ofConfined()) {
            var confinedBuffer = NetworkBufferAllocator.staticAllocator().arena(arena).allocate(256);
            confinedBuffer.write(VAR_INT, Integer.MAX_VALUE);
            confinedBuffer.write(RAW_BYTES, stringBytes);
            assertEquals(Integer.MAX_VALUE, confinedBuffer.read(VAR_INT));
            buffer = confinedBuffer.copy(confinedBuffer.readIndex(), confinedBuffer.readableBytes(), 0, confinedBuffer.readableBytes());
        }
        var bytes = buffer.read(RAW_BYTES);
        assertArrayEquals(stringBytes, bytes);
    }

    @Test
    public void testArenaStrategyResizeable() {
        var strategy = new ConfinedArenaStrategy();
        try {
            var buffer = NetworkBufferAllocator.resizeableAllocator().arena(strategy).allocate(256);
            buffer.write(VAR_INT, Integer.MAX_VALUE);
            assertEquals(256, buffer.capacity());
            buffer.trim();
            assertEquals(VAR_INT.sizeOf(Integer.MAX_VALUE), buffer.capacity(), "buffer did not shrink to expected");
            assertEquals(1, strategy.called);
            assertEquals(1, strategy.arenas.size());
            assertEquals(Integer.MAX_VALUE, buffer.read(VAR_INT));
        } finally {
            strategy.close();
        }
    }

    @Test
    public void testStaticAllocatorIsNotResizable() {
        try (var arena = Arena.ofConfined()) {
            var buffer = NetworkBufferAllocator.staticAllocator().arena(arena).allocate(8);
            assertFalse(buffer.isResizable());
            assertFalse(buffer.requestCapacity(16));
            assertThrows(UnsupportedOperationException.class, () -> buffer.resize(16));
            assertThrows(UnsupportedOperationException.class, buffer::trim);
        }
    }

    @Test
    public void testResizeableAllocatorRequestCapacityReleasesOldArena() {
        var strategy = new ConfinedArenaStrategy();
        try {
            var buffer = NetworkBufferAllocator.resizeableAllocator().arena(strategy).allocate(4);
            buffer.write(VAR_INT, 127);
            assertEquals(1, strategy.arenas.size());
            assertEquals(0, strategy.called);

            assertTrue(buffer.requestCapacity(8));
            assertTrue(buffer.capacity() >= 8);
            assertEquals(1, strategy.arenas.size());
            assertEquals(1, strategy.called);
            assertEquals(127, buffer.read(VAR_INT));
        } finally {
            strategy.close();
        }
    }

    @Test
    public void testResizeableTrimNoOpWhenFull() {
        var strategy = new ConfinedArenaStrategy();
        try {
            var buffer = NetworkBufferAllocator.resizeableAllocator().arena(strategy).allocate(8);
            buffer.write(RAW_BYTES, new byte[8]);
            assertEquals(8, buffer.readableBytes());
            assertEquals(8, buffer.capacity());

            buffer.trim();

            assertEquals(0, strategy.called);
            assertEquals(1, strategy.arenas.size());
            assertEquals(8, buffer.capacity());
        } finally {
            strategy.close();
        }
    }
}
