package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.AnimationEvent;
import fr.themode.minestom.net.packet.client.play.ClientAnimationPacket;
import fr.themode.minestom.net.packet.server.play.AnimationPacket;

public class AnimationListener {

    public static void animationListener(ClientAnimationPacket packet, Player player) {
        AnimationEvent animationEvent = new AnimationEvent(packet.hand);
        player.callCancellableEvent(AnimationEvent.class, animationEvent, () -> {
            AnimationPacket animationPacket = new AnimationPacket();
            animationPacket.entityId = player.getEntityId();
            animationPacket.animation = animationEvent.getHand() == Player.Hand.MAIN ? AnimationPacket.Animation.SWING_MAIN_ARM : AnimationPacket.Animation.SWING_OFF_HAND;
            player.sendPacketToViewers(animationPacket);
        });
    }

}
