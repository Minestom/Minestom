package net.minestom.server.snapshot;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class ChunkSnapshotIntegrationTest {

    @Test
    public void blocks(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 0, 0, Block.STONE);
        var snapshot = ServerSnapshot.update();

        var inst = snapshot.instances().iterator().next();
        assertEquals(Block.STONE, inst.getBlock(0, 0, 0));

        assertEquals(1, inst.chunks().size());
        var chunk = inst.chunks().iterator().next();
        assertEquals(Block.STONE, chunk.getBlock(0, 0, 0));
    }
}
