package net.minestom.server.command;

import net.minestom.server.command.builder.suggestion.Suggestion;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
sealed public interface ParseResult {

    ExecutableCommand toExecutable();

    @ApiStatus.Internal
    @Nullable Suggestion suggestion(CommandSender sender);

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
