package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.location.RelativeVec;
import org.jetbrains.annotations.NotNull;

public class TeleportCommand extends Command {

    public TeleportCommand() {
        super("tp");

        setDefaultExecutor((origin, context) -> origin.sender().sendMessage(Component.text("Usage: /tp x y z")));

        var posArg = ArgumentType.RelativeVec3("pos");
        var playerArg = ArgumentType.Word("player");

        addSyntax(this::onPlayerTeleport, playerArg);
        addSyntax(this::onPositionTeleport, posArg);
    }

    private void onPlayerTeleport(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        final String playerName = context.get("player");
        Player pl = MinecraftServer.getConnectionManager().getPlayer(playerName);
        if (origin.entity() instanceof Player player) {
            player.teleport(pl.getPosition());
        }
        origin.sender().sendMessage(Component.text("Teleported to player " + playerName));
    }

    private void onPositionTeleport(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        final Player player = (Player) origin.entity();
        final RelativeVec relativeVec = context.get("pos");
        final Pos position = player.getPosition().withCoord(relativeVec.from(player));
        player.teleport(position);
        player.sendMessage(Component.text("You have been teleported to " + position));
    }
}
