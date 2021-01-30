package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.PlayerEvent;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is modifying his position.
 */
public class PlayerMoveEvent extends PlayerEvent implements CancellableEvent {

    private final Position currentPosition;
    private Position newPosition;

    private boolean cancelled;

    public PlayerMoveEvent(@NotNull Player player, @NotNull Position newPosition) {
        super(player);
        this.currentPosition = player.getPosition();
        this.newPosition = newPosition;
    }

    /**
     * Gets current position of the player.
     *
     * @return the current position
     */
    @NotNull
    public Position getCurrentPosition() {
        return currentPosition;
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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
