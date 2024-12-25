package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.click.Click;
import org.jetbrains.annotations.NotNull;

/**
 * Called before {@link InventoryClickEvent}, used to potentially cancel the click.
 */
public class InventoryPreClickEvent implements InventoryEvent, PlayerInstanceEvent, CancellableEvent {

    private final AbstractInventory inventory;
    private final Player player;
    private Click click;

    private boolean cancelled;

    public InventoryPreClickEvent(@NotNull AbstractInventory inventory,
                                  @NotNull Player player,
                                  @NotNull Click click) {
        this.inventory = inventory;
        this.player = player;
        this.click = click;
    }

    /**
     * Gets the player who is trying to click on the inventory.
     *
     * @return the player who clicked
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the player's click.
     */
    public @NotNull Click getClick() {
        return click;
    }

    /**
     * Sets the player's click.
     */
    public void setClick(@NotNull Click click) {
        this.click = click;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull AbstractInventory getInventory() {
        return inventory;
    }
}
