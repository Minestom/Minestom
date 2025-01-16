package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.MutableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutator;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when an {@link AbstractInventory} is closed by a player.
 */
public record InventoryCloseEvent(@NotNull AbstractInventory inventory, @NotNull Player player, boolean fromClient, @Nullable Inventory newInventory) implements InventoryEvent, PlayerInstanceEvent, MutableEvent<InventoryCloseEvent> {

    public InventoryCloseEvent(@NotNull AbstractInventory inventory, @NotNull Player player, boolean fromClient) {
        this(inventory, player, fromClient, null);
    }

    /**
     * Gets the player who closed the inventory.
     *
     * @return the player who closed the inventory
     */
    @Override
    public @NotNull Player player() {
        return player;
    }

    /**
     * Gets whether the client closed the inventory or the server did.
     *
     * @return true if the client closed the inventory, false if the server closed the inventory
     */
    @Override
    public boolean fromClient() {
        return fromClient;
    }

    /**
     * Gets the new inventory to open.
     *
     * @return the new inventory to open, null if there isn't any
     */
    @Override
    public @Nullable Inventory newInventory() {
        return newInventory;
    }

    /**
     * The Original inventory that was closed.
     *
     * @return the inventory that was closed
     */
    @Override
    public @NotNull AbstractInventory inventory() {
        return inventory;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutator<InventoryCloseEvent> {
        private final AbstractInventory inventory;
        private final Player player;
        private final boolean fromClient;
        private Inventory newInventory;

        public Mutator(InventoryCloseEvent event) {
            this.inventory = event.inventory;
            this.player = event.player;
            this.fromClient = event.fromClient;
            this.newInventory = event.newInventory;
        }

        /**
         * Gets the new inventory to open.
         *
         * @return the new inventory to open, null if there isn't any
         */
        public @Nullable Inventory getNewInventory() {
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
        public @NotNull InventoryCloseEvent mutated() {
            return new InventoryCloseEvent(this.inventory, this.player, this.fromClient, this.newInventory);
        }
    }
}
