package net.minestom.server.command.builder;

public record CommandResult(Type type, String input, CommandData commandData) {
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
         * Either {@link Command#getCondition()} or {@link CommandSyntax#getCommandCondition()} failed
         */
        PRECONDITION_FAILED,
        /**
         * Command is not registered, it is also the default result type.
         */
        UNKNOWN
    }
}
