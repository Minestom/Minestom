package net.minestom.server.inventory.condition;

import net.minestom.server.item.ItemStack;

/**
 * Used by {@link InventoryCondition} to step in inventory click processing.
 */
public class InventoryConditionResult {

    private ItemStack clickedItem, cursorItem;
    private boolean cancel;

    public InventoryConditionResult(ItemStack clickedItem, ItemStack cursorItem) {
        this.clickedItem = clickedItem;
        this.cursorItem = cursorItem;
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

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }
}
