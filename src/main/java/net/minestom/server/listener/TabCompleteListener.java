package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket;
import net.minestom.server.network.packet.server.play.TabCompletePacket;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

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
        if (text.startsWith("/")) {
            text = text.substring(1);
        }
        // No space yet: the cursor is still in the command name, so suggest matching command names (the parser below
        // only suggests argument values). Mirrors vanilla, whose literal nodes self-suggest. Modern clients do this locally.
        if (text.indexOf(' ') == -1) {
            return commandNameSuggestion(commandSender, text);
        }
        if (text.endsWith(" ")) {
            // Append a placeholder char if the command ends with a space allowing the parser to find suggestion
            // for the next arg without typing the first char of it, this is probably the most hacky solution, but hey
            // it works as intended :)
            text = text + '\00';
        }
        return MinecraftServer.getCommandManager().parseCommand(commandSender, text).suggestion(commandSender);
    }

    private static Suggestion commandNameSuggestion(CommandSender sender, String commandName) {
        // start=1 (just past the leading '/'): the same offset convention the parser uses for argument suggestions.
        final Suggestion suggestion = new Suggestion(commandName, 1, commandName.length());
        MinecraftServer.getCommandManager().getCommands().stream()
                .filter(command -> {
                    final CommandCondition condition = command.getCondition();
                    return condition == null || condition.canUse(sender, null);
                })
                .flatMap(command -> Arrays.stream(command.getNames()))
                .filter(name -> name.regionMatches(true, 0, commandName, 0, commandName.length()))
                .distinct().sorted()
                .forEach(name -> suggestion.addEntry(new SuggestionEntry(name)));
        return suggestion;
    }
}
