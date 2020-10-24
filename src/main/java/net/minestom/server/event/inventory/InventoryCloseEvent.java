package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when an {@link Inventory} is closed by a player.
 */
public class InventoryCloseEvent extends Event {

    private final Player player;
    private final Inventory inventory;
    private Inventory newInventory;

    public InventoryCloseEvent(@NotNull Player player, @Nullable Inventory inventory) {
        this.player = player;
        this.inventory = inventory;
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

    /**
     * Gets the closed inventory.
     *
     * @return the closed inventory, null if this is the player inventory
     */
    @Nullable
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Gets the new inventory to open.
     *
     * @return the new inventory to open, null if there isn't any
     */
    @Nullable
    public Inventory getNewInventory() {
        return newInventory;
    }

    /**
     * Can be used to open a new inventory after closing the previous one.
     *
     * @param newInventory the inventory to open, null to do not open any
     */
    public void setNewInventory(@Nullable Inventory newInventory) {
        this.newInventory = newInventory;
    }
}
