package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.common.ClientPingRequestPacket;
import net.minestom.server.network.packet.server.common.PingResponsePacket;
import org.jetbrains.annotations.NotNull;

public final class PlayPingListener {

    public static void requestListener(@NotNull ClientPingRequestPacket packet, @NotNull Player player) {
        player.sendPacket(new PingResponsePacket(packet.number()));
    }
}
