package net.minestom.server.event.inventory;

import net.minestom.server.event.Event;
import net.minestom.server.inventory.Inventory;

public class InventoryCloseEvent extends Event {

    private Inventory inventory;
    private Inventory newInventory;

    public InventoryCloseEvent(Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * @return the closed inventory, null if this is the player inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    public Inventory getNewInventory() {
        return newInventory;
    }

    /**
     * Can be used to open a new inventory after closing the previous one
     *
     * @param newInventory the inventory to open, can be null
     */
    public void setNewInventory(Inventory newInventory) {
        this.newInventory = newInventory;
    }
}
