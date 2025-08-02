package net.minestom.server.event.player;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;

/**
 * Called when a player uses the F3+F4 menu to try and change their gamemode.
 */
public class PlayerGameModeRequestEvent implements PlayerInstanceEvent {

    private final Player player;
    private final GameMode requestedGameMode;

    public PlayerGameModeRequestEvent(Player player, GameMode requestedGameMode) {
        this.player = player;
        this.requestedGameMode = requestedGameMode;
    }

    /**
     * Gets the requested gamemode.
     *
     * @return the requested gamemode
     */
    public GameMode getRequestedGameMode() {
        return requestedGameMode;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
