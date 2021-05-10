package net.minestom.server.ping;

import net.minestom.server.network.player.PlayerConnection;

/**
 * Consumer used to fill a {@link ResponseData} object before being sent to a connection.
 *
 * <p>Can be specified in {@link net.minestom.server.MinecraftServer#start(String, int,
 * ResponseDataConsumer)}.
 *
 * @deprecated listen to the {@link net.minestom.server.event.server.ServerListPingEvent} instead
 */
@FunctionalInterface
@Deprecated
public interface ResponseDataConsumer {

    /**
     * A method to fill the data of the response.
     *
     * @param playerConnection The player connection to which the response should be sent.
     * @param responseData     The data for the response.
     */
    void accept(PlayerConnection playerConnection, ResponseData responseData);
}
