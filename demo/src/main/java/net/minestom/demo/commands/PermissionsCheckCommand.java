package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.permission.Permission;

public class PermissionsCheckCommand extends Command {
    public PermissionsCheckCommand() {
        super("check");
        setCondition(Conditions::playerOnly);
        Permission permission = new Permission("permissionscheck");

        setCondition(permission::matchCondition);

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Executed");
        });
    }
}
