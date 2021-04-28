package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.particle.ParticleType;
import net.minestom.server.particle.data.Particle;
import net.minestom.server.registry.Registry;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving a {@link ParticleType}.
 */
public class ArgumentParticle extends ArgumentRegistry<Particle> {

    public ArgumentParticle(String id) {
        super(id, true);
    }

    @Override
    public Particle getRegistry(@NotNull String value) {
        String[] split = value.split(StringUtils.SPACE, 2);

        ParticleType<?> particleType = Registry.PARTICLE_REGISTRY.get(split[0]);

        if (particleType == null) return null;

        if (split.length == 1) {
            return particleType.readData(null);
        } else {
            return particleType.readData(split[1]);
        }
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:particle";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("ParticleType<%s>", getId());
    }
}
