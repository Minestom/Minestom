package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.inventory.Inventory;

public class InventoryOpenEvent extends CancellableEvent {

    private final Player player;
    private Inventory inventory;

    public InventoryOpenEvent(Player player, Inventory inventory) {
        this.player = player;
        this.inventory = inventory;
    }

    /**
     * Get the player who opens the inventory
     *
     * @return the player who opens the inventory
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the inventory to open, this could have been change by the {@link #setInventory(Inventory)}
     *
     * @return the inventory to open
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Change the inventory to open.
     * to do not open any inventory see {@link #setCancelled(boolean)}
     *
     * @param inventory the inventory to open
     * @throws NullPointerException if {@code inventory} is null
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
