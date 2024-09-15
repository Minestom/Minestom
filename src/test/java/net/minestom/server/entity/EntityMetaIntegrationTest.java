package net.minestom.server.entity;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
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
        var connection2 = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 1));
        var otherPlayer = connection2.connect(instance, new Pos(0, 42, 0));

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
        assertEquals(1, packets.size());
        validMetaDataPackets(packets, player.getEntityId(), entry -> {
            final Object content = entry.value();
            if (entry.type() == Metadata.TYPE_BYTE) {
                assertEquals((byte) 34, content);
            } else if (entry.type() == Metadata.TYPE_BOOLEAN) {
                assertTrue((boolean) content);
            } else if (entry.type() == Metadata.TYPE_POSE) {
                assertEquals(EntityPose.SNEAKING, content);
            } else {
                Assertions.fail("Invalid MetaData entry");
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
            if (entry.type() == Metadata.TYPE_BYTE) {
                assertTrue(content.equals((byte) 2) || content.equals((byte) 0));
            } else if (entry.type() == Metadata.TYPE_BOOLEAN) {
                assertFalse((boolean) content);
            } else if (entry.type() == Metadata.TYPE_POSE) {
                assertEquals(EntityPose.STANDING, content);
            } else {
                Assertions.fail("Invalid MetaData entry");
            }
        });
        assertEquals(4, packets.size());
    }

    private void validMetaDataPackets(List<EntityMetaDataPacket> packets, int entityId, Consumer<Metadata.Entry<?>> contentChecker) {
        for (var packet : packets) {
            assertEquals(packet.entityId(), entityId);
            for (var entry : packet.entries().values()) {
                contentChecker.accept(entry);
            }
        }
    }

    @Test
    public void customName(Env env) {
        //Base things.
        var connection = env.createConnection();
        var instance = env.createFlatInstance();
        Pos startPos = new Pos(0, 42, 1);

        //Viewer.
        var player = connection.connect(instance, startPos);

        //Tracks incoming packets.
        var incomingPackets = connection.trackIncoming(EntityMetaDataPacket.class);

        //Creates entity and name.
        Entity entity = new Entity(EntityType.BEE);
        entity.setAutoViewable(false);
        entity.getEntityMeta().setNotifyAboutChanges(false);
        entity.setCustomName(Component.text("Custom Name"));
        entity.setCustomNameVisible(true);
        entity.setInstance(instance, startPos);
        entity.getEntityMeta().setNotifyAboutChanges(true);
        entity.addViewer(player);

        //Listen packets to check if entity name is "Custom Name".
        //This is first test, and it is not related to "custom name" bug. Therefore, it should work.
        var packets = incomingPackets.collect();
        validMetaDataPackets(packets, entity.getEntityId(), entry -> {
            if (entry.type() != Metadata.TYPE_OPT_CHAT) return;
            assertEquals(Component.text("Custom Name"), entry.value());
        });

        //Removes viewer.
        entity.removeViewer(player);

        //Tracks incoming packets again. (resets previous)
        incomingPackets = connection.trackIncoming(EntityMetaDataPacket.class);

        //Sets entity name again.
        entity.setCustomName(Component.text("Custom Name 2"));

        //After setting entity's name, we add viewer again to see if the entity name is "Custom Name 2"
        entity.addViewer(player);

        //Checks if entity name is "Custom Name 2" in the metadata entry.
        assertEquals(Component.text("Custom Name 2"), entity.getCustomName());

        //Listen packets to check if entity name is "Custom Name 2".
        packets = incomingPackets.collect();
        validMetaDataPackets(packets, entity.getEntityId(), entry -> {
            if (entry.type() != Metadata.TYPE_OPT_CHAT) return;
            assertEquals(Component.text("Custom Name 2"), entry.value());
        });
    }
}
