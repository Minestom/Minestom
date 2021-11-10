package net.minestom.server.command.builder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandResult {

    protected Type type = Type.UNKNOWN;
    protected String input;
    protected ParsedCommand parsedCommand;
    protected CommandData commandData;

    public @NotNull Type getType() {
        return type;
    }

    public @NotNull String getInput() {
        return input;
    }

    public @Nullable ParsedCommand getParsedCommand() {
        return parsedCommand;
    }

    public @Nullable CommandData getCommandData() {
        return commandData;
    }

    public enum Type {
        /**
         * Command and syntax successfully found.
         */
        SUCCESS,
        /**
         * Command found, but the syntax is invalid.
         * Executor sets to {@link Command#getDefaultExecutor()}.
         */
        INVALID_SYNTAX,
        /**
         * Command cancelled by an event listener.
         */
        CANCELLED,
        /**
         * Command is not registered, it is also the default result type.
         */
        UNKNOWN
    }

    public static @NotNull CommandResult of(@NotNull Type type, @NotNull String input) {
        CommandResult result = new CommandResult();
        result.type = type;
        result.input = input;
        return result;
    }
}
