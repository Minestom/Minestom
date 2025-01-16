package net.minestom.server.event.player;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called by the SpectateListener when a player starts spectating an entity.
 */
public record PlayerSpectateEvent(@NotNull Player player, @NotNull Entity target) implements PlayerEvent {}
