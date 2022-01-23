package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.parser.ArgumentQueryResult;
import net.minestom.server.command.builder.parser.CommandParser;
import net.minestom.server.command.builder.parser.CommandQueryResult;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket;
import net.minestom.server.network.packet.server.play.TabCompletePacket;
import net.minestom.server.utils.StringUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

public class TabCompleteListener {

    public static void listener(ClientTabCompletePacket packet, Player player) {
        final String text = packet.text();

        String commandString = text.replaceFirst(CommandManager.COMMAND_PREFIX, "");
        String[] split = commandString.split(StringUtils.SPACE);
        String commandName = split[0];
        String args = commandString.replaceFirst(Pattern.quote(commandName), "");

        final CommandQueryResult commandQueryResult = CommandParser.findCommand(MinecraftServer.getCommandManager().getDispatcher(), commandString);
        if (commandQueryResult == null) {
            // Command not found
            return;
        }

        final ArgumentQueryResult queryResult = CommandParser.findEligibleArgument(commandQueryResult.command(),
                commandQueryResult.args(), commandString, text.endsWith(StringUtils.SPACE), false,
                CommandSyntax::hasSuggestion, Argument::hasSuggestion);
        if (queryResult == null) {
            // Suggestible argument not found
            return;
        }

        final Argument<?> argument = queryResult.argument();

        final SuggestionCallback suggestionCallback = argument.getSuggestionCallback();
        if (suggestionCallback != null) {
            final String input = queryResult.input();
            final int inputLength = input.length();

            final int commandLength = Arrays.stream(split).map(String::length).reduce(0, Integer::sum) +
                    StringUtils.countMatches(args, StringUtils.SPACE_CHAR);
            final int trailingSpaces = !input.isEmpty() ? text.length() - text.trim().length() : 0;

            final int start = commandLength - inputLength + 1 - trailingSpaces;

            Suggestion suggestion = new Suggestion(input, start, inputLength);
            suggestionCallback.apply(player, queryResult.context(), suggestion);

            player.getPlayerConnection().sendPacket(new TabCompletePacket(packet.transactionId(), suggestion.getStart(), suggestion.getLength(),
                    suggestion.getEntries().stream()
                            .map(suggestionEntry -> new TabCompletePacket.Match(suggestionEntry.getEntry(), suggestionEntry.getTooltip())).toList()));
        }
    }

}
