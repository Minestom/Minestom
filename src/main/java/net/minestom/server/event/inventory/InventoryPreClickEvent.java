package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.*;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called before {@link InventoryClickEvent}, used to potentially cancel the click.
 */
public class InventoryPreClickEvent implements MultipleItemEvent, InventoryEvent, PlayerEvent, EntityInstanceEvent, CancellableEvent {

    private final Inventory inventory;
    private final Player player;
    private final int slot;
    private final ClickType clickType;
    private final ItemStack[] itemStacks;

    private boolean cancelled;

    public InventoryPreClickEvent(@Nullable Inventory inventory,
                                  @NotNull Player player,
                                  int slot, @NotNull ClickType clickType,
                                  @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        this.inventory = inventory;
        this.player = player;
        this.slot = slot;
        this.clickType = clickType;
        this.itemStacks = new ItemStack[] { clicked, cursor };
    }

    /**
     * Gets the player who is trying to click on the inventory.
     *
     * @return the player who clicked
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the clicked slot number.
     *
     * @return the clicked slot number
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Gets the click type.
     *
     * @return the click type
     */
    @NotNull
    public ClickType getClickType() {
        return clickType;
    }

    /**
     * Gets the item who have been clicked.
     *
     * @return the clicked item
     */
    @NotNull
    public ItemStack getClickedItem() {
        return itemStacks[0];
    }

    /**
     * Changes the clicked item.
     *
     * @param clickedItem the clicked item
     */
    public void setClickedItem(@NotNull ItemStack clickedItem) {
        this.itemStacks[0] = clickedItem;
    }

    /**
     * Gets the item who was in the player cursor.
     *
     * @return the cursor item
     */
    @NotNull
    public ItemStack getCursorItem() { return itemStacks[1]; }

    /**
     * Changes the cursor item.
     *
     * @param cursorItem the cursor item
     */
    public void setCursorItem(@NotNull ItemStack cursorItem) { this.itemStacks[1] = cursorItem; }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @Nullable Inventory getInventory() {
        return inventory;
    }

    @Override
    public @NotNull ItemStack[] getItemStacks() { return itemStacks; }
}
