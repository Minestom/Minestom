package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.AnimationEvent;
import fr.themode.minestom.net.packet.client.play.ClientAnimationPacket;
import fr.themode.minestom.net.packet.server.play.EntityAnimationPacket;

public class AnimationListener {

    public static void animationListener(ClientAnimationPacket packet, Player player) {
        AnimationEvent animationEvent = new AnimationEvent(packet.hand);
        player.callCancellableEvent(AnimationEvent.class, animationEvent, () -> {
            EntityAnimationPacket entityAnimationPacket = new EntityAnimationPacket();
            entityAnimationPacket.entityId = player.getEntityId();
            entityAnimationPacket.animation = animationEvent.getHand() == Player.Hand.MAIN ? EntityAnimationPacket.Animation.SWING_MAIN_ARM : EntityAnimationPacket.Animation.SWING_OFF_HAND;
            player.sendPacketToViewers(entityAnimationPacket);
        });
    }

}
