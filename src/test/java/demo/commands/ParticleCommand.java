package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentParticle;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.ParticleCreator;
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

        ParticlePacket packet = ParticleCreator.createParticlePacket(context.get(particle),
                player.getPosition().getX(), player.getPosition().getY() + 3, player.getPosition().getZ(),
                0, 0, 0, 1);
        player.getPlayerConnection().sendPacket(packet);
    }
}
