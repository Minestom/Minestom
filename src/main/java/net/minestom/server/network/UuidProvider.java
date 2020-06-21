package net.minestom.server.network;

import net.minestom.server.network.player.PlayerConnection;

import java.util.UUID;

@FunctionalInterface
public interface UuidProvider {
    UUID provide(PlayerConnection playerConnection, String username);
}
