package net.minestom.server.particle.data;

import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

public record BlockParticleData(Block block) implements ParticleData {
    BlockParticleData(NetworkBuffer reader) {
        this(Block.fromStateId(reader.read(NetworkBuffer.VAR_INT).shortValue()));
    }

    BlockParticleData() {
        this(Block.STONE);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.VAR_INT, (int) block.stateId());
    }

    @Override
    public boolean validate(int particleId) {
        return particleId == Particle.BLOCK.id();
    }
}
