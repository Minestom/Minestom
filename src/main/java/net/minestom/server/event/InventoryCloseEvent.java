package net.minestom.server.event;

import net.minestom.server.inventory.Inventory;

public class InventoryCloseEvent extends Event {

    private Inventory inventory;

    public InventoryCloseEvent(Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * @return the closed inventory, null if this is the player inventory
     */
    public Inventory getInventory() {
        return inventory;
    }
}
