package net.minestom.server.instance;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
