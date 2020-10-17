package net.minestom.server.ping;

import net.minestom.server.network.player.PlayerConnection;

/**
 * Consumer used to fill a {@link ResponseData} object before being sent to a connection.
 * <p>
 * Can be specified in {@link net.minestom.server.MinecraftServer#start(String, int, ResponseDataConsumer)}.
 */
@FunctionalInterface
public interface ResponseDataConsumer {
    void accept(PlayerConnection playerConnection, ResponseData responseData);
}
