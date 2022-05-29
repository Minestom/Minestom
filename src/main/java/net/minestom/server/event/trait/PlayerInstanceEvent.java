package net.minestom.server.event.trait;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents an {@link PlayerEvent} which happen in {@link Player#getInstance()}.
 * Useful if you need to listen to player events happening in its instance.
 * <p>
 * Be aware that the player's instance must be non-null.
 */
@ApiStatus.Internal
@ApiStatus.Experimental
public interface PlayerInstanceEvent extends PlayerEvent, EntityInstanceEvent {
}
