package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.inventory.Inventory;
import fr.themode.minestom.inventory.InventoryClickHandler;
import fr.themode.minestom.inventory.PlayerInventory;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.client.play.ClientClickWindowPacket;
import fr.themode.minestom.net.packet.client.play.ClientCloseWindow;
import fr.themode.minestom.net.packet.server.play.ConfirmTransactionPacket;
import fr.themode.minestom.net.packet.server.play.SetSlotPacket;

public class WindowListener {

    public static void clickWindowListener(ClientClickWindowPacket packet, Player player) {
        InventoryClickHandler clickHandler = player.getOpenInventory();
        if (clickHandler == null) {
            clickHandler = player.getInventory();
        }

        byte windowId = packet.windowId;
        short slot = packet.slot;
        byte button = packet.button;
        short actionNumber = packet.actionNumber;
        int mode = packet.mode;

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
                        clickHandler.leftClick(player, slot);
                        break;
                    case 1:
                        // Right click
                        clickHandler.rightClick(player, slot);
                        break;
                }
                break;
            case 1:
                clickHandler.shiftClick(player, slot); // Shift + left/right have identical behavior
                break;
            case 2:
                clickHandler.changeHeld(player, slot, button);
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
                clickHandler.doubleClick(player, slot);
                break;
        }

        ItemStack cursorItem = clickHandler instanceof Inventory ? ((Inventory) clickHandler).getCursorItem(player) : ((PlayerInventory) clickHandler).getCursorItem();
        SetSlotPacket setSlotPacket = new SetSlotPacket();
        setSlotPacket.windowId = -1;
        setSlotPacket.slot = -1;
        setSlotPacket.itemStack = cursorItem;

        player.getPlayerConnection().sendPacket(setSlotPacket);
        player.getPlayerConnection().sendPacket(confirmTransactionPacket);
    }

    public static void closeWindowListener(ClientCloseWindow packet, Player player) {
        // if windowId == 0 then it is player's inventory, meaning that they hadn't been any open inventory packet
        player.closeInventory();
    }

}
