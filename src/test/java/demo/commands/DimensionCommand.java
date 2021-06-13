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
import net.minestom.server.world.World;
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
        final World world = player.getWorld();
        final String typeName = commandContext.get(dimension_type);
        final Optional<World> targetWorld = MinecraftServer.getWorldManager().getWorlds().stream().filter(in -> in.getDimensionType().toString().equals(typeName)).findFirst();
        if (targetWorld.isPresent()) {
            if (world != null) {
                if (targetWorld.get() != world) {
                    player.sendMessage(Component.text("You were in " + world.getDimensionType()));
                    player.setWorld(targetWorld.get());
                    player.sendMessage(Component.text("You are now in " + typeName));
                } else {
                    player.sendMessage(Component.text("You are already in the World"));
                }
            } else {
                player.setWorld(targetWorld.get());
                player.sendMessage(Component.text("You did the impossible and you are now in " + typeName));
            }
        } else {
            player.sendMessage(Component.text("Could not find World with dimension " + typeName));
        }
    }
}
