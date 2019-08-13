package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.inventory.Inventory;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.net.packet.server.play.ConfirmTransactionPacket;
import fr.themode.minestom.net.packet.server.play.SetSlotPacket;
import fr.themode.minestom.utils.Utils;

public class ClientClickWindowPacket implements ClientPlayPacket {

    public byte windowId;
    public short slot;
    public byte button;
    public short actionNumber;
    public int mode;
    // TODO clicked item

    @Override
    public void process(Player player) {
        Inventory inventory = player.getOpenInventory();
        System.out.println("Window id: " + windowId + " | slot: " + slot + " | button: " + button + " | mode: " + mode);

        ConfirmTransactionPacket confirmTransactionPacket = new ConfirmTransactionPacket();
        confirmTransactionPacket.windowId = windowId;
        confirmTransactionPacket.actionNumber = actionNumber;
        confirmTransactionPacket.accepted = true; // Change depending on output

        switch (mode) {
            case 0:
                switch (button) {
                    case 0:
                        // Left click
                        inventory.leftClick(player, slot);
                        break;
                    case 1:
                        // Right click
                        inventory.rightClick(player, slot);
                        break;
                }
                break;
            case 1:
                switch (button) {
                    case 0:
                        // Shift + left click
                        break;
                    case 1:
                        // Shift + right click
                        break;
                }
                break;
            case 2:
                // Number key 1-9
                break;
            case 3:
                // Middle click (only creative players in non-player inventories)
                break;
            case 4:
                // Dropping functions
                break;
            case 5:
                // Dragging
                break;
            case 6:
                // Double click (merge similar items)
                break;
        }

        ItemStack cursorItem = inventory.getCursorItem(player);
        SetSlotPacket setSlotPacket = new SetSlotPacket();
        setSlotPacket.windowId = -1;
        setSlotPacket.slot = -1;
        setSlotPacket.itemStack = cursorItem;

        player.getPlayerConnection().sendPacket(setSlotPacket);
        player.getPlayerConnection().sendPacket(confirmTransactionPacket);
    }

    @Override
    public void read(Buffer buffer) {
        this.windowId = buffer.getByte();
        this.slot = buffer.getShort();
        this.button = buffer.getByte();
        this.actionNumber = buffer.getShort();
        this.mode = Utils.readVarInt(buffer);
        // TODO read clicked item
    }
}
