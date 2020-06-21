package net.minestom.server.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;

/**
 * Represent an inventory which can receive click input
 * all methods returning boolean returns true if the action is successful, false otherwise
 * <p>
 * See https://wiki.vg/Protocol#Click_Window for more information
 */
public interface InventoryClickHandler {

    /**
     * Called when a player left click in the inventory. Can also be to drop the cursor item
     *
     * @param player the player who clicked
     * @param slot   the slot number
     * @return true if the click hasn't been cancelled, false otherwise
     */
    boolean leftClick(Player player, int slot);

    /**
     * Called when a player right click in the inventory. Can also be to drop the cursor item
     *
     * @param player the player who clicked
     * @param slot   the slot number
     * @return true if the click hasn't been cancelled, false otherwise
     */
    boolean rightClick(Player player, int slot);

    /**
     * Called when a player shift click in the inventory
     *
     * @param player the player who clicked
     * @param slot   the slot number
     * @return true if the click hasn't been cancelled, false otherwise
     */
    boolean shiftClick(Player player, int slot); // shift + left/right click have the same behavior

    /**
     * Called when a player held click in the inventory
     *
     * @param player the player who clicked
     * @param slot   the slot number
     * @param key    the held slot (0-8) pressed
     * @return true if the click hasn't been cancelled, false otherwise
     */
    boolean changeHeld(Player player, int slot, int key);

    boolean middleClick(Player player, int slot);

    /**
     * Called when a player press the drop button
     *
     * @param player the player who clicked
     * @param mode
     * @param slot   the slot number
     * @param button -999 if clicking outside, normal if he is not
     * @return true if the drop hasn't been cancelled, false otherwise
     */
    boolean drop(Player player, int mode, int slot, int button);

    boolean dragging(Player player, int slot, int button);

    /**
     * Called when a player double click in the inventory
     *
     * @param player the player who clicked
     * @param slot   the slot number
     * @return true if the click hasn't been cancelled, false otherwise
     */
    boolean doubleClick(Player player, int slot);

    default void callClickEvent(Player player, Inventory inventory, int slot,
                                ClickType clickType, ItemStack clicked, ItemStack cursor) {
        InventoryClickEvent inventoryClickEvent = new InventoryClickEvent(player, inventory, slot, clickType, clicked, cursor);
        player.callEvent(InventoryClickEvent.class, inventoryClickEvent);
    }

}
