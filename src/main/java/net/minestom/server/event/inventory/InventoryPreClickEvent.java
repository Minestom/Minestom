package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.item.ItemStackUtils;

public class InventoryPreClickEvent extends CancellableEvent {

    private final Player player;
    private final Inventory inventory;
    private final int slot;
    private final ClickType clickType;
    private ItemStack clickedItem;
    private ItemStack cursorItem;

    public InventoryPreClickEvent(Player player, Inventory inventory, int slot, ClickType clickType, ItemStack clicked, ItemStack cursor) {
        this.player = player;
        this.inventory = inventory;
        this.slot = slot;
        this.clickType = clickType;
        this.clickedItem = clicked;
        this.cursorItem = cursor;
    }

    /**
     * Get the player who is trying to click on the inventory
     *
     * @return the player who clicked
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Can be null if the clicked inventory is the player one
     *
     * @return the inventory where the click happened, null if this is the player's inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Get the clicked slot number
     *
     * @return the clicked slot number
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Get the click type
     *
     * @return the click type
     */
    public ClickType getClickType() {
        return clickType;
    }

    /**
     * Get the item who have been clicked
     *
     * @return the clicked item
     */
    public ItemStack getClickedItem() {
        return clickedItem;
    }

    /**
     * Change the clicked item
     *
     * @param clickedItem the clicked item
     */
    public void setClickedItem(ItemStack clickedItem) {
        this.clickedItem = ItemStackUtils.notNull(clickedItem);
    }

    /**
     * Get the item who was in the player cursor
     *
     * @return the cursor item
     */
    public ItemStack getCursorItem() {
        return cursorItem;
    }

    /**
     * Change the cursor item
     *
     * @param cursorItem the cursor item
     */
    public void setCursorItem(ItemStack cursorItem) {
        this.cursorItem = ItemStackUtils.notNull(cursorItem);
    }
}
