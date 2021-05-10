package net.minestom.server.inventory.click;

import net.minestom.server.item.ItemStack;

public class InventoryClickResult {

    private ItemStack clicked;
    private ItemStack cursor;

    private boolean playerInventory;

    private boolean cancel;
    private boolean refresh;

    public InventoryClickResult(ItemStack clicked, ItemStack cursor) {
        this.clicked = clicked;
        this.cursor = cursor;
    }

    public ItemStack getClicked() {
        return clicked;
    }

    protected void setClicked(ItemStack clicked) {
        this.clicked = clicked;
    }

    public ItemStack getCursor() {
        return cursor;
    }

    protected void setCursor(ItemStack cursor) {
        this.cursor = cursor;
    }

    public boolean isPlayerInventory() {
        return playerInventory;
    }

    protected void setPlayerInventory(boolean playerInventory) {
        this.playerInventory = playerInventory;
    }

    public boolean isCancel() {
        return cancel;
    }

    protected void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public boolean doRefresh() {
        return refresh;
    }

    protected void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }
}
