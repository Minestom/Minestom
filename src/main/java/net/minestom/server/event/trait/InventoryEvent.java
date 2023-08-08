package net.minestom.server.event.trait;

import net.minestom.server.event.Event;
import net.minestom.server.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Represents any event inside an {@link Inventory}.
 */
public interface InventoryEvent extends Event {

    /**
     * Gets the inventory.
     *
     * @return the inventory (may be a player inventory)
     */
    @NotNull Inventory getInventory();
}
