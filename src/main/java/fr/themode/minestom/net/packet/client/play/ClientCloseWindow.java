package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.inventory.Inventory;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Utils;

public class ClientCloseWindow implements ClientPlayPacket {

    public int windowId;

    @Override
    public void process(Player player) {
        System.out.println("CLOSED: " + windowId);
        Inventory openInventory = player.getOpenInventory();
        // if windowId == 0 then it is player's inventory, meaning that they hadn't been any open inventory packet
        if (openInventory != null)
            player.closeInventory();
    }

    @Override
    public void read(Buffer buffer) {
        this.windowId = Utils.readVarInt(buffer);
    }
}
