package net.minestom.server.event.player;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the gamemode of a player is being modified.
 */
public class PlayerGameModeChangeEvent implements PlayerInstanceEvent, CancellableEvent {

    private final Player player;
    private GameMode newGameMode;

    private boolean cancelled;

    public PlayerGameModeChangeEvent(@NotNull Player player, @NotNull GameMode newGameMode) {
        this.player = player;
        this.newGameMode = newGameMode;
    }

    /**
     * Gets the target gamemode.
     *
     * @return the target gamemode
     */
    public @NotNull GameMode getNewGameMode() {
        return newGameMode;
    }

    /**
     * Changes the target gamemode.
     *
     * @param newGameMode the new target gamemode
     */
    public void setNewGameMode(@NotNull GameMode newGameMode) {
        this.newGameMode = newGameMode;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
