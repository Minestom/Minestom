package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;

public class PermissionsAddCommand extends Command {
    public PermissionsAddCommand() {
        super("add");

        setCondition(Conditions::playerOnly);

        setDefaultExecutor((sender, context) -> {
            sender.addPermission(new Permission("permissionscheck"));
            ((Player) sender).refreshCommands();
        });
    }
}
