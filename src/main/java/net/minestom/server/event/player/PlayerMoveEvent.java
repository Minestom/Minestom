package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.utils.Position;

/**
 * Called when a player is modifying his position
 */
public class PlayerMoveEvent extends CancellableEvent {

    private final Player player;
    private Position newPosition;

    public PlayerMoveEvent(Player player, Position newPosition) {
        this.player = player;
        this.newPosition = newPosition;
    }

    /**
     * Get the player who is moving
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the target position
     *
     * @return the new position
     */
    public Position getNewPosition() {
        return newPosition;
    }

    /**
     * Change the target position
     *
     * @param newPosition the new target position
     */
    public void setNewPosition(Position newPosition) {
        this.newPosition = newPosition;
    }
}
