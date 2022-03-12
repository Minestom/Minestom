package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket;
import net.minestom.server.network.packet.server.play.TabCompletePacket;

public class TabCompleteListener {

    public static void listener(ClientTabCompletePacket packet, Player player) {
        final String text = packet.text();

        if (!text.startsWith(CommandManager.COMMAND_PREFIX)) {
            return;
        }

        StringReader reader = new StringReader(text, CommandManager.COMMAND_PREFIX.length());

        Suggestion suggestion = MinecraftServer.getCommandManager().getDispatcher().tabComplete(CommandOrigin.ofPlayer(player), reader);
        if (suggestion != null) {
            TabCompletePacket tabCompletePacket = new TabCompletePacket();
            tabCompletePacket.transactionId = packet.transactionId();
            tabCompletePacket.start = suggestion.getStart();
            tabCompletePacket.length = suggestion.getLength();
            tabCompletePacket.matches = suggestion.getEntries()
                    .stream()
                    .map(suggestionEntry -> {
                        TabCompletePacket.Match match = new TabCompletePacket.Match();
                        match.match = suggestionEntry.entry();
                        match.hasTooltip = suggestionEntry.tooltip() != null;
                        match.tooltip = suggestionEntry.tooltip();
                        return match;
                    }).toArray(TabCompletePacket.Match[]::new);
            player.getPlayerConnection().sendPacket(tabCompletePacket);
        }
    }

}
