package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player open an {@link Inventory}.
 * <p>
 * Executed by {@link Player#openInventory(Inventory)}.
 */
public class InventoryOpenEvent implements InventoryEvent, PlayerInstanceEvent, CancellableEvent {

    private final Player player;
    private Inventory inventory;

    private boolean cancelled;

    public InventoryOpenEvent(@NotNull Inventory inventory, @NotNull Player player) {
        this.inventory = inventory;
        this.player = player;
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
     * Gets the inventory to open.
     *
     * @return the inventory to open
     */
    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    /**
     * Changes the inventory to open.
     * <p>
     * To do not open any inventory see {@link #setCancelled(boolean)}.
     *
     * @param inventory the inventory to open
     */
    public void setInventory(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
