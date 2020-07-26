package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;

/**
 * Called when the player swings his hand
 */
public class PlayerHandAnimationEvent extends CancellableEvent {

    private final Player player;
    private final Player.Hand hand;

    public PlayerHandAnimationEvent(Player player, Player.Hand hand) {
        this.player = player;
        this.hand = hand;
    }

    /**
     * The player who is swinging his arm
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the hand used
     *
     * @return the hand
     */
    public Player.Hand getHand() {
        return hand;
    }
}
