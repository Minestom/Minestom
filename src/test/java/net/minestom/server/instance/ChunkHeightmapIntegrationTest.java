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

        int heightmap = chunk.motionBlockingHeightmap().getHeight(0, 0);
        assertEquals(39, heightmap);
    }

    @Test
    public void heightMapPlaceTest(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        var chunk = instance.getChunk(0, 0);

        {
            instance.setBlock(0, 40, 0, Block.STONE);
            int heightmap = chunk.motionBlockingHeightmap().getHeight(0, 0);
            assertEquals(40, heightmap);
        }

        {
            instance.setBlock(0, 45, 0, Block.STONE);
            int heightmap = chunk.motionBlockingHeightmap().getHeight(0, 0);
            assertEquals(45, heightmap);
        }
    }

    @Test
    public void motionBlockingFluidTest(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        var chunk = instance.getChunk(0, 0);
        var heightmap = chunk.motionBlockingHeightmap();

        // Inherently waterlogged blocks count even without a "waterlogged" property
        instance.setBlock(0, 45, 0, Block.KELP);
        assertEquals(45, heightmap.getHeight(0, 0));

        instance.setBlock(1, 45, 0, Block.SEAGRASS);
        assertEquals(45, heightmap.getHeight(1, 0));

        // Waterlogged state of a block without collision counts, dry state does not
        instance.setBlock(2, 45, 0, Block.GLOW_LICHEN.withProperty("waterlogged", "true"));
        assertEquals(45, heightmap.getHeight(2, 0));

        instance.setBlock(3, 45, 0, Block.GLOW_LICHEN);
        assertEquals(39, heightmap.getHeight(3, 0));

        // Solid but not motion blocking
        instance.setBlock(4, 45, 0, Block.COBWEB);
        assertEquals(39, heightmap.getHeight(4, 0));

        instance.setBlock(5, 45, 0, Block.BAMBOO_SAPLING);
        assertEquals(39, heightmap.getHeight(5, 0));
    }

    @Test
    public void heightMapRemoveTest(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        var chunk = instance.getChunk(0, 0);

        {
            instance.setBlock(0, 45, 0, Block.STONE);
            int heightmap = chunk.motionBlockingHeightmap().getHeight(0, 0);
            assertEquals(45, heightmap);
        }

        {
            instance.setBlock(0, 45, 0, Block.AIR);
            int heightmap = chunk.motionBlockingHeightmap().getHeight(0, 0);
            assertEquals(39, heightmap);
        }
    }
}
