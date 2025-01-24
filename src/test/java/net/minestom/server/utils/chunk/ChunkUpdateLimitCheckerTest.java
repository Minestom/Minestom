package net.minestom.server.utils.chunk;

import net.minestom.server.instance.DynamicChunk;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class ChunkUpdateLimitCheckerTest {

    @Test
    public void testHistory(Env env) {
        var instance = env.createFlatInstance();
        var limiter = new ChunkUpdateLimitChecker(3);

        assertTrue(limiter.addToHistory(new DynamicChunk(instance, 0, 0)));
        assertTrue(limiter.addToHistory(new DynamicChunk(instance, 0, 1)));
        assertTrue(limiter.addToHistory(new DynamicChunk(instance, 0, 2)));
        // history : 0, 1, 2

        assertFalse(limiter.addToHistory(new DynamicChunk(instance, 0, 0)));
        // history : 1, 2, 0
        assertFalse(limiter.addToHistory(new DynamicChunk(instance, 0, 1)));
        // history : 2, 0, 1
        assertFalse(limiter.addToHistory(new DynamicChunk(instance, 0, 2)));
        // history : 0, 1, 2

        assertFalse(limiter.addToHistory(new DynamicChunk(instance, 0, 2)));
        // history : 1, 2, 2
        assertTrue(limiter.addToHistory(new DynamicChunk(instance, 0, 0)));
    }

    @Test
    public void testOneSlotHistory(Env env) {
        var instance = env.createFlatInstance();
        var limiter = new ChunkUpdateLimitChecker(1);
        assertTrue(limiter.addToHistory(new DynamicChunk(instance, 0, 0)));
        assertFalse(limiter.addToHistory(new DynamicChunk(instance, 0, 0)));
        assertTrue(limiter.addToHistory(new DynamicChunk(instance, 0, 1)));
        assertTrue(limiter.addToHistory(new DynamicChunk(instance, 0, 0)));
    }

    @Test
    public void testDisabling(Env env) {
        var instance = env.createFlatInstance();
        var limiter = new ChunkUpdateLimitChecker(0);
        assertTrue(limiter.addToHistory(new DynamicChunk(instance, 0, 0)));
        assertTrue(limiter.addToHistory(new DynamicChunk(instance, 0, 0)));
        assertTrue(limiter.addToHistory(new DynamicChunk(instance, 0, 1)));
    }
}
