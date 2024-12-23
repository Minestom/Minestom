package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerLoadedEvent;
import net.minestom.server.network.packet.client.play.ClientPlayerLoadedPacket;
import org.jetbrains.annotations.NotNull;

public final class PlayerLoadedListener {

    public static void listener(@NotNull ClientPlayerLoadedPacket packet, @NotNull Player player) {
        EventDispatcher.call(new PlayerLoadedEvent(player));
    }

}
