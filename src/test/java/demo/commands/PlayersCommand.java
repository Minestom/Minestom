package demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;

import java.util.Collection;
import java.util.stream.Collectors;

public class PlayersCommand extends Command {

    public PlayersCommand() {
        super("players");
        setDefaultExecutor(this::usage);
    }

    private void usage(CommandSender sender, CommandContext context) {
        final Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
        final int playerCount = players.size();
        sender.sendMessage("Total players: " + playerCount);
        final int limit = 15;
        if (playerCount <= limit) {
            for (final Player player : players) {
                sender.sendMessage(player.getUsername());
            }
        } else {
            for (final Player player : players.stream().limit(limit).collect(Collectors.toList())) {
                sender.sendMessage(player.getUsername());
            }
            sender.sendMessage("...");
        }
    }

}
