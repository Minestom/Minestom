package net.minestom.server.instance.batch;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class BatchPlaceIntegrationTest {

    @Test
    public void subSectionBatchFill(Env env) {
        var instance = env.process().instance().createInstanceContainer();
        var batch = BatchPlace.batch(new Vec(4, 4, 4), modifier -> modifier.fill(Block.STONE));

        instance.setBlocks(0, 0, 0, batch);

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    assertEquals(Block.STONE, instance.getBlock(x, y, z));
                }
            }
        }

        for (int x = 4; x < 16; x++) {
            for (int y = 4; y < 16; y++) {
                for (int z = 4; z < 16; z++) {
                    assertEquals(Block.AIR, instance.getBlock(x, y, z), "Block at " + x + ", " + y + ", " + z + " was not air");
                }
            }
        }
    }

    @Test
    public void setAbsolute(Env env) {
        var instance = env.process().instance().createInstanceContainer();
        var batch = BatchPlace.batch(new Vec(16, 16, 16), modifier -> {
            modifier.setBlock(0, 0, 0, Block.STONE);
            modifier.setBlock(15, 15, 15, Block.STONE);
        });

        instance.setBlocks(0, 0, 0, batch);
        assertEquals(Block.STONE, instance.getBlock(0, 0, 0));
        assertEquals(Block.STONE, instance.getBlock(15, 15, 15));
        assertEquals(Block.AIR, instance.getBlock(1, 0, 0));
    }

    @Test
    public void setAbsoluteBound(Env env) {
        var instance = env.process().instance().createInstanceContainer();
        var batch = BatchPlace.batch(new Vec(16, 16, 16),
                modifier -> modifier.setBlock(32, 32, 32, Block.STONE));

        instance.setBlocks(32, 32, 32, batch);
        assertEquals(Block.STONE, instance.getBlock(32, 32, 32));
        assertNull(instance.getChunk(0, 0));
        instance.loadChunk(0, 0).join();
        assertEquals(Block.AIR, instance.getBlock(0, 0, 0));

        instance.setBlock(32, 32, 32, Block.AIR);

        assertThrows(Exception.class, () -> instance.setBlocks(0, 0, 0, batch), "Block outside of chunk");
        instance.setBlock(32, 32, 32, Block.AIR);
        assertEquals(Block.AIR, instance.getBlock(0, 0, 0));
    }

    @Test
    public void setRelative(Env env) {
        var instance = env.process().instance().createInstanceContainer();
        var batch = BatchPlace.batch(new Vec(16, 16, 16), modifier -> {
            modifier.setRelative(0, 0, 0, Block.STONE);
            modifier.setRelative(15, 15, 15, Block.STONE);
        });

        instance.setBlocks(0, 0, 0, batch);
        assertEquals(Block.STONE, instance.getBlock(0, 0, 0));
        assertEquals(Block.STONE, instance.getBlock(15, 15, 15));
        assertEquals(Block.AIR, instance.getBlock(1, 0, 0));
    }

    @Test
    public void fillSection(Env env) {
        var instance = env.process().instance().createInstanceContainer();
        var batch = BatchPlace.batch(new Vec(16, 16, 16), modifier -> modifier.fill(Block.STONE));

        instance.setBlocks(0, 0, 0, batch);

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    assertEquals(Block.STONE, instance.getBlock(x, y, z));
                }
            }
        }
    }
}
