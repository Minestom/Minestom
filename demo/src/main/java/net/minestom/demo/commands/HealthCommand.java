package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HealthCommand extends Command {

    public HealthCommand() {
        super("health");

        setCondition(Conditions::playerOnly);

        setDefaultExecutor(this::defaultExecutor);

        var modeArg = ArgumentType.Word("mode").from("set", "add");

        var valueArg = ArgumentType.Integer("value").between(0, 100);

        setArgumentCallback(this::onModeError, modeArg);
        setArgumentCallback(this::onValueError, valueArg);

        addSyntax(this::sendSuggestionMessage, modeArg);
        addSyntax(this::onHealthCommand, modeArg, valueArg);
    }

    private void defaultExecutor(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        origin.sender().sendMessage(Component.text("Correct usage: health set|add <number>"));
    }

    private void onModeError(@NotNull CommandOrigin origin, @NotNull CommandException exception) {
        origin.sender().sendMessage(Component.text("Expected 'set' or 'add'", NamedTextColor.RED));
        origin.sender().sendMessage(exception.generateContextMessage());
    }

    private void onValueError(@NotNull CommandOrigin origin, @NotNull CommandException exception) {
        origin.sender().sendMessage(exception.getDisplayComponent());
        origin.sender().sendMessage(exception.generateContextMessage());
    }

    private void sendSuggestionMessage(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        origin.sender().sendMessage(Component.text("/health " + context.get("mode") + " [Integer]"));
    }

    private void onHealthCommand(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        if (!(origin.entity() instanceof Player player)) {
            return;
        }
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

        origin.sender().sendMessage(Component.text("You have now " + player.getHealth() + " health"));
    }

}