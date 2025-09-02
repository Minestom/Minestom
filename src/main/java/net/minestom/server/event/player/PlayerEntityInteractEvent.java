package net.minestom.server.event.player;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.PlayerInstanceEvent;

/**
 * Called when a {@link Player} interacts (right-click) with an {@link Entity}.
 */
public class PlayerEntityInteractEvent implements PlayerInstanceEvent {

    private final Player player;
    private final Entity entityTarget;
    private final PlayerHand hand;
    private final Point interactPosition;

    public PlayerEntityInteractEvent(Player player, Entity entityTarget, PlayerHand hand,
                                     Point interactPosition) {
        this.player = player;
        this.entityTarget = entityTarget;
        this.hand = hand;
        this.interactPosition = interactPosition;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the {@link Entity} with who {@link #getPlayer()} is interacting.
     *
     * @return the {@link Entity}
     */
    public Entity getTarget() {
        return entityTarget;
    }

    /**
     * Gets with which hand the player interacted with the entity.
     *
     * @return the hand
     */
    public PlayerHand getHand() {
        return hand;
    }

    /**
     * Gets the position at which the entity was interacted
     *
     * @see net.minestom.server.network.packet.client.play.ClientInteractEntityPacket.InteractAt
     * @return the interaction position
     */
    public Point getInteractPosition() {
        return interactPosition;
    }
}