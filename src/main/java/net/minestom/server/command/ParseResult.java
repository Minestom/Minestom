package net.minestom.server.command;

import org.jetbrains.annotations.NotNull;

sealed public interface ParseResult {
    @NotNull ExecutionResult execute(@NotNull CommandSender sender);

    sealed interface UnknownCommand extends ParseResult
            permits CommandParserImpl.UnknownCommandResult {
    }

    sealed interface KnownCommand extends ParseResult
            permits CommandParserImpl.InternalKnownCommand, KnownCommand.Invalid, KnownCommand.Valid {

        sealed interface Valid extends KnownCommand
                permits CommandParserImpl.ValidCommand {
        }

        sealed interface Invalid extends KnownCommand
                permits CommandParserImpl.InvalidCommand {
        }
    }
}
