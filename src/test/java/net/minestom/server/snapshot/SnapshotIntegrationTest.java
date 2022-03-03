package net.minestom.server.snapshot;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

        assertEquals(snapshot, inst.server(), "Instance must have access to the server snapshot");

        assertEquals(0, inst.time());
        assertEquals(0, inst.worldAge());

        assertEquals(0, inst.chunks().size());
        assertEquals(0, inst.entities().size());
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

    @Test
    public void entity(Env env) {
        var instance = env.createFlatInstance();
        var ent = new Entity(EntityType.ZOMBIE);
        ent.setInstance(instance).join();
        var snapshot = ServerSnapshot.update();

        var inst = snapshot.instances().iterator().next();
        var entities = inst.entities();
        assertEquals(1, entities.size());

        var entity = entities.iterator().next();
        assertEquals(EntityType.ZOMBIE, entity.type());
        assertEquals(ent.getUuid(), entity.uuid());
        assertEquals(ent.getEntityId(), entity.id());
        assertEquals(ent.getPosition(), entity.position());
        assertEquals(ent.getVelocity(), entity.velocity());
        assertEquals(inst, entity.instance());
        assertEquals(inst.chunkAt(entity.position()), entity.chunk());
        assertEquals(ent.getViewers().size(), entity.viewers().size());
        assertEquals(ent.getPassengers().size(), entity.passengers().size());
        assertNull(ent.getVehicle());
        assertNull(entity.vehicle());
    }
}
