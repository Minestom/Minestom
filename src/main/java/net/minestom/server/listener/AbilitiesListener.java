package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerStartFlyingEvent;
import net.minestom.server.event.player.PlayerStopFlyingEvent;
import net.minestom.server.network.packet.client.play.ClientPlayerAbilitiesPacket;

public class AbilitiesListener {

    public static void listener(ClientPlayerAbilitiesPacket packet, Player player) {
        final boolean canFly = player.isAllowFlying() || player.isCreative();

        if (canFly) {
            final boolean isFlying = (packet.flags() & 0x2) > 0;

            player.refreshFlying(isFlying);

            if (isFlying) {
                PlayerStartFlyingEvent startFlyingEvent = new PlayerStartFlyingEvent(player);
                EventDispatcher.call(startFlyingEvent);
            } else {
                PlayerStopFlyingEvent stopFlyingEvent = new PlayerStopFlyingEvent(player);
                EventDispatcher.call(stopFlyingEvent);
            }
        }
    }
}
