package net.minestom.server.entity;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.entity.EntityFireExtinguishEvent;
import net.minestom.server.event.entity.EntitySetFireEvent;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
public class EntityFireTest {
    @Test
    public void duration(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();

        final int fireTicks = 10;
        LivingEntity entity = new LivingEntity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Vec(0, 0, 0));

        entity.setFireTicks(fireTicks);
        assertTrue(entity.getEntityMeta().isOnFire());

        for (int i = 0; i < fireTicks; i++) {
            assertTrue(entity.getEntityMeta().isOnFire());
            assertEquals(fireTicks - i, entity.getFireTicks());
            entity.tick(0);
        }

        assertFalse(entity.getEntityMeta().isOnFire());
        assertEquals(entity.getFireTicks(), 0);
    }

    @Test
    public void nonNegativeFireDuration(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();

        LivingEntity entity = new LivingEntity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Vec(0, 0, 0));

        // Natural fire decay
        entity.setFireTicks(5);
        for (int i = 0; i < 20; i++) {
            assertTrue(entity.getFireTicks() >= 0);
        }

        // Explicit negative
        entity.setFireTicks(-1);
        assertEquals(0, entity.getFireTicks());

        // Explicit negative in event
        env.listen(EntitySetFireEvent.class).followup(e -> {
            e.setFireTicks(-1);
        });

        entity.setFireTicks(1);
        assertEquals(entity.getFireTicks(), 0);
    }

    @Test
    public void setFireMetadata(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();

        LivingEntity entity = new LivingEntity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Vec(0, 0, 0));

        // Do not extinguish an entity when they're set on fire explicitly
        entity.getEntityMeta().setOnFire(true);
        for (int i = 0; i < 40; i++) {
            entity.tick(0);
            assertTrue(entity.getEntityMeta().isOnFire());
        }

        // Unless setFireTicks has been called to activate the internal remainingFireTicks timer
        entity.setFireTicks(1);
        entity.tick(0);
        assertFalse(entity.isOnFire());
    }

    @Test
    public void extinguishEvent(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();

        LivingEntity entity = new LivingEntity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Vec(0, 0, 0));

        AtomicInteger callCount = new AtomicInteger();
        env.listen(EntityFireExtinguishEvent.class).followup(e -> {
            callCount.getAndIncrement();
            if (callCount.get() == 2) assertTrue(e.isNatural());
            else assertFalse(e.isNatural());
        });

        // Don't call when the entity is already on fire
        entity.setFireTicks(0);
        assertEquals(0, callCount.get());

        // Call now, the entity is set on fire
        entity.setFireTicks(1);
        entity.setFireTicks(-1);
        assertEquals(1, callCount.get());

        // Call naturally
        entity.setFireTicks(3);
        for (int i = 0; i < 3; i++) {
            entity.tick(0);
        }
        assertEquals(2, callCount.get());

        // Don't call if cancelled EntitySetFireEvent
        env.listen(EntitySetFireEvent.class).followup(e -> {
            e.setCancelled(true);
        });
        entity.setFireTicks(5);
        assertEquals(2, callCount.get());
    }
}
