package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.client.play.ClientEntityActionPacket;
import net.minestom.server.network.packet.client.play.ClientInputPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class PlayerSprintingMetadataTest {

    @Test
    public void sprintingMetadata(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));

        player.addPacketToQueue(new ClientInputPacket(true, false, false, false, false, false, true));
        player.addPacketToQueue(new ClientEntityActionPacket(
                player.getEntityId(),
                ClientEntityActionPacket.Action.START_SPRINTING,
                0
        ));

        var tracker = connection.trackIncoming(EntityMetaDataPacket.class);
        player.interpretPacketQueue();

        var packets = tracker.collect();
        assertEquals(1, packets.size(), "Expected single packet, got multiple");
    }

}
