package net.minestom.server.network;

import net.minestom.server.entity.Player;
import net.minestom.server.network.player.PlayerConnection;

import java.util.UUID;

@FunctionalInterface
public interface PlayerProvider {

    /**
     * Should create a new {@link Player} object based on his data
     *
     * @param uuid       the player {@link UUID}
     * @param username   the player username
     * @param connection the player connection
     * @return a newly create {@link Player} object
     */
    Player createPlayer(UUID uuid, String username, PlayerConnection connection);
}
