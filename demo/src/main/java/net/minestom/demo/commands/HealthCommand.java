package net.minestom.demo.commands;

import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.entity.Player;

public class HealthCommand extends Command {

    public HealthCommand() {
        super("health");

        setCondition(Conditions::playerOnly);

        setDefaultExecutor(HealthCommand::defaultExecutor);

        var modeArg = ArgumentType.Word("mode").from("set", "add");

        var valueArg = ArgumentType.Integer("value").between(0, 100);

        setArgumentCallback(HealthCommand::onModeError, modeArg);
        setArgumentCallback(HealthCommand::onValueError, valueArg);

        addSyntax(HealthCommand::sendSuggestionMessage, modeArg);
        addSyntax(HealthCommand::onHealthCommand, modeArg, valueArg);
    }

    private static void defaultExecutor(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.text("Correct usage: health set|add <number>"));
    }

    private static void onModeError(CommandSender sender, ArgumentSyntaxException exception) {
        sender.sendMessage(Component.text("SYNTAX ERROR: '" + exception.getInput() + "' should be replaced by 'set' or 'add'"));
    }

    private static void onValueError(CommandSender sender, ArgumentSyntaxException exception) {
        final int error = exception.getErrorCode();
        final String input = exception.getInput();
        switch (error) {
            case ArgumentNumber.NOT_NUMBER_ERROR ->
                    sender.sendMessage(Component.text("SYNTAX ERROR: '" + input + "' isn't a number!"));
            case ArgumentNumber.TOO_LOW_ERROR, ArgumentNumber.TOO_HIGH_ERROR ->
                    sender.sendMessage(Component.text("SYNTAX ERROR: " + input + " is not between 0 and 100"));
            default -> {
            }
        }
    }

    private static void sendSuggestionMessage(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.text("/health " + context.get("mode") + " [Integer]"));
    }

    private static void onHealthCommand(CommandSender sender, CommandContext context) {
        final Player player = (Player) sender;
        final String mode = context.get("mode");
        final int value = context.get("value");

        switch (mode.toLowerCase(Locale.ROOT)) {
            case "set" -> player.setHealth(value);
            case "add" -> player.setHealth(player.getHealth() + value);
            default -> {
            }
        }

        player.sendMessage(Component.text("You have now " + player.getHealth() + " health"));
    }

}