package net.minestom.server.entity.player;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket;
import net.minestom.server.network.packet.client.play.ClientTeleportConfirmPacket;
import net.minestom.server.network.packet.server.play.EntityPositionPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class PlayerMovementIntegrationTest {

    @Test
    public void teleportConfirm(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 40, 0));
        // No confirmation
        p1.addPacketToQueue(new ClientPlayerPositionPacket(new Pos(0.2, 40, 0), true));
        p1.interpretPacketQueue();
        assertEquals(new Pos(0, 40, 0), p1.getPosition());
        // Confirmation
        p1.addPacketToQueue(new ClientTeleportConfirmPacket(p1.getLastSentTeleportId()));
        p1.addPacketToQueue(new ClientPlayerPositionPacket(new Pos(0.2, 40, 0), true));
        p1.interpretPacketQueue();
        assertEquals(new Pos(0.2, 40, 0), p1.getPosition());
    }

    // FIXME
    //@Test
    public void singleTickMovementUpdate(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var p1 = env.createPlayer(instance, new Pos(0, 40, 0));
        connection.connect(instance, new Pos(0, 40, 0)).join();

        p1.addPacketToQueue(new ClientTeleportConfirmPacket(p1.getLastSentTeleportId()));
        p1.addPacketToQueue(new ClientPlayerPositionPacket(new Pos(0.2, 40, 0), true));
        p1.addPacketToQueue(new ClientPlayerPositionPacket(new Pos(0.4, 40, 0), true));
        var tracker = connection.trackIncoming(EntityPositionPacket.class);
        p1.interpretPacketQueue();

        // Position update should only be sent once per tick independently of the number of packets
        tracker.assertSingle();
    }
}
