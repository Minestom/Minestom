package net.minestom.server.listener;

import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandProcessor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandDispatcher;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.parser.CommandParser;
import net.minestom.server.command.builder.parser.CommandSuggestionHolder;
import net.minestom.server.command.builder.parser.ValidSyntaxHolder;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket;
import net.minestom.server.network.packet.server.play.TabCompletePacket;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class TabCompleteListener {

    private static final CommandManager COMMAND_MANAGER = MinecraftServer.getCommandManager();

    public static void listener(ClientTabCompletePacket packet, Player player) {
        final String text = packet.text;

        {
            String commandString = packet.text.replaceFirst("/", "");
            String[] split = commandString.split(StringUtils.SPACE);
            String commandName = split[0];
            final CommandDispatcher commandDispatcher = MinecraftServer.getCommandManager().getDispatcher();
            final Command command = commandDispatcher.findCommand(commandName);
            final Collection<CommandSyntax> syntaxes = command.getSyntaxes();
            List<ValidSyntaxHolder> validSyntaxes = new ArrayList<>(syntaxes.size());
            Int2ObjectRBTreeMap<CommandSuggestionHolder> syntaxesSuggestions = new Int2ObjectRBTreeMap<>(Collections.reverseOrder());

            String[] args = commandString.replaceFirst(Pattern.quote(commandName), "").trim().split(StringUtils.SPACE);
            if (args.length == 1 && args[0].length() == 0) {
                args = new String[0];
            }

            for (CommandSyntax syntax : syntaxes) {
                CommandParser.parse(syntax, syntax.getArguments(), args, validSyntaxes, syntaxesSuggestions);
            }

            if (!syntaxesSuggestions.isEmpty()) {
                final int max = syntaxesSuggestions.firstIntKey();
                final CommandSuggestionHolder suggestionHolder = syntaxesSuggestions.get(max);
                final CommandSyntax syntax = suggestionHolder.syntax;
                final int argIndex = suggestionHolder.argIndex;
                final Argument<?> argument = syntax.getArguments()[argIndex];

                final SuggestionCallback suggestionCallback = argument.suggestionCallback;
                if (suggestionCallback != null) {
                    Suggestion suggestion = new Suggestion();
                    suggestionCallback.apply(suggestion);

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
                System.out.println("arg: " + argument.getClass());
            }

            System.out.println("test " + syntaxesSuggestions.size() + " " + validSyntaxes.size());

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
