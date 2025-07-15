package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import org.jspecify.annotations.Nullable;

/**
 * Called when an {@link AbstractInventory} is closed by a player.
 */
public class InventoryCloseEvent implements InventoryEvent, PlayerInstanceEvent {

    private final AbstractInventory inventory;
    private final Player player;
    private final boolean fromClient;
    private Inventory newInventory;

    public InventoryCloseEvent(AbstractInventory inventory, Player player, boolean fromClient) {
        this.inventory = inventory;
        this.player = player;
        this.fromClient = fromClient;
    }

    /**
     * Gets the player who closed the inventory.
     *
     * @return the player who closed the inventory
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets whether the client closed the inventory or the server did.
     *
     * @return true if the client closed the inventory, false if the server closed the inventory
     */
    public boolean isFromClient() {
        return fromClient;
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

    @Override
    public AbstractInventory getInventory() {
        return inventory;
    }
}
