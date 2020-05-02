package net.minestom.server.event;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;

public class InventoryPreClickEvent extends CancellableEvent {

    private Inventory inventory;
    private int slot;
    private ClickType clickType;
    private ItemStack clickedItem;
    private ItemStack cursorItem;

    public InventoryPreClickEvent(Inventory inventory, int slot, ClickType clickType, ItemStack clicked, ItemStack cursor) {
        this.inventory = inventory;
        this.slot = slot;
        this.clickType = clickType;
        this.clickedItem = clicked;
        this.cursorItem = cursor;
    }

    /**
     * Can be null if the clicked inventory is the player one
     *
     * @return
     */
    public Inventory getInventory() {
        return inventory;
    }

    public int getSlot() {
        return slot;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public ItemStack getClickedItem() {
        return clickedItem;
    }

    public void setClickedItem(ItemStack clickedItem) {
        this.clickedItem = clickedItem;
    }

    public ItemStack getCursorItem() {
        return cursorItem;
    }

    public void setCursorItem(ItemStack cursorItem) {
        this.cursorItem = cursorItem;
    }
}
