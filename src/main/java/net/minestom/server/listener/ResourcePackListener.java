package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerResourcePackStatusEvent;
import net.minestom.server.network.packet.client.play.ClientResourcePackStatusPacket;
import net.minestom.server.resourcepack.ResourcePackStatus;

public class ResourcePackListener {

    public static void listener(ClientResourcePackStatusPacket packet, Player player) {
        final ResourcePackStatus result = packet.result;
        PlayerResourcePackStatusEvent resourcePackStatusEvent = new PlayerResourcePackStatusEvent(player, result);
        EventDispatcher.call(resourcePackStatusEvent);
    }
}
