package net.minestom.server.network;

import net.minestom.server.network.player.PlayerConnection;

import java.util.UUID;

/**
 * Used when you want to provide your own {@link UUID} object for players instead of using the default one.
 * <p>
 * Sets with {@link ConnectionManager#setUuidProvider(UuidProvider)}.
 */
@FunctionalInterface
public interface UuidProvider {

    /**
     * Called when a new {@link UUID} is requested.
     * <p>
     * The {@link UUID} does not need to be persistent between restart, but being sure that all players have a different
     * one is good practice. Otherwise, undefined behavior can happen.
     *
     * @param playerConnection the connection who requires a new unique id
     * @param username         the username given by the connection
     * @return the new {@link UUID} for the player
     */
    UUID provide(PlayerConnection playerConnection, String username);
}
