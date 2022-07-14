package net.minestom.server.command;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

interface CommandParser {
    static @NotNull CommandParser parser() {
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
    @NotNull ParseResult parse(@NotNull Graph graph, @NotNull String input);

}
