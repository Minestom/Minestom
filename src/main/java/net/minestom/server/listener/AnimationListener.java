package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.animation.AnimationEvent;
import net.minestom.server.network.packet.client.play.ClientAnimationPacket;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;

public class AnimationListener {

    public static void animationListener(ClientAnimationPacket packet, Player player) {
        AnimationEvent animationEvent = new AnimationEvent(player, packet.hand);
        player.callCancellableEvent(AnimationEvent.class, animationEvent, () -> {
            EntityAnimationPacket entityAnimationPacket = new EntityAnimationPacket();
            entityAnimationPacket.entityId = player.getEntityId();
            entityAnimationPacket.animation = animationEvent.getHand() == Player.Hand.MAIN ? EntityAnimationPacket.Animation.SWING_MAIN_ARM : EntityAnimationPacket.Animation.SWING_OFF_HAND;
            player.sendPacketToViewers(entityAnimationPacket);
        });
    }

}
