package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an argument giving a {@link Particle}.
 */
// TODO: Correctly parse this argument. There should be extra information at the end that depends on the particle that is currently not handled.
public class ArgumentParticle extends ArgumentRegistry<Particle> {

    public ArgumentParticle(@NotNull String id) {
        super(id);
    }

    @Override
    public @Nullable Particle getRegistry(@NotNull String key) {
        return Particle.fromNamespaceId(key);
    }

    @Override
    public @NotNull CommandException createException(@NotNull String input, int position, @NotNull String id) {
        return CommandException.PARTICLE_NOTFOUND.generateException(input, position, id);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:particle";

        nodeMaker.addNodes(argumentNode);
    }

    @Override
    public String toString() {
        return String.format("Particle<%s>", getId());
    }
}
