package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientAnimationPacket;

public class AnimationListener {

    public static void animationListener(ClientAnimationPacket packet, Player player) {
        final Player.Hand hand = packet.hand();
        final ItemStack itemStack = player.getItemInHand(hand);
        //itemStack.onLeftClick(player, hand);
        PlayerHandAnimationEvent handAnimationEvent = new PlayerHandAnimationEvent(player, hand);
        EventDispatcher.callCancellable(handAnimationEvent, () -> {
            switch (hand) {
                case MAIN -> player.swingMainHand();
                case OFF -> player.swingOffHand();
            }
        });
    }

}
