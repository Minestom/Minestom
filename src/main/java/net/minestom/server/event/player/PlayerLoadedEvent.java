package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player indicates that they have finished loading into the world.
 *
 * <p>This is driven by the client so should be considered as such.</p>
 */
public record PlayerLoadedEvent(@NotNull Player player) implements PlayerInstanceEvent {}