package net.minestom.server.instance;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.instance.InstanceWorldPositionChangeEvent;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

//Microtus start - integrate world spawn position
@ExtendWith(MicrotusExtension.class)
class InstanceWorldPositionIntegrationTest {

    @Test
    void testInstanceWorldPositionUpdate(@NotNull Env env) {
        var instance = env.createFlatInstance();
        assertEquals(Pos.ZERO, instance.getWorldSpawnPosition());
        instance.setWorldSpawnPosition(new Pos(1, 2, 3));
        assertNotEquals(Pos.ZERO, instance.getWorldSpawnPosition());
        Pos newSpawnPosition = new Pos(100, 200, 35, 90, 0);
        instance.setWorldSpawnPosition(newSpawnPosition);
        assertEquals(newSpawnPosition, instance.getWorldSpawnPosition());
        env.destroyInstance(instance);
    }

    @Test
    void testCancelledWorldPositionUpdate(@NotNull Env env) {
        var instance = env.createFlatInstance();
        assertFalse(instance.setWorldSpawnPosition(Pos.ZERO));
        env.destroyInstance(instance);
    }

    @Test
    void testInstanceWorldPositionChangeEvent(@NotNull Env env) {
        var instance = env.createFlatInstance();
        var listener = env.listen(InstanceWorldPositionChangeEvent.class);
        Pos newSpawnPosition = new Pos(100, 200, 35, 90, 0);
        listener.followup(event -> {
            assertEquals(Pos.ZERO, event.getOldPosition());
            assertEquals(newSpawnPosition, event.getNewPosition());
        });
        instance.setWorldSpawnPosition(newSpawnPosition);
        env.destroyInstance(instance);
    }
}
//Microtus end - integrate world spawn position
