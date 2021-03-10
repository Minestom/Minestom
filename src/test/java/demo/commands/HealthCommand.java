package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.entity.Player;

public class HealthCommand extends Command {

    public HealthCommand() {
        super("health");

        setCondition(this::condition);

        setDefaultExecutor(this::defaultExecutor);

        var modeArg = ArgumentType.Word("mode").from("set", "add");

        var valueArg = ArgumentType.Integer("value").between(0, 100);

        setArgumentCallback(this::onModeError, modeArg);
        setArgumentCallback(this::onValueError, valueArg);

        addSyntax(this::sendSuggestionMessage, modeArg);
        addSyntax(this::onHealthCommand, modeArg, valueArg);
    }

    private boolean condition(CommandSender sender, String commandString) {
        if (!sender.isPlayer()) {
            sender.sendMessage("The command is only available for player");
            return false;
        }
        return true;
    }

    private void defaultExecutor(CommandSender sender, CommandContext context) {
        sender.sendMessage("Correct usage: health [set/add] [number]");
    }

    private void onModeError(CommandSender sender, ArgumentSyntaxException exception) {
        sender.sendMessage("SYNTAX ERROR: '" + exception.getInput() + "' should be replaced by 'set' or 'add'");
    }

    private void onValueError(CommandSender sender, ArgumentSyntaxException exception) {
        final int error = exception.getErrorCode();
        final String input = exception.getInput();
        switch (error) {
            case ArgumentNumber.NOT_NUMBER_ERROR:
                sender.sendMessage("SYNTAX ERROR: '" + input + "' isn't a number!");
                break;
            case ArgumentNumber.RANGE_ERROR:
                sender.sendMessage("SYNTAX ERROR: " + input + " is not between 0 and 100");
                break;
        }
    }

    private void sendSuggestionMessage(CommandSender sender, CommandContext context) {
        sender.sendMessage("/health " + context.get("mode") + " [Integer]");
    }

    private void onHealthCommand(CommandSender sender, CommandContext context) {
        final Player player = (Player) sender;
        final String mode = context.get("mode");
        final int value = context.get("value");

        switch (mode.toLowerCase()) {
            case "set":
                player.setHealth(value);
                break;
            case "add":
                player.setHealth(player.getHealth() + value);
                break;
        }

        player.sendMessage("You have now " + player.getHealth() + " health");
    }

}