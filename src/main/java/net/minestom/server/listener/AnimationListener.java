package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.animation.AnimationEvent;
import net.minestom.server.network.packet.client.play.ClientAnimationPacket;

public class AnimationListener {

    public static void animationListener(ClientAnimationPacket packet, Player player) {
        AnimationEvent animationEvent = new AnimationEvent(player, packet.hand);
        player.callCancellableEvent(AnimationEvent.class, animationEvent, () -> {
            final Player.Hand hand = animationEvent.getHand();
            switch (hand) {
                case MAIN:
                    player.swingMainHand();
                    break;
                case OFF:
                    player.swingOffHand();
                    break;
            }
        });
    }

}
