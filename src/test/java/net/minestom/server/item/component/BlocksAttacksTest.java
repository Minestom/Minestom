package net.minestom.server.item.component;

import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.client.play.ClientUseItemPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class BlocksAttacksTest {

    @Test
    public void test(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        player.setItemInMainHand(ItemStack.of(Material.SHIELD));

        player.addPacketToQueue(new ClientUseItemPacket(PlayerHand.MAIN, 0, 0f, 0f));
        player.interpretPacketQueue();

        assertTrue(player.isUsingItem());
        assertTrue(player.getPlayerMeta().isHandActive());

        player.addPacketToQueue(new ClientPlayerDiggingPacket(ClientPlayerDiggingPacket.Status.UPDATE_ITEM_STATE, player.getPosition(), BlockFace.NORTH, 1));
        player.interpretPacketQueue();

        assertFalse(player.isUsingItem());
        assertFalse(player.getPlayerMeta().isHandActive());

        player.setItemInMainHand(ItemStack.of(Material.DIAMOND_SWORD).with(DataComponents.BLOCKS_ATTACKS,
                new BlocksAttacks(1f, 1f, List.of(), BlocksAttacks.ItemDamageFunction.DEFAULT, null, null, null)));

        player.addPacketToQueue(new ClientUseItemPacket(PlayerHand.MAIN, 0, 0f, 0f));
        player.interpretPacketQueue();

        assertTrue(player.isUsingItem());
        assertTrue(player.getPlayerMeta().isHandActive());
    }
}
