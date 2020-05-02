package net.minestom.server.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.InventoryClickEvent;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;

public interface InventoryClickHandler {

    void leftClick(Player player, int slot);

    void rightClick(Player player, int slot);

    void shiftClick(Player player, int slot); // shift + left/right click have the same behavior

    void changeHeld(Player player, int slot, int key);

    void middleClick(Player player, int slot);

    void drop(Player player, int mode, int slot, int button);

    void dragging(Player player, int slot, int button);

    void doubleClick(Player player, int slot);

    default void callClickEvent(Player player, Inventory inventory, int slot,
                                ClickType clickType, ItemStack clicked, ItemStack cursor) {
        InventoryClickEvent inventoryClickEvent = new InventoryClickEvent(inventory, slot, clickType, clicked, cursor);
        player.callEvent(InventoryClickEvent.class, inventoryClickEvent);
    }

}
