package net.minestom.server.listener.common;

import net.minestom.server.network.packet.client.common.ClientCookieResponsePacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

public final class CookieListener {

    public static void handleCookieResponse(@NotNull ClientCookieResponsePacket packet, @NotNull PlayerConnection connection) {
        connection.receiveCookieResponse(packet.key(), packet.value());
    }

    private CookieListener() {}
}
