package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEntityType;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class SummonCommand extends Command {

    private final ArgumentEntityType entity;
    private final ArgumentRelativeVec3 pos;

    public SummonCommand() {
        super("summon");
        setCondition(Conditions::playerOnly);

        entity = ArgumentType.EntityType("entity type");
        pos = ArgumentType.RelativeVec3("pos");
        addSyntax(this::execute, entity, pos);
    }

    private void execute(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        final Entity entity = new Entity(commandContext.get(this.entity));
        //noinspection ConstantConditions - One couldn't possibly execute a command without being in an instance
        entity.setInstance(commandSender.asPlayer().getInstance(), commandContext.get(pos).from(commandSender.asPlayer()).toPosition());
    }
}
