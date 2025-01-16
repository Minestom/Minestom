package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import org.jetbrains.annotations.NotNull;

/**
 * Called every time a player send a message starting by '/'.
 */
public record PlayerCommandEvent(@NotNull Player player, @NotNull String command, boolean cancelled) implements PlayerInstanceEvent, CancellableEvent<PlayerCommandEvent> {

    public PlayerCommandEvent(@NotNull Player player, @NotNull String command) {
        this(player, command, false);
    }

    /**
     * Gets the command used (command name + arguments).
     *
     * @return the command that the player wants to execute
     */
    @NotNull
    public String command() {
        return command;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<PlayerCommandEvent> {
        private final Player player;
        private String command;

        private boolean cancelled;

        public Mutator(PlayerCommandEvent event) {
            this.player = event.player;
            this.command = event.command;

            this.cancelled = event.cancelled;
        }

        /**
         * Gets the command used (command name + arguments).
         *
         * @return the command that the player wants to execute
         */
        public @NotNull String getCommand() {
            return command;
        }

        /**
         * Changes the command to execute.
         *
         * @param command the new command
         */
        public void setCommand(@NotNull String command) {
            this.command = command;
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
        public @NotNull PlayerCommandEvent mutated() {
            return new PlayerCommandEvent(this.player, this.command, this.cancelled);
        }
    }
}
