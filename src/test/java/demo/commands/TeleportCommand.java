package demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.location.RelativeVec;

public class TeleportCommand extends Command {

    public TeleportCommand() {
        super("tp");

        setDefaultExecutor((source, context) -> source.sendMessage("Usage: /tp x y z"));

        var posArg = ArgumentType.RelativeVec3("pos");
        var playerArg = ArgumentType.Word("player");

        addSyntax(this::onPlayerTeleport, playerArg);
        addSyntax(this::onPositionTeleport, posArg);
    }

    private void onPlayerTeleport(CommandSender sender, CommandContext context) {
        final String playerName = context.get("player");
        Player pl = MinecraftServer.getConnectionManager().getPlayer(playerName);
        if (pl != null && sender.isPlayer()) {
            Player player = (Player) sender;
            player.teleport(pl.getPosition());
        }
        sender.sendMessage("Teleported to player " + playerName);
    }

    private void onPositionTeleport(CommandSender sender, CommandContext context) {
        final Player player = sender.asPlayer();

        final RelativeVec relativeVec = context.get("pos");
        final Position position = relativeVec.from(player).toPosition();

        player.teleport(position);
        player.sendMessage("You have been teleported to " + position);
    }

}
