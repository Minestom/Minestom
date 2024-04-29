package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an {@link Inventory} is closed by a player.
 */
public class InventoryCloseEvent implements InventoryEvent, PlayerInstanceEvent, CancellableEvent {

    private final Inventory inventory;
    private final Player player;
    private boolean cancelled;

    public InventoryCloseEvent(@NotNull Inventory inventory, @NotNull Player player) {
        this.inventory = inventory;
        this.player = player;
    }

    /**
     * Gets the player who closed the inventory.
     *
     * @return the player who closed the inventory
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
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
