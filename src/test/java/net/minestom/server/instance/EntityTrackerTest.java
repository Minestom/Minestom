package net.minestom.server.instance;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntitySelector;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EntityTrackerTest {
    @Test
    public void register() {
        var ent1 = new Entity(EntityType.ZOMBIE);
        var updater = new EntityTracker.Update() {
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
        assertTrue(tracker.selectEntityStream(EntitySelector.selector(builder -> builder.chunk(Vec.ZERO))).toList().isEmpty());

        tracker.register(ent1, Vec.ZERO, updater);
        assertEquals(1, tracker.selectEntityStream(EntitySelector.selector(builder -> builder.chunk(Vec.ZERO))).toList().size());

        tracker.unregister(ent1, updater);
        assertEquals(0, tracker.selectEntityStream(EntitySelector.selector(builder -> builder.chunk(Vec.ZERO))).toList().size());
    }

    @Test
    public void move() {
        var ent1 = new Entity(EntityType.ZOMBIE);
        var updater = new EntityTracker.Update() {
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

        tracker.register(ent1, Vec.ZERO, updater);
        assertEquals(1, tracker.selectEntityStream(EntitySelector.selector(builder -> builder.chunk(Vec.ZERO))).toList().size());

        tracker.move(ent1, new Vec(32, 0, 32), updater);
        assertEquals(0, tracker.selectEntityStream(EntitySelector.selector(builder -> builder.chunk(Vec.ZERO))).toList().size());
        assertEquals(1, tracker.selectEntityStream(EntitySelector.selector(builder -> builder.chunk(new Vec(32, 0, 32)))).toList().size());
    }

    @Test
    public void tracking() {
        var ent1 = new Entity(EntityType.ZOMBIE);
        var ent2 = new Entity(EntityType.ZOMBIE);

        EntityTracker tracker = EntityTracker.newTracker();
        tracker.register(ent1, Vec.ZERO, new EntityTracker.Update() {
            @Override
            public void add(@NotNull Entity entity) {
                fail("No other entity should be registered yet");
            }

            @Override
            public void remove(@NotNull Entity entity) {
                fail("No other entity should be registered yet");
            }
        });

        tracker.register(ent2, Vec.ZERO, new EntityTracker.Update() {
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

        tracker.move(ent1, new Vec(Integer.MAX_VALUE, 0, 0), new EntityTracker.Update() {
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

        tracker.move(ent1, Vec.ZERO, new EntityTracker.Update() {
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
        var updater = new EntityTracker.Update() {
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
        tracker.register(ent2, new Vec(5, 0, 0), updater);
        tracker.register(ent3, new Vec(50, 0, 0), updater);

        tracker.selectEntityConsume(EntitySelector.selector(builder -> builder.range(4)), Vec.ZERO, entity -> fail("No entity should be nearby"));

        tracker.register(ent1, Vec.ZERO, updater);

        Set<Entity> entities = new HashSet<>();

        entities.add(ent1);
        tracker.selectEntityConsume(EntitySelector.selector(builder -> builder.range(4)), Vec.ZERO, entity -> assertTrue(entities.remove(entity)));
        assertEquals(0, entities.size());

        entities.add(ent1);
        tracker.selectEntityConsume(EntitySelector.selector(builder -> builder.range(4.99)), Vec.ZERO, entity -> assertTrue(entities.remove(entity)));
        assertEquals(0, entities.size());

        entities.add(ent1);
        entities.add(ent2);
        tracker.selectEntityConsume(EntitySelector.selector(builder -> builder.range(5)), Vec.ZERO, entity -> assertTrue(entities.remove(entity)));
        assertEquals(0, entities.size());

        entities.add(ent1);
        entities.add(ent2);
        entities.add(ent3);
        tracker.selectEntityConsume(EntitySelector.selector(builder -> builder.range(50)), Vec.ZERO, entity -> assertTrue(entities.remove(entity)));
        assertEquals(0, entities.size());

        // Chunk border
        tracker.move(ent1, new Vec(16, 0, 0), updater);
        entities.add(ent1);
        tracker.selectEntityConsume(EntitySelector.selector(builder -> builder.range(2)), new Vec(15, 0, 0), entity -> assertTrue(entities.remove(entity)));
        assertEquals(0, entities.size());
    }

    @Test
    public void nearbySingleChunk() {
        var ent1 = new Entity(EntityType.ZOMBIE);
        var ent2 = new Entity(EntityType.ZOMBIE);
        var ent3 = new Entity(EntityType.ZOMBIE);
        var updater = new EntityTracker.Update() {
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
        tracker.register(ent1, new Vec(5, 0, 5), updater);
        tracker.register(ent2, new Vec(8, 0, 8), updater);
        tracker.register(ent3, new Vec(17, 0, 17), updater);

        Set<Entity> entities = new HashSet<>();

        entities.add(ent1);
        entities.add(ent2);
        tracker.selectEntityConsume(EntitySelector.selector(builder -> builder.range(16)), Vec.ZERO, entities::add);
        assertEquals(Set.of(ent1, ent2), entities);
        entities.clear();

        entities.add(ent1);
        entities.add(ent2);
        tracker.selectEntityConsume(EntitySelector.selector(builder -> builder.range(5)), new Vec(8, 0, 8), entity -> assertTrue(entities.remove(entity)));
        assertEquals(0, entities.size());

        entities.add(ent2);
        tracker.selectEntityConsume(EntitySelector.selector(builder -> builder.range(1)), new Vec(8, 0, 8), entity -> assertTrue(entities.remove(entity)));
        assertEquals(0, entities.size());
    }
}
