package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;

public class PermissionsRemoveCommand extends Command {
    public PermissionsRemoveCommand() {
        super("remove");

        setCondition(Conditions::playerOnly);

        setDefaultExecutor((sender, context) -> {
            sender.removePermission("permissionscheck");
            ((Player) sender).refreshCommands();
        });
    }
}
