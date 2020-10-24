package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is modifying his position.
 */
public class PlayerMoveEvent extends CancellableEvent {

    private final Player player;
    private Position newPosition;

    public PlayerMoveEvent(@NotNull Player player, @NotNull Position newPosition) {
        this.player = player;
        this.newPosition = newPosition;
    }

    /**
     * Gets the player who is moving.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the target position.
     *
     * @return the new position
     */
    @NotNull
    public Position getNewPosition() {
        return newPosition;
    }

    /**
     * Changes the target position.
     *
     * @param newPosition the new target position
     */
    public void setNewPosition(@NotNull Position newPosition) {
        this.newPosition = newPosition;
    }
}
