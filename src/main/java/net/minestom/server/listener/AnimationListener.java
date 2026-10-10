package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.network.packet.client.play.ClientPunchPacket;

public class AnimationListener {

    public static void punchListener(ClientPunchPacket packet, Player player) {
        PlayerHandAnimationEvent handAnimationEvent = new PlayerHandAnimationEvent(player, PlayerHand.MAIN);
        EventDispatcher.callCancellable(handAnimationEvent, () -> player.swingMainHand(true));
    }

}
