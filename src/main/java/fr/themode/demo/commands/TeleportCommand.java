package fr.themode.demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.Position;

public class TeleportCommand extends Command {
    public TeleportCommand() {
        super("tp");

        setDefaultExecutor((source, args) -> source.sendMessage("Usage: /tp x y z"));

        Argument x = ArgumentType.Float("x");
        Argument y = ArgumentType.Float("y");
        Argument z = ArgumentType.Float("z");
        Argument playerArg = ArgumentType.Word("pl");

        addCallback((source, value, error) -> {
            System.out.println("error: " + error);
        }, x);

        addSyntax((sender, args) -> {
            Player pl = MinecraftServer.getConnectionManager().getPlayer(args.getWord("pl"));
            if (pl != null && sender.isPlayer()) {
                Player player = (Player) sender;
                player.teleport(pl.getPosition());
            }
        }, playerArg);

        addSyntax((source, args) -> {
            final float posX = args.getFloat("x");
            final float posY = args.getFloat("y");
            final float posZ = args.getFloat("z");

            ((Player) source).teleport(new Position(posX, posY, posZ));
            source.sendMessage("TELEPORTING");
        }, x, y, z);
    }
}
