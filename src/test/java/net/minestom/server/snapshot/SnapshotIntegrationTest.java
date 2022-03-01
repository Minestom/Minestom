package net.minestom.server.snapshot;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class SnapshotIntegrationTest {

    @Test
    public void instance(Env env) {
        env.createFlatInstance();
        var snapshot = ServerSnapshot.update();

        // Ensure that the collection is immutable
        {
            var instances = snapshot.instances();
            assertEquals(1, instances.size());

            env.createFlatInstance();
            instances = snapshot.instances();
            assertEquals(1, instances.size());
        }

        var inst = snapshot.instances().iterator().next();

        assertEquals(0, inst.time());
        assertEquals(0, inst.worldAge());

        assertEquals(0, inst.chunks().size());
        assertEquals(0, inst.entities().size());
        assertEquals(0, inst.players().size());
    }

    @Test
    public void chunk(Env env) {
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
