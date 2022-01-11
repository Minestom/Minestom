package net.minestom.server.command.builder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CommandResult(@NotNull Type type, @NotNull String input, int position, @Nullable ParsedCommand parsedCommand) {

    public CommandResult(@NotNull Type type, @NotNull String input, int position) {
        this(type, input, position, null);
    }

    /**
     * Represents the type of result that this instance represents.
     */
    public enum Type {

        /**
         * This type indicates that the command provided was not found.
         */
        UNKNOWN_COMMAND,

        /**
         * This type indicates that a command was found and that a syntax was successfully completed.
         */
        SUCCESS,

        /**
         * This type indicates that a command was found but none of its syntaxes were valid.
         */
        FAILURE,

        /**
         * This type indicates that the execution was cancelled because the event that it created was cancelled.
         */
        CANCELLED
    }
}
