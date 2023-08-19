package net.minestom.server.event.trait;

import net.minestom.server.event.Event;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.ContainerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents any event inside an {@link ContainerInventory}.
 */
public interface InventoryEvent extends Event {

    /**
     * Gets the inventory.
     *
     * @return the inventory, null if this is a player's inventory
     * @deprecated use {@link #getEventInventory()} instead
     */
    @Deprecated
    default @Nullable Inventory getInventory() {
        var inventory = getEventInventory();
        return inventory instanceof ContainerInventory ? inventory : null;
    }

    /**
     * Gets the inventory.
     *
     * @return the inventory (may be a player inventory)
     */
    @NotNull Inventory getEventInventory();
}
