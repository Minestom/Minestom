package demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class PlayersCommand extends Command {

    public PlayersCommand() {
        super("players");
        setDefaultExecutor(this::usage);
    }

    private void usage(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        final Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
        final int playerCount = players.size();
        origin.sender().sendMessage(Component.text("Total players: " + playerCount));
        final int limit = 15;
        if (playerCount <= limit) {
            for (final Player player : players) {
                origin.sender().sendMessage(Component.text(player.getUsername()));
            }
        } else {
            for (final Player player : players.stream().limit(limit).toList()) {
                origin.sender().sendMessage(Component.text(player.getUsername()));
            }
            origin.sender().sendMessage(Component.text("..."));
        }
    }

}
