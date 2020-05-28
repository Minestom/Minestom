package net.minestom.server.ping;

import net.minestom.server.network.player.PlayerConnection;

@FunctionalInterface
public interface ResponseDataConsumer {
    void accept(PlayerConnection playerConnection, ResponseData responseData);
}
