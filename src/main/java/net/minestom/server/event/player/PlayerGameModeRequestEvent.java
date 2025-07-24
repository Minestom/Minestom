package net.minestom.server.event.player;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player uses the F3+F4 menu to try and change their gamemode.
 */
public class PlayerGameModeRequestEvent implements PlayerInstanceEvent {

    private final Player player;
    private final GameMode requestedGameMode;

    public PlayerGameModeRequestEvent(@NotNull Player player, @NotNull GameMode requestedGameMode) {
        this.player = player;
        this.requestedGameMode = requestedGameMode;
    }

    /**
     * Gets the requested gamemode.
     *
     * @return the requested gamemode
     */
    public @NotNull GameMode getRequestedGameMode() {
        return requestedGameMode;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
