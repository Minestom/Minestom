package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerStartFlyingWithElytraEvent extends PlayerEvent {

    public PlayerStartFlyingWithElytraEvent(@NotNull Player player) {
        super(player);
    }
}
