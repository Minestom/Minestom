package net.minestom.server.listener.common;

import net.minestom.server.network.packet.client.common.ClientCookieResponsePacket;
import net.minestom.server.network.player.PlayerConnection;

public value class CookieListener {

    public static void handleCookieResponse(ClientCookieResponsePacket packet, PlayerConnection connection) {
        connection.receiveCookieResponse(packet.key(), packet.value());
    }

    private CookieListener() {}
}
