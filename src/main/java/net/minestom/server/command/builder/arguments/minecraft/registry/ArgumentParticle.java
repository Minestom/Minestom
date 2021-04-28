package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.data.ParticleData;
import net.minestom.server.registry.Registry;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving a {@link Particle}.
 */
public class ArgumentParticle extends ArgumentRegistry<ParticleData> {

    public ArgumentParticle(String id) {
        super(id, true);
    }

    @Override
    public ParticleData getRegistry(@NotNull String value) {
        String[] split = value.split(StringUtils.SPACE, 2);

        Particle<?> particle = Registry.PARTICLE_REGISTRY.get(split[0]);

        if (particle == null) return null;

        if (split.length == 1) {
            return particle.readData(null);
        } else {
            return particle.readData(split[1]);
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
        return String.format("Particle<%s>", getId());
    }
}
