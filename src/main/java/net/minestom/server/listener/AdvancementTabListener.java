package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.AdvancementTabEvent;
import net.minestom.server.network.packet.client.play.ClientAdvancementTabPacket;

public class AdvancementTabListener {

    public static void listener(ClientAdvancementTabPacket packet, Player player) {
        final String tabIdentifier = packet.tabIdentifier();
        if (tabIdentifier != null) {
            EventDispatcher.call(new AdvancementTabEvent(player, packet.action(), tabIdentifier));
        }
    }
}
