package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.utils.time.TimeUnit;
import org.junit.jupiter.api.Test;

import java.time.temporal.TemporalUnit;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class EntityScheduledRemovalIntegrationTest {

    @Test
    public void tickRemoval(Env env) throws InterruptedException {
        var instance = env.createFlatInstance();
        var entity = new TestEntity(2, TimeUnit.SERVER_TICK);
        entity.setInstance(instance, new Pos(0, 40, 0)).join();

        assertFalse(entity.isRemoved());
        assertEquals(0, entity.getAliveTicks());

        Thread.sleep(150); // Ensure that time is not used for tick scheduling

        env.tick();
        assertFalse(entity.isRemoved());
        assertEquals(1, entity.getAliveTicks());

        env.tick();
        assertTrue(entity.isRemoved());
        assertEquals(1, entity.getAliveTicks());
    }

    static final class TestEntity extends Entity {
        public TestEntity(long delay, TemporalUnit unit) {
            super(EntityType.ZOMBIE);
            scheduleRemove(delay, unit);
        }
    }
}
