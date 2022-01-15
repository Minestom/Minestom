package demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.entity.EntityFinder;

import java.util.List;

public class EntitySelectorCommand extends Command {

    public EntitySelectorCommand() {
        super("ent");

        setDefaultExecutor((sender, context) -> System.out.println("DEFAULT"));

        ArgumentEntity argumentEntity = ArgumentType.Entity("entities").onlyPlayers(true);

        setArgumentCallback((sender, exception) -> exception.printStackTrace(), argumentEntity);

        addSyntax((origin, context) -> {
            EntityFinder entityFinder = context.get("entities");
            List<Entity> entities = entityFinder.find(origin.sender());
            System.out.println("found " + entities.size() + " entities");
        });

    }
}
