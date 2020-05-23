package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.utils.validate.Check;

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

    public void setInventory(Inventory inventory) {
        Check.notNull(inventory, "Inventory cannot be null!");
        this.inventory = inventory;
    }
}
