package net.minestom.server.listener;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.player.PlayerChangeDiggingDirectionEvent;
import net.minestom.server.event.player.PlayerStabEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket;
import net.minestom.server.network.packet.server.play.AcknowledgeBlockChangePacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class PlayerActionListenerIntegrationTest {

    @Test
    public void testStabInvalidWeapon(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 0, 0));

        var tracker = env.trackEvent(PlayerStabEvent.class, EventFilter.PLAYER, player);

        PlayerActionListener.playerActionListener(new ClientPlayerActionPacket(
                ClientPlayerActionPacket.Status.STAB,
                Vec.ZERO, BlockFace.NORTH, 0
        ), player);
        tracker.assertEmpty();
    }

    @Test
    public void testStabWithWeapon(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 0, 0));
        player.setItemInMainHand(ItemStack.of(Material.NETHERITE_SPEAR));

        var tracker = env.trackEvent(PlayerStabEvent.class, EventFilter.PLAYER, player);
        PlayerActionListener.playerActionListener(new ClientPlayerActionPacket(
                ClientPlayerActionPacket.Status.STAB,
                Vec.ZERO, BlockFace.NORTH, 0
        ), player);

        tracker.assertSingle();
    }

    @Test
    public void changedDiggingDirectionIsAcknowledged(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        var acks = connection.trackIncoming(AcknowledgeBlockChangePacket.class);
        var events = env.trackEvent(PlayerChangeDiggingDirectionEvent.class, EventFilter.PLAYER, player);
        player.addPacketToQueue(new ClientPlayerActionPacket(
                ClientPlayerActionPacket.Status.CHANGED_DIGGING_DIRECTION,
                new Pos(0, 39, 0), BlockFace.EAST, 7
        ));
        player.interpretPacketQueue();

        acks.assertSingle(ack -> assertEquals(7, ack.sequence()));
        events.assertSingle(event -> {
            assertEquals(new BlockVec(0, 39, 0), event.getBlockPosition());
            assertEquals(BlockFace.EAST, event.getBlockFace());
            assertEquals(Block.STONE, event.getBlock());
        });
    }

    @Test
    public void changedDiggingDirectionInUnloadedChunkIsIgnored(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        var acks = connection.trackIncoming(AcknowledgeBlockChangePacket.class);
        var events = env.trackEvent(PlayerChangeDiggingDirectionEvent.class, EventFilter.PLAYER, player);
        player.addPacketToQueue(new ClientPlayerActionPacket(
                ClientPlayerActionPacket.Status.CHANGED_DIGGING_DIRECTION,
                new Pos(10_000, 39, 10_000), BlockFace.EAST, 7
        ));
        player.interpretPacketQueue();

        acks.assertEmpty();
        events.assertEmpty();
    }
}
