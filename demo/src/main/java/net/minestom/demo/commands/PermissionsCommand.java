package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;

public class PermissionsCommand extends Command {


    public PermissionsCommand() {
        super("permissions", "perm");

        setCondition(Conditions::playerOnly);

        addSubcommand(new PermissionsAddCommand());
        addSubcommand(new PermissionsCheckCommand());
        addSubcommand(new PermissionsRemoveCommand());

    }
}
