package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;

import java.util.Objects;

public class PlayerGameRulesRequestEvent implements PlayerInstanceEvent {
    private final Player player;

    public PlayerGameRulesRequestEvent(Player player) {
        this.player = Objects.requireNonNull(player, "player");
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
