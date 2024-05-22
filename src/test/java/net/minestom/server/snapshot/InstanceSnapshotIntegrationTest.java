package net.minestom.server.snapshot;

import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
class InstanceSnapshotIntegrationTest {

    @Test
    void basic(Env env) {
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
}
