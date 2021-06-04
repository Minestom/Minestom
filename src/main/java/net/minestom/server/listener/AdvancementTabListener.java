package net.minestom.server.listener;

import net.minestom.server.advancements.AdvancementAction;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.AdvancementTabEvent;
import net.minestom.server.network.packet.client.play.ClientAdvancementTabPacket;

public class AdvancementTabListener {

    public static void listener(ClientAdvancementTabPacket packet, Player player) {
        final AdvancementAction action = packet.action;
        final String tabId = packet.tabIdentifier;
        AdvancementTabEvent advancementTabEvent = new AdvancementTabEvent(player, action, tabId);

        EventDispatcher.call(advancementTabEvent);
    }
}
