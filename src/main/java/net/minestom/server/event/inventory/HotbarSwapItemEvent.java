package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is trying to swap his main and off hand item.
 */
public class HotbarSwapItemEvent implements InventoryEvent, PlayerInstanceEvent, CancellableEvent {

    private final Player player;
    private final PlayerInventory playerInventory;
    private final Inventory inventory;

    private final int clickedSlot;
    private final int hotbarSlot;

    private ItemStack swappedItem, hotbarItem;

    private boolean cancelled;

    public HotbarSwapItemEvent(@NotNull Player player, @NotNull PlayerInventory playerInventory, @NotNull Inventory inventory,
                                int clickedSlot, int hotbarSlot, @NotNull ItemStack swappedItem, @NotNull ItemStack hotbarItem) {
        this.player = player;
        this.playerInventory = playerInventory;
        this.inventory = inventory;

        this.clickedSlot = clickedSlot;
        this.hotbarSlot = hotbarSlot;

        this.swappedItem = swappedItem;
        this.hotbarItem = hotbarItem;
    }

    /**
     * Gets the slot in {@link #getInventory()} that will be swapped with the offhand.
     *
     * @return the non-offhand slot
     */
    public int getClickedSlot() {
        return clickedSlot;
    }

    public int getHotbarSlot() {
        return hotbarSlot;
    }

    /**
     * Gets the item which will be in player main hand after the event.
     *
     * @return the item in main hand
     */
    @NotNull
    public ItemStack getSwappedItem() {
        return swappedItem;
    }

    /**
     * Changes the item which will be in the player main hand.
     *
     * @param swappedItem the main hand item
     */
    public void setSwappedItem(@NotNull ItemStack swappedItem) {
        this.swappedItem = swappedItem;
    }

    /**
     * Gets the item which will be in player off hand after the event.
     *
     * @return the item in off hand
     */
    @NotNull
    public ItemStack getHotbarItem() {
        return hotbarItem;
    }

    /**
     * Changes the item which will be in the player off hand.
     *
     * @param hotbarItem the off hand item
     */
    public void setHotbarItem(@NotNull ItemStack hotbarItem) {
        this.hotbarItem = hotbarItem;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public @NotNull PlayerInventory getPlayerInventory() {
        return playerInventory;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
