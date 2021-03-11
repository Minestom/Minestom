package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandProcessor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.parser.ArgumentQueryResult;
import net.minestom.server.command.builder.parser.CommandParser;
import net.minestom.server.command.builder.parser.CommandQueryResult;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket;
import net.minestom.server.network.packet.server.play.TabCompletePacket;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

public class TabCompleteListener {

    private static final CommandManager COMMAND_MANAGER = MinecraftServer.getCommandManager();

    public static void listener(ClientTabCompletePacket packet, Player player) {
        final String text = packet.text;

        {
            String commandString = packet.text.replaceFirst(CommandManager.COMMAND_PREFIX, "");
            String[] split = commandString.split(StringUtils.SPACE);
            String commandName = split[0];

            String args = commandString.replaceFirst(commandName, "");
            String[] argsSplit = new String[split.length - 1];
            System.arraycopy(split, 1, argsSplit, 0, argsSplit.length);

            final CommandQueryResult commandQueryResult = CommandParser.findCommand(commandName, argsSplit);
            if (commandQueryResult == null) {
                System.out.println("COMMAND NOT FOUND");
                return;
            }

            final ArgumentQueryResult queryResult = CommandParser.findSuggestibleArgument(commandQueryResult.command,
                    commandQueryResult.args, commandString);
            if (queryResult == null) {
                System.out.println("QUERY NOT FOUND");
                return;
            }

            final Argument<?> argument = queryResult.argument;

            final SuggestionCallback suggestionCallback = argument.getSuggestionCallback();
            if (suggestionCallback != null) {
                final String input = queryResult.input;
                final int inputLength = input.length();

                final int commandLength = Arrays.stream(split).map(String::length).reduce(0, Integer::sum) +
                        StringUtils.countMatches(args, StringUtils.SPACE);
                final int trailingSpaces = !input.isEmpty() ? text.length() - text.trim().length() : 0;

                final int start = commandLength - inputLength + 1 - trailingSpaces;

                Suggestion suggestion = new Suggestion(input, start, inputLength);
                suggestionCallback.apply(player, queryResult.context, suggestion);

                TabCompletePacket tabCompletePacket = new TabCompletePacket();
                tabCompletePacket.transactionId = packet.transactionId;
                tabCompletePacket.start = suggestion.getStart();
                tabCompletePacket.length = suggestion.getLength();
                tabCompletePacket.matches = suggestion.getEntries()
                        .stream()
                        .map(suggestionEntry -> {
                            TabCompletePacket.Match match = new TabCompletePacket.Match();
                            match.match = suggestionEntry.getEntry();
                            match.hasTooltip = suggestionEntry.getTooltip() != null;
                            match.tooltip = suggestionEntry.getTooltip();
                            return match;
                        }).toArray(TabCompletePacket.Match[]::new);

                player.getPlayerConnection().sendPacket(tabCompletePacket);
            }

            if (true)
                return;
        }

        final String[] split = packet.text.split(Pattern.quote(StringUtils.SPACE));

        final String commandName = split[0].replaceFirst(CommandManager.COMMAND_PREFIX, "");

        // Tab complete for CommandProcessor
        final CommandProcessor commandProcessor = COMMAND_MANAGER.getCommandProcessor(commandName);
        if (commandProcessor != null) {
            final int start = findStart(text, split);
            final String[] matches = commandProcessor.onWrite(player, text);
            if (matches != null && matches.length > 0) {
                sendTabCompletePacket(packet.transactionId, start, matches, player);
            }
        } else {
            // Tab complete for Command
            final Command command = COMMAND_MANAGER.getCommand(commandName);
            if (command != null) {
                final int start = findStart(text, split);
                final String[] matches = command.onDynamicWrite(player, text);
                if (matches != null && matches.length > 0) {
                    sendTabCompletePacket(packet.transactionId, start, matches, player);
                }
            }
        }


    }

    private static int findStart(String text, String[] split) {
        final boolean endSpace = text.endsWith(StringUtils.SPACE);
        int start;
        if (endSpace) {
            start = text.length();
        } else {
            final String lastArg = split[split.length - 1];
            start = text.lastIndexOf(lastArg);
        }
        return start;
    }

    private static void sendTabCompletePacket(int transactionId, int start, String[] matches, Player player) {
        TabCompletePacket tabCompletePacket = new TabCompletePacket();
        tabCompletePacket.transactionId = transactionId;
        tabCompletePacket.start = start;
        tabCompletePacket.length = 20;

        TabCompletePacket.Match[] matchesArray = new TabCompletePacket.Match[matches.length];
        for (int i = 0; i < matchesArray.length; i++) {
            TabCompletePacket.Match match = new TabCompletePacket.Match();
            match.match = matches[i];
            matchesArray[i] = match;
        }

        tabCompletePacket.matches = matchesArray;

        player.getPlayerConnection().sendPacket(tabCompletePacket);
    }


}
