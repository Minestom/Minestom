package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the player swings his hand.
 */
public class PlayerHandAnimationEvent extends CancellableEvent {

    private final Player player;
    private final Player.Hand hand;

    public PlayerHandAnimationEvent(@NotNull Player player, @NotNull Player.Hand hand) {
        this.player = player;
        this.hand = hand;
    }

    /**
     * The player who is swinging his arm.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
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
}
