package demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.location.RelativeVec;

public class TeleportCommand extends Command {

    private static final ArgumentRelativeVec3 posArg = ArgumentType.RelativeVec3("pos");
    private static final ArgumentWord playerArg = ArgumentType.Word("player");

    public TeleportCommand() {
        super("tp");

        setDefaultExecutor((source, args) -> source.sendMessage("Usage: /tp x y z"));

        addSyntax(this::onPlayerTeleport, playerArg);
        addSyntax(this::onPositionTeleport, posArg);
    }

    private void onPlayerTeleport(CommandSender sender, Arguments args) {
        final String playerName = args.get(playerArg);
        Player pl = MinecraftServer.getConnectionManager().getPlayer(playerName);
        if (pl != null && sender.isPlayer()) {
            Player player = (Player) sender;
            player.teleport(pl.getPosition());
        }
        sender.sendMessage("Teleported to player "+playerName);
    }

    private void onPositionTeleport(CommandSender sender, Arguments args) {
        final Player player = sender.asPlayer();

        final RelativeVec relativeVec = args.get(posArg);
        final Position position = relativeVec.fromRelativePosition(player).toPosition();

        player.teleport(position);
        player.sendMessage("You have been teleported to " + position);
    }

}
