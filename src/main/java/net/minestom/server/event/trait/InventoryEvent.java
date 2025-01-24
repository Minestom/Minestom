package net.minestom.server.event.trait;

import net.minestom.server.event.Event;
import net.minestom.server.inventory.AbstractInventory;
import org.jetbrains.annotations.NotNull;

/**
 * Represents any event inside an {@link AbstractInventory}.
 */
public interface InventoryEvent extends Event {

    /**
     * Gets the inventory that was clicked.
     */
    @NotNull AbstractInventory getInventory();
}
