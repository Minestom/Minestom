package net.minestom.server.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;

/**
 * Represent an inventory which can receive click input
 * <p>
 * all methods returning boolean returns true if the action is successful, false otherwise
 */
public interface InventoryClickHandler {

    boolean leftClick(Player player, int slot);

    boolean rightClick(Player player, int slot);

    boolean shiftClick(Player player, int slot); // shift + left/right click have the same behavior

    boolean changeHeld(Player player, int slot, int key);

    boolean middleClick(Player player, int slot);

    boolean drop(Player player, int mode, int slot, int button);

    boolean dragging(Player player, int slot, int button);

    boolean doubleClick(Player player, int slot);

    default void callClickEvent(Player player, Inventory inventory, int slot,
                                ClickType clickType, ItemStack clicked, ItemStack cursor) {
        InventoryClickEvent inventoryClickEvent = new InventoryClickEvent(inventory, slot, clickType, clicked, cursor);
        player.callEvent(InventoryClickEvent.class, inventoryClickEvent);
    }

}
