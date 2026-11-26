package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.thread.TickThread;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class EntityExternalTickIntegrationTest {

    @Test
    public void tickOwnership(Env env) {
        var instance = env.createFlatInstance();
        var entity = new TickCounter();
        entity.setInstance(instance, new Pos(0, 42, 0)).join();

        env.tick();
        assertEquals(1, entity.ticks, "Server-ticked by default");

        entity.setExternallyTicked(true);
        env.tick();
        env.tick();
        assertEquals(1, entity.ticks, "The server must not tick an externally ticked entity");

        entity.tick(System.currentTimeMillis());
        assertEquals(2, entity.ticks, "The external system ticks it directly");

        entity.setExternallyTicked(false);
        env.tick();
        assertEquals(3, entity.ticks, "Server ticking resumes");
    }

    @Test
    public void chunkCrossing(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(4, 0).join();
        var entity = new TickCounter();
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setExternallyTicked(true);

        // A chunk crossing re-registers a regular entity with the dispatcher
        entity.teleport(new Pos(64, 42, 0)).join();
        env.tick();
        env.tick();
        assertEquals(0, entity.ticks, "A chunk crossing must not hand the entity back to the server");
    }

    @Test
    public void evictionWindow(Env env) throws Exception {
        var instance = env.createFlatInstance();
        var entity = new TickCounter();
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setExternallyTicked(true);

        // Eviction is a queued dispatcher signal: until it drains, tick threads may still reach the entity
        var done = new CompletableFuture<Void>();
        Thread thread = new TickThread("test-tick-thread") {
            @Override
            public void run() {
                entity.tick(System.currentTimeMillis());
                done.complete(null);
            }
        };
        thread.start();
        done.get(5, TimeUnit.SECONDS);
        assertEquals(0, entity.ticks, "a tick-thread call must be ignored while externally ticked");
    }

    static final class TickCounter extends Entity {
        int ticks;

        TickCounter() {
            super(EntityType.ZOMBIE);
        }

        @Override
        public void update(long time) {
            ticks++;
        }
    }
}
