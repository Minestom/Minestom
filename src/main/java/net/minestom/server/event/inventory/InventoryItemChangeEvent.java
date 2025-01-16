package net.minestom.server.event.inventory;

import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.RecursiveEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when {@link AbstractInventory#setItemStack(int, ItemStack)} is being invoked.
 * This event cannot be cancelled and items related to the change are already moved.
 */
public record InventoryItemChangeEvent(@NotNull AbstractInventory inventory, int slot,
                                       @NotNull ItemStack previousItem, @NotNull ItemStack newItem) implements InventoryEvent, RecursiveEvent {

    /**
     * Gets the changed slot number.
     *
     * @return the changed slot number.
     */
    @Override
    public int slot() {
        return slot;
    }

    /**
     * Gets a previous item that was on changed slot.
     *
     * @return a previous item that was on changed slot.
     */
    @Override
    public @NotNull ItemStack previousItem() {
        return previousItem;
    }

    /**
     * Gets a new item on a changed slot.
     *
     * @return a new item on a changed slot.
     */
    @Override
    public @NotNull ItemStack newItem() {
        return newItem;
    }

    @Override
    public @NotNull AbstractInventory inventory() {
        return inventory;
    }
}
