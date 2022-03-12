package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.entity.EntityFinder;

public class RemoveCommand extends Command {

    public RemoveCommand() {
        super("remove");
        addSubcommand(new RemoveEntities());
    }

    static class RemoveEntities extends Command {
        private final ArgumentEntity entity;

        public RemoveEntities() {
            super("entities");
            setCondition(Conditions::playerOnly);
            entity = ArgumentType.Entity("entity");
            addSyntax((origin, context) -> {
                final EntityFinder entityFinder = context.get(entity);
                entityFinder.find(origin.sender()).forEach(Entity::remove);
            }, entity);
        }
    }
}