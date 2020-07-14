package fr.themode.demo.commands;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.util.Optional;

public class TestCommand extends Command {

    public TestCommand() {
        super("msg");
        setDefaultExecutor(this::usage);

        Argument player = ArgumentType.Word("player");
        Argument message = ArgumentType.StringArray("array");

        addSyntax(this::execute, player, message);
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Usage: /whisper <player> <message>");
    }

    private void execute(CommandSender sender, Arguments arguments) {
        Player player = (Player) sender;
        String targetName = arguments.getWord("player");
        String[] Message = arguments.getStringArray("array");
        Optional<Player> target = player.getInstance().getPlayers().stream().filter(p -> p.getUsername().equalsIgnoreCase(targetName)).findFirst();
        if (target.isPresent()) {
            if (target.get() == player) {
                player.sendMessage("You cannot message yourself");
            } else {
                String message = "";
                for (int i = 0; i < Message.length; i++) {
                    if (i != 0) {
                        message = message + " ";
                    }
                    message = message + Message[i];
                }
                player.sendMessage("You -> " + targetName + ": " + message);
                target.get().sendMessage(player.getUsername() + " -> You: " + message);
            }
        } else {
            player.sendMessage(ColoredText.ofFormat("{@argument.player.unknown}"));
        }
    }

    private boolean isAllowed(Player player) {
        return true; // TODO: permissions
    }
}
