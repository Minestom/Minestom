package net.minestom.server.event.inventory;

import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.RecursiveEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when {@link AbstractInventory#safeItemInsert(int, ItemStack)} is being invoked.
 * This event cannot be cancelled and items related to the change are already moved.
 *
 * @see PlayerInventoryItemChangeEvent
 */
@SuppressWarnings("JavadocReference")
public class InventoryItemChangeEvent implements InventoryEvent, RecursiveEvent {

    private final Inventory inventory;
    private final int slot;
    private final ItemStack previousItem;
    private final ItemStack newItem;

    public InventoryItemChangeEvent(@Nullable Inventory inventory, int slot,
                                    @NotNull ItemStack previousItem, @NotNull ItemStack newItem) {
        this.inventory = inventory;
        this.slot = slot;
        this.previousItem = previousItem;
        this.newItem = newItem;
    }

    /**
     * Gets the changed slot number.
     *
     * @return the changed slot number.
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Gets a previous item that was on changed slot.
     *
     * @return a previous item that was on changed slot.
     */
    public @NotNull ItemStack getPreviousItem() {
        return previousItem;
    }

    /**
     * Gets a new item on a changed slot.
     *
     * @return a new item on a changed slot.
     */
    public @NotNull ItemStack getNewItem() {
        return newItem;
    }

    @Override
    public @Nullable Inventory getInventory() {
        return inventory;
    }
}
