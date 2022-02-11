package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.utils.time.TimeUnit;
import org.junit.jupiter.api.Test;

import java.time.temporal.TemporalUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class EntityRemovalIntegrationTest {

    @Test
    public void destructionPacket(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        connection.connect(instance, new Pos(0, 40, 0)).join();

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 40, 0)).join();

        var tracker = connection.trackIncoming(DestroyEntitiesPacket.class);
        entity.remove();
        tracker.assertSingle(packet -> assertEquals(List.of(entity.getEntityId()), packet.entityIds()));
    }

    @Test
    public void instanceRemoval(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 40, 0)).join();
        assertFalse(entity.isRemoved());

        entity.remove();
        assertTrue(entity.isRemoved());
        assertFalse(instance.getEntities().contains(entity), "Entity must not be in the instance anymore");
    }

    @Test
    public void tickTimedRemoval(Env env) throws InterruptedException {
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
