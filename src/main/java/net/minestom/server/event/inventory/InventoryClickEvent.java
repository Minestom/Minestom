package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.Click;
import org.jetbrains.annotations.NotNull;

/**
 * Called after {@link InventoryPreClickEvent} and before {@link InventoryPostClickEvent}.
 */
public class InventoryClickEvent implements InventoryEvent, PlayerInstanceEvent, CancellableEvent {

    private final PlayerInventory playerInventory;
    private final Inventory inventory;
    private final Player player;
    private final Click.Info info;
    private Click.Result changes;

    private boolean cancelled;

    public InventoryClickEvent(@NotNull PlayerInventory playerInventory, @NotNull Inventory inventory,
                               @NotNull Player player, @NotNull Click.Info info, @NotNull Click.Result changes) {
        this.playerInventory = playerInventory;
        this.inventory = inventory;
        this.player = player;
        this.info = info;
        this.changes = changes;
    }

    /**
     * Gets the player who is trying to click on the inventory.
     *
     * @return the player who clicked
     */
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * Gets the info about the click that occurred. This is enough to fully describe the click.
     *
     * @return the click info
     */
    public @NotNull Click.Info getClickInfo() {
        return info;
    }

    /**
     * Gets the changes that will occur as a result of this click.
     *
     * @return the changes
     */
    public @NotNull Click.Result getChanges() {
        return changes;
    }

    /**
     * Updates the changes that will occur as a result of this click.
     *
     * @param changes the new results
     */
    public void setChanges(@NotNull Click.Result changes) {
        this.changes = changes;
    }

    /**
     * Gets the player inventory that was involved with the click.
     *
     * @return the player inventory
     */
    public @NotNull PlayerInventory getPlayerInventory() {
        return playerInventory;
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
