package net.minestom.server.event.inventory;

import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.RecursiveEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a slot was changed in an inventory.
 * This event cannot be cancelled and items related to the change are already moved.
 */
public class InventoryItemChangeEvent implements InventoryEvent, RecursiveEvent {

    private final Inventory inventory;
    private final int slot;
    private final ItemStack previousItem;
    private final ItemStack newItem;

    public InventoryItemChangeEvent(@NotNull Inventory inventory, int slot,
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
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
