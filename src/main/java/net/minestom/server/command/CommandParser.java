package net.minestom.server.command;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.suggestion.Suggestion;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.List;

@ApiStatus.Internal
public interface CommandParser {
    static CommandParser parser() {
        return CommandParserImpl.PARSER;
    }

    /**
     * Parses the command by following the graph
     *
     * @param graph structure to use for parsing
     * @param input command string without prefix
     * @return the parsed command which can be executed and cached
     */
    @Contract("_, _ -> new")
    Result parse(CommandSender sender, Graph graph, String input);

    sealed interface Result {
        ExecutableCommand executable();

        @ApiStatus.Internal
        @Nullable Suggestion suggestion(CommandSender sender);

        @ApiStatus.Internal
        List<Argument<?>> args();

        sealed interface UnknownCommand extends Result
                permits CommandParserImpl.UnknownCommandResult {
        }

        sealed interface KnownCommand extends Result
                permits CommandParserImpl.InternalKnownCommand, Result.KnownCommand.Invalid, Result.KnownCommand.Valid {

            sealed interface Valid extends KnownCommand
                    permits CommandParserImpl.ValidCommand {
            }

            sealed interface Invalid extends KnownCommand
                    permits CommandParserImpl.InvalidCommand {
            }
        }
    }
}
