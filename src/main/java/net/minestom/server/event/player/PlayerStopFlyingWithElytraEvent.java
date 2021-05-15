package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerStopFlyingWithElytraEvent extends PlayerEvent {

    public PlayerStopFlyingWithElytraEvent(@NotNull Player player) {
        super(player);
    }
}
