package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.particle.ParticleType;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving a {@link ParticleType}.
 */
public class ArgumentParticle extends ArgumentRegistry<ParticleType> {

    public ArgumentParticle(String id) {
        super(id);
    }

    @Override
    public ParticleType getRegistry(@NotNull String value) {
        return Registries.getParticleType(value);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:particle";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }
}
