package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.Click;
import org.jetbrains.annotations.NotNull;

/**
 * Called after {@link InventoryClickEvent}, this event cannot be cancelled and items related to the click
 * are already moved.
 */
public class InventoryPostClickEvent implements InventoryEvent, PlayerInstanceEvent {

    private final Player player;
    private final Inventory inventory;
    private final Click.Info info;
    private final Click.Result changes;

    public InventoryPostClickEvent(@NotNull Player player, @NotNull Inventory inventory, @NotNull Click.Info info, @NotNull Click.Result changes) {
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
    public @NotNull Click.Info getClickInfo() {
        return info;
    }

    /**
     * Gets the changes that occurred as a result of this click.
     *
     * @return the changes
     */
    public @NotNull Click.Result getChanges() {
        return changes;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
