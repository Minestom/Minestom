package net.minestom.server.inventory.click;

import net.minestom.server.item.ItemStack;

public final class InventoryClickResult {
    private ItemStack clicked;
    private ItemStack cursor;
    private boolean cancel;

    public InventoryClickResult(ItemStack clicked, ItemStack cursor) {
        this.clicked = clicked;
        this.cursor = cursor;
    }

    public ItemStack getClicked() {
        return clicked;
    }

    void setClicked(ItemStack clicked) {
        this.clicked = clicked;
    }

    public ItemStack getCursor() {
        return cursor;
    }

    void setCursor(ItemStack cursor) {
        this.cursor = cursor;
    }

    public boolean isCancel() {
        return cancel;
    }

    void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    InventoryClickResult cancelled() {
        setCancel(true);
        return this;
    }
}
