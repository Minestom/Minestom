package net.minestom.server.network;

import net.minestom.server.entity.Player;
import net.minestom.server.network.player.PlayerConnection;

import java.util.UUID;

@FunctionalInterface
public interface PlayerProvider {
    Player getPlayer(UUID uuid, String username, PlayerConnection connection);
}
