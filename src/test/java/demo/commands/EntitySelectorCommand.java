package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntities;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.entity.EntityFinder;

import java.util.List;

public class EntitySelectorCommand extends Command {

    public EntitySelectorCommand() {
        super("ent");

        setDefaultExecutor((sender, args) -> System.out.println("DEFAULT"));

        ArgumentEntities argumentEntities = ArgumentType.Entities("entities");

        setArgumentCallback((sender, exception) -> exception.printStackTrace(), argumentEntities);

        addSyntax(this::executor, argumentEntities);

    }

    private void executor(CommandSender commandSender, Arguments arguments) {
        EntityFinder query = arguments.getEntities("entities");
        System.out.println("SUCCESS COMMAND");
    }
}
