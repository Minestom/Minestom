package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;

/**
 * Command that make a player change gamemode
 */
public class GamemodeCommand extends Command {
    public GamemodeCommand() {
        super("gamemode", "g", "gm");

        setCondition(this::isAllowed);

        setDefaultExecutor(this::usage);

        var player = ArgumentType.Entity("player")
                .onlyPlayers(true)
                .singleEntity(true);

        var mode = ArgumentType.Enum("gamemode", GameMode.class)
                .setFormat(ArgumentEnum.Format.LOWER_CASED);

        setArgumentCallback(this::targetCallback, player);
        setArgumentCallback(this::gameModeCallback, mode);

        addSyntax(this::executeOnSelf, mode);
        addSyntax(this::executeOnOther, player, mode);
    }

    private void usage(CommandSender sender, CommandContext context) {
        sender.sendMessage("Usage: /gamemode [player] <gamemode>");
    }

    private void executeOnSelf(CommandSender sender, CommandContext context) {
        Player player = (Player) sender;

        GameMode gamemode = context.get("gamemode");
        assert gamemode != null; // mode is not supposed to be null, because gamemodeName will be valid
        player.setGameMode(gamemode);
        player.sendMessage("You are now playing in " + gamemode.toString().toLowerCase());
    }

    private void executeOnOther(CommandSender sender, CommandContext context) {
        GameMode gamemode = context.get("gamemode");
        EntityFinder targetFinder = context.get("player");
        Player target = targetFinder.findFirstPlayer(sender);
        assert gamemode != null; // mode is not supposed to be null, because gamemodeName will be valid
        assert target != null;
        target.setGameMode(gamemode);
        target.sendMessage("You are now playing in " + gamemode.toString().toLowerCase());
    }

    private void targetCallback(CommandSender sender, ArgumentSyntaxException exception) {
        sender.sendMessage("'" + exception.getInput() + "' is not a valid player name.");
    }

    private void gameModeCallback(CommandSender sender, ArgumentSyntaxException exception) {
        sender.sendMessage("'" + exception.getInput() + "' is not a valid gamemode!");
    }

    private boolean isAllowed(CommandSender sender, String commandString) {
        if (!sender.isPlayer()) {
            sender.sendMessage("The command is only available for player");
            return false;
        }
        return true;
    }
}
