package net.minestom.server.instance;

import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class InstanceBlockIntegrationTest {

    @Test
    public void basic(Env env) {
        var instance = env.createFlatInstance();
        assertThrows(NullPointerException.class, () -> instance.getBlock(0, 0, 0),
                "No exception throw when getting a block in an unloaded chunk");

        instance.loadChunk(0, 0).join();
        assertEquals(Block.AIR, instance.getBlock(0, 50, 0));

        instance.setBlock(0, 50, 0, Block.GRASS);
        assertEquals(Block.GRASS, instance.getBlock(0, 50, 0));

        instance.setBlock(0, 50, 0, Block.STONE);
        assertEquals(Block.STONE, instance.getBlock(0, 50, 0));

        assertThrows(NullPointerException.class, () -> instance.getBlock(16, 0, 0),
                "No exception throw when getting a block in an unloaded chunk");
        instance.loadChunk(1, 0).join();
        assertEquals(Block.AIR, instance.getBlock(16, 50, 0));
    }

    @Test
    public void unloadCache(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();

        instance.setBlock(0, 50, 0, Block.GRASS);
        assertEquals(Block.GRASS, instance.getBlock(0, 50, 0));

        instance.unloadChunk(0, 0);
        assertThrows(NullPointerException.class, () -> instance.getBlock(0, 0, 0),
                "No exception throw when getting a block in an unloaded chunk");

        instance.loadChunk(0, 0).join();
        assertEquals(Block.AIR, instance.getBlock(0, 50, 0));
    }

    @Test
    public void blockNbt(Env env) {
        var instance = env.createFlatInstance();
        assertThrows(NullPointerException.class, () -> instance.getBlock(0, 0, 0),
                "No exception throw when getting a block in an unloaded chunk");

        instance.loadChunk(0, 0).join();

        var tag = Tag.Integer("key");
        var block = Block.STONE.withTag(tag, 5);
        var point = new Vec(0, 50, 0);
        // Initial placement
        instance.setBlock(point, block);
        assertEquals(5, instance.getBlock(point).getTag(tag));

        // Override
        instance.setBlock(point, block.withTag(tag, 7));
        assertEquals(7, instance.getBlock(point).getTag(tag));

        // Different block type
        instance.setBlock(point, Block.GRASS.withTag(tag, 8));
        assertEquals(8, instance.getBlock(point).getTag(tag));
    }

    @Test
    public void basicTracker(Env env) {
        var instance = env.createFlatInstance();

        instance.loadChunk(0, 0).join();

        AtomicBoolean called = new AtomicBoolean(false);
        instance.trackBlock(new Vec(0, 0, 0), block -> called.set(true));
        instance.setBlock(0, 0, 0, Block.GRASS);

        assertEquals(Block.GRASS, instance.getBlock(0, 0, 0), "Block not set");
        assertTrue(called.get(), "Tracker not called");
    }

    @Test
    public void singleLoadChunkTracker(Env env) {
        var instance = env.createFlatInstance();

        AtomicBoolean called = new AtomicBoolean(false);
        instance.trackBlock(new Vec(0, 0, 0), block -> called.set(true));
        instance.loadChunk(0, 0).join();
        assertTrue(called.get(), "Tracker not called");
    }

    @Test
    public void singleGenerateChunkTracker(Env env) {
        var instance = env.createFlatInstance();

        instance.setGenerator(unit -> unit.modifier().fill(Block.STONE));

        AtomicBoolean called = new AtomicBoolean(false);
        instance.trackBlock(new Vec(0, 0, 0), block -> called.set(block == Block.STONE));
        instance.loadChunk(0, 0).join();
        assertTrue(called.get(), "Tracker not called with the correct block.");
    }
}
