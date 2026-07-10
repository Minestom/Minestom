package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.common.ClientPingRequestPacket;
import net.minestom.server.network.packet.server.common.PingResponsePacket;

public final class PlayPingListener {

    public static void requestListener(ClientPingRequestPacket packet, Player player) {
        player.sendPacket(new PingResponsePacket(packet.number()));
    }
}
