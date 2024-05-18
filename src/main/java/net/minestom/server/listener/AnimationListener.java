package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientAnimationPacket;
import org.jetbrains.annotations.NotNull;

public final class AnimationListener {

    private AnimationListener() {}

    public static void animationListener(@NotNull ClientAnimationPacket packet, @NotNull Player player) {
        player.swingHand(packet.hand());
    }
}
