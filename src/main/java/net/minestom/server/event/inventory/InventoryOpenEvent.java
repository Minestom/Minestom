package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a player open an {@link Inventory}.
 * <p>
 * Executed by {@link Player#openInventory(Inventory)}.
 */
public class InventoryOpenEvent extends CancellableEvent {

    private final Player player;
    private Inventory inventory;

    public InventoryOpenEvent(@NotNull Player player, @Nullable Inventory inventory) {
        this.player = player;
        this.inventory = inventory;
    }

    /**
     * Gets the player who opens the inventory.
     *
     * @return the player who opens the inventory
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the inventory to open, this could have been change by the {@link #setInventory(Inventory)}.
     *
     * @return the inventory to open, null to just close the current inventory if any
     */
    @Nullable
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Changes the inventory to open.
     * <p>
     * To do not open any inventory see {@link #setCancelled(boolean)}.
     *
     * @param inventory the inventory to open
     */
    public void setInventory(@Nullable Inventory inventory) {
        this.inventory = inventory;
    }
}
