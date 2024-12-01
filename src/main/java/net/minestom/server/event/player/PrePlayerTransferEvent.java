package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public record PrePlayerTransferEvent(Player player, String host, int port) implements PlayerEvent {
    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
