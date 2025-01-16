package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the player swings his hand.
 */
public record PlayerHandAnimationEvent(@NotNull Player player, @NotNull PlayerHand hand, boolean cancelled) implements PlayerInstanceEvent, CancellableEvent<PlayerHandAnimationEvent> {

    public PlayerHandAnimationEvent(@NotNull Player player, @NotNull PlayerHand hand) {
        this(player, hand, false);
    }

    /**
     * Gets the hand used.
     *
     * @return the hand
     */
    @Override
    public @NotNull PlayerHand hand() {
        return hand;
    }


    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<PlayerHandAnimationEvent> {
        private final Player player;
        private final PlayerHand hand;

        private boolean cancelled;

        public Mutator(PlayerHandAnimationEvent event) {
            this.player = event.player;
            this.hand = event.hand;

            this.cancelled = event.cancelled;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        @Override
        public @NotNull PlayerHandAnimationEvent mutated() {
            return new PlayerHandAnimationEvent(this.player, this.hand, this.cancelled);
        }
    }
}
