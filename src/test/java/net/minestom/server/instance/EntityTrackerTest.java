package net.minestom.server.instance;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.EntityTracker;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EntityTrackerTest {
    @Test
    public void register() {
        var ent1 = new Entity(EntityType.ZOMBIE);
        var updater = new EntityTracker.Update<>() {
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
        };
        EntityTracker tracker = EntityTracker.newTracker();
        var chunkEntities = tracker.chunkEntities(Vec.ZERO, EntityTracker.Target.ENTITIES);
        assertTrue(chunkEntities.isEmpty());

        tracker.register(ent1, Vec.ZERO, EntityTracker.Target.ENTITIES, updater);
        assertEquals(1, chunkEntities.size());

        tracker.unregister(ent1, EntityTracker.Target.ENTITIES, updater);
        assertEquals(0, chunkEntities.size());
    }

    @Test
    public void move() {
        var ent1 = new Entity(EntityType.ZOMBIE);
        var updater = new EntityTracker.Update<>() {
            @Override
            public void add(@NotNull Entity entity) {
                fail("No other entity should be registered yet");
            }

            @Override
            public void remove(@NotNull Entity entity) {
                fail("No other entity should be registered yet");
            }
        };

        EntityTracker tracker = EntityTracker.newTracker();

        tracker.register(ent1, Vec.ZERO, EntityTracker.Target.ENTITIES, updater);
        assertEquals(1, tracker.chunkEntities(Vec.ZERO, EntityTracker.Target.ENTITIES).size());

        tracker.move(ent1, new Vec(32, 0, 32), EntityTracker.Target.ENTITIES, updater);
        assertEquals(0, tracker.chunkEntities(Vec.ZERO, EntityTracker.Target.ENTITIES).size());
        assertEquals(1, tracker.chunkEntities(new Vec(32, 0, 32), EntityTracker.Target.ENTITIES).size());
    }

    @Test
    public void tracking() {
        var ent1 = new Entity(EntityType.ZOMBIE);
        var ent2 = new Entity(EntityType.ZOMBIE);

        EntityTracker tracker = EntityTracker.newTracker();
        tracker.register(ent1, Vec.ZERO, EntityTracker.Target.ENTITIES, new EntityTracker.Update<>() {
            @Override
            public void add(@NotNull Entity entity) {
                fail("No other entity should be registered yet");
            }

            @Override
            public void remove(@NotNull Entity entity) {
                fail("No other entity should be registered yet");
            }
        });

        tracker.register(ent2, Vec.ZERO, EntityTracker.Target.ENTITIES, new EntityTracker.Update<>() {
            @Override
            public void add(@NotNull Entity entity) {
                assertNotSame(ent2, entity);
                assertSame(ent1, entity);
            }

            @Override
            public void remove(@NotNull Entity entity) {
                fail("No other entity should be removed yet");
            }
        });

        tracker.move(ent1, new Vec(Integer.MAX_VALUE, 0, 0), EntityTracker.Target.ENTITIES, new EntityTracker.Update<>() {
            @Override
            public void add(@NotNull Entity entity) {
                assertNotSame(ent1, entity);
                fail("No other entity should be added");
            }

            @Override
            public void remove(@NotNull Entity entity) {
                assertNotSame(ent1, entity);
                assertSame(ent2, entity);
            }
        });

        tracker.move(ent1, Vec.ZERO, EntityTracker.Target.ENTITIES, new EntityTracker.Update<>() {
            @Override
            public void add(@NotNull Entity entity) {
                assertNotSame(ent1, entity);
                assertSame(ent2, entity);
            }

            @Override
            public void remove(@NotNull Entity entity) {
                fail("no entity to remove");
            }
        });
    }

    @Test
    public void nearby() {
        var ent1 = new Entity(EntityType.ZOMBIE);
        var ent2 = new Entity(EntityType.ZOMBIE);
        var ent3 = new Entity(EntityType.ZOMBIE);
        var updater = new EntityTracker.Update<>() {
            @Override
            public void add(@NotNull Entity entity) {
                // Empty
            }

            @Override
            public void remove(@NotNull Entity entity) {
                // Empty
            }
        };

        EntityTracker tracker = EntityTracker.newTracker();
        tracker.register(ent2, new Vec(5, 0, 0), EntityTracker.Target.ENTITIES, updater);
        tracker.register(ent3, new Vec(50, 0, 0), EntityTracker.Target.ENTITIES, updater);

        tracker.nearbyEntities(Vec.ZERO, 4, EntityTracker.Target.ENTITIES, entity -> fail("No entity should be nearby"));

        tracker.register(ent1, Vec.ZERO, EntityTracker.Target.ENTITIES, updater);

        Set<Entity> entities = new HashSet<>();

        entities.add(ent1);
        tracker.nearbyEntities(Vec.ZERO, 4, EntityTracker.Target.ENTITIES, entity -> assertTrue(entities.remove(entity)));
        assertEquals(0, entities.size());

        entities.add(ent1);
        tracker.nearbyEntities(Vec.ZERO, 4.99, EntityTracker.Target.ENTITIES, entity -> assertTrue(entities.remove(entity)));
        assertEquals(0, entities.size());

        entities.add(ent1);
        entities.add(ent2);
        tracker.nearbyEntities(Vec.ZERO, 5, EntityTracker.Target.ENTITIES, entity -> assertTrue(entities.remove(entity)));
        assertEquals(0, entities.size());

        entities.add(ent1);
        entities.add(ent2);
        entities.add(ent3);
        tracker.nearbyEntities(Vec.ZERO, 50, EntityTracker.Target.ENTITIES, entity -> assertTrue(entities.remove(entity)));
        assertEquals(0, entities.size());
    }

    @Test
    public void collectionView() {
        var ent1 = new Entity(EntityType.ZOMBIE);
        var updater = new EntityTracker.Update<>() {
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
        };

        EntityTracker tracker = EntityTracker.newTracker();
        var entities = tracker.entities();
        var chunkEntities = tracker.chunkEntities(Vec.ZERO, EntityTracker.Target.ENTITIES);

        assertTrue(entities.isEmpty());
        assertTrue(chunkEntities.isEmpty());
        tracker.register(ent1, Vec.ZERO, EntityTracker.Target.ENTITIES, updater);
        assertEquals(1, entities.size());
        assertEquals(1, chunkEntities.size());

        assertThrows(Exception.class, () -> entities.add(new Entity(EntityType.ZOMBIE)));
        assertThrows(Exception.class, () -> chunkEntities.add(new Entity(EntityType.ZOMBIE)));
    }
}
