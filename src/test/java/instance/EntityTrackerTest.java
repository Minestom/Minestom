package instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.EntityTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EntityTrackerTest {
    @Test
    public void register() {
        var ent1 = new Entity(EntityType.ZOMBIE);
        var updated = new EntityTracker.Update<>() {
            @Override
            public void add(@NotNull Entity entity) {
                assertNotSame(ent1, entity);
                fail("No other entity should be registered yet");
            }

            @Override
            public void remove(@NotNull Entity entity) {
                assertNotSame(ent1, entity);
                fail("No other entity should be registered yet");
            }

            @Override
            public void updateTracker(@NotNull Point point, @Nullable EntityTracker tracker) {
                // Empty
            }
        };
        EntityTracker tracker = EntityTracker.newTracker();
        var chunkEntities = tracker.chunkEntities(Vec.ZERO, EntityTracker.Target.ENTITIES);
        assertTrue(chunkEntities.isEmpty());

        tracker.register(ent1, Vec.ZERO, EntityTracker.Target.ENTITIES, updated);
        assertTrue(chunkEntities.isEmpty());

        chunkEntities = tracker.chunkEntities(Vec.ZERO, EntityTracker.Target.ENTITIES);
        assertEquals(1, chunkEntities.size());

        tracker.unregister(ent1, Vec.ZERO, EntityTracker.Target.ENTITIES, updated);
        chunkEntities = tracker.chunkEntities(Vec.ZERO, EntityTracker.Target.ENTITIES);
        assertEquals(0, chunkEntities.size());
    }

    @Test
    public void move() {

    }
}
