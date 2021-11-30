package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerResourcePackStatusEvent;
import net.minestom.server.network.packet.client.play.ClientResourcePackStatusPacket;

public class ResourcePackListener {

    public static void listener(ClientResourcePackStatusPacket packet, Player player) {
        EventDispatcher.call(new PlayerResourcePackStatusEvent(player, packet.status()));
    }
}
