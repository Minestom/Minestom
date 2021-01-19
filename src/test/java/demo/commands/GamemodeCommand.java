package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

import java.util.Optional;

/**
 * Command that make a player change gamemode
 */
public class GamemodeCommand extends Command {

    private static final ArgumentWord player = ArgumentType.Word("player");
    private static final ArgumentWord mode;

    static {
        GameMode[] gameModes = GameMode.values();
        String[] names = new String[gameModes.length];
        for (int i = 0; i < gameModes.length; i++) {
            names[i] = gameModes[i].name().toLowerCase();
        }

        mode = ArgumentType.Word("node").from(names);
    }

    public GamemodeCommand() {
        super("gamemode", "g", "gm");

        setCondition(this::isAllowed);

        setDefaultExecutor(this::usage);

        setArgumentCallback(this::gameModeCallback, mode);

        addSyntax(this::executeOnSelf, mode);
        addSyntax(this::executeOnOther, player, mode);
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Usage: /gamemode [player] <gamemode>");
    }

    private void executeOnSelf(CommandSender sender, Arguments arguments) {
        Player player = (Player) sender;

        String gamemodeName = arguments.get(mode);
        GameMode mode = GameMode.valueOf(gamemodeName.toUpperCase());
        assert mode != null; // mode is not supposed to be null, because gamemodeName will be valid
        player.setGameMode(mode);
        player.sendMessage("You are now playing in " + gamemodeName);
    }

    private void executeOnOther(CommandSender sender, Arguments arguments) {
        Player player = (Player) sender;

        String gamemodeName = arguments.get(mode);
        String targetName = arguments.get(GamemodeCommand.player);
        GameMode mode = GameMode.valueOf(gamemodeName.toUpperCase());
        assert mode != null; // mode is not supposed to be null, because gamemodeName will be valid
        Optional<Player> target = player.getInstance().getPlayers().stream().filter(p -> p.getUsername().equalsIgnoreCase(targetName)).findFirst();
        if (target.isPresent()) {
            target.get().setGameMode(mode);
            target.get().sendMessage("You are now playing in " + gamemodeName);
        } else {
            player.sendMessage("'" + targetName + "' is not a valid player name.");
        }
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
