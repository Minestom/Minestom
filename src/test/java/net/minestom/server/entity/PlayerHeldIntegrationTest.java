package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class PlayerHeldIntegrationTest {

    @Test
    public void playerHeld(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));

        player.getInventory().setItemStack(1, ItemStack.of(Material.STONE));
        assertEquals(ItemStack.AIR, player.getItemInMainHand());
        assertEquals(0, player.getHeldSlot());

        player.addPacketToQueue(new ClientHeldItemChangePacket((short) 1));
        player.interpretPacketQueue();

        assertEquals(ItemStack.of(Material.STONE), player.getItemInMainHand());
        assertEquals(1, player.getHeldSlot());
    }

    @Test
    public void playerHeldEvent(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));

        player.getInventory().setItemStack(1, ItemStack.of(Material.STONE));
        assertEquals(ItemStack.AIR, player.getItemInMainHand());
        assertEquals(0, player.getHeldSlot());

        player.addPacketToQueue(new ClientHeldItemChangePacket((short) 1));
        var listener = env.listen(PlayerChangeHeldSlotEvent.class);
        listener.followup(event -> {
            assertEquals(player, event.getPlayer());
            assertEquals(0, event.getOldSlot());
            assertEquals(1, event.getNewSlot());
        });
        player.interpretPacketQueue();
        assertEquals(ItemStack.of(Material.STONE), player.getItemInMainHand());
        assertEquals(1, player.getHeldSlot());
    }

    @Test
    public void playerChangingSlots(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0)).join();

        player.getInventory().setItemStack(1, ItemStack.of(Material.STONE));
        player.getInventory().setItemStack(3, ItemStack.of(Material.OAK_PLANKS));

        player.addPacketToQueue(new ClientHeldItemChangePacket((short) 1));
        var listener = env.listen(PlayerChangeHeldSlotEvent.class);
        listener.followup(event -> {
            assertEquals(player, event.getPlayer());
            assertEquals(0, event.getOldSlot());
            assertEquals(1, event.getNewSlot());
            assertEquals(ItemStack.AIR, event.getItemInOldSlot());
            assertEquals(ItemStack.of(Material.STONE), event.getItemInNewSlot());
        });
        player.interpretPacketQueue();

        player.addPacketToQueue(new ClientHeldItemChangePacket((short) 3));
        listener.followup(event -> {
            assertEquals(player, event.getPlayer());
            assertEquals(1, event.getOldSlot());
            assertEquals(3, event.getNewSlot());
            assertEquals(ItemStack.of(Material.STONE), event.getItemInOldSlot());
            assertEquals(ItemStack.of(Material.OAK_PLANKS), event.getItemInNewSlot());
        });
        player.interpretPacketQueue();
    }
}
