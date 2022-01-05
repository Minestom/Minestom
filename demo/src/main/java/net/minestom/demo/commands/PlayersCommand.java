package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;

import java.util.Collection;

public class PlayersCommand extends Command {

    public PlayersCommand() {
        super("players");
        setDefaultExecutor(this::usage);
    }

    private void usage(CommandSender sender, CommandContext context) {
        final Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
        final int playerCount = players.size();
        sender.sendMessage(Component.text("Total players: " + playerCount));
        final int limit = 15;
        if (playerCount <= limit) {
            for (final Player player : players) {
                sender.sendMessage(Component.text(player.getUsername()));
            }
        } else {
            for (final Player player : players.stream().limit(limit).toList()) {
                sender.sendMessage(Component.text(player.getUsername()));
            }
            sender.sendMessage(Component.text("..."));
        }
    }

}
