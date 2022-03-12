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
            player.getPlayerConnection().sendPacket(
                    new TabCompletePacket(packet.transactionId(), suggestion.getStart(), suggestion.getLength(),
                            suggestion.getEntries().stream().map(entry -> new TabCompletePacket.Match(entry.entry(), entry.tooltip())).toList()
                    )
            );
        }
    }

}
