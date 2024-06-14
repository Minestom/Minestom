package net.minestom.server.particle.data;

import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public record FallingDustParticleData(Block block) implements ParticleData {
    FallingDustParticleData(NetworkBuffer reader) {
        this(read(reader));
    }

    FallingDustParticleData() {
        this(Block.STONE);
    }

    private static Block read(NetworkBuffer reader) {
        short blockState = reader.read(NetworkBuffer.VAR_INT).shortValue();
        Block block = Block.fromStateId(blockState);
        Check.stateCondition(block == null, "Block state {0} is invalid", blockState);
        return block;
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
