package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class TransferCommand extends Command {
    public TransferCommand() {
        super("transfer");

        var hostArgument = ArgumentType.String("host");
        var portArgument = ArgumentType.Integer("port");

        this.addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                player.getPlayerConnection().transfer(
                        context.get(hostArgument),
                        context.get(portArgument));
            } else {
                sender.sendMessage(Component.text(
                        "You must be a player to use this command!",
                        NamedTextColor.RED));
            }
        }, hostArgument, portArgument);
    }
}
