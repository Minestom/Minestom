package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionState;

import java.util.Collection;
import java.util.List;

public class PlayersCommand extends Command {

    public PlayersCommand() {
        super("players");
        setDefaultExecutor(this::usage);
    }

    private void usage(CommandSender sender, CommandContext context) {
        final var players = List.copyOf(MinecraftServer.getConnectionManager().getOnlinePlayers());
        final int playerCount = players.size();
        sender.sendMessage(Component.text("Total players: " + playerCount));

        final int limit = 15;
        for (int i = 0; i < Math.min(limit, playerCount); i++) {
            final var player = players.get(i);
            sender.sendMessage(Component.text(player.getUsername()));
        }

        if (playerCount > limit) sender.sendMessage(Component.text("..."));
    }

}
