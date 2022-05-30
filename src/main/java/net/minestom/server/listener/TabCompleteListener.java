package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandSender;
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
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.regex.Pattern;

public class TabCompleteListener {

    public static void listener(ClientTabCompletePacket packet, Player player) {
        final String text = packet.text();
        final Suggestion suggestion = getSuggestion(player, text);
        if (suggestion != null) {
            player.sendPacket(new TabCompletePacket(
                    packet.transactionId(),
                    suggestion.getStart(),
                    suggestion.getLength(),
                    suggestion.getEntries().stream()
                            .map(suggestionEntry -> new TabCompletePacket.Match(suggestionEntry.getEntry(), suggestionEntry.getTooltip()))
                            .toList())
            );
        }
    }

    public static @Nullable Suggestion getSuggestion(CommandSender commandSender, String text) {
        String commandString = text.replaceFirst(CommandManager.COMMAND_PREFIX, "");
        String[] split = commandString.split(StringUtils.SPACE);
        String commandName = split[0];
        String args = commandString.replaceFirst(Pattern.quote(commandName), "");

        final CommandQueryResult commandQueryResult = CommandParser.findCommand(MinecraftServer.getCommandManager().getDispatcher(), commandString);
        if (commandQueryResult == null) {
            // Command not found
            return null;
        }

        final ArgumentQueryResult queryResult = CommandParser.findEligibleArgument(commandQueryResult.command(),
                commandQueryResult.args(), commandString, text.endsWith(StringUtils.SPACE), false,
                CommandSyntax::hasSuggestion, Argument::hasSuggestion);
        if (queryResult == null) {
            // Suggestible argument not found
            return null;
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
            suggestionCallback.apply(commandSender, queryResult.context(), suggestion);

            return suggestion;
        }
        return null;
    }
}
