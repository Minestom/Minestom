package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public record PlayerAntiCheatFailEvent(@NotNull Player player, @NotNull String reason) implements PlayerEvent {

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
