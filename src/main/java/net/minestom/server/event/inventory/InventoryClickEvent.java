package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called after {@link InventoryPreClickEvent}, this event cannot be cancelled and items related to the click
 * are already moved.
 */
public record InventoryClickEvent(@NotNull AbstractInventory inventory, @NotNull Player player,
                                  int slot, @NotNull ClickType clickType,
                                  @NotNull ItemStack clickedItem, @NotNull ItemStack cursorItem) implements InventoryEvent, PlayerInstanceEvent {

    /**
     * Gets the player who clicked in the inventory.
     *
     * @return the player who clicked in the inventory
     */
    public @NotNull Player player() {
        return player;
    }

    /**
     * Gets the clicked slot number.
     *
     * @return the clicked slot number
     */
    public int slot() {
        return slot;
    }

    /**
     * Gets the click type.
     *
     * @return the click type
     */
    public @NotNull ClickType clickType() {
        return clickType;
    }

    /**
     * Gets the clicked item.
     *
     * @return the clicked item
     */
    public @NotNull ItemStack clickedItem() {
        return clickedItem;
    }

    /**
     * Gets the item in the player cursor.
     *
     * @return the cursor item
     */
    public @NotNull ItemStack cursorItem() {
        return cursorItem;
    }

    @Override
    public @NotNull AbstractInventory inventory() {
        return inventory;
    }
}
