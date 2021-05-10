package demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DimensionCommand extends Command {

    private final ArgumentWord dimension_type;

    public DimensionCommand() {
        super("dimensiontest");
        setCondition(Conditions::playerOnly);
        dimension_type = ArgumentType.Word("dimension type");
        dimension_type.from(MinecraftServer.getDimensionTypeManager().unmodifiableList().stream().map(DimensionType::getName).map(Object::toString).toArray(String[]::new));

        addSyntax(this::execute, dimension_type);
    }

    private void execute(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        final Player player = commandSender.asPlayer();
        final Instance instance = player.getInstance();
        final String typeName = commandContext.get(dimension_type);
        final Optional<Instance> targetInstance = MinecraftServer.getInstanceManager().getInstances().stream().filter(in -> in.getDimensionType().toString().equals(typeName)).findFirst();
        if (targetInstance.isPresent()) {
            if (instance != null) {
                if (targetInstance.get() != instance) {
                    player.sendMessage(Component.text("You were in " + instance.getDimensionType()));
                    player.setInstance(targetInstance.get());
                    player.sendMessage(Component.text("You are now in " + typeName));
                } else {
                    player.sendMessage(Component.text("You are already in the instance"));
                }
            } else {
                player.setInstance(targetInstance.get());
                player.sendMessage(Component.text("You did the impossible and you are now in " + typeName));
            }
        } else {
            player.sendMessage(Component.text("Could not find instance with dimension " + typeName));
        }
    }
}
