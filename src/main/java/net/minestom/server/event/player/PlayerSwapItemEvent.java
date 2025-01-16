package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is trying to swap his main and off hand item.
 */
public record PlayerSwapItemEvent(@NotNull Player player, @NotNull ItemStack mainHandItem, @NotNull ItemStack offHandItem, boolean cancelled) implements PlayerInstanceEvent, CancellableEvent<PlayerSwapItemEvent> {

    public PlayerSwapItemEvent(@NotNull Player player, @NotNull ItemStack mainHandItem, @NotNull ItemStack offHandItem) {
        this(player, mainHandItem, offHandItem, false);
    }

    /**
     * Gets the item which will be in player main hand after the event.
     *
     * @return the item in main hand
     */
    @Override
    public @NotNull ItemStack mainHandItem() {
        return mainHandItem;
    }

    /**
     * Gets the item which will be in player off hand after the event.
     *
     * @return the item in off hand
     */
    @Override
    public @NotNull ItemStack offHandItem() {
        return offHandItem;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<PlayerSwapItemEvent> {
        private final Player player;
        private ItemStack mainHandItem;
        private ItemStack offHandItem;
        private boolean cancelled;

        public Mutator(PlayerSwapItemEvent event) {
            this.player = event.player;
            this.mainHandItem = event.mainHandItem;
            this.offHandItem = event.offHandItem;
            this.cancelled = event.cancelled;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }

        /**
         * Gets the item which will be in player main hand after the event.
         *
         * @return the item in main hand
         */
        public @NotNull ItemStack getMainHandItem() {
            return mainHandItem;
        }

        /**
         * Changes the item which will be in the player main hand.
         *
         * @param mainHandItem the main hand item
         */
        public void setMainHandItem(@NotNull ItemStack mainHandItem) {
            this.mainHandItem = mainHandItem;
        }

        /**
         * Gets the item which will be in player off hand after the event.
         *
         * @return the item in off hand
         */
        public @NotNull ItemStack getOffHandItem() {
            return offHandItem;
        }

        /**
         * Changes the item which will be in the player off hand.
         *
         * @param offHandItem the off hand item
         */
        public void setOffHandItem(@NotNull ItemStack offHandItem) {
            this.offHandItem = offHandItem;
        }

        @Override
        public @NotNull PlayerSwapItemEvent mutated() {
            return new PlayerSwapItemEvent(this.player, this.mainHandItem, this.offHandItem, this.cancelled);
        }
    }
}
