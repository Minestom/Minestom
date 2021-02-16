package net.minestom.server.command.builder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandResult {

    protected Type type = Type.UNKNOWN;
    protected String input;
    protected ParsedCommand parsedCommand;
    protected CommandData commandData;

    @NotNull
    public Type getType() {
        return type;
    }

    @NotNull
    public String getInput() {
        return input;
    }

    @Nullable
    public ParsedCommand getParsedCommand() {
        return parsedCommand;
    }

    @Nullable
    public CommandData getCommandData() {
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

    @NotNull
    public static CommandResult of(@NotNull Type type, @NotNull String input) {
        CommandResult result = new CommandResult();
        result.type = type;
        result.input = input;
        return result;
    }

}
