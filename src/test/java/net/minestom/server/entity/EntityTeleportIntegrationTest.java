package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.EntityPositionSyncPacket;
import net.minestom.server.network.packet.server.play.PlayerPositionAndLookPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class EntityTeleportIntegrationTest {

    @Test
    public void entityChunkTeleport(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());
        assertEquals(new Pos(0, 42, 0), entity.getPosition());

        entity.teleport(new Pos(1, 42, 1)).join();
        assertEquals(new Pos(1, 42, 1), entity.getPosition());
    }

    @Test
    public void entityTeleport(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());
        assertEquals(new Pos(0, 42, 0), entity.getPosition());

        entity.teleport(new Pos(52, 42, 52)).join();
        assertEquals(new Pos(52, 42, 52), entity.getPosition());
    }

    @Test
    public void playerChunkTeleport(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));
        assertEquals(instance, player.getInstance());
        assertEquals(new Pos(0, 40, 0), player.getPosition());

        var viewerConnection = env.createConnection();
        viewerConnection.connect(instance, new Pos(0, 40, 0));

        var tracker = connection.trackIncoming(ServerPacket.class);
        var viewerTracker = viewerConnection.trackIncoming(ServerPacket.class);
        var teleportPosition = new Pos(1, 42, 1).withYaw(5);
        player.teleport(teleportPosition).join();
        assertEquals(teleportPosition, player.getPosition());

        // Verify received packet(s)
        tracker.assertSingle(PlayerPositionAndLookPacket.class,
                packet -> assertEquals(teleportPosition, packet.position()));
        // Verify broadcast packet(s)

        viewerTracker.assertCount(1);
        viewerTracker.assertSingle(EntityPositionSyncPacket.class, packet -> {
            assertEquals(player.getEntityId(), packet.entityId());
            assertEquals(teleportPosition, packet.position());
            assertEquals(teleportPosition.yaw(), packet.yaw());
        });
    }

    @Test
    public void playerTeleport(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));
        assertEquals(instance, player.getInstance());
        assertEquals(new Pos(0, 40, 0), player.getPosition());

        var viewerConnection = env.createConnection();
        viewerConnection.connect(instance, new Pos(0, 40, 0));

        var teleportPosition = new Pos(4999, 42, 4999);
        player.teleport(teleportPosition).join();
        assertEquals(teleportPosition, player.getPosition());
    }

    @Test
    public void playerTeleportWithFlagsTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 0, 0));

        player.teleport(new Pos(10, 10, 10, 90, 0)).join();
        assertEquals(new Pos(10, 10, 10, 90, 0), player.getPosition());

        player.teleport(new Pos(0, 0, 0, 0, 0), null, RelativeFlags.ALL).join();
        assertEquals(new Pos(10, 10, 10, 90, 0), player.getPosition());

        player.teleport(new Pos(5, 10, 2, 5, 5), null, RelativeFlags.VIEW).join();
        assertEquals(new Pos(5, 10, 2, 95, 5), player.getPosition());
    }

    @Test
    public void entityTeleportToInfinity(Env env) throws ExecutionException, InterruptedException, TimeoutException {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());
        assertEquals(new Pos(0, 42, 0), entity.getPosition());

        entity.teleport(new Pos(Double.POSITIVE_INFINITY, 42, 52)).join();
        CompletableFuture.runAsync(() -> entity.tick(0 /* 0 is fine here, it's just a delta*/))
                .get(10, TimeUnit.SECONDS);
        // This should not hang forever

        // The position should have been capped at 2 billion.
        assertEquals(new Pos(Entity.MAX_COORDINATE, 42, 52), entity.getPosition());
    }
}
