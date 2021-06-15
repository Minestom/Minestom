package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is modifying his position.
 */
public class PlayerMoveEvent implements PlayerEvent, CancellableEvent {

    private final Position newPosition;
    private final Player player;

    private boolean cancelled;

    public PlayerMoveEvent(@NotNull Player player, @NotNull Position newPosition) {
        this.player = player;
        this.newPosition = newPosition;
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
        this.newPosition.set(newPosition);
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
