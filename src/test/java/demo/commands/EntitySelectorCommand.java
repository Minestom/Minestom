package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.entity.EntityFinder;

import java.util.List;

public class EntitySelectorCommand extends Command {

    public EntitySelectorCommand() {
        super("ent");

        setDefaultExecutor((sender, args) -> System.out.println("DEFAULT"));

        ArgumentEntity argumentEntity = ArgumentType.Entity("entities").onlyPlayers(true);

        setArgumentCallback((sender, exception) -> exception.printStackTrace(), argumentEntity);

        addSyntax(this::executor, argumentEntity);

    }

    private void executor(CommandSender commandSender, Arguments arguments) {
        Instance instance = commandSender.asPlayer().getInstance();
        EntityFinder entityFinder = arguments.get("entities");
        List<Entity> entities = entityFinder.find(commandSender);
        System.out.println("found " + entities.size() + " entities");
    }
}
