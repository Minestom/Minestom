package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.utils.entity.EntityFinder;

public class EntitySelectorCommand extends Command {

    public EntitySelectorCommand() {
        super("ent");

        setDefaultExecutor((sender, args) -> System.out.println("DEFAULT"));

        Argument test = ArgumentType.String("test");

        ArgumentEntity argumentEntity = ArgumentType.Entities("entities");

        setArgumentCallback((sender, exception) -> exception.printStackTrace(), argumentEntity);

        addSyntax(this::executor, test);

    }

    private void executor(CommandSender commandSender, Arguments arguments) {
        System.out.println("test "+arguments.getString("test"));
    }
}
