package net.minestom.server.instance;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MicrotusExtension.class)
class EntityTrackerIntegrationTest {

    @Test
    void maxDistance(Env env) {
        final Instance instance = env.createFlatInstance();
        final Pos spawnPos = new Pos(0, 41, 0);
        final int viewDistanceInChunks = ServerFlag.ENTITY_VIEW_DISTANCE;

        final Player viewer = env.createPlayer(instance, spawnPos);
        final AtomicInteger viewersCount = new AtomicInteger();
        final Entity entity = new Entity(EntityType.ZOMBIE) {
            @Override
            public void updateNewViewer(@NotNull Player player) {
                viewersCount.incrementAndGet();
            }

            @Override
            public void updateOldViewer(@NotNull Player player) {
                viewersCount.decrementAndGet();
            }
        };
        entity.setInstance(instance, spawnPos).join();
        assertEquals(1, viewersCount.get());
        viewer.teleport(new Pos(viewDistanceInChunks * 16 + 15, 41, 0)).join(); // viewer at max chunk range
        assertEquals(1, viewersCount.get());
        viewer.teleport(new Pos(viewDistanceInChunks * 16 + 16, 41, 0)).join(); // viewer outside of chunk range
        assertEquals(0, viewersCount.get());
        viewer.teleport(new Pos(viewDistanceInChunks * 16 + 15, 41, 0)).join(); // viewer back to max chunk range
        assertEquals(1, viewersCount.get());
    }

    @Test
    void cornerInstanceSwap(Env env) {
        final Instance instance = env.createFlatInstance();
        final Instance anotherInstance = env.createFlatInstance();
        final Pos spawnPos = new Pos(0, 41, 0);
        final int viewDistanceInChunks = ServerFlag.ENTITY_VIEW_DISTANCE;

        final Player viewer = env.createPlayer(instance, spawnPos);
        final AtomicInteger viewersCount = new AtomicInteger();
        final Entity entity = new Entity(EntityType.ZOMBIE) {
            @Override
            public void updateNewViewer(@NotNull Player player) {
                viewersCount.incrementAndGet();
            }

            @Override
            public void updateOldViewer(@NotNull Player player) {
                viewersCount.decrementAndGet();
            }
        };
        entity.setInstance(instance, spawnPos).join();
        assertEquals(1, viewersCount.get());
        viewer.teleport(new Pos(viewDistanceInChunks * 16 + 15, 41, 0)).join(); // viewer at max chunk range
        assertEquals(1, viewersCount.get());
        viewer.setInstance(anotherInstance, spawnPos).join(); // viewer swapped instance
        assertEquals(0, viewersCount.get());
        viewer.setInstance(instance, spawnPos).join(); // viewer back to spawn
        assertEquals(1, viewersCount.get());
    }

    @Test
    void viewable(Env env) {
        final Instance instance = env.createFlatInstance();
        final Pos spawnPos = new Pos(0, 41, 0);
        var viewable = instance.getEntityTracker().viewable(spawnPos.chunkX(), spawnPos.chunkZ());
        assertEquals(0, viewable.getViewers().size());

        final Player player = env.createPlayer(instance, spawnPos);
        assertEquals(1, viewable.getViewers().size());
        assertSame(viewable, instance.getEntityTracker().viewable(spawnPos.chunkX(), spawnPos.chunkZ()));

        player.teleport(new Pos(10_000, 41, 0)).join();
        assertEquals(0, viewable.getViewers().size());

        player.teleport(spawnPos).join();
        assertEquals(1, viewable.getViewers().size());
    }

    @Test
    void viewableShared(Env env) {
        final InstanceContainer instance = (InstanceContainer) env.createFlatInstance();
        var shared = env.process().instance().createSharedInstance(instance);
        var sharedList = instance.getSharedInstances();

        final Pos spawnPos = new Pos(0, 41, 0);
        var viewable = instance.getEntityTracker().viewable(sharedList, spawnPos.chunkX(), spawnPos.chunkZ());
        assertEquals(0, viewable.getViewers().size());

        final Player player = env.createPlayer(instance, spawnPos);
        assertEquals(1, viewable.getViewers().size());
        assertSame(viewable, instance.getEntityTracker().viewable(sharedList, spawnPos.chunkX(), spawnPos.chunkZ()));

        player.setInstance(shared).join();
        assertEquals(1, viewable.getViewers().size());

        player.teleport(new Pos(10_000, 41, 0)).join();
        assertEquals(0, viewable.getViewers().size());

        var shared2 = env.process().instance().createSharedInstance(instance);
        player.setInstance(shared2, spawnPos).join();
        assertEquals(1, viewable.getViewers().size());
    }
}
