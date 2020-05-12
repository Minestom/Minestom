package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.inventory.Inventory;

public class InventoryOpenEvent extends CancellableEvent {

    private Player player;
    private Inventory inventory;

    public InventoryOpenEvent(Player player, Inventory inventory) {
        this.player = player;
        this.inventory = inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
