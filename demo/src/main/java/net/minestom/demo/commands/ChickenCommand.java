package net.minestom.demo.commands;

import net.minestom.demo.entity.ChickenCreature;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEntityType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.utils.location.RelativeVec;

public class ChickenCommand extends Command {

    public ChickenCommand() {
        super("chicken");
        setCondition(Conditions::playerOnly);

        setDefaultExecutor(this::execute);
    }

    private void execute(CommandSender commandSender, CommandContext commandContext) {
        var chicken = new ChickenCreature();
        chicken.setInstance(((Player) commandSender).getInstance(), ((Player) commandSender).getPosition());
    }
}
