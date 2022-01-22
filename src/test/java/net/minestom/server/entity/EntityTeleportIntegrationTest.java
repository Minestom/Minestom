package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.EntityTeleportPacket;
import net.minestom.server.network.packet.server.play.PlayerPositionAndLookPacket;
import org.junit.jupiter.api.Test;

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
        var player = connection.connect(instance, new Pos(0, 40, 0)).join();
        assertEquals(instance, player.getInstance());
        assertEquals(new Pos(0, 40, 0), player.getPosition());

        var viewerConnection = env.createConnection();
        viewerConnection.connect(instance, new Pos(0, 40, 0)).join();

        var tracker = connection.trackIncoming(ServerPacket.class);
        var viewerTracker = viewerConnection.trackIncoming(ServerPacket.class);
        var teleportPosition = new Pos(1, 42, 1);
        player.teleport(teleportPosition).join();
        assertEquals(teleportPosition, player.getPosition());

        // Verify received packet(s)
        tracker.assertSingle(PlayerPositionAndLookPacket.class,
                packet -> assertEquals(teleportPosition, packet.position()));
        // Verify broadcast packet(s)
        viewerTracker.assertSingle(EntityTeleportPacket.class, packet -> {
            assertEquals(player.getEntityId(), packet.entityId());
            assertEquals(teleportPosition, packet.position());
        });
    }

    @Test
    public void playerTeleport(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0)).join();
        assertEquals(instance, player.getInstance());
        assertEquals(new Pos(0, 40, 0), player.getPosition());

        var viewerConnection = env.createConnection();
        viewerConnection.connect(instance, new Pos(0, 40, 0)).join();

        var teleportPosition = new Pos(4999, 42, 4999);
        player.teleport(teleportPosition).join();
        assertEquals(teleportPosition, player.getPosition());
    }
}
