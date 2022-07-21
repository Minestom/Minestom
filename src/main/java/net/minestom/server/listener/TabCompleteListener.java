package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket;
import net.minestom.server.network.packet.server.play.TabCompletePacket;
import org.jetbrains.annotations.Nullable;

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
        if (text.endsWith(" ")) {
            // Append a placeholder char if the command ends with a space allowing the parser to find suggestion
            // for the next arg without typing the first char of it, this is probably the most hacky solution, but hey
            // it works as intended :)
            text = text + '\00';
        }
        return MinecraftServer.getCommandManager().parseCommand(text).suggestion(commandSender);
    }
}
