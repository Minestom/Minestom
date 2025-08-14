package net.minestom.demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEntityType;
import net.minestom.server.entity.Player;

public class SetEntityType extends Command {
    private final ArgumentEntityType entityTypeArg = ArgumentType.EntityType("type");

    public SetEntityType() {
        super("setentitytype");

        addSyntax(this::execute, entityTypeArg);
    }

    private void execute(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) {
            return;
        }

        var entityType = context.get(entityTypeArg);
        player.switchEntityType(entityType);
        player.sendMessage("set entity type to " + entityType);
    }
}
