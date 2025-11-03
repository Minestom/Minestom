package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerDebugSubscriptionsRequestEvent;
import net.minestom.server.network.packet.client.play.ClientDebugSubscriptionRequestPacket;

public final class DebugSubscriptionListener {

    public static void requestListener(ClientDebugSubscriptionRequestPacket packet, Player player) {
        PlayerDebugSubscriptionsRequestEvent event = new PlayerDebugSubscriptionsRequestEvent(player, packet.subscriptions());
        EventDispatcher.call(event);
    }
}
