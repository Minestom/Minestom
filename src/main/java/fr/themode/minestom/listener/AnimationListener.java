package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.play.ClientAnimationPacket;
import fr.themode.minestom.net.packet.server.play.AnimationPacket;

public class AnimationListener {

    public static void animationListener(ClientAnimationPacket packet, Player player) {
        AnimationPacket animationPacket = new AnimationPacket();
        animationPacket.playerId = player.getEntityId();
        animationPacket.animation = packet.hand == Player.Hand.MAIN ? AnimationPacket.Animation.SWING_MAIN_ARM : AnimationPacket.Animation.SWING_OFF_HAND;
        player.sendPacketToViewers(animationPacket);
    }

}
