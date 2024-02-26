package net.minestom.server.particle.data;

import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

public record FallingDustParticleData(Block block) implements ParticleData {
    FallingDustParticleData(NetworkBuffer reader) {
        this(Block.fromStateId(reader.read(NetworkBuffer.VAR_INT).shortValue()));
    }

    FallingDustParticleData() {
        this(Block.STONE);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.VAR_INT, (int) block.stateId());
    }

    @Override
    public boolean validate(int particleId) {
        return particleId == Particle.FALLING_DUST.id();
    }
}
