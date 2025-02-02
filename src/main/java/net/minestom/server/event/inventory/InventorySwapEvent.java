package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player open an {@link AbstractInventory}, but had already an open inventory of any kind.
 * If you call {@link Player#openInventory(Inventory)}, the client will just swap the inventories.
 * The biggest advantage by swapping instead of closing and opening inventories is,
 * that the client doesn't reset the cursor position within the inventory.
 * <p>
 * Executed by {@link Player#openInventory(Inventory)} if another inventory was already open.
 */
public class InventorySwapEvent implements PlayerInstanceEvent {

    private final AbstractInventory oldInventory;
    private final AbstractInventory newInventory;
    private final Player player;

    public InventorySwapEvent(@NotNull AbstractInventory oldInventory, @NotNull AbstractInventory newInventory, @NotNull Player player) {
        this.oldInventory = oldInventory;
        this.newInventory = newInventory;
        this.player = player;
    }

    /**
     * Gets the player who opens the inventory.
     *
     * @return the player who opens the inventory
     */
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * Gets the old inventory, which was closed by the client.
     *
     * @return the old inventory
     */
    public @NotNull AbstractInventory getOldInventory() {
        return oldInventory;
    }

    /**
     * Gets the new inventory, which was opened by the client.
     *
     * @return the new inventory
     */
    public @NotNull AbstractInventory getNewInventory() {
        return newInventory;
    }
}
