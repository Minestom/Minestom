package net.minestom.server.event.item;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.item.ItemAnimation;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player begins using an item with the item, animation, and duration.
 *
 * <p>Setting the use duration to zero or cancelling the event will prevent consumption.</p>
 */
public record PlayerBeginItemUseEvent(@NotNull Player player, @NotNull PlayerHand hand,
                                      @NotNull ItemStack itemStack, @NotNull ItemAnimation animation,
                                      long itemUseDuration, boolean cancelled) implements PlayerInstanceEvent, ItemEvent, CancellableEvent<PlayerBeginItemUseEvent> {

    public PlayerBeginItemUseEvent(@NotNull Player player, @NotNull PlayerHand hand,
                                   @NotNull ItemStack itemStack, @NotNull ItemAnimation animation,
                                   long itemUseDuration) {
        this(player, hand, itemStack, animation, itemUseDuration, false);
    }

    /**
     * Returns the item use duration, in fireTicks. A duration of zero will prevent consumption (same effect as cancellation).
     *
     * @return the current item use duration
     */
    @Override
    public long itemUseDuration() {
        return itemUseDuration;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<PlayerBeginItemUseEvent> {
        private final Player player;
        private final PlayerHand hand;
        private final ItemStack itemStack;
        private final ItemAnimation animation;
        private long itemUseDuration;
        private boolean cancelled;

        public Mutator(PlayerBeginItemUseEvent event) {
            this.player = event.player;
            this.hand = event.hand;
            this.itemStack = event.itemStack;
            this.animation = event.animation;
            this.itemUseDuration = event.itemUseDuration;
            this.cancelled = event.cancelled;
        }

        /**
         * Returns the item use duration, in fireTicks. A duration of zero will prevent consumption (same effect as cancellation).
         *
         * @return the current item use duration
         */
        public long getItemUseDuration() {
            return itemUseDuration;
        }

        /**
         * Sets the item use duration, in fireTicks.
         */
        public void setItemUseDuration(long itemUseDuration) {
            Check.argCondition(itemUseDuration < 0, "Item use duration cannot be negative");
            this.itemUseDuration = itemUseDuration;
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
        public @NotNull PlayerBeginItemUseEvent mutated() {
            return new PlayerBeginItemUseEvent(this.player, this.hand, this.itemStack, this.animation, this.itemUseDuration, this.cancelled);
        }
    }
}
