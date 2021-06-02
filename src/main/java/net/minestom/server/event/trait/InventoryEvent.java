package net.minestom.server.event.trait;

import net.minestom.server.event.Event;
import net.minestom.server.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

public interface InventoryEvent extends Event {

    /**
     * Gets the inventory.
     *
     * @return the inventory, null if this is a player's inventory
     */
    @Nullable Inventory getInventory();
}