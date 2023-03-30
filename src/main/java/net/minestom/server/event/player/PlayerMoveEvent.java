package net.minestom.server.event.player;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is modifying his position.
 */
public class PlayerMoveEvent implements PlayerInstanceEvent, CancellableEvent {

    private final Player player;
    private Pos newPosition;
    private final boolean onGround;

    private boolean cancelled;

    public PlayerMoveEvent(@NotNull Player player, @NotNull Pos newPosition, boolean onGround) {
        this.player = player;
        this.newPosition = newPosition;
        this.onGround = onGround;
    }

    /**
     * Gets the target position.
     *
     * @return the new position
     */
    public @NotNull Pos getNewPosition() {
        return newPosition;
    }

    /**
     * Changes the target position.
     *
     * @param newPosition the new target position
     */
    public void setNewPosition(@NotNull Pos newPosition) {
        this.newPosition = newPosition;
    }

    /**
     * Gets if the player is now on the ground.
     * This is the original value that the client sent,
     * and is not modified by setting the new position.
     *
     * @return onGround
     */
    public boolean isOnGround() {
        return onGround;
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
