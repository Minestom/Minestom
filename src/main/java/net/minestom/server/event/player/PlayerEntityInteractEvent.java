package net.minestom.server.event.player;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

/**
 * Called when a player interacts (right-click) with an entity
 */
public class PlayerEntityInteractEvent extends Event {

    private final Player player;
    private final Entity entityTarget;
    private final Player.Hand hand;

    public PlayerEntityInteractEvent(Player player, Entity entityTarget, Player.Hand hand) {
        this.player = player;
        this.entityTarget = entityTarget;
        this.hand = hand;
    }

    /**
     * Get the player who is interacting
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the entity with who {@link #getPlayer()} is interacting
     *
     * @return the entity
     */
    public Entity getTarget() {
        return entityTarget;
    }

    /**
     * Get with which hand the player interacted with the entity
     *
     * @return the hand
     */
    public Player.Hand getHand() {
        return hand;
    }
}