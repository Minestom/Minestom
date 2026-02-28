package net.minestom.server.entity.player;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientEntityActionPacket;
import net.minestom.server.network.packet.client.play.ClientInputPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket;
import net.minestom.server.network.packet.client.play.ClientUseItemPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
//@Disabled // Need to run only this set of tests manually due to the server flag being required to execute properly
public class PlayerReceiveSelfMetadataTest {

    @BeforeAll
    public static void setFlag() {
        System.setProperty("minestom.dont-echo-player-input", "true");
    }


    @Test
    public void testDoNotRecieveSelfMetadataWhenFlagSet(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        Pos startingPlayerPos = new Pos(0, 42, 0);
        var player = connection.connect(instance, startingPlayerPos);

        var tracker = connection.trackIncoming(EntityMetaDataPacket.class);
        player.addPacketToQueue(new ClientInputPacket(true, false, false, false, false, true, false));
        player.interpretPacketQueue();
        tracker.assertCount(2); // 1 for metadata ENTITY_FLAGS, 1 for pose update
        EntityMetaDataPacket firstPacket = tracker.collect().getFirst();
        assertTrue(player.isSneaking());
        assertEquals(player.getEntityId(), firstPacket.entityId());
        // We should not have the crouching bit set
        Byte firstValue = (Byte) firstPacket.entries().get(MetadataDef.ENTITY_FLAGS.index()).value();
        assertEquals((byte) 0, firstValue);
        tracker = connection.trackIncoming(EntityMetaDataPacket.class);
        player.getEntityMeta().setOnFire(true);
        // Check to see if we have both bits set, since we aren't in a player input state we should get back the crouching bit
        tracker.assertSingle(secondPacket -> {
            assertEquals(player.getEntityId(), secondPacket.entityId());
            Byte secondValue = (Byte) secondPacket.entries().get(MetadataDef.ENTITY_FLAGS.index()).value();
            Byte expected = (byte) (((MetadataDef.Entry.BitMask) MetadataDef.IS_CROUCHING).bitMask() | ((MetadataDef.Entry.BitMask) MetadataDef.IS_ON_FIRE).bitMask());
            assertEquals(expected, secondValue);
        });
    }

    @Test
    public void testDoNotReceiveFlyingPoseChangeWhenFlagSet(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        Pos startingPlayerPos = new Pos(0, 42, 0);
        var player = connection.connect(instance, startingPlayerPos);

        var tracker = connection.trackIncoming(EntityMetaDataPacket.class);
        player.addPacketToQueue(new ClientEntityActionPacket(player.getEntityId(), ClientEntityActionPacket.Action.START_FLYING_ELYTRA, 0));
        player.interpretPacketQueue();
        tracker.assertCount(2); // 1 for metadata ENTITY_FLAGS, 1 for pose update
        EntityMetaDataPacket firstPacket = tracker.collect().getFirst();
        assertEquals(player.getEntityId(), firstPacket.entityId());
        Byte firstValue = (Byte) firstPacket.entries().get(MetadataDef.ENTITY_FLAGS.index()).value();
        assertEquals(((MetadataDef.Entry.BitMask) MetadataDef.IS_FLYING_WITH_ELYTRA).bitMask(), firstValue);
        EntityMetaDataPacket secondPacket = tracker.collect().getLast();
        assertEquals(0, secondPacket.entries().size()); // 0 entries because the pose update was filtered out
        tracker = connection.trackIncoming(EntityMetaDataPacket.class);
        player.getEntityMeta().setOnFire(true);
        // Check to see if we have both bits set
        tracker.assertSingle(thirdPacket -> {
            assertEquals(player.getEntityId(), thirdPacket.entityId());
            Byte secondValue = (Byte) thirdPacket.entries().get(MetadataDef.ENTITY_FLAGS.index()).value();
            Byte expected = (byte) (((MetadataDef.Entry.BitMask) MetadataDef.IS_FLYING_WITH_ELYTRA).bitMask() | ((MetadataDef.Entry.BitMask) MetadataDef.IS_ON_FIRE).bitMask());
            assertEquals(expected, secondValue);
        });

        tracker = connection.trackIncoming(EntityMetaDataPacket.class);
        player.setPose(EntityPose.SPIN_ATTACK);
        tracker.assertSingle(finalPacket -> {
            assertEquals(player.getEntityId(), finalPacket.entityId());
            assertEquals(EntityPose.SPIN_ATTACK, finalPacket.entries().get(MetadataDef.POSE.index()).value());
        });
    }

    @Test
    public void testDoNotReceiveHandChangesWhenFlagSet(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        Pos startingPlayerPos = new Pos(0, 42, 0);
        var player = connection.connect(instance, startingPlayerPos);

        player.setItemInMainHand(ItemStack.of(Material.BOW));
        var tracker = connection.trackIncoming(EntityMetaDataPacket.class);
        player.addPacketToQueue(new ClientUseItemPacket(PlayerHand.MAIN, 1, 90f, 0f));
        player.interpretPacketQueue();
        tracker.assertCount(1);
        EntityMetaDataPacket firstPacket = tracker.collect().getFirst();
        assertEquals(player.getEntityId(), firstPacket.entityId());
        // No metadata updates from server
        assertEquals(0, firstPacket.entries().size());
        // Server still has correct value
        assertTrue(player.getPlayerMeta().isHandActive());

        tracker = connection.trackIncoming(EntityMetaDataPacket.class);
        player.addPacketToQueue(new ClientPlayerActionPacket(ClientPlayerActionPacket.Status.UPDATE_ITEM_STATE, Pos.ZERO, BlockFace.BOTTOM, 1));
        player.interpretPacketQueue();
        tracker.assertCount(1);
        EntityMetaDataPacket secondPacket = tracker.collect().getFirst();
        // No metadata updates from server
        assertEquals(0, secondPacket.entries().size());
        // Server still has correct value
        assertFalse(player.getPlayerMeta().isHandActive());
    }

    @Test
    public void testViewerReceivesMetadataUpdates(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var connection2 = env.createConnection();
        Pos startingPlayerPos = new Pos(0, 42, 0);
        var player = connection.connect(instance, startingPlayerPos);
        var player2 = connection2.connect(instance, startingPlayerPos);

        player.setItemInMainHand(ItemStack.of(Material.BOW));
        var trackerPlayer2 = connection2.trackIncoming(EntityMetaDataPacket.class);
        player.addPacketToQueue(new ClientUseItemPacket(PlayerHand.MAIN, 1, 90f, 0f));
        player.interpretPacketQueue();
        trackerPlayer2.assertCount(1);
        EntityMetaDataPacket firstPacket = trackerPlayer2.collect().getFirst();
        assertEquals(player.getEntityId(), firstPacket.entityId());
        // Viewer sees metadata update
        assertEquals(1, firstPacket.entries().size());
        Byte value = (Byte) firstPacket.entries().get(MetadataDef.LivingEntity.IS_HAND_ACTIVE.index()).value();
        assertEquals(((MetadataDef.Entry.BitMask) MetadataDef.LivingEntity.IS_HAND_ACTIVE).bitMask(), value);

        trackerPlayer2 = connection2.trackIncoming(EntityMetaDataPacket.class);
        player.addPacketToQueue(new ClientPlayerActionPacket(ClientPlayerActionPacket.Status.UPDATE_ITEM_STATE, Pos.ZERO, BlockFace.BOTTOM, 1));
        player.interpretPacketQueue();
        trackerPlayer2.assertCount(1);
        EntityMetaDataPacket secondPacket = trackerPlayer2.collect().getFirst();
        // Hand is no longer active
        assertEquals(1, secondPacket.entries().size());
        // Server still has correct value
        assertEquals((byte) 0, secondPacket.entries().get(MetadataDef.LivingEntity.IS_HAND_ACTIVE.index()).value());
    }
}
