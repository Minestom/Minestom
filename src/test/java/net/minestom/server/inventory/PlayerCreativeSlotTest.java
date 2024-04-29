package net.minestom.server.inventory;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.listener.CreativeInventoryActionListener;
import net.minestom.server.network.packet.client.play.ClientCreativeInventoryActionPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class PlayerCreativeSlotTest {

    @Test
    public void testCreativeSlots(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, player.getInstance());

        player.setGameMode(GameMode.CREATIVE);
        player.addPacketToQueue(new ClientCreativeInventoryActionPacket((short) PlayerInventoryUtils.OFF_HAND_SLOT, ItemStack.of(Material.STICK)));
        player.interpretPacketQueue();
        assertEquals(Material.STICK, player.getItemInOffHand().material());
    }

    @Test
    public void testBoundsCheck(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        player.setGameMode(GameMode.CREATIVE);

        assertDoesNotThrow(() -> CreativeInventoryActionListener.listener(new ClientCreativeInventoryActionPacket((short) 76, ItemStack.of(Material.OAK_LOG)), player));
    }
}
