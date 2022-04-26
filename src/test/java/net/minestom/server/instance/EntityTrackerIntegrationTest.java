package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class EntityTrackerIntegrationTest {

    @Test
    public void maxDistance(Env env) {
        final Instance instance = env.createFlatInstance();
        final Instance anotherInstance = env.createFlatInstance();
        final Pos spawnPos = new Pos(0, 41, 0);
        final int viewDistanceInChunks = MinecraftServer.getEntityViewDistance();

        final Player viewer = createTestPlayer();
        final AtomicInteger viewersCount = new AtomicInteger();
        final Entity entity = new Entity(EntityType.ZOMBIE) {
            @Override
            public void updateNewViewer(Player player) {
                viewersCount.incrementAndGet();
            }
            @Override
            public void updateOldViewer(Player player) {
                viewersCount.decrementAndGet();
            }
        };
        entity.setInstance(instance, spawnPos).join();
        assertEquals(0, viewersCount.get());
        viewer.setInstance(instance, spawnPos).join(); // viewer at spawn
        assertEquals(1, viewersCount.get());
        viewer.teleport(new Pos(viewDistanceInChunks * 16 + 15, 41, 0)).join(); // viewer at max chunk range
        assertEquals(1, viewersCount.get());
        viewer.teleport(new Pos(viewDistanceInChunks * 16 + 16, 41, 0)).join(); // viewer outside of chunk range
        assertEquals(0, viewersCount.get());
        viewer.teleport(new Pos(viewDistanceInChunks * 16 + 15, 41, 0)).join(); // viewer back to max chunk range
        assertEquals(1, viewersCount.get());
    }

    @Test
    public void cornerInstanceSwap(Env env) {
        final Instance instance = env.createFlatInstance();
        final Instance anotherInstance = env.createFlatInstance();
        final Pos spawnPos = new Pos(0, 41, 0);
        final int viewDistanceInChunks = MinecraftServer.getEntityViewDistance();

        final Player viewer = createTestPlayer();
        final AtomicInteger viewersCount = new AtomicInteger();
        final Entity entity = new Entity(EntityType.ZOMBIE) {
            @Override
            public void updateNewViewer(Player player) {
                viewersCount.incrementAndGet();
            }
            @Override
            public void updateOldViewer(Player player) {
                viewersCount.decrementAndGet();
            }
        };
        entity.setInstance(instance, spawnPos).join();
        assertEquals(0, viewersCount.get());
        viewer.setInstance(instance, spawnPos).join(); // viewer at spawn
        assertEquals(1, viewersCount.get());
        viewer.teleport(new Pos(viewDistanceInChunks * 16 + 15, 41, 0)).join(); // viewer at max chunk range
        assertEquals(1, viewersCount.get());
        viewer.setInstance(anotherInstance, spawnPos).join(); // viewer swapped instance
        assertEquals(0, viewersCount.get());
        viewer.setInstance(instance, spawnPos).join(); // viewer back to spawn
        assertEquals(1, viewersCount.get());
    }

    private Player createTestPlayer() {
        return new Player(UUID.randomUUID(), "TestPlayer", new PlayerConnection() {
            @Override
            public void sendPacket(@NotNull SendablePacket packet) {
                // nothing
            }

            @Override
            public @NotNull SocketAddress getRemoteAddress() {
                return null;
            }
        });
    }

}
