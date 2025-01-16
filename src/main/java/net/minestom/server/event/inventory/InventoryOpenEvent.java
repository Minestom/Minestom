package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player open an {@link AbstractInventory}.
 * <p>
 * Executed by {@link Player#openInventory(Inventory)}.
 */
public record InventoryOpenEvent(@NotNull AbstractInventory inventory, @NotNull Player player, boolean cancelled) implements InventoryEvent, PlayerInstanceEvent, CancellableEvent<InventoryOpenEvent> {

    public InventoryOpenEvent(@NotNull AbstractInventory inventory, @NotNull Player player) {
        this(inventory, player, false);
    }

    /**
     * Gets the player who opens the inventory.
     *
     * @return the player who opens the inventory
     */
    @Override
    public @NotNull Player player() {
        return player;
    }

    /**
     * Gets the inventory to open, this could have been change by the {@link Mutator#setInventory(AbstractInventory)}.
     *
     * @return the inventory to open, null to just close the current inventory if any
     */
    @Override
    public @NotNull AbstractInventory inventory() {
        return inventory;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<InventoryOpenEvent> {
        private AbstractInventory inventory;
        private final Player player;
        private boolean cancelled;

        public Mutator(InventoryOpenEvent event) {
            this.inventory = event.inventory;
            this.player = event.player;
            this.cancelled = event.cancelled;
        }

        /**
         * Gets the inventory to open, this could have been change by the {@link #setInventory(AbstractInventory)}.
         *
         * @return the inventory to open, null to just close the current inventory if any
         */
        public @NotNull AbstractInventory getInventory() {
            return inventory;
        }

        /**
         * Changes the inventory to open.
         * <p>
         * To do not open any inventory see {@link #setCancelled(boolean)}.
         *
         * @param inventory the inventory to open
         */
        public void setInventory(@NotNull AbstractInventory inventory) {
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

        @Override
        public @NotNull InventoryOpenEvent mutated() {
            return new InventoryOpenEvent(this.inventory, this.player, this.cancelled);
        }
    }
}
