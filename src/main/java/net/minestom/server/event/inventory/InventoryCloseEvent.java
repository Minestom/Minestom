package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.inventory.Inventory;

public class InventoryCloseEvent extends Event {

    private final Player player;
    private final Inventory inventory;
    private Inventory newInventory;

    public InventoryCloseEvent(Player player, Inventory inventory) {
        this.player = player;
        this.inventory = inventory;
    }

    /**
     * Get the player who closed the inventory
     *
     * @return the player who closed the inventory
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the closed inventory
     *
     * @return the closed inventory, null if this is the player inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Get the new inventory to open
     *
     * @return the new inventory to open, null if there isn't any
     */
    public Inventory getNewInventory() {
        return newInventory;
    }

    /**
     * Can be used to open a new inventory after closing the previous one
     *
     * @param newInventory the inventory to open, null to do not open any
     */
    public void setNewInventory(Inventory newInventory) {
        this.newInventory = newInventory;
    }
}
