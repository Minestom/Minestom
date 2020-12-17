package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the player swings his hand.
 */
public class PlayerHandAnimationEvent extends PlayerEvent implements CancellableEvent {

    private final Player.Hand hand;

    private boolean cancelled;

    public PlayerHandAnimationEvent(@NotNull Player player, @NotNull Player.Hand hand) {
        super(player);
        this.hand = hand;
    }

    /**
     * Gets the hand used.
     *
     * @return the hand
     */
    @NotNull
    public Player.Hand getHand() {
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
}
