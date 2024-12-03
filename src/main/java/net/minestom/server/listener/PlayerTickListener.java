package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerTickEndEvent;
import net.minestom.server.network.packet.client.play.ClientTickEndPacket;
import org.jetbrains.annotations.NotNull;

public final class PlayerTickListener {

    public static void listener(@NotNull ClientTickEndPacket packet, @NotNull Player player) {
        EventDispatcher.call(new PlayerTickEndEvent(player));
    }

}
