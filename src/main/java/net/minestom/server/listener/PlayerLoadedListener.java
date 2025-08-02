package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerLoadedEvent;
import net.minestom.server.network.packet.client.play.ClientPlayerLoadedPacket;

public final class PlayerLoadedListener {

    public static void listener(ClientPlayerLoadedPacket packet, Player player) {
        EventDispatcher.call(new PlayerLoadedEvent(player));
    }

}
