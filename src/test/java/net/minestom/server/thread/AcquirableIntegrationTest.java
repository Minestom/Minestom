package net.minestom.server.thread;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class AcquirableIntegrationTest {

    @Test
    public void localTest(Env env) {
        var instance = env.createFlatInstance();

        var zombie = new Entity(EntityType.ZOMBIE) {
            @Override
            public void tick(long time) {
                super.tick(time);
                assertTrue(this.getAcquirable().isLocal());
            }
        };
        zombie.setInstance(instance, new Pos(1, 41, 1)).join();
        var acquirable = zombie.getAcquirable();
        // Check ownership before initialization
        assertFalse(acquirable.isOwned());
        acquirable.sync(entity -> assertFalse(acquirable.isLocal()));
        Thread.startVirtualThread(() -> assertFalse(acquirable.isLocal()));

        env.tick(); // Ensure the entity can access itself

        // Check ownership after initialization
        assertFalse(acquirable.isOwned());
        acquirable.sync(entity -> assertFalse(acquirable.isLocal()));
        Thread.startVirtualThread(() -> assertFalse(acquirable.isLocal()));
    }

    @Test
    public void ownedTest(Env env) {
        var instance = env.createFlatInstance();

        var zombie = new Entity(EntityType.ZOMBIE) {
            @Override
            public void tick(long time) {
                super.tick(time);
                assertTrue(this.getAcquirable().isOwned());
            }
        };
        zombie.setInstance(instance, new Pos(1, 41, 1)).join();
        var acquirable = zombie.getAcquirable();
        // Check ownership before initialization
        assertFalse(acquirable.isOwned());
        acquirable.sync(entity -> assertTrue(acquirable.isOwned()));
        Thread.startVirtualThread(() -> assertFalse(acquirable.isOwned()));

        env.tick(); // Ensure the entity can access itself

        // Check ownership after initialization
        assertFalse(acquirable.isOwned());
        acquirable.sync(entity -> assertTrue(acquirable.isOwned()));
        Thread.startVirtualThread(() -> assertFalse(acquirable.isOwned()));
    }
}
