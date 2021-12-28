package net.minestom.server.event.player;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a players GameMode changes using {@link Player#setGameMode(GameMode)}.
 */
public class PlayerGameModeChangeEvent implements PlayerEvent, EntityInstanceEvent, CancellableEvent {

    private final Player player;
    private GameMode gameMode;

    private boolean cancelled;

    public PlayerGameModeChangeEvent(@NotNull Player player, @NotNull GameMode gameMode) {
        this.player = player;
        this.gameMode = gameMode;
    }

    /**
     * Gets the player who's GameMode will change.
     *
     * @return the player
     */
    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * Gets the GameMode which the player will be in.
     *
     * @return the GameMode
     */
    public @NotNull GameMode getGameMode() {
        return gameMode;
    }

    /**
     * Changes the GameMode which the player will be in.
     *
     * @param gameMode the new GameMode
     */
    public void setGameMode(@NotNull GameMode gameMode) {
        this.gameMode = gameMode;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
