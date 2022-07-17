package net.minestom.server.listener;

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
        return null; //TODO Reimplement with new system
    }
}
