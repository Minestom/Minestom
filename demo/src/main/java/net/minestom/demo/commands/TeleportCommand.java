package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.location.RelativeVec;

public class TeleportCommand extends Command {

    public TeleportCommand() {
        super("tp");

        setDefaultExecutor((source, _) -> source.sendMessage(Component.text("Usage: /tp x y z")));

        var posArg = ArgumentType.RelativeVec3("pos");
        var playerArg = ArgumentType.Word("player");

        addSyntax(TeleportCommand::onPlayerTeleport, playerArg);
        addSyntax(TeleportCommand::onPositionTeleport, posArg);
    }

    private static void onPlayerTeleport(CommandSender sender, CommandContext context) {
        final String playerName = context.get("player");
        Player pl = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(playerName);
        if (sender instanceof Player player) {
            player.teleport(pl.getPosition()).join();
        }
        sender.sendMessage(Component.text("Teleported to player " + playerName));
    }

    private static void onPositionTeleport(CommandSender sender, CommandContext context) {
        final Player player = (Player) sender;

        final RelativeVec relativeVec = context.get("pos");
        final Pos position = player.getPosition().withCoord(relativeVec.from(player));
        player.teleport(position).join();
        player.sendMessage(Component.text("You have been teleported to " + position));
    }
}
