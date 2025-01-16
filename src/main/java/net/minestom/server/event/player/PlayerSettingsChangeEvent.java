package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called after the player signals the server that his settings has been modified.
 *
 * @param player the player who changed his settings; You can retrieve the new player settings with {@link Player#getSettings()}.
 */
public record PlayerSettingsChangeEvent(@NotNull Player player) implements PlayerEvent {}
