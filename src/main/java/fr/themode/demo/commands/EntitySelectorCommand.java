package fr.themode.demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntities;
import net.minestom.server.entity.Entity;

import java.util.List;

public class EntitySelectorCommand extends Command {

    public EntitySelectorCommand() {
        super("ent");

        ArgumentEntities argumentEntities = ArgumentType.Entities("entities");

        addSyntax(this::executor, argumentEntities);

    }

    private void executor(CommandSender commandSender, Arguments arguments) {
        List<Entity> entities = arguments.getEntities("entities");
    }
}
