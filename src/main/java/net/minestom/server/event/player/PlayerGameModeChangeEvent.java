package net.minestom.server.event.player;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the gamemode of a player is being modified.
 */
public record PlayerGameModeChangeEvent(@NotNull Player player, @NotNull GameMode newGameMode, boolean cancelled) implements PlayerInstanceEvent, CancellableEvent<PlayerGameModeChangeEvent> {
    public PlayerGameModeChangeEvent(@NotNull Player player, @NotNull GameMode newGameMode) {
        this(player, newGameMode, false);
    }

    /**
     * Gets the target gamemode.
     *
     * @return the target gamemode
     */
    public @NotNull GameMode newGameMode() {
        return newGameMode;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }


    public static class Mutator implements EventMutatorCancellable<PlayerGameModeChangeEvent> {
        private final Player player;
        private GameMode newGameMode;

        private boolean cancelled;

        public Mutator(PlayerGameModeChangeEvent event) {
            this.player = event.player;
            this.newGameMode = event.newGameMode;

            this.cancelled = event.cancelled;
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
        public @NotNull PlayerGameModeChangeEvent mutated() {
            return new PlayerGameModeChangeEvent(this.player, this.newGameMode, this.cancelled);
        }
    }
}
