package net.minestom.server.instance;

import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class ChunkHeightmapIntegrationTest {
    @Test
    public void testChunkHeightmap(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        var chunk = instance.getChunk(0, 0);
        assert chunk != null;

        var heightmap = chunk.motionBlockingHeightmap().getHeight(0, 0);
        assertEquals(39, heightmap);
    }

    @Test
    public void heightMapPlaceTest(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        var chunk = instance.getChunk(0, 0);
        assert chunk != null;

        {
            instance.setBlock(0, 40, 0, Block.STONE);
            var heightmap = chunk.motionBlockingHeightmap().getHeight(0, 0);
            assertEquals(40, heightmap);
        }

        {
            instance.setBlock(0, 45, 0, Block.STONE);
            var heightmap = chunk.motionBlockingHeightmap().getHeight(0, 0);
            assertEquals(45, heightmap);
        }
    }

    @Test
    public void heightMapRemoveTest(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        var chunk = instance.getChunk(0, 0);
        assert chunk != null;

        {
            instance.setBlock(0, 45, 0, Block.STONE);
            var heightmap = chunk.motionBlockingHeightmap().getHeight(0, 0);
            assertEquals(45, heightmap);
        }

        {
            instance.setBlock(0, 45, 0, Block.AIR);
            var heightmap = chunk.motionBlockingHeightmap().getHeight(0, 0);
            assertEquals(39, heightmap);
        }
    }
}
