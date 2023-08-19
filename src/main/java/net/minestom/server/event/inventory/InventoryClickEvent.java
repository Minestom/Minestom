package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.inventory.click.ClickResult;
import org.jetbrains.annotations.NotNull;

/**
 * Called after {@link InventoryPreClickEvent}, this event cannot be cancelled and items related to the click
 * are already moved.
 */
public class InventoryClickEvent implements InventoryEvent, PlayerInstanceEvent {

    private final Player player;
    private final Inventory inventory;
    private final ClickInfo info;
    private final ClickResult changes;

    public InventoryClickEvent(@NotNull Player player, @NotNull Inventory inventory, @NotNull ClickInfo info, @NotNull ClickResult changes) {
        this.player = player;
        this.inventory = inventory;
        this.info = info;
        this.changes = changes;
    }

    /**
     * Gets the player who clicked in the inventory.
     *
     * @return the player who clicked in the inventory
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the info about the click that was already processed.
     *
     * @return the click info
     */
    public @NotNull ClickInfo getClickInfo() {
        return info;
    }

    /**
     * Gets the changes that occurred as a result of this click.
     *
     * @return the changes
     */
    public @NotNull ClickResult getChanges() {
        return changes;
    }

    @Override
    public @NotNull Inventory getEventInventory() {
        return inventory;
    }
}
