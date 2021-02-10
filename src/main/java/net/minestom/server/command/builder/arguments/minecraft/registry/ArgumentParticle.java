package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving a {@link Particle}.
 */
public class ArgumentParticle extends ArgumentRegistry<Particle> {

    public ArgumentParticle(String id) {
        super(id);
    }

    @Override
    public Particle getRegistry(@NotNull String value) {
        return Registries.getParticle(value);
    }

    @NotNull
    @Override
    public DeclareCommandsPacket.Node[] toNodes(boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:particle";
        return new DeclareCommandsPacket.Node[]{argumentNode};
    }
}
