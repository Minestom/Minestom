package net.minestom.server.snapshot;

import net.minestom.testing.Env;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.testing.extension.MicrotusExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MicrotusExtension.class)
class EntitySnapshotIntegrationTest {

    @Test
    void basic(Env env) {
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
