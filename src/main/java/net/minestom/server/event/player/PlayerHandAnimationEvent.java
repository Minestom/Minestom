package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the player swings his hand.
 */
public class PlayerHandAnimationEvent implements PlayerInstanceEvent, CancellableEvent {

    private final Player player;
    private final PlayerHand hand;

    private boolean cancelled;

    public PlayerHandAnimationEvent(@NotNull Player player, @NotNull PlayerHand hand) {
        this.player = player;
        this.hand = hand;
    }

    /**
     * Gets the hand used.
     *
     * @return the hand
     */
    @NotNull
    public PlayerHand getHand() {
        return hand;
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
    public @NotNull Player getPlayer() {
        return player;
    }
}
