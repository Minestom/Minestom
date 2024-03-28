package net.minestom.server.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.client.play.ClientCloseWindowPacket;
import net.minestom.server.network.packet.server.play.CloseWindowPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class InventoryCloseStateTest {


    @Test
    public void doNotReceiveClosePacketFromServerWhenSendingClientCloseWindowPacket(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, player.getInstance());

        var packetTracker = connection.trackIncoming(CloseWindowPacket.class);
        var inventory = new ContainerInventory(InventoryType.CHEST_2_ROW, Component.text("Test"));
        player.openInventory(inventory);
        player.closeInventory(); // Closes the inventory server-side, should send a CloseWindowPacket
        player.openInventory(inventory);
        // Send the close window packet
        player.addPacketToQueue(new ClientCloseWindowPacket(inventory.getWindowId()));
        player.interpretPacketQueue();
        packetTracker.assertSingle(closeWindowPacket -> assertEquals(inventory.getWindowId(), closeWindowPacket.windowId()));
        packetTracker.assertCount(1); // Assert we only get 1 close window packet from the closeInventory(); call
    }
}
