package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentParticle;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ParticleCommand extends Command {

    private final ArgumentParticle particle;

    public ParticleCommand() {
        super("particle");
        setCondition(Conditions::playerOnly);

        particle = ArgumentType.Particle("particle");
        addSyntax(this::execute, particle);
    }

    private void execute(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = sender.asPlayer();

        player.sendParticle(context.get(particle), player.getPosition().add(0, 3, 0));
    }
}
