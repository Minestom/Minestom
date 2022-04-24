package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class EntityMetaIntegrationTest {

    @Test
    public void notifyAboutChanges(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var otherPlayer = connection.connect(instance, new Pos(0, 42, 0)).join();
        var player = connection.connect(instance, new Pos(0, 42, 1)).join();

        assertTrue(player.getViewers().contains(otherPlayer));

        var incomingPackets = connection.trackIncoming(EntityMetaDataPacket.class);

        player.getEntityMeta().setNotifyAboutChanges(false);
        player.setInvisible(true);
        player.setNoGravity(true);
        player.setSneaking(true);
        // No packets should be received here: notifyAboutChanges is off
        incomingPackets.assertEmpty();
        incomingPackets = connection.trackIncoming(EntityMetaDataPacket.class);

        player.getEntityMeta().setNotifyAboutChanges(true);

        var packets = incomingPackets.collect();
        // Two packets should be received: One for the player, one for the viewer
        assertEquals(2, packets.size());
        validMetaDataPackets(packets, player.getEntityId(), entry -> {
            final Object content = entry.value();
            switch (entry.type()) {
                case Metadata.TYPE_BYTE -> assertEquals((byte) 34, content);
                case Metadata.TYPE_BOOLEAN -> assertTrue((boolean) content);
                case Metadata.TYPE_POSE -> assertEquals(Entity.Pose.SNEAKING, content);
                default -> Assertions.fail("Invalid MetaData entry");
            }
        });

        // Now test the "normal" behavior: Updates should be sent instantly
        incomingPackets = connection.trackIncoming(EntityMetaDataPacket.class);
        player.setInvisible(false);
        player.setNoGravity(false);
        player.setSneaking(false);
        packets = incomingPackets.collect();
        validMetaDataPackets(packets, player.getEntityId(), entry -> {
            final Object content = entry.value();
            switch (entry.type()) {
                case Metadata.TYPE_BYTE -> assertTrue(content.equals((byte) 2) || content.equals((byte) 0));
                case Metadata.TYPE_BOOLEAN -> assertFalse((boolean) content);
                case Metadata.TYPE_POSE -> assertEquals(Entity.Pose.STANDING, content);
                default -> Assertions.fail("Invalid MetaData entry");
            }
        });
        // 4 changes, for two viewers
        assertEquals(4 * 2, packets.size());
    }

    private void validMetaDataPackets(List<EntityMetaDataPacket> packets, int entityId, Consumer<Metadata.Entry<?>> contentChecker) {
        for (var packet : packets) {
            assertEquals(packet.entityId(), entityId);
            for (var entry : packet.entries().values()) {
                contentChecker.accept(entry);
            }
        }
    }
}
