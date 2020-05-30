package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.utils.Position;

/**
 * Called when a player is modifying his position
 */
public class PlayerMoveEvent extends CancellableEvent {

    private Player player;
    private Position newPosition;

    public PlayerMoveEvent(Player player, float x, float y, float z, float yaw, float pitch) {
        this.player = player;
        this.newPosition = new Position(x, y, z, yaw, pitch);
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
}
